/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.micro.ext.mailqueue;

import java.io.File;
import java.util.Date;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.adb.query.Db;
import de.mhus.lib.annotations.util.Interval;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.concurrent.LocalLock;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.mail.MSendMail;
import de.mhus.lib.core.mail.MailAttachment;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.xdb.XdbService;
import de.mhus.micro.ext.api.mailqueue.MailQueueOperation;
import de.mhus.osgi.api.scheduler.SchedulerService;
import de.mhus.osgi.api.scheduler.SchedulerServiceAdapter;

@Component(service = SchedulerService.class, immediate = true)
@Interval("interval=*/5 * * * * *")
public class MailQueueTimer extends SchedulerServiceAdapter {

    private static final CfgInt CFG_MAX_ATTEMPTS =
            new CfgInt(MailQueueOperation.class, "maxAttempts", 10);
    private static final CfgInt CFG_MAX_MAILS_PER_ROUND =
            new CfgInt(MailQueueOperation.class, "maxMailsPerRound", 30);
    private static final CfgString CFG_NEXT_SEND_ATTEMPT_INTERVAL =
            new CfgString(MailQueueOperation.class, "nextSendAttemptInterval", "15m");
    private static final long LOCK_TIMEOUT = MPeriod.MINUTE_IN_MILLISECOUNDS;
    private static MailQueueTimer instance;
    private Lock lock = new LocalLock("MailQueueLock");

    @Activate
    public void doActivate(ComponentContext ctx) {
        System.out.println("Start CryptaDbSchema");
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }

    public static MailQueueTimer instance() {
        return instance;
    }

    @Override
    public void run(Object environment) {
        try {
            Date now = new Date();
            XdbService manager = MailQueueDbImpl.instance().getManager();
            if (manager == null) {
                log().w("XdbService is null");
                return;
            }
            int mailCnt = 0;
            for (SopMailTask task :
                    manager.getByQualification(
                            Db.query(SopMailTask.class)
                                    .eq(_SopMailTask._STATUS, MailQueueOperation.STATUS.READY)
                                    .le(_SopMailTask._NEXT_SEND_ATTEMPT, now))) {

                mailCnt++;
                if (CFG_MAX_MAILS_PER_ROUND.value() > 0
                        && mailCnt > CFG_MAX_MAILS_PER_ROUND.value())
                    continue; // iterate all but do not send - this will close the db handle at the
                              // end

                sendMail(task);
            }
        } catch (Throwable t) {
            log().e(t);
        }
    }

    public boolean sendMail(SopMailTask task) throws Exception {
        if (!lock.lock(LOCK_TIMEOUT)) {
            log().w("can't obtain lock", lock);
            return false;
        }
        try {
            task.reload();
            if (task.getStatus() != MailQueueOperation.STATUS.READY) {
                log().d("Ignore Mail: wrong status", task);
                return false;
            }
            try {
                sendMailInternal(task);
                task.setStatus(MailQueueOperation.STATUS.SENT);
                task.save();
                return true;
            } catch (Throwable t) {
                if (t instanceof NullPointerException // any null pointer is not a connection error
                        || "javax.mail.internet.InternetAddress"
                                .equals(t.getStackTrace()[0].getClassName()) // email address error
                ) {
                    log().e("prepare error", t.getStackTrace()[0].getClassName(), task, t);
                    // fatal prepare error
                    task.setStatus(MailQueueOperation.STATUS.ERROR_PREPARE);
                } else {
                    log().e("send error", t.getStackTrace()[0].getClassName(), t);
                    // error to retry
                    task.setSendAttempts(task.getSendAttempts() + 1);
                    if (task.getSendAttempts() > CFG_MAX_ATTEMPTS.value()) {
                        task.setStatus(MailQueueOperation.STATUS.ERROR);
                    } else {
                        task.setNextSendAttempt(
                                new Date(
                                        System.currentTimeMillis()
                                                + MPeriod.toTime(
                                                        CFG_NEXT_SEND_ATTEMPT_INTERVAL.value(),
                                                        MPeriod.MINUTE_IN_MILLISECOUNDS * 15)));
                    }
                }
                task.setLastError(t.toString());
                task.save();
                return false;
            }
        } finally {
            lock.release();
        }
    }

    private void sendMailInternal(SopMailTask task) throws Exception {

        MProperties source = MailQueueOperationImpl.getSourceConfig(task);

        File dir = MailQueueOperationImpl.getMailFolder(task);

        MProperties prop = MProperties.load(new File(dir, "config.properties"));

        MailAttachment[] attachments = null;
        if (prop.getInt("attachmnets", 0) > 0) {
            attachments = new MailAttachment[prop.getInt("attachmnets", 0)];
            for (int i = 0; i < attachments.length; i++) {
                String name =
                        MFile.getFileName(MUri.toUri(prop.getString("attachment" + i)).getPath());
                attachments[i] = new MailAttachment(new File(dir, "attachment" + i), name, false);
            }
        }

        String html = MFile.readFile(new File(dir, "content.html"));
        log().d("send", task);
        String to = source.getString("to", task.getTo());
        String cc = source.getString("cc", task.getCc());
        String bcc = source.getString("bcc", task.getBcc());

        M.l(MSendMail.class)
                .sendHtmlMail(
                        task.getFrom(),
                        toMailArray(to),
                        toMailArray(cc),
                        toMailArray(bcc),
                        task.getSubject(),
                        html,
                        attachments);

        if (source.getBoolean("cleanupAfterSent", true)) {
            log().d("cleanup", task, dir);
            MFile.deleteDir(dir);
        }
    }

    private String[] toMailArray(String to) {
        if (to == null) return null;
        return to.split(";");
    }
}

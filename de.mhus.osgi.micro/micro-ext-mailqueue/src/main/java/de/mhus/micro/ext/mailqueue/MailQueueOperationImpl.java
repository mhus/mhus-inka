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
import java.util.HashMap;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationToIfcProxy;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.micro.ext.api.dfs.DfsApi;
import de.mhus.micro.ext.api.dfs.FileQueueApi;
import de.mhus.micro.ext.api.mailqueue.MailMessage;
import de.mhus.micro.ext.api.mailqueue.MailQueueOperation;
import de.mhus.osgi.api.services.MOsgi;

@Component(immediate = true, service = Operation.class)
public class MailQueueOperationImpl extends OperationToIfcProxy implements MailQueueOperation {

    @Override
    public UUID[] scheduleHtmlMail(
            String source,
            String from,
            String to,
            String subject,
            String content,
            IReadProperties properties,
            String... attachments)
            throws MException {
        MailMessage msg =
                new MailMessage(source, from, to, null, null, subject, content, attachments, false);
        scheduleHtmlMail(msg, properties);
        return msg.getTasks();
    }

    @Override
    public void scheduleHtmlMail(MailMessage mails, IReadProperties properties) throws MException {
        if (mails == null) {
            log().d("mails are null");
            return;
        }
        // create task
        
        for (MailMessage mail : mails.getSeparateMails()) {
            SopMailTask task = MailQueueDbImpl.instance().getManager().inject(new SopMailTask(mail));
            if (properties != null) task.getProperties().putReadProperties(properties);
            task.save();
            try {
                // create folder
                File dir = getMailFolder(task);

                if (mail.getContent().startsWith(DfsApi.SCHEME_DFQ + ":")) {
                    FileQueueApi dfq = M.l(FileQueueApi.class);
                    File contentFrom = dfq.loadFile(MUri.toUri(mail.getContent()));
                    MFile.copyFile(contentFrom, new File(dir, "content.html"));
                } else {
                    MFile.writeFile(new File(dir, "content.html"), mail.getContent());
                }
                MProperties prop = new MProperties();

                if (mail.getAttachments() != null && mail.getAttachments().length > 0) {
                    FileQueueApi dfq = M.l(FileQueueApi.class);
                    int cnt = 0;
                    for (String atta : mail.getAttachments()) {
                        File file = dfq.loadFile(MUri.toUri(atta));
                        File dest = new File(dir, "attachment" + cnt);
                        MFile.copyFile(file, dest);
                        prop.setString("attachment" + cnt, atta);
                        cnt++;
                    }
                    prop.setInt("attachments", cnt);
                }

                prop.save(new File(dir, "config.properties"));

                // set state of task
                task.setStatus(STATUS.READY);
                task.save();
                mails.addTaskId(task.getId());

                if (task.getProperties().getBoolean(MailQueueOperation.SEND_IMMEDIATELY, true))
                    MailQueueTimer.instance().sendMail(task);

            } catch (Throwable t) {
                log().w(t);
                task.setStatus(STATUS.ERROR_PREPARE);
                task.setLastError(t.toString());
                task.save();
                return;
            }
        }
    }

    public static File getMailFolder(SopMailTask task) {
        File dir = MApi.getFile(MApi.SCOPE.DATA, "mailqueue/mails/" + task.getId());
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static MProperties getSourceConfig(SopMailTask task) {
        File file = MApi.getFile(MApi.SCOPE.DATA, "mailqueue/sources/" + task.getSource() + ".properties");
        if (!file.exists()) return new MProperties();
        return MProperties.load(file);
    }

    @Override
    protected Class<?> getInterfaceClass() {
        return MailQueueOperation.class;
    }

    @Override
    protected Object getInterfaceObject() {
        return this;
    }

    @Override
    protected Version getInterfaceVersion() {
        return MOsgi.getBundelVersion(this.getClass());
    }

    @Override
    protected void initOperationDescription(HashMap<String, String> parameters) {}

    @Override
    public STATUS getStatus(UUID id) throws MException {
        SopMailTask task = MailQueueDbImpl.instance().getManager().getObject(SopMailTask.class, id);
        if (task == null) throw new NotFoundException(id);
        return task.getStatus();
    }

    @Override
    public Date getLastSendAttempt(UUID id) throws MException {
        SopMailTask task = MailQueueDbImpl.instance().getManager().getObject(SopMailTask.class, id);
        if (task == null) throw new NotFoundException(id);
        return task.getLastSendAttempt();
    }
}

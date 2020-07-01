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

import java.util.Date;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType;
import de.mhus.lib.basics.consts.GenerateConst;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.micro.ext.api.mailqueue.MailMessage;
import de.mhus.micro.ext.api.mailqueue.MailQueueOperation;

@GenerateConst
public class SopMailTask extends DbMetadata {

    @DbPersistent private String source;
    @DbPersistent private String from;
    @DbPersistent private String to;
    @DbPersistent private String cc;
    @DbPersistent private String bcc;
    @DbPersistent private String subject;
    @DbPersistent private MailQueueOperation.STATUS status = MailQueueOperation.STATUS.NEW;
    @DbPersistent private Date lastSendAttempt;
    @DbPersistent private Date nextSendAttempt = new Date();

    @DbPersistent(type = DbType.TYPE.STRING, size = 700)
    private String lastError;

    @DbPersistent private int sendAttempts = 0;
    @DbPersistent private MProperties properties;

    public SopMailTask() {}

    public SopMailTask(MailMessage mail) {
        this(
                mail.getSource(),
                mail.getFrom(),
                mail.getTo(),
                mail.getCc(),
                mail.getBcc(),
                mail.getSubject());
    }

    public SopMailTask(
            String source, String from, String to, String cc, String bcc, String subject) {
        this.source = source;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = M.trunc(subject, 200);
        this.properties = new MProperties();
    }

    @Override
    public DbMetadata findParentObject() throws MException {
        return null;
    }

    public MailQueueOperation.STATUS getStatus() {
        return status;
    }

    public void setStatus(MailQueueOperation.STATUS status) {
        this.status = status;
    }

    public Date getLastSendAttempt() {
        return lastSendAttempt;
    }

    public void setLastSendAttempt(Date lastSendAttempt) {
        this.lastSendAttempt = lastSendAttempt;
    }

    public Date getNextSendAttempt() {
        return nextSendAttempt;
    }

    public void setNextSendAttempt(Date nextSendAttempt) {
        this.nextSendAttempt = nextSendAttempt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = M.trunc(lastError, 700);
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getCc() {
        return cc;
    }

    public String getBcc() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, getId(), source, to, subject);
    }

    public MProperties getProperties() {
        return properties;
    }
}

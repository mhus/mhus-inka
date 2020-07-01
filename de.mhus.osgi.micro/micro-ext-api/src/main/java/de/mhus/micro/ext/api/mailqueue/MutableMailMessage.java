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
package de.mhus.micro.ext.api.mailqueue;

import de.mhus.lib.core.MString;

public class MutableMailMessage {

    private String source;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String content;
    String[] attachments;
    private boolean individual = true;

    public MutableMailMessage() {}

    public MutableMailMessage(MailMessage msg) {
        this.source = msg.getSource();
        this.from = msg.getFrom();
        this.to = msg.getTo();
        this.cc = msg.getCc();
        this.bcc = msg.getBcc();
        this.subject = msg.getSubject();
        this.content = msg.getContent();
        this.attachments = msg.getAttachments();
        this.individual = msg.isIndividual();
    }

    public MutableMailMessage(
            String source,
            String from,
            String to,
            String cc,
            String bcc,
            String subject,
            String content,
            String[] attachments,
            boolean individual) {
        this.source = source;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.content = content;
        this.attachments = attachments;
        this.individual = individual;
    }

    public MailMessage toMessage() {
        return new MailMessage(
                source, from, to, cc, bcc, subject, content, attachments, individual);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setTo(String[] to) {
        this.to = MString.join(to, ';');
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setCc(String[] cc) {
        this.cc = MString.join(cc, ';');
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public void setBcc(String[] bcc) {
        this.bcc = MString.join(bcc, ';');
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String[] getAttachments() {
        return attachments;
    }

    public void setAttachments(String[] attachments) {
        this.attachments = attachments;
    }

    public boolean isIndividual() {
        return individual;
    }

    public void setIndividual(boolean individual) {
        this.individual = individual;
    }
}

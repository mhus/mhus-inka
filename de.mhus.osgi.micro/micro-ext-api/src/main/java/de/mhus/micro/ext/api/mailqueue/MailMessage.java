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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MailMessage implements Externalizable {

    private String source;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String content;
    String[] attachments;
    private boolean individual = true;
    private List<UUID> tasks;

    public MailMessage() {}

    public MailMessage(
            String source,
            String from,
            String to,
            String cc,
            String bcc,
            String subject,
            String content,
            String[] attachments,
            boolean individual) {
        super();
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

    public String getSource() {
        return source;
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

    public String getContent() {
        return content;
    }

    public String[] getAttachments() {
        return attachments;
    }

    public boolean isIndividual() {
        return individual;
    }

    /**
     * Split a individual (every receiver gets his own message) into separate MailMessages with one
     * receiver
     *
     * @return List of Messages
     */
    public MailMessage[] getSeparateMails() {
        if (!individual || to.indexOf(';') < 0) return new MailMessage[] {this};
        String[] toList = to.split(";");
        MailMessage[] out = new MailMessage[toList.length];
        for (int i = 0; i < out.length; i++)
            out[i] =
                    new MailMessage(
                            source, from, toList[i], cc, bcc, subject, content, toList, individual);
        return out;
    }

    public void addTaskId(UUID id) {
        if (tasks == null) tasks = new LinkedList<>();
        tasks.add(id);
    }

    public UUID[] getTasks() {
        if (tasks == null) return new UUID[0];
        return tasks.toArray(new UUID[tasks.size()]);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(source);
        out.writeObject(from);
        out.writeObject(to);
        out.writeObject(cc);
        out.writeObject(bcc);
        out.writeObject(subject);
        out.writeObject(content);
        out.writeObject(attachments);
        out.writeBoolean(individual);
        out.writeObject(tasks);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        source = (String) in.readObject();
        from = (String) in.readObject();
        to = (String) in.readObject();
        cc = (String) in.readObject();
        bcc = (String) in.readObject();
        subject = (String) in.readObject();
        content = (String) in.readObject();
        attachments = (String[]) in.readObject();
        individual = in.readBoolean();
        tasks = (List<UUID>) in.readObject();
    }
}

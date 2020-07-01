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
package de.mhus.micro.prov.jms;

import java.util.Locale;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.shiro.SubjectEnvironment;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.jms.JmsContext;
import de.mhus.lib.jms.JmsInterceptor;
import de.mhus.micro.core.api.JmsApi;

public class TicketAccessInterceptor extends MLog implements JmsInterceptor {

    //	public static final CfgString TICKET_KEY = new CfgString(JmsApi.class, "aaaTicketParameter",
    // "mhus.ticket");
    //	public static final CfgString LOCALE_KEY = new CfgString(JmsApi.class, "aaaLocaleParameter",
    // "mhus.locale");
    public static final CfgBoolean RELAXED = new CfgBoolean(JmsApi.class, "aaaRelaxed", true);

    @Override
    public void begin(JmsContext context) {
        String ticket;
        try {
            ticket = context.getMessage().getStringProperty(JmsApi.PARAM_AAA_TICKET);
        } catch (JMSException e) {
            throw new MRuntimeException(e);
        }
        Subject subject =  AccessUtil.login(ticket);
        try {
            String localeStr = context.getMessage().getStringProperty(JmsApi.PARAM_LOCALE);
            if (localeStr != null) {
                AccessUtil.setLocale(subject, localeStr);
            }
        } catch (Throwable t) {
            log().d("Incoming Access Denied", context.getMessage());
            throw new RuntimeException(t);
        }
        SubjectEnvironment env = AccessUtil.useSubject(subject);
        context.getProperties().put(TicketAccessInterceptor.class.getCanonicalName(), env);
    }

    @Override
    public void end(JmsContext context) {

        SubjectEnvironment env = (SubjectEnvironment) context.getProperties().get(TicketAccessInterceptor.class.getCanonicalName());
        if (env == null) return;

        env.close();

    }

    @Override
    public void prepare(JmsContext context) {
        Message message = context.getMessage();
        Subject subject = AccessUtil.getSubject();

        String ticket = AccessUtil.createTrustTicket(JmsApi.TRUST_NAME.value(), subject);
        Locale locale = AccessUtil.getLocale(subject);
        try {
            message.setStringProperty(JmsApi.PARAM_AAA_TICKET, ticket);
            message.setStringProperty(JmsApi.PARAM_LOCALE, locale.toString());
        } catch (JMSException e) {
            throw new MRuntimeException(e);
        }
    }

    @Override
    public void answer(JmsContext context) {}
}

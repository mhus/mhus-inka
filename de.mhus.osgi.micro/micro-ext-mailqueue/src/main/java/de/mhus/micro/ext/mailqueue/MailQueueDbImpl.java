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

import java.util.List;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.db.osgi.api.adb.AbstractCommonAdbConsumer;
import de.mhus.db.osgi.api.adb.CommonDbConsumer;
import de.mhus.db.osgi.api.adb.ReferenceCollector;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;

@Component(service = CommonDbConsumer.class, property = "commonService=adb", immediate = true)
public class MailQueueDbImpl extends AbstractCommonAdbConsumer {

    private static MailQueueDbImpl instance;

    public static MailQueueDbImpl instance() {
        return instance;
    }
    
    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }
    
    @Override
    public DbManager getManager() {
        return super.getManager();
    }

    @Override
    public void registerObjectTypes(List<Class<?>> list) {
        list.add(SopMailTask.class);
    }

    @Override
    public void doInitialize() {}

    @Override
    public void doDestroy() {}

    @Override
    public boolean canCreate(Object obj) throws MException {
        return true; // TODO can everybody create mail queue items?
    }

    @Override
    public void collectReferences(Object object, ReferenceCollector collector) {}

    @Override
    public void doCleanup() {}

    @Override
    public void doPostInitialize(XdbService manager) throws Exception {}

}

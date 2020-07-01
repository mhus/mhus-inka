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
package de.mhus.micro.osgi;

import java.util.Collection;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.micro.core.MicroServiceRegistry;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.core.api.OperationApi;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.micro.core.api.OperationsProvider;

@Component(immediate = true, service = OperationApi.class)
public class OperationApiImpl extends MLog implements OperationApi {

    private ServiceTracker<OperationsProvider, OperationsProvider> nodeTracker;
    private MicroServiceRegistry register = new MicroServiceRegistry();
    private BundleContext context;
    public static OperationApiImpl instance;
    private TimerIfc timer;
    private MTimerTask timerTask;
    private CfgLong CFG_OPERATION_SYNC = new CfgLong(OperationApi.class, "syncInterval", 300000);

    @Activate
    public void doActivate(ComponentContext ctx) {
        context = ctx.getBundleContext();
        nodeTracker =
                new ServiceTracker<>(
                        context, OperationsProvider.class, new MyServiceTrackerCustomizer());
        nodeTracker.open(true);
        instance = this;

        MThread.asynchron(
                new Runnable() {

                    @Override
                    public void run() {
                        MThread.sleep(10000); //TODO check status
                        synchronize();
                    }
                });
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        if (timer != null) timer.cancel();

        instance = null;
        context = null;
    }

    @Reference(
            service = TimerFactory.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.OPTIONAL)
    public void addTimerFactory(TimerFactory factory) {
        log().i("create timer");
        timer = factory.getTimer();
        timerTask =
                new MTimerTask() {

                    @Override
                    public void doit() throws Exception {
                        synchronize();
                    }
                };
        timer.schedule(timerTask, CFG_OPERATION_SYNC.value(), MPeriod.MINUTE_IN_MILLISECOUNDS);
    }

    public void removeTimerFactory(TimerFactory factory) {}

    private class MyServiceTrackerCustomizer
            implements ServiceTrackerCustomizer<OperationsProvider, OperationsProvider> {

        @Override
        public OperationsProvider addingService(ServiceReference<OperationsProvider> reference) {

            OperationsProvider service = context.getService(reference);
            if (service != null) {
                String name = String.valueOf(reference.getProperty("provider"));
                log().i("register", name);
                synchronized (register) {
                    OperationsProvider o = register.getRegistry().put(name, service);
                    if (o != null) log().w("Provider was already registered", name);
                }
            }
            return service;
        }

        @Override
        public void modifiedService(
                ServiceReference<OperationsProvider> reference, OperationsProvider service) {

            if (service != null) {
                String name = String.valueOf(reference.getProperty("provider"));
                log().i("modified", name);
                synchronized (register) {
                    register.getRegistry().put(name, service);
                }
            }
        }

        @Override
        public void removedService(
                ServiceReference<OperationsProvider> reference, OperationsProvider service) {

            if (service != null) {
                String name = String.valueOf(reference.getProperty("provider"));
                log().i("unregister", name);
                synchronized (register) {
                    register.getRegistry().remove(name);
                }
            }
        }
    }

    public OperationsProvider getProvider(String name) {
        return register.getProvider(name);
    }

    @Override
    public String[] getProviderNames() {
        return register.getProviderNames();
    }

    public OperationsProvider[] getProviders() {
        return register.getProviders();
    }

    @Override
    public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
        return register.getOperation(addr);
    }

    @Override
    public List<OperationDescriptor> findOperations(
            String filter, VersionRange version, Collection<String> providedTags) {
        return register.findOperations(filter, version, providedTags);
    }

    @Override
    public OperationDescriptor findOperation(
            String filter, VersionRange version, Collection<String> providedTags)
            throws NotFoundException {
        return register.findOperation(filter, version, providedTags);
    }

    @Override
    public OperationResult doExecute(
            String filter,
            VersionRange version,
            Collection<String> providedTags,
            IConfig request,
            String... executeOptions)
            throws NotFoundException {
        return register.doExecute(filter, version, providedTags, request, executeOptions);
    }

    @Override
    public OperationResult doExecute(
            OperationDescriptor desc, IConfig request, String... executeOptions)
            throws NotFoundException {
        return register.doExecute(desc, request, executeOptions);
    }


    @Override
    public void synchronize() {
        register.synchronize();
    }

    @Override
    public void reset() {
        nodeTracker.close();
        nodeTracker.open(true);
    }
}

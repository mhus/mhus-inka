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
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.base.service.IdentUtil;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.operation.NotSuccessful;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.micro.core.api.OperationException;
import de.mhus.micro.core.api.OperationUtil;
import de.mhus.micro.core.api.OperationsProvider;

@Component(immediate = true, service = OperationsProvider.class, property = "provider=local")
public class LocalOperationsProvider extends MLog implements OperationsProvider {

    static final String PROVIDER_NAME = "local";

    private BundleContext context;
    private ServiceTracker<Operation, Operation> nodeTracker;
    private HashMap<UUID, LocalOperationDescriptor> register = new HashMap<>();
    public static LocalOperationsProvider instance;

    @Activate
    public void doActivate(ComponentContext ctx) {
        context = ctx.getBundleContext();
        nodeTracker =
                new ServiceTracker<>(context, Operation.class, new MyServiceTrackerCustomizer());
        nodeTracker.open(true);
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
        context = null;
    }

    private class MyServiceTrackerCustomizer
            implements ServiceTrackerCustomizer<Operation, Operation> {

        @Override
        public Operation addingService(ServiceReference<Operation> reference) {

            Operation service = context.getService(reference);
            if (service != null) {
                OperationDescription desc = service.getDescription();
                if (desc != null && desc.getPath() != null) {
                    log().i("register", desc);
                    synchronized (register) {
                        LocalOperationDescriptor descriptor = createDescriptor(reference, service);
                        if (register.put(desc.getUuid(), descriptor) != null)
                            log().w("Operation already defined", desc.getPath());
                    }
                } else {
                    log().w(
                                    "no description found, not registered",
                                    reference.getProperty("objectClass"));
                }
            }
            return service;
        }

        private LocalOperationDescriptor createDescriptor(
                ServiceReference<Operation> reference, Operation service) {
            TreeSet<String> tags = new TreeSet<>();
            Object tagsStr = reference.getProperty("tags");
            if (tagsStr instanceof String[]) {
                for (String item : (String[]) tagsStr) tags.add(item);
            } else if (tagsStr instanceof String) {
                for (String item : ((String) tagsStr).split(";")) tags.add(item);
            }
            service.getDescription().getForm();
            OperationDescription desc = service.getDescription();

            Object tagsStr2 =
                    desc.getParameters() == null
                            ? null
                            : desc.getParameters().get(OperationDescription.TAGS);
            if (tagsStr2 != null)
                for (String item : String.valueOf(tagsStr2).split(";")) tags.add(item);

            tags.add(OperationDescriptor.TAG_IDENT + "=" + IdentUtil.getServerIdent());
            tags.add(OperationDescriptor.TAG_HOST + "=localhost");
            tags.add(OperationDescription.TAG_TECH + "=" + OperationDescription.TECH_JAVA);

            // String acl = OperationUtil.getOption(tags, OperationDescriptor.TAG_DEFAULT_ACL, "");
            
            return new LocalOperationDescriptor(
                    service.getUuid(),
                    OperationAddress.create(PROVIDER_NAME, desc),
                    desc,
                    tags,
                    service);
        }

        @Override
        public void modifiedService(ServiceReference<Operation> reference, Operation service) {

            if (service != null) {
                OperationDescription desc = service.getDescription();
                if (desc != null && desc.getPath() != null) {
                    log().i("modified", desc);
                    synchronized (register) {
                        LocalOperationDescriptor descriptor = createDescriptor(reference, service);
                        register.put(desc.getUuid(), descriptor);
                    }
                }
            }
        }

        @Override
        public void removedService(ServiceReference<Operation> reference, Operation service) {

            if (service != null) {
                OperationDescription desc = service.getDescription();
                if (desc != null && desc.getPath() != null) {
                    log().i("unregister", desc);
                    synchronized (register) {
                        register.remove(desc.getUuid());
                    }
                }
            }
        }
    }

    @Override
    public void collectOperations(
            List<OperationDescriptor> list,
            String filter,
            VersionRange version,
            Collection<String> providedTags) {
        synchronized (register) {
            for (OperationDescriptor desc : register.values()) {
                if (OperationUtil.matches(desc, filter, version, providedTags)) list.add(desc);
            }
        }
    }

    @Override
    public OperationResult doExecute(
            String filter,
            VersionRange version,
            Collection<String> providedTags,
            IProperties properties,
            String... executeOptions)
            throws NotFoundException {
        OperationDescriptor d = null;
        synchronized (register) {
            for (OperationDescriptor desc : register.values()) {
                if (OperationUtil.matches(desc, filter, version, providedTags)) {
                    d = desc;
                    break;
                }
            }
        }
        if (d == null)
            throw new NotFoundException("operation not found", filter, version, providedTags);
        return doExecute(d, properties);
    }

    @Override
    public OperationResult doExecute(
            OperationDescriptor desc, IProperties properties, String... executeOptions)
            throws NotFoundException {
        Operation operation = null;
        if (desc instanceof LocalOperationDescriptor) {
            operation = ((LocalOperationDescriptor) desc).operation;
        }
        if (operation == null) {
            if (!PROVIDER_NAME.equals(desc.getProvider()))
                throw new NotFoundException("description is from another provider", desc);
            synchronized (register) {
                LocalOperationDescriptor local = findOperation(desc);
                if (local != null) operation = local.operation;
            }
        }
        if (operation == null) throw new NotFoundException("operation not found", desc);

        if (!AccessUtil.isPermitted("local.operation:execute:" + desc.getPath()))
            throw new AccessDeniedException("access denied", desc.getPath());

        DefaultTaskContext taskContext = new DefaultTaskContext(getClass());
        taskContext.setParameters(properties);
        try {
            OperationResult res = operation.doExecute(taskContext);
            return res;
        } catch (OperationException e) {
            log().w(desc, properties, e);
            return new NotSuccessful(operation, e.getMessage(), e.getCaption(), e.getReturnCode());
        } catch (Exception e) {
            log().w(desc, properties, e);
            return new NotSuccessful(operation, e.toString(), OperationResult.INTERNAL_ERROR);
        }
    }

    private LocalOperationDescriptor findOperation(OperationDescriptor desc) {
        if (MValidator.isUUID(desc.getPath())) return register.get(UUID.fromString(desc.getPath()));

        for (LocalOperationDescriptor value : register.values())
            if (desc.getPath().equals(value.getPath())
                    && desc.getVersionString().equals(value.getVersionString())) {
                return value;
            }
        return null;
    }

    private LocalOperationDescriptor findOperation(OperationAddress desc) {
        for (LocalOperationDescriptor value : register.values())
            if (value.operation.getUuid().toString().equals(desc.getPath())
                    || desc.getPath().equals(value.getPath())
                            && desc.getVersionString().equals(value.getVersionString())) {
                return value;
            }
        return null;
    }

    private class LocalOperationDescriptor extends OperationDescriptor {

        private Operation operation;

        public LocalOperationDescriptor(
                UUID uuid,
                OperationAddress address,
                OperationDescription description,
                Collection<String> tags,
                Operation operation) {
            super(uuid, address, description, tags);
            this.operation = operation;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T adaptTo(Class<T> ifc) {
            if (ifc == Operation.class) return (T) operation;
            return super.adaptTo(ifc);
        }
    }

    @Override
    public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
        synchronized (register) {
            LocalOperationDescriptor ret = findOperation(addr);
            if (ret == null) throw new NotFoundException("operation not found", addr);
            return ret;
        }
    }

    @Override
    public void synchronize() {
        // already up to date
    }
}

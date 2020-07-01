package de.mhus.micro.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.util.OperationResultProxy;
import de.mhus.lib.core.util.Value;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.core.api.OperationApi;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.micro.core.api.OperationUtil;
import de.mhus.micro.core.api.OperationsProvider;


public class MicroServiceRegistry extends MLog {

    private Map<String, OperationsProvider> register = new HashMap<>();

    public Map<String, OperationsProvider> getRegistry() {
        return register;
    }
    
    public OperationsProvider getProvider(String name) {
        return register.get(name);
    }

    public String[] getProviderNames() {
        return register.keySet().toArray(new String[register.size()]);
    }

    public OperationsProvider[] getProviders() {
        return register.values().toArray(new OperationsProvider[register.size()]);
    }

    public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
        OperationsProvider provider = getProvider(addr.getProvider());
        return provider.getOperation(addr);
    }

    public List<OperationDescriptor> findOperations(
            String filter, VersionRange version, Collection<String> providedTags) {
        LinkedList<OperationDescriptor> list = new LinkedList<>();
        for (OperationsProvider provider : getProviders())
            try {
                provider.collectOperations(list, filter, version, providedTags);
            } catch (Throwable t) {
                log().d(filter, version, providedTags, t);
            }
        return list;
    }

    public OperationDescriptor findOperation(
            String filter, VersionRange version, Collection<String> providedTags)
            throws NotFoundException {
        LinkedList<OperationDescriptor> list = new LinkedList<>();
        for (OperationsProvider provider : getProviders()) {
            try {
                provider.collectOperations(list, filter, version, providedTags);
            } catch (Throwable t) {
                log().d(filter, version, providedTags, t);
            }
            if (list.size() > 0) return list.getFirst();
        }
        throw new NotFoundException("operation not found", filter, version, providedTags);
    }

    public OperationResult doExecute(
            String filter,
            VersionRange version,
            Collection<String> providedTags,
            IProperties properties,
            String... executeOptions)
            throws NotFoundException {

        if (OperationUtil.isOption(executeOptions, OperationApi.LOCAL_ONLY)) {
            synchronized (register) {
                OperationsProvider provider = register.get(OperationApi.DEFAULT_PROVIDER_NAME);
                return unwrap(
                        provider.doExecute(
                                filter, version, providedTags, properties, executeOptions),
                        executeOptions);
            }
        } else {
            for (OperationsProvider provider : getProviders()) {
                try {
                    return unwrap(
                            provider.doExecute(
                                    filter, version, providedTags, properties, executeOptions),
                            executeOptions);
                } catch (NotFoundException nfe) {
                }
            }
        }

        throw new NotFoundException(
                "operation not found", filter, version, providedTags, executeOptions);
    }

    public OperationResult doExecute(
            OperationDescriptor desc, IProperties properties, String... executeOptions)
            throws NotFoundException {
        OperationsProvider provider = getProvider(desc.getProvider());
        if (provider == null)
            throw new NotFoundException("provider for operation not found", desc, executeOptions);
        return unwrap(provider.doExecute(desc, properties), executeOptions);
    }

    protected OperationResult unwrap(OperationResult res, String[] executeOptions) {

        if (OperationUtil.isOption(executeOptions, OperationApi.RAW_RESULT)) return res;

        // unwrap result.result
        if (res != null && res.getResult() != null && res.getResult() instanceof Value) {
            OperationResultProxy wrap = new OperationResultProxy(res);
            wrap.setResult(((Value<?>) res.getResult()).getValue());
            res = wrap;
        }
        return res;
    }

    public void synchronize() {
        for (OperationsProvider provider : getProviders()) {
            try {
                provider.synchronize();
            } catch (Throwable e) {
                log().d(provider, e);
            }
        }
    }
}

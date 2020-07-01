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
package de.mhus.micro.core.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;

public class OperationsSelector {

    private String filter;
    private VersionRange version;
    private Collection<String> providedTags;
    private LinkedList<Selector> selectors = new LinkedList<>();
    private IConfig properties;
    private String[] executeOptions;

    public OperationsSelector setFilter(Class<?> filter) {
        this.filter = filter == null ? null : filter.getCanonicalName();
        return this;
    }

    public OperationsSelector setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public OperationsSelector setVersion(String version) {
        this.version = new VersionRange(version);
        return this;
    }

    public OperationsSelector setVersion(VersionRange version) {
        this.version = version;
        return this;
    }

    public OperationsSelector setTags(String... providedTags) {
        this.providedTags = MCollection.toTreeSet(providedTags);
        return this;
    }

    public OperationsSelector setTags(Collection<String> providedTags) {
        this.providedTags = providedTags;
        return this;
    }

    public OperationsSelector addSelector(Selector selector) {
        selectors.add(selector);
        return this;
    }

    public OperationsSelector putProperties(String key, Object value) {
        if (properties == null) properties = new MConfig();
        properties.put(key, value);
        return this;
    }

    public OperationsSelector putAllProperties(IProperties all) {
        if (properties == null) properties = new MConfig();
        properties.putAll(all);
        return this;
    }

    public OperationsSelector setExecutionOptions(String... executeOptions) {
        this.executeOptions = executeOptions;
        return this;
    }

    public OperationDescriptor doSelect() {
        List<OperationDescriptor> list =
                M.l(OperationApi.class).findOperations(filter, version, providedTags);
        if (list == null || list.size() == 0) return null;
        for (Selector selector : selectors) selector.select(list);
        if (list.size() == 0) return null;
        return list.get(0);
    }

    public List<OperationDescriptor> doSelectAll() {
        List<OperationDescriptor> list =
                M.l(OperationApi.class).findOperations(filter, version, providedTags);
        if (list == null || list.size() == 0) return null;
        for (Selector selector : selectors) selector.select(list);
        return list;
    }

    public OperationResult doExecute(IConfig properties, String... executeOptions)
            throws NotFoundException {
        OperationDescriptor desc = doSelect();
        if (desc == null) throw new NotFoundException(filter, version, providedTags);
        return M.l(OperationApi.class).doExecute(desc, properties, executeOptions);
    }

    public OperationResult doExecute() throws NotFoundException {
        return doExecute(properties, executeOptions);
    }

    public List<OperationResult> doExecuteAll(IConfig properties, String... executeOptions)
            throws NotFoundException {
        List<OperationDescriptor> list = doSelectAll();
        LinkedList<OperationResult> res = new LinkedList<>();
        for (OperationDescriptor desc : list) {
            OperationResult r = M.l(OperationApi.class).doExecute(desc, properties, executeOptions);
            if (r != null) res.add(r);
        }
        return res;
    }

    public List<OperationResult> doExecuteAll() throws NotFoundException {
        return doExecuteAll(properties, executeOptions);
    }

    public String getFilter() {
        return filter;
    }

    public VersionRange getVersion() {
        return version;
    }

    public Collection<String> getProvidedTags() {
        if (providedTags == null) return null;
        return Collections.unmodifiableCollection(providedTags);
    }
}

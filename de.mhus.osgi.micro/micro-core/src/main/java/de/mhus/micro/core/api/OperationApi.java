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
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;

public interface OperationApi {

    static final String DEFAULT_PROVIDER_NAME = "local";
    static final String LOCAL_ONLY = "localOnly";
    /**
     * Results could be wrapped and unwrapped, usually the api unwrap the result. Set this to get
     * the raw result object. It will unwrap results from type 'Value'
     */
    static final String RAW_RESULT = "rawResult";

    default List<OperationDescriptor> findOperations(
            Class<?> clazz, VersionRange version, Collection<String> providedTags) {
        return findOperations(clazz.getCanonicalName(), version, providedTags);
    }

    List<OperationDescriptor> findOperations(
            String filter, VersionRange version, Collection<String> providedTags);

    default OperationDescriptor findOperation(
            Class<?> clazz, VersionRange version, Collection<String> providedTags)
            throws NotFoundException {
        return findOperation(clazz.getCanonicalName(), version, providedTags);
    }

    OperationDescriptor findOperation(
            String filter, VersionRange version, Collection<String> providedTags)
            throws NotFoundException;

    OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException;

    OperationResult doExecute(
            String filter,
            VersionRange version,
            Collection<String> providedTags,
            IConfig request,
            String... executeOptions)
            throws NotFoundException;

    OperationResult doExecute(
            OperationDescriptor desc, IConfig request, String... executeOptions)
            throws NotFoundException;

    void synchronize();

    String[] getProviderNames();

    void reset();
}

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
package de.mhus.lib.core.directory;

import de.mhus.lib.annotations.activator.DefaultImplementation;
import de.mhus.lib.core.lang.MObject;

@DefaultImplementation(ClassLoaderResourceProvider.class)
public abstract class MResourceProvider<T extends ResourceNode<?>> extends MObject
        implements IResourceProvider<T> {

    /**
     * Return a requested resource.
     *
     * @param path The name or path to the resource.
     * @return The resource or null if not found.
     */
    @Override
    public abstract T getResourceByPath(String path);

    @Override
    public abstract T getResourceById(String id);
}

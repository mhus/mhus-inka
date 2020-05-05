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
package de.mhus.lib.core.mapi;

import java.util.HashSet;

public abstract class MBase {

    protected MBase parent;
    protected HashSet<String> local;

    public MBase(MBase parent) {
        this.parent = parent;
    }

    public abstract void destroy();

    public abstract boolean isDestroyed();

    public <T> T lookup(Class<T> ifc) {
        return lookup(ifc, null);
    }

    public abstract <T, D extends T> T lookup(Class<T> ifc, Class<D> def);

    public abstract boolean isBase(Class<?> ifc);

    public abstract void addObject(Class<?> ifc, Object obj);

    public abstract boolean removeObject(Class<?> ifc);

    public MBase getParent() {
        return parent;
    }

    /**
     * Define this interface as local handled. The base will not ask the parent base to lookup this
     * interface.
     *
     * @param ifc
     */
    public void setLocal(Class<?> ifc) {
        if (local == null) local = new HashSet<String>();
        local.add(ifc.getCanonicalName());
    }
}

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

import java.lang.reflect.InvocationTargetException;

import de.mhus.lib.core.MActivator;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.activator.MutableActivator;

public class DefaultBase extends MBase {

    MActivator activator;

    public DefaultBase(MBase parent) {
        super(parent);
        this.activator = MApi.get().createActivator();
    }

    @Override
    public void destroy() {
        if (activator != null) activator.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return activator == null || activator.isDestroyed();
    }

    @Override
    public boolean isBase(Class<?> ifc) {
        return activator != null && activator.isInstance(ifc)
                || parent != null && parent.isBase(ifc);
    }

    public MActivator getActivator() {
        return activator;
    }

    @Override
    public <T, D extends T> T lookup(Class<T> ifc, Class<D> def) {
        try {
            if (activator == null) {
                if (parent != null) return parent.lookup(ifc, def);
            } else {

                if (parent != null
                        && !activator.isInstance(ifc)
                        && (local == null || !local.contains(ifc.getCanonicalName())))
                    return parent.lookup(ifc, def);
            }

            T ret = activator.getObject(ifc);
            if (ret != null) return ret;

        } catch (Exception e) {
            MApi.dirtyLogDebug("info: fallback to default", ifc, def, e);
        }

        if (def == null) return null;

        try {
            return def.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException e) {
            MApi.dirtyLogDebug(ifc, e);
        }
        return null;
    }

    @Override
    public void addObject(Class<?> ifc, Object obj) {
        MActivator act = getActivator();
        if (act instanceof MutableActivator) ((MutableActivator) act).addObject(ifc, null, obj);
    }

    @Override
    public boolean removeObject(Class<?> ifc) {
        MActivator act = getActivator();
        if (act instanceof MutableActivator)
            return ((MutableActivator) act).removeObject(ifc, null);
        return false;
    }
}

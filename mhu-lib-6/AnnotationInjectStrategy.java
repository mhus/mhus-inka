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
package de.mhus.lib.core.base;

import de.mhus.lib.annotations.base.Bind;
import de.mhus.lib.annotations.base.IgnoreBind;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.mapi.MBase;
import de.mhus.lib.core.pojo.AttributesStrategy;
import de.mhus.lib.core.pojo.DefaultFilter;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoParser;

public class AnnotationInjectStrategy extends InjectStrategy {

    @SuppressWarnings("unchecked")
    @Override
    public void inject(Object object, MBase base) {

        IgnoreBind classIgnoreBind =
                object.getClass()
                        .getAnnotation(IgnoreBind.class); // to save time use IgnoreBind ...
        if (classIgnoreBind != null) return;

        Bind classBind = MSystem.findAnnotation(object.getClass(), Bind.class);
        if (classBind == null) return;

        PojoModel model =
                new PojoParser()
                        .parse(
                                object,
                                new AttributesStrategy(
                                        false, false, null, new Class[] {Bind.class}))
                        .filter(new DefaultFilter(true, true, false, true, false))
                        .getModel();
        for (PojoAttribute<Object> attr : model) {
            try {
                Bind bind = attr.getAnnotation(Bind.class);
                if (bind != null) {
                    Class<?> ifc = attr.getType();
                    if (bind.name() != Class.class) ifc = bind.name();
                    Object obj = base.lookup(ifc);
                    attr.set(object, obj);
                }
            } catch (Throwable e) {
                e.printStackTrace(); // TODO
            }
        }
    }
}

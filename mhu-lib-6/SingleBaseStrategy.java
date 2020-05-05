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

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.mapi.MBase;

public class SingleBaseStrategy extends BaseFindStrategy {

    private MBase defaultBase = MApi.get().getBaseControl().createBase(null, null);

    @Override
    public MBase find(Object... attributes) {
        //		Thread.currentThread().
        return defaultBase;
    }

    @Override
    public MBase install(MBase base) {
        MBase cur = defaultBase;
        //		defaultBase = base; // do not overwrite
        return cur;
    }
}

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

import java.util.List;

import de.mhus.lib.core.M;
import de.mhus.lib.core.crypt.MRandom;

public class SelectorRandom implements Selector {

    @Override
    public void select(List<OperationDescriptor> list) {
        if (list.size() <= 1) return;
        MRandom random = M.l(MRandom.class);
        int pos = random.getInt() % list.size();
        OperationDescriptor item = list.get(pos);
        list.clear();
        list.add(item);
    }
}

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

import java.util.HashSet;
import java.util.List;

public class SelectorLocalRoundRobin implements Selector {

    private HashSet<String> done = new HashSet<>();

    @Override
    public void select(List<OperationDescriptor> list) {
        if (list.size() <= 1) return;
        int cnt = 0;
        while (true) {
            if (cnt >= list.size()) // reset already done list
            done.clear();
            OperationDescriptor item = list.get(cnt);
            String uuid = item.getUuid().toString();
            if (!done.contains(uuid)) {
                done.add(uuid);
                list.clear();
                list.add(item);
                return;
            }
            cnt++;
        }
    }
}

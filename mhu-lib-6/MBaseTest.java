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
package de.mhus.lib.test;

import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.base.AnnotationInjectStrategy;
import de.mhus.lib.core.base.BaseByThreadStrategy;
// import static org.junit.jupiter.api.Assertions.*;

public class MBaseTest {

    @Test
    public void testInject() {
        MApi.get().getBaseControl().setFindStrategy(new BaseByThreadStrategy());
        MApi.get().getBaseControl().setInjectStrategy(new AnnotationInjectStrategy());
        @SuppressWarnings("unchecked")
        LinkedList<String> list = MApi.get().getBaseControl().base().lookup(LinkedList.class);
        list.add("TestString");
        new MBaseTestInjectionObject().test();
        new MBaseTestInjectionObjectExtended().test();
    }
}

/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.core.util.lambda;

import java.util.Map;

import de.mhus.lib.core.util.ImmutableMap;

/**
 * Adapted from https://github.com/benjiman/benjiql
 * 
 * @author mikehummel
 *
 */
public class DefaultValues {

    private static final Map<Class<?>, Object> defaultValues = ImmutableMap.<Class<?>,Object>builder()
        .put(String.class, "string")
        .put(Integer.class,0)
        .put(Float.class, 0f)
        .put(Double.class, 0d)
        .put(Long.class, 0L)
        .put(Character.class, 'c')
        .put(Byte.class, (byte)0)
        .put(int.class, 0)
        .put(float.class,0f)
        .put(double.class,0d)
        .put(long.class, 0L)
        .put(char.class, 'c')
        .put(byte.class, (byte)0)
        .build();

    @SuppressWarnings("unchecked")
    public static <T> T getDefault(Class<T> cls) {
        return (T) defaultValues.get(cls);
    }
}
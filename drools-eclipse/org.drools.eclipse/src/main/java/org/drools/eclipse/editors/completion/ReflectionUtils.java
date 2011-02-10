/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.editors.completion;

import java.lang.reflect.Field;

public class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Object getField(Object instance, String name) {
        Class clazz = instance.getClass();

        do {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (name.equals(f.getName())) {
                    try {
                        f.setAccessible(true);
                        return f.get(instance);

                    } catch (SecurityException ex) {
                        return null;
                    } catch (IllegalArgumentException ex) {
                        return null;
                    } catch (IllegalAccessException ex) {
                        return null;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz.getSuperclass() != null);
        return null;
    }
}

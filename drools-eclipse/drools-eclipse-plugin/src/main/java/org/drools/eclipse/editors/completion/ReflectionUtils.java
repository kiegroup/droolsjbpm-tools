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

package gmr.all.framework.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DefaultValueUtil {
	private static final Map<Class<?>, Object> MAP = new HashMap<Class<?>, Object>();

	static {
		MAP.put(int.class, 0);
		MAP.put(double.class, 0.0);
		MAP.put(float.class, 0.0f);
		MAP.put(long.class, 0L);
		MAP.put(boolean.class, false);
		MAP.put(short.class, 0);
		MAP.put(char.class, 0);

		MAP.put(Integer.class, null);
		MAP.put(Double.class, null);
		MAP.put(Float.class, null);
		MAP.put(Long.class, null);
		MAP.put(Boolean.class, null);
		MAP.put(Short.class, null);
		MAP.put(Character.class, null);

		MAP.put(String.class, null);
		MAP.put(Date.class, null);
	}

	public static Object getDefaultValue(Class<?> c) {
		return MAP.get(c);
	}

	public static Map<Class<?>, Object> getMap() {
		return MAP;
	}
}

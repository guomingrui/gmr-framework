package gmr.all.framework.util;

/**
 * 类型转化工具类
 * 
 * @author gmr
 */
public class CastUtil {
	public static String castToString(Object castValue, String defaultValue) {
		return castValue != null ? String.valueOf(castValue) : defaultValue;
	}

	/**
	 * 默认值为""
	 * 
	 * @param castValue
	 * @return
	 */
	public static String castToString(Object castValue) {
		return castToString(castValue, "");
	}

	public static int castToInt(Object castValue, int defaultValue) {
		if (castValue != null) {
			String value = castToString(castValue);
			try {
				defaultValue = Integer.valueOf(value);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	/**
	 * 默认值为 0
	 * 
	 * @param castValue
	 * @return
	 */
	public static int castToInt(Object castValue) {
		return castToInt(castValue, 0);
	}

	public static double castToDouble(Object castValue, double defaultValue) {
		if (castValue != null) {
			String value = castToString(castValue);
			try {
				defaultValue = Double.valueOf(value);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	/**
	 * 默认值为0.0
	 * 
	 * @param castValue
	 * @return
	 */
	public static double castToDouble(Object castValue) {
		return castToDouble(castValue, 0.0);
	}

	public static long castToLong(Object castValue, long defaultValue) {
		if (castValue != null) {
			String value = castToString(castValue);
			try {
				defaultValue = Long.valueOf(value);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	/**
	 * 默认值为0.0f
	 * 
	 * @param castValue
	 * @return
	 */
	public static float castToFloat(Object castValue) {
		return castToFloat(castValue, 0.0f);
	}

	public static float castToFloat(Object castValue, float defaultValue) {
		if (castValue != null) {
			String value = castToString(castValue);
			try {
				defaultValue = Float.valueOf(value);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	/**
	 * 默认值为0
	 * 
	 * @param castValue
	 * @return
	 */
	public static long castToLong(Object castValue) {
		return castToLong(castValue, 0);
	}

	public static boolean castToBoolean(Object castValue, boolean defaultValue) {
		if (castValue != null) {
			String value = castToString(castValue);
			try {
				defaultValue = Boolean.valueOf(value);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	/**
	 * 默认值为false
	 * 
	 * @param castValue
	 * @return
	 */
	public static boolean castToBoolean(Object castValue) {
		return castToBoolean(castValue, false);
	}

	public static long castToLong(String castValue) {
		try {
			return Long.valueOf(castValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public static boolean castToBoolean(String castValue) {
		try {
			return Boolean.valueOf(castValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public static int castToInt(String castValue) {
		try {
			return Integer.valueOf(castValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public static double castToDouble(String castValue) {
		try {
			return Double.valueOf(castValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public static float castToFloat(String castValue) {
		try {
			return Float.valueOf(castValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public static Object castSuitable(Class<?> c, String value) {
		if (c.equals(int.class) || c.equals(Integer.class)) {
			castToInt(value);
		} else if (c.equals(long.class) || c.equals(Long.class)) {
			castToLong(value);
		} else if (c.equals(short.class) || c.equals(Short.class)) {
			castToInt(value);
		} else if (c.equals(char.class) || c.equals(Character.class)) {
			castToInt(value);
		} else if (c.equals(double.class) || c.equals(Double.class)) {
			castToDouble(value);
		} else if (c.equals(float.class) || c.equals(Float.class)) {
			castToFloat(value);
		} else if (c.equals(boolean.class) || c.equals(Boolean.class)) {
			castToBoolean(value);
		} else if (c.equals(String.class)) {
			return value;
		}
		return c;
	}

}

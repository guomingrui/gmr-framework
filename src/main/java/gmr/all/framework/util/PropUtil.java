package gmr.all.framework.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 属性获取工具类
 * 
 * @author gmr
 *
 */
public class PropUtil {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PropUtil.class);

	/**
	 * 从配置文件加载属性
	 * 
	 * @param fileName
	 * @return
	 */
	public static Properties loadProperties(String fileName) {
		Properties props = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(fileName);
		try {
			if (in == null) {
				throw new FileNotFoundException("fileName is not found");
			}
			props = new Properties();
			props.load(in);

		} catch (IOException e) {
			LOGGER.error("load propertites failure",e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error("close inputstream failure",e);
				}
			}
		}
		return props;
	}

	public static String getString(Properties props, String key,
			String defaultValue) {
		if (props.containsKey(key)) {
			return props.getProperty(key);
		}
		return defaultValue;
	}

	/**
	 * 默认是""
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static String getString(Properties props, String key) {
		return getString(props, key, "");
	}

	public static int getInt(Properties props, String key, int defaultValue) {
		if (props.containsKey(key)) {
			defaultValue = CastUtil.castToInt(props.getProperty(key),
					defaultValue);
		}
		return defaultValue;
	}

	/**
	 * 默认是0
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static int getInt(Properties props, String key) {
		return getInt(props, key, 0);
	}

	public static double getDouble(Properties props, String key,
			double defaultValue) {
		if (props.containsKey(key)) {
			defaultValue = CastUtil.castToDouble(props.getProperty(key),
					defaultValue);
		}
		return defaultValue;
	}

	/**
	 * 默认是0.0
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static double getDouble(Properties props, String key) {
		return getDouble(props, key, 0.0);
	}

	public static long getLong(Properties props, String key, long defaultValue) {
		if (props.containsKey(key)) {
			defaultValue = CastUtil.castToLong(props.getProperty(key),
					defaultValue);
		}
		return defaultValue;
	}

	/**
	 * 默认是0
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static long getLong(Properties props, String key) {
		return getLong(props, key, 0);
	}

	public static boolean getBoolean(Properties props, String key,
			boolean defaultValue) {
		if (props.containsKey(key)) {
			defaultValue = CastUtil.castToBoolean(props.getProperty(key),
					defaultValue);
		}
		return defaultValue;
	}

	/**
	 * 默认是false
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(Properties props, String key) {
		return getBoolean(props, key, false);
	}

}

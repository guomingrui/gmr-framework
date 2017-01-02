package gmr.all.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射Bean工具类
 * 
 * @author gmr
 *
 */
public class BeanUtil {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BeanUtil.class);

	/**
	 * 产生Class的实例
	 * 
	 * @param c
	 * @return
	 */
	public static Object newInstance(Class<?> c) {
		try {
			return c.newInstance();
		} catch (Exception e) {
			LOGGER.error("newInstance failure", e);
			throw new RuntimeException();
		}
	}

	/**
	 * 调用实例方法
	 * 
	 * @param method
	 * @param instance
	 * @param params
	 * @return
	 */
	public static Object invokeMethod(Method method, Object instance,
			Object... params) {
		try {
			method.setAccessible(true);
			return method.invoke(instance, params);
		} catch (Exception e) {
			LOGGER.error("invoke method failure", e);
			throw new RuntimeException("invoke method failure", e);
		}
	}

	/**
	 * 设置属性域的值
	 * @param field
	 * @param instance
	 * @param value
	 */
	public static void setField(Field field, Object instance, Object value) {
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (Exception e) {
			LOGGER.error("set field failure", e);
			throw new RuntimeException(e);
		}
	}
	

}

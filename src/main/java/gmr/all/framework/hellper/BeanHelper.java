package gmr.all.framework.hellper;

import gmr.all.framework.util.BeanUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Bean 的助手类
 * 
 * @author gmr
 *
 */
/**
 * @author gmr
 *
 */
/**
 * @author gmr
 *
 */
public class BeanHelper {
	private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<Class<?>, Object>();
	
	static {
		/*
		 * 根据加载的beanClass初始化bean
		 */
		Set<Class<?>> beanSet = ClassHelper.getBeanClasses();
		for (Class<?> beanClass : beanSet) {
			BEAN_MAP.put(beanClass, BeanUtil.newInstance(beanClass));
		}
	}

	public static void setBean(Class<?> beanClass, Object bean) {
		BEAN_MAP.put(beanClass, bean);
	}

	public static Map<Class<?>, Object> getBeanMap() {
		return BEAN_MAP;
	}

	/**
	 * 获取bean类
	 * 
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> c) {
		if (!BEAN_MAP.containsKey(c)) {
			throw new RuntimeException("can't get bean " + c);
		}
		return (T) BEAN_MAP.get(c);
	}

}

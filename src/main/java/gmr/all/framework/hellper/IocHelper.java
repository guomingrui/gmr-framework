package gmr.all.framework.hellper;

import gmr.all.framework.annotation.Autowired;
import gmr.all.framework.util.BeanUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Ioc反转注入
 * @author gmr
 *
 */
public class IocHelper {
	static {
		/*
		 * 将生成的bean对象注入
		 */
		Map<Class<?>, Object> map = BeanHelper.getBeanMap();
		Set<Entry<Class<?>, Object>> entrySet = map.entrySet();
		for (Entry<Class<?>, Object> entry : entrySet) {
			Object beanInstance = entry.getValue();
			/*
			 * 注入属性对象
			 */
			Field[] fields = entry.getKey().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Autowired.class)) {
					BeanUtil.setField(field, beanInstance,
							map.get(field.getType()));
				}
			}
		}
	}
}

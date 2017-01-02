package gmr.all.framework.hellper;

import gmr.all.framework.annotation.Aspect;
import gmr.all.framework.proxy.AspectProxy;
import gmr.all.framework.proxy.Proxy;
import gmr.all.framework.proxy.ProxyManager;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AopHelper {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AopHelper.class);

	static {
		/*
		 * 根据切面注解以及代理继承确定代理bean
		 */
		Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
		Map<Class<?>, List<Proxy>> targetMap;
		try {
			targetMap = createTargetMap(proxyMap);

			for (Entry<Class<?>, List<Proxy>> entry : targetMap.entrySet()) {
				Class<?> targetClass = entry.getKey();
				BeanHelper
						.setBean(
								entry.getKey(),
								ProxyManager.createProxy(targetClass,
										entry.getValue()));
			}
		} catch (Exception e) {
			LOGGER.error("load AOP error", e);
		}

	}

	/**
	 * 获取切面注解下的目标class
	 * 
	 * @param aspect
	 * @return
	 * @throws Exception
	 */
	private static Set<Class<?>> createTargerClassSet(Aspect aspect) {
		Set<Class<?>> classSet = new HashSet<Class<?>>();
		Class<? extends Annotation> annotation = aspect.value();
		if (annotation != null && !annotation.equals(aspect)) {
			classSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
		}
		return classSet;
	}

	/**
	 * 获取代理对目标类的映射（一个代理对应的多个目标）
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Map<Class<?>, Set<Class<?>>> createProxyMap() {
		Set<Class<?>> classSet = ClassHelper
				.getClassSetBySuper(AspectProxy.class);
		Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap<Class<?>, Set<Class<?>>>();
		for (Class<?> proxyClass : classSet) {
			Aspect annotation = proxyClass.getAnnotation(Aspect.class);
			if (annotation != null) {
				Set<Class<?>> targetClassSet = createTargerClassSet(annotation);
				proxyMap.put(proxyClass, targetClassSet);
			}
		}
		return proxyMap;
	}

	/**
	 * 获取代理对目标类的映射（一个目标对应的多个代理，即目标的代理链）
	 * 
	 * @param proxyMap
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	public static Map<Class<?>, List<Proxy>> createTargetMap(
			Map<Class<?>, Set<Class<?>>> proxyMap)
			throws InstantiationException, IllegalAccessException {
		Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();
		for (Entry<Class<?>, Set<Class<?>>> entry : proxyMap.entrySet()) {
			Class<?> proxyClass = entry.getKey();
			Set<Class<?>> targetClassSet = entry.getValue();
			for (Class<?> targetClass : targetClassSet) {
				if (!targetMap.containsKey(targetClass)) {
					targetMap.put(targetClass, new ArrayList<Proxy>());
				}
				targetMap.get(targetClass)
						.add((Proxy) proxyClass.newInstance());
			}
		}
		return targetMap;
	}

}

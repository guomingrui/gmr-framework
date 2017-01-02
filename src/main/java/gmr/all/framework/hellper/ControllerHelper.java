package gmr.all.framework.hellper;

import gmr.all.framework.annotation.RequestMapping;
import gmr.all.framework.annotation.RequestParam;
import gmr.all.framework.annotation.ResponseBody;
import gmr.all.framework.bean.Handler;
import gmr.all.framework.bean.Param;
import gmr.all.framework.enums.RequestMethod;
import gmr.all.framework.interceptor.HandlerInterceptor;
import gmr.all.framework.resolver.HandlerExceptionResolver;
import gmr.all.framework.util.DefaultValueUtil;
import gmr.all.framework.util.UrlUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Controller 助手类
 * 
 * @author gmr
 *
 */
public class ControllerHelper {

	/*
	 * 方法映射MAP value [0]相匹映射MAP value [1]通配映射MAP
	 */
	private static final Map<RequestMethod, Map<String, Handler>[]> METHOD_MAP = new HashMap<RequestMethod, Map<String, Handler>[]>();
	/* 相匹映射MAP */
	private static final int MAPING_MAP = 0;
	/* 通配映射MAP */
	private static final int WILDCARD_MAP = 1;
	/* 精确路径集合，不含方法 */
	private static final Set<String> CONTAIN_URL_SET = new HashSet<String>();
	/* 模糊路径集合，不含方法 */
	private static final Set<String> WILDCARD_URL_SET = new HashSet<String>();

	/*
	 * 装载MAPING_MAP和WILDCARD_MAP
	 */
	@SuppressWarnings("unchecked")
	private static void init() {
		for (RequestMethod requestMethod : RequestMethod.values()) {
			METHOD_MAP.put(requestMethod, new HashMap[] {
					new HashMap<String, Handler>(),
					new HashMap<String, Handler>() });
		}
	}

	/**
	 * 添加mapping方法Handler到所有类型的请求url
	 * MappingType支持值为MAPING_MAP(准确映射)和WILDCARD_MAP(通配映射)
	 * 
	 * @param url
	 * @param MappingType
	 *            是否是通配类型
	 * @param handler
	 */
	private static void putAllMethod(String url, int MappingType,
			Handler handler, RequestMethod[] requestMethods) {
		for (RequestMethod requestMethod : requestMethods) {
			METHOD_MAP.get(requestMethod)[MAPING_MAP].put(url, handler);
		}
	}

	static {
		init();
		BeanHelper.setBean(HandlerExceptionResolver.class, null);
		for (Entry<Class<?>, Object> classEntry : BeanHelper.getBeanMap()
				.entrySet()) {
			if (HandlerExceptionResolver.class.isAssignableFrom(classEntry
					.getKey())
					&& !HandlerExceptionResolver.class.equals(classEntry
							.getKey())) {
				BeanHelper.setBean(HandlerExceptionResolver.class,
						classEntry.getValue());
			}
		}
		Set<Class<?>> classSet = ClassHelper.getControllerClasses();
		for (Class<?> c : classSet) {
			String controllerUrl = "";
			RequestMethod[] defaultType = null;
			List<HandlerInterceptor> defaultInterceptors = new ArrayList<HandlerInterceptor>();
			/*
			 * 提取Class上的requestMapping注解的值 并设为该Controller下requestMapping方法的默认值
			 */

			if (c.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping controllerMapping = c
						.getAnnotation(RequestMapping.class);
				if (controllerMapping.value() != null
						&& controllerMapping.value().trim().length() != 0) {
					controllerUrl = "/" + controllerMapping.value() + "/";
				}

				addInterceptor(defaultInterceptors,
						controllerMapping.interceptors());
				defaultType = controllerMapping.method();
			}
			/*
			 * 加url方法到url映射列表
			 */
			for (Method method : c.getDeclaredMethods()) {
				if (method.isAnnotationPresent(RequestMapping.class)) {
					RequestMapping methodMapping = method
							.getAnnotation(RequestMapping.class);
					String methodUrl = "/" + methodMapping.value() + "/";
					RequestMethod[] methodType = methodMapping.method();
					boolean isResponseBody = false;
					// 默认url精确匹配
					int mappingType = MAPING_MAP;
					// 如果返回值不是视图
					if (method.isAnnotationPresent(ResponseBody.class)) {
						isResponseBody = true;
					}
					/*
					 * 处理url
					 */
					if (methodUrl == null || methodUrl.trim().length() == 0) {
						methodUrl = "/*/";
					}
					methodUrl = UrlUtil.getUrl(controllerUrl + methodUrl);
					if (!methodUrl.matches("(/\\w+)*/")) {
						continue;
					}
					// 如果请求URL是否是通配类型
					if (methodUrl.contains("*")) {
						mappingType = WILDCARD_MAP;
						methodUrl = methodUrl.replace("*", "\\w+");
						// 增加模糊url
						WILDCARD_URL_SET.add(methodUrl);
					} else {
						// 增加精确url
						CONTAIN_URL_SET.add(methodUrl);
					}
					/*
					 * 获取方法适用请求类型
					 */
					if (methodType == null || methodType.length == 0) {
						if (defaultType == null || defaultType.length == 0) {
							// 不拒绝任何请求类型
							methodType = RequestMethod.values();
						} else {
							// 采用默认请求类型
							methodType = defaultType;
						}
					}
					/*
					 * 获取方法请求参数
					 */
					Parameter[] paramters = method.getParameters();
					List<Param> list = new ArrayList<Param>();
					for (Parameter parameter : paramters) {
						RequestParam requestParam = parameter
								.getDeclaredAnnotation(RequestParam.class);
						if (requestParam != null) {
							// 设置了requestParam注解
							list.add(new Param(parameter.getType(),
									requestParam.value()));
						} else {
							// 未设置了requestParam注解
							if (DefaultValueUtil.getMap().containsKey(
									parameter.getType())) {
								// 存在的默认类型，取默认值
								list.add(new Param(parameter.getType(), null));
							} else {
								// 其它类型
								String simpleName = parameter.getType()
										.getSimpleName();

								// 将类名作为基础参数名
								// 首字母转为小写
								list.add(new Param(parameter.getType(),
										simpleName.replaceFirst(simpleName
												.substring(0, 1), simpleName
												.substring(0, 1).toLowerCase())));

							}
						}
					}
					// 添加拦截器方法
					List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>(
							defaultInterceptors);
					addInterceptor(interceptors, methodMapping.interceptors());
					// 添加url映射方法
					putAllMethod(methodUrl, mappingType, new Handler(c, method,
							isResponseBody, list, interceptors), methodType);
				}
			}
		}
	}

	/**
	 * 返回映射Handler的值
	 * 
	 * @param requestMethod
	 * @param url
	 * @return
	 */
	public static Handler getHandler(RequestMethod requestMethod, String url) {
		Map<String, Handler>[] map = METHOD_MAP.get(requestMethod);
		// url统一化
		url = UrlUtil.getUrl(url);
		Handler handler = map[MAPING_MAP].get(url);
		if (handler == null) {
			for (String wildcardUrl : map[WILDCARD_MAP].keySet()) {
				if (url.matches(wildcardUrl)) {
					handler = map[WILDCARD_MAP].get(wildcardUrl);
				}
			}
		}
		return handler;
	}

	public static Map<RequestMethod, Map<String, Handler>[]> getMethodMap() {
		return METHOD_MAP;
	}

	/**
	 * 判断是否存在该url映射
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isUrlExist(String url) {
		// url统一化
		url = UrlUtil.getUrl(url);
		boolean isExist = CONTAIN_URL_SET.contains(url);
		if (!isExist) {
			for (String wildcardUrl : WILDCARD_URL_SET) {
				if (url.matches(wildcardUrl)) {
					isExist = true;
				}
			}
		}
		return isExist;
	}

	/**
	 * 添加拦截器
	 * 
	 * @param interceptors
	 * @param interceptorClasses
	 */
	private static void addInterceptor(List<HandlerInterceptor> interceptors,
			Class<? extends HandlerInterceptor>[] interceptorClasses) {
		for (Class<? extends HandlerInterceptor> interceptorClass : interceptorClasses) {
			HandlerInterceptor interceptor = null;
			try {
				interceptor = BeanHelper.getBean(interceptorClass);
			} catch (Exception e) {

			}
			if (interceptor == null) {
				try {
					interceptor = interceptorClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				BeanHelper.setBean(interceptorClass, interceptor);
			}
			interceptors.add(interceptor);
		}
	}
}

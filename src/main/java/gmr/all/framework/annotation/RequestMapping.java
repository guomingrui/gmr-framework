package gmr.all.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gmr.all.framework.enums.RequestMethod;
import gmr.all.framework.interceptor.HandlerInterceptor;

/**
 * url映射注解
 * 
 * @author gmr
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
	/**
	 * url映射路径
	 * 
	 * @return
	 */
	public String value();

	/**
	 * 请求方法
	 * 
	 * @return
	 */
	public RequestMethod[] method() default {};
	
	/**
	 * 拦截器方法
	 * @return
	 */
	public Class<? extends HandlerInterceptor>[] interceptors() default {};

	/**
	 * 请求头
	 * 
	 * @return
	 */
	// public abstract String[] headers();
}

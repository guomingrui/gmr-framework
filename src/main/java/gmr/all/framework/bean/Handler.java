package gmr.all.framework.bean;

import gmr.all.framework.interceptor.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装了请求url处理方法的信息
 * 
 * @author gmr
 *
 */
public class Handler {

	/*
	 * 所在Controller
	 */
	private Class<?> controllerClass;

	/*
	 * url映射方法
	 */
	private Method method;
	/*
	 * 返回是否是请求体
	 */
	private boolean isResponseBody = false;

	/*
	 * 方法参数
	 */
	private List<Param> params;
	/*
	 * 拦截器对象链
	 */
	private List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();

	public List<HandlerInterceptor> getInterceptors() {
		return interceptors;
	}

	public void setInterceptors(List<HandlerInterceptor> interceptors) {
		this.interceptors = interceptors;
	}


	public Handler(Class<?> controllerClass, Method method,
			boolean isResponseBody, List<Param> params,
			List<HandlerInterceptor> interceptors) {
		this.controllerClass = controllerClass;
		this.method = method;
		this.isResponseBody = isResponseBody;
		this.params = params;
		this.interceptors = interceptors;
	}


	public List<Param> getParams() {
		return params;
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public Method getMethod() {
		return method;
	}

	public boolean isResponseBody() {
		return isResponseBody;
	}

}

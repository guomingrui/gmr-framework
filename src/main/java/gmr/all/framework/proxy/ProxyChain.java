package gmr.all.framework.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.MethodProxy;

/**
 * 链式代理
 */
public class ProxyChain {
	private final Class<?> targetClass;
	private final Object targetObject;
	private final Method targetMethod;
	private final MethodProxy methodProxy;
	private final Object[] methodParams;

	private List<Proxy> proxyList = new ArrayList<Proxy>();
	private int proxyIndex = 0;

	public ProxyChain(Class<?> targetClass, Object targetObject,
			Method targetMethod, MethodProxy methodProxy,
			Object[] methodParams, List<Proxy> proxyList) {
		super();
		this.targetClass = targetClass;
		this.targetObject = targetObject;
		this.targetMethod = targetMethod;
		this.methodProxy = methodProxy;
		this.methodParams = methodParams;
		this.proxyList = proxyList;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}


	public Object[] getMethodParams() {
		return methodParams;
	}

	public Method getTargetMethod() {
		return targetMethod;
	}

	public Object doProxyChain() throws Throwable {
		Object methodResult;
		if (proxyIndex < proxyList.size()) {
			//doProxy递归调用，产生链式效应
			methodResult = proxyList.get(this.proxyIndex++).doProxy(this);
		} else {
			//链式结束
			methodResult = methodProxy.invokeSuper(this.targetObject,this.methodParams);
		}
		return methodResult;
	}
}

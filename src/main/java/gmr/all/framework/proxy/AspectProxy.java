package gmr.all.framework.proxy;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AspectProxy implements Proxy {

	private Logger lOGGER = LoggerFactory.getLogger(AspectProxy.class);

	public Object doProxy(ProxyChain proxyChain) throws Throwable {
		Object result = null;
		Class<?> targetClass = proxyChain.getTargetClass();
		Method targetMethod = proxyChain.getTargetMethod();
		Object[] methodParams = proxyChain.getMethodParams();
		begin();
		try {
			if (intercept(targetClass, targetMethod, methodParams)) {
				before(targetClass, targetMethod, methodParams);
				result = proxyChain.doProxyChain();
				after(targetClass, targetMethod, methodParams, result);
			} else {
				result = proxyChain.doProxyChain();
			}
		} catch (Throwable e) {
			lOGGER.error("error in doing proxy", e);
			throwable(targetClass, targetMethod, methodParams, e);
			throw e;
		} finally {
			end();
		}
		return result;
	}

	public boolean intercept(Class<?> targetClass, Method targetMethod,
			Object[] methodParams) throws Throwable {
		return true;
	}

	public void begin() {
	}

	public void before(Class<?> targetClass, Method targetMethod,
			Object[] methodParams) {
	}

	public void after(Class<?> targetClass, Method targetMethod,
			Object[] methodParams, Object result) {
	}

	public void throwable(Class<?> targetClass, Method targetMethod,
			Object[] methodParams, Throwable e) throws Throwable{
	}

	public void end() {
	}

}

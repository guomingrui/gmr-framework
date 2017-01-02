package gmr.all.framework.proxy;

import gmr.all.framework.annotation.Aspect;
import gmr.all.framework.annotation.Service;
import gmr.all.framework.factory.SessionFactory;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *	Service的拦截切面
 */
@Aspect(Service.class)
public class ServiceAspect extends AspectProxy {
	private static final Logger lOGGER = LoggerFactory.getLogger(AspectProxy.class);

	@Override
	public void before(Class<?> targetClass, Method targetMethod,
			Object[] methodParams) {
		lOGGER.debug("begin trastration");
		SessionFactory.getCurrentSession().beginTranstaction();
	}

	@Override
	public void end() {
		lOGGER.debug("end trastration");
		SessionFactory.getCurrentSession().commit();
	}

	@Override
	public void throwable(Class<?> targetClass, Method targetMethod,
			Object[] methodParams, Throwable e) throws Throwable {
		if(e instanceof RuntimeException){
			SessionFactory.getCurrentSession().rollback();
		}
		throw e;
	}
	
	

}

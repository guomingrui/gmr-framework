package gmr.all.framework.proxy;

import gmr.all.framework.annotation.Aspect;
import gmr.all.framework.annotation.Service;
import gmr.all.framework.factory.SessionFactory;

import java.lang.reflect.Method;


/**
 *	Controller的拦截切面
 */
@Aspect(Service.class)
public class ControllerAspect extends AspectProxy {
	@Override
	public void before(Class<?> targetClass, Method targetMethod,
			Object[] methodParams) {
		SessionFactory.getCurrentSession().beginTranstaction();
	}

	@Override
	public void end() {
		SessionFactory.getCurrentSession().commit();
	}
}

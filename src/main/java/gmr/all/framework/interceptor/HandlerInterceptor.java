package gmr.all.framework.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract interface HandlerInterceptor {
	public abstract boolean preHandle(
			HttpServletRequest paramHttpServletRequest,
			HttpServletResponse paramHttpServletResponse)
			throws Exception;


	public abstract void afterNormalCompletion(
			HttpServletRequest paramHttpServletRequest,
			HttpServletResponse paramHttpServletResponse, Object paramObject) throws Exception;
}
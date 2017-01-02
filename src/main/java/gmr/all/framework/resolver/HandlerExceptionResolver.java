package gmr.all.framework.resolver;

import gmr.all.framework.bean.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract interface HandlerExceptionResolver {
	public abstract ModelAndView resolveException(
			HttpServletRequest paramHttpServletRequest,
			HttpServletResponse paramHttpServletResponse,
			Exception paramException);
}
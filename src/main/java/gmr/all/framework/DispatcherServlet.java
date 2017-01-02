package gmr.all.framework;

import gmr.all.framework.bean.Handler;
import gmr.all.framework.bean.ModelAndView;
import gmr.all.framework.bean.Param;
import gmr.all.framework.enums.RequestMethod;
import gmr.all.framework.hellper.BeanHelper;
import gmr.all.framework.hellper.ConfigHelper;
import gmr.all.framework.hellper.ControllerHelper;
import gmr.all.framework.interceptor.HandlerInterceptor;
import gmr.all.framework.resolver.HandlerExceptionResolver;
import gmr.all.framework.util.BeanUtil;
import gmr.all.framework.util.CastUtil;
import gmr.all.framework.util.DefaultValueUtil;
import gmr.all.framework.util.UrlUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

	private static HandlerExceptionResolver exceptionResolver;
	private static final long serialVersionUID = 3057125583392252506L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DispatcherServlet.class);

	// private List<Method> list = new ArrayList<Meth>();
	@Override
	public void init(ServletConfig config) throws ServletException {
		// 初始化助手类
		HelperLoader.init();
		exceptionResolver = BeanHelper.getBean(HandlerExceptionResolver.class);
		// 获取Servlet上下文对象(ServletContext)
		ServletContext servletContext = config.getServletContext();
		// 静态资源文件Servlet
		ServletRegistration defaultRegistration = servletContext
				.getServletRegistration("default");
		defaultRegistration.addMapping(ConfigHelper.getAppAssetPath() + "*");
		// jsp文件的Servlet
		ServletRegistration jspRegistration = servletContext
				.getServletRegistration("jsp");
		jspRegistration.addMapping(ConfigHelper.getAppJspPath() + "*");
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String url = UrlUtil.getDecodeUrl("/" + request.getPathInfo() + "/");
		// 转换方法
		RequestMethod method = getRequestMethod(request);
		if (method == null) {
			LOGGER.info("不能处理的请求类型" + request.getMethod());
			response.sendError(405, "不能处理的请求类型");
			return;
		}
		Handler handler = ControllerHelper.getHandler(method, url);
		if (handler == null) {
			if (ControllerHelper.isUrlExist(url)) {
				// 找不到映射路径
				response.sendError(405, "不支持的请求类型");
			} else {
				// 找不到映射路径z
				response.sendError(404, "找不到该页面");
			}
			return;
		}
		// 获取并生成方法参数
		Object[] objects = getMethodParmters(handler.getParams(), request,
				response);

		/* 反射调用mapping方法,并获取返回值 */
		Object result = null;
		try {
			boolean isPass = true;
			List<HandlerInterceptor> interceptors = handler.getInterceptors();
			int length = interceptors.size();
			for (int i = 0; i < length; ++i) {
				if (!(isPass = interceptors.get(i).preHandle(request, response))) {
					break;
				}
			}
			if (!isPass) {
				return;
			}
			result = BeanUtil.invokeMethod(handler.getMethod(),
					BeanHelper.getBean(handler.getControllerClass()), objects);
			for (int i = length - 1; i >= 0; --i) {
				interceptors.get(i).afterNormalCompletion(request, response,
						result);
			}
		} catch (Exception e) {
			// 这里可以实现异常解析器
			// 暂未实现自定义异常解析器功能
			if (DispatcherServlet.exceptionResolver != null) {
				exceptionResolver.resolveException(request, response, e);
			} else {
				throw new RuntimeException(e);
			}
		}
		/*
		 * 解析返回值
		 */
		if (result == null) {
			return;
		}
		if (handler.isResponseBody()) {
			// 请求体
			// 返回text内容
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(result.toString());
			response.getWriter().flush();
			response.getWriter().close();
			return;
		}
		ModelAndView modelAndView = getModelAndView(result, request);

		resolveModelAndView(modelAndView, request, response);
	}

	/**
	 * 解析ModelAndView
	 * 
	 * @param modelAndView
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void resolveModelAndView(ModelAndView modelAndView,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String viewPath = modelAndView.getViewPath();
		if (viewPath.startsWith("redirect:")) {
			// 重定向
			if (request.getContextPath().length() > 0) {
				viewPath = request.getContextPath() + "/"
						+ viewPath.replaceFirst("redirect:", "");
			} else {
				viewPath = viewPath.replaceFirst("redirect:", "");
			}
			response.sendRedirect(viewPath);
			return;
		}
		Map<String, Object> map = modelAndView.getModel();
		if (map != null) {
			for (Entry<String, Object> entry : map.entrySet()) {
				request.setAttribute(entry.getKey(), entry.getValue());
			}
		}
		if (viewPath.startsWith("forward:")) {
			// 请求转发
			viewPath = viewPath.replaceFirst("forward:", "");
			request.getRequestDispatcher(viewPath).forward(request, response);
			return;
		}
		request.getRequestDispatcher(
				ConfigHelper.getAppJspPath() + viewPath + ".html").forward(
				request, response);
	}

	/**
	 * 解析返回值并生成modelAndView
	 * 
	 * @param result
	 * @param request
	 * @return
	 */
	private ModelAndView getModelAndView(Object result,
			HttpServletRequest request) {
		ModelAndView modelAndView = null;
		if (result instanceof String) {
			modelAndView = new ModelAndView((String) result);
		} else if (result instanceof ModelAndView) {
			modelAndView = (ModelAndView) result;
		} else {
			modelAndView = new ModelAndView(request.getPathInfo());
		}
		return modelAndView;
	}

	/**
	 * 获取请求方法,判断是否支持该类型请求,并转为RequestMethod
	 * 
	 * @param request
	 * @return
	 */
	private RequestMethod getRequestMethod(HttpServletRequest request) {
		try {
			RequestMethod method = RequestMethod.valueOf(request.getMethod());
			if (method == RequestMethod.POST) {
				String hiddenMethod = request.getParameter("_method");
				if (hiddenMethod != null) {
					hiddenMethod.toUpperCase();
					method = RequestMethod.valueOf(request.getMethod());
					if (method == RequestMethod.GET) {
						method = RequestMethod.POST;
					}
				}
			}
			return method;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取并生成要调用mapping方法的参数
	 * 
	 * @param list
	 * @param request
	 * @param response
	 * @return
	 */
	private Object[] getMethodParmters(List<Param> list,
			HttpServletRequest request, HttpServletResponse response) {
		Object[] objects = new Object[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Param param = list.get(i);
			if (param.getParamName() == null) {
				// 未声明,取默认值
				objects[i] = DefaultValueUtil.getDefaultValue(param
						.getParamClass());
			} else if (DefaultValueUtil.getMap().containsKey(
					param.getParamClass())) {
				// 声明，取相关的值
				if (param.getParamClass().equals(Array.class)) {
					// 是数组
					objects[i] = request.getParameterValues(param
							.getParamName());
				} else {
					// 非数组
					objects[i] = request.getParameter(param.getParamName());
				}
			} else if (HttpServletRequest.class.equals(param.getParamClass())) {
				// request对象
				objects[i] = request;
			} else if (HttpServletResponse.class.equals(param.getParamClass())) {
				// response对象
				objects[i] = response;
			} else if (HttpSession.class.equals(param.getParamClass())) {
				// session对象
				objects[i] = request.getSession();
			} else {
				// 初始化对象
				try {
					objects[i] = param.getParamClass().newInstance();
				} catch (Exception e) {
					LOGGER.error(param.getParamClass().getName()
							+ " has not default Constructor", e);
					throw new RuntimeException();
				}
				// 注入到参数对象里面
				for (Field field : objects[i].getClass().getDeclaredFields()) {
					if (DefaultValueUtil.getMap().containsKey(
							field.getGenericType())) {
						String fieldValue = request.getParameter(field
								.getName());
						if (fieldValue != null) {
							// 查到值，注入
							BeanUtil.setField(field, objects[i], CastUtil
									.castSuitable(field.getType(), fieldValue));
						}
					}
				}
			}

		}
		return objects;
	}

}

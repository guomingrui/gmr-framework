package gmr.all.framework;


import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewServlet extends HttpServlet {

	private static final long serialVersionUID = 3057125583392252506L;
	private static String mappingPath = null;
	// private List<Method> list = new ArrayList<Meth>();
	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("信息:"+mappingPath+request.getPathInfo());
		request.getRequestDispatcher(request.getPathInfo()).forward(request, response);
	}

}

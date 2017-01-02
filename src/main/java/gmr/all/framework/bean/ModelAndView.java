package gmr.all.framework.bean;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
	String viewPath = null;
	Map<String, Object> model = null;

	public ModelAndView(String viewPath, Map<String, Object> model) {
		this.viewPath = viewPath;
		this.model = model;
	}

	public ModelAndView(String viewPath) {
		this.viewPath = viewPath;
	}

	public String getViewPath() {
		return viewPath;
	}

	public void setViewPath(String viewPath) {
		this.viewPath = viewPath;
	}

	public Map<String, Object> getModel() {
		return model;
	}

	public void addAttribute(String key, Object value) {
		if (model == null) {
			model = new HashMap<String, Object>();
		}
		model.put(key, value);
	}

	public void addModel(Map<String, Object> map) {
		if (model == null) {
			model = map;
		} else {
			model.putAll(map);
		}
	}

}

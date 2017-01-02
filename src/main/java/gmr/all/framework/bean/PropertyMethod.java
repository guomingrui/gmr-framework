package gmr.all.framework.bean;

import java.lang.reflect.Method;

/**
 * 属性对应的get和set方法
 */
public class PropertyMethod {
	private Method getMethod;
	private Method setMethod;

	
	public Method getGetMethod() {
		return getMethod;
	}

	public void setGetMethod(Method getMethod) {
		this.getMethod = getMethod;
	}

	public Method getSetMethod() {
		return setMethod;
	}

	public void setSetMethod(Method setMethod) {
		this.setMethod = setMethod;
	}

	public boolean isGetExist() {
		return getMethod != null ? true : false;
	}

	public boolean isSetExist() {
		return setMethod != null ? true : false;
	}

	public Object get(Object target) throws Exception {
		return getMethod.invoke(target);
	}

	public void set(Object target, Object value) throws Exception {
		setMethod.invoke(target, value);
	}

}

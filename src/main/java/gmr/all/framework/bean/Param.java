package gmr.all.framework.bean;

/**
 * 请求参数类型
 * 
 * @author gmr
 *
 */
public class Param {
	private Class<?> paramClass;
	private String paramName;

	public Param(Class<?> paramClass, String paramName) {
		this.paramClass = paramClass;
		this.paramName = paramName;
	}

	public Class<?> getParamClass() {
		return paramClass;
	}

	public String getParamName() {
		return paramName;
	}

	// private Map<String, String> paramMap;
	//
	// public Param(Map<String, String> paramMap) {
	// this.paramMap = paramMap;
	// }
	//
	// public Map<String, String> getParamMap() {
	// return paramMap;
	// }
	//
	// public void setParamMap(Map<String, String> paramMap) {
	// this.paramMap = paramMap;
	// }
	//
	// public Long getLong(String name) {
	// String value = paramMap.get(name);
	// if (value == null) {
	// return null;
	// }
	// return CastUtil.castToLong(value);
	// }
	//
	// public Double getDouble(String name) {
	// String value = paramMap.get(name);
	// if (value == null) {
	// return null;
	// }
	// return CastUtil.castToDouble(value);
	// }
	//
	// public String getString(String name) {
	// return paramMap.get(name);
	// }
	//
	// public Integer getInteger(String name) {
	// String value = paramMap.get(name);
	// if (value == null) {
	// return null;
	// }
	// return CastUtil.castToInt(value);
	// }
	//
	// public Boolean getBoolean(String name) {
	// String value = paramMap.get(name);
	// if (value == null) {
	// return null;
	// }
	// return CastUtil.castToBoolean(value);
	// }

}

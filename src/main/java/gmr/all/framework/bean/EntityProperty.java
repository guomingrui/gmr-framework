package gmr.all.framework.bean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实体属性名表
 * 得重构一遍该class
 */
public class EntityProperty {
	private static Logger LOGGER = LoggerFactory
			.getLogger(EntityProperty.class);
	private String entityName;
	private Class<?> entityClass;
	private String tableName;
	private String idName;// 主键名称(属性名)
	// 设置属性对应的set方法
	private Map<String, Method> setPropertyMap = new HashMap<String, Method>();
	// 设置属性对应的set方法
	private Map<String, Method> getPropertyMap = new HashMap<String, Method>();
	// 实体属性名与对应的表字段名
	private Map<String, String> tableEntityMap = new HashMap<String, String>();
	// 实体属性名与对应中间表的表名
	private Map<String, String> tableMiddleMap = new HashMap<String, String>();
	// 实体属性名与对应的中间表字段名,Property,key该属性映射，value对应的字段映射entity
	private Map<String, Property> tableMiddleFirstMap = new HashMap<String, Property>();
	// 实体属性名对应的实体Class
	private Map<String, Class<?>> constraintEntityMap = new HashMap<String, Class<?>>();

	public Method getGetIdMethod() {
		return this.getPropertyMap.get(this.idName);
	}

	public Object getIdValue(Object obj) {
		try {
			return getGetIdMethod().invoke(obj);
		} catch (Exception e) {
			throw new RuntimeException("get Id method error",e);
		}
	}
	public Map<String, Method> getSetPropertyMap() {
		return this.setPropertyMap;
	}
	
	public Map<String, Method> getGetPropertyMap() {
		return this.getPropertyMap;
	}
	
	public Object getPropertyValue(String propertyName,Object instance){
		try {
			return this.getPropertyMap.get(propertyName).invoke(instance);
		} catch (Exception e) {
			throw new RuntimeException("get property method error",e);
		}
	}
	
	public void setGetMethod(String propertyName,Method getMethod){
		this.getPropertyMap.put(propertyName,getMethod);
	}

	
	public Property getMiddleColumns(String propertyName){
		return this.tableMiddleFirstMap.get(propertyName);
	}
	
	public void putConstraintEntity(String propertyName, Class<?> entitClass) {
		constraintEntityMap.put(propertyName, entitClass);
	}

	public Map<String, Class<?>> getConstraintEntityMap() {
		return this.constraintEntityMap;
	}

	public Class<?> getConstraintEntityClass(String propertyName) {
		return this.constraintEntityMap.get(propertyName);
	}

	public void setObject(String propertyName, Object instance, Object setParam) {
		try {
			setPropertyMap.get(propertyName).invoke(instance, setParam);
		} catch (Exception e) {
			LOGGER.error(propertyName + " has no correct set method", e);
			throw new RuntimeException(propertyName + " has no correct set method",e);
		}
	}

	public String getIdName() {
		return idName;
	}

	/**
	 * 获取关联对象的表名
	 * 
	 * @return
	 */
	public Map<String, String> getMiddleTableNameMap() {
		return this.tableMiddleMap;
	}

	public Map<String, String> getTableEntityMap() {
		return tableEntityMap;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void putTableMapping(String entityColumnName, String tableColumnName) {
		tableEntityMap.put(entityColumnName, tableColumnName);
	}

	public void setSetMethod(String entityColumnName, Method setMethod) {
		setPropertyMap.put(entityColumnName, setMethod);
	}

	public void putTableMiddleMapping(String propertyName, String tableColumn) {
		tableMiddleMap.put(propertyName, tableColumn);
	}

	public void putTableValuelMapping(String propertyName, Property property) {
		tableMiddleFirstMap.put(propertyName, property);
	}

	public String getEntityMappingColumn(String propertyName) {
		return tableEntityMap.get(propertyName);
	}
}

package gmr.all.framework.hellper;

import gmr.all.framework.annotation.Column;
import gmr.all.framework.annotation.Entity;
import gmr.all.framework.annotation.GeneratedValue;
import gmr.all.framework.annotation.Id;
import gmr.all.framework.annotation.JoinColumn;
import gmr.all.framework.annotation.JoinTable;
import gmr.all.framework.annotation.ManyToMany;
import gmr.all.framework.annotation.ManyToOne;
import gmr.all.framework.annotation.OneToMany;
import gmr.all.framework.annotation.OneToOne;
import gmr.all.framework.annotation.Table;
import gmr.all.framework.bean.EntityProperty;
import gmr.all.framework.bean.Property;
import gmr.all.framework.bean.PropertyMethod;
import gmr.all.framework.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBInitHelper {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DBInitHelper.class);
	// private static final
	// 实体名对应的Map
	private static final Map<String, EntityProperty> ENTITY_NAME_MAP = new HashMap<String, EntityProperty>();
	// 实体Class对应的Map
	private static final Map<Class<?>, EntityProperty> ENTITY_CLASS_MAP = new HashMap<Class<?>, EntityProperty>();
	// map指向字段
	private static Map<String, Property> referenceMap = new HashMap<String, Property>();
	static {
		// 获取包下Entity
		Set<Class<?>> entityClassSet = getEntityClassSet();
		// 初始化表的数量及表名
		Map<String, Map<String, String>> tableMap = getTableNameMap(entityClassSet);
		// 初始化基本字段（无关联字段）
		if(ConfigHelper.getJdbcUrl().length() > 0){
			initTableBaseColumnMap(tableMap, entityClassSet);
			// 初始化关联字段
			initTableConstraintColumnMap(tableMap, entityClassSet);

			try {
				// 建表
				// 建表完成
				TableGeneratorHelper.creating(tableMap, referenceMap);
			} catch (Exception e) {
				LOGGER.error("create table error", e);
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
	}

	public static EntityProperty getEntityProperty(Class<?> entityClass) {
		return ENTITY_CLASS_MAP.get(entityClass);
	}

	public static EntityProperty getEntityProperty(String entityName) {
		return ENTITY_NAME_MAP.get(entityName);
	}

	/**
	 * 初始化关联字段类型
	 */
	private static void initConstraintColumn(Class<?> entityClass,
			Method method, Map<String, String> map,
			Map<String, Map<String, String>> tableMap, String entityTableName) {
		String defaultColumnName = firstToLower(method.getName().replaceFirst(
				"get", ""));
		String columnPropertyName = defaultColumnName;
		StringBuilder constraint = new StringBuilder();
		// 获取方法返回值以及各大注解值
		Class<?> returnType = method.getReturnType();
		OneToOne oneToOne = method.getAnnotation(OneToOne.class);
		OneToMany oneToMany = method.getAnnotation(OneToMany.class);
		ManyToOne manyToOne = method.getAnnotation(ManyToOne.class);
		ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
		JoinColumn joinColumn = method.getAnnotation(JoinColumn.class);
		JoinTable joinTable = method.getAnnotation(JoinTable.class);
		boolean isCollection = false;
		EntityProperty entityProperty = ENTITY_CLASS_MAP.get(entityClass);
		String tableName = null;
		String tableJoinColumn = null;
		String inverseJoinColumn = null;
		String joinColumnType = null;
		String inverseJoinColumnType = null;
		String firstForeignKey = null;
		String secondForeignKey = null;
		/*
		 * 集合获取类型
		 */
		if (Collection.class.isAssignableFrom(returnType)) {
			if (manyToMany == null) {
				LOGGER.error("Collection must has ManyToMany annotation");
				throw new RuntimeException();
			}
			returnType = manyToMany.entityClass();
			isCollection = true;
		}
		if (returnType.isPrimitive()
				|| Number.class.isAssignableFrom(returnType)
				|| Boolean.class.equals(returnType)
				|| Character.class.equals(returnType)
				|| String.class.equals(returnType)
				|| Byte.class.equals(returnType)
				|| Date.class.equals(returnType)) {
			// 如果是java基本属性以及一些常用的基本对象类型
		} else if (returnType.isAnnotationPresent(Entity.class)) {
			EntityProperty otherEntityProperty = ENTITY_CLASS_MAP
					.get(returnType);
			String joinIdName = entityProperty
					.getEntityMappingColumn(entityProperty.getIdName());
			String inverseJoinIdName = otherEntityProperty
					.getEntityMappingColumn(otherEntityProperty.getIdName());
			if (joinIdName != null) {
				joinColumnType = map.get(joinIdName).trim().split(" ")[0];
			} else {
				LOGGER.error("eneity must hava ID");
				throw new RuntimeException("eneity must hava ID");
			}
			if (inverseJoinIdName != null) {
				inverseJoinColumnType = tableMap
						.get(otherEntityProperty.getTableName())
						.get(inverseJoinIdName).trim().split(" ")[0];
			} else {
				LOGGER.error("eneity must hava ID");
				throw new RuntimeException();
			}
			// 如果是实体关联关系
			// 确定主键映射方式
			if (joinTable != null) {
				// 放置中间表
				// 获取中间表
				tableName = joinTable.tableName();
				tableJoinColumn = joinTable.joinColumn();
				inverseJoinColumn = joinTable.inverseJoinColumn();
				// 创建外主键语句
				firstForeignKey = "CONSTRAINT k_" + System.currentTimeMillis()
						+ constraint.hashCode() + "d FOREIGN KEY("
						+ tableJoinColumn + ") REFERENCES "
						+ entityProperty.getTableName() + "(" + joinIdName
						+ ")";

				secondForeignKey = "CONSTRAINT s_" + System.currentTimeMillis()
						+ constraint.hashCode() + "d FOREIGN KEY("
						+ inverseJoinColumn + ") REFERENCES "
						+ otherEntityProperty.getTableName() + "("
						+ inverseJoinIdName + ")";
			} else if (joinColumn != null) {
				if (joinColumn.name().length() != 0) {
					defaultColumnName = joinColumn.name();
				}

				if (!joinColumn.nullable()) {
					constraint.append(" NOT NULL ");
				}
				if (joinColumn.unique()) {
					constraint.append(" UNIQUE ");
				}
			}
			if (manyToMany != null) {
				if (!isCollection) {
					LOGGER.error(entityClass
							+ " Many To Many must the Collection");
					throw new RuntimeException();
				}
				if (joinTable == null) {
					LOGGER.error(entityClass
							+ " Many To Many must the annotation JoinTable");
					throw new RuntimeException();
				}
				// 字段对应的外键名字

			} else if (joinTable != null) {
			} else if (oneToMany != null) {
				if (!isCollection) {
					LOGGER.error(entityClass
							+ " One To Many must the Collection");
					throw new RuntimeException();
				}
				tableName = otherEntityProperty.getTableName();
				tableJoinColumn = defaultColumnName;
				inverseJoinColumn = otherEntityProperty
						.getEntityMappingColumn(otherEntityProperty.getIdName());
				inverseJoinColumnType = null;
				firstForeignKey = "CONSTRAINT k_" + System.currentTimeMillis()
						+ constraint.hashCode() + "d FOREIGN KEY("
						+ tableJoinColumn + ") REFERENCES "
						+ entityProperty.getTableName() + "(" + joinIdName
						+ ")";

			} else if (isCollection) {
				LOGGER.error(entityClass
						+ " in Collection only on Many To Many");
				throw new RuntimeException();
			} else if (manyToOne != null || oneToOne != null) {
				tableName = entityTableName;
				tableJoinColumn = entityProperty
						.getEntityMappingColumn(entityProperty.getIdName());
				inverseJoinColumn = defaultColumnName;
				joinColumnType = null;
				secondForeignKey = "CONSTRAINT s_" + System.currentTimeMillis()
						+ constraint.hashCode() + "d FOREIGN KEY("
						+ inverseJoinColumn + ") REFERENCES "
						+ otherEntityProperty.getTableName() + "("
						+ inverseJoinIdName + ")";
			} else {
				LOGGER.error("the Method " + method.getName()
						+ " has incorrect annocation");
				throw new RuntimeException();
			}
			// 添加外键语句
			// joinTable方式
			referenceMap.put(tableName, new Property(firstForeignKey,
					secondForeignKey));
			// 添加中间表映射
			entityProperty.putConstraintEntity(columnPropertyName, returnType);
			entityProperty.putTableMiddleMapping(columnPropertyName, tableName);
			entityProperty.putTableValuelMapping(columnPropertyName,
					new Property(tableJoinColumn, inverseJoinColumn));
		} else {
			// 关联关系出错
			LOGGER.error("the Method " + method.getName()
					+ " can't find the mapping entity");
			throw new RuntimeException("the Method " + method.getName()
					+ " can't find the mapping entity");
		}

		// 添加主键类型
		if (joinColumnType != null) {
			tableMap.get(tableName).put(tableJoinColumn,
					joinColumnType + constraint.toString());
		}
		if (inverseJoinColumnType != null) {

			tableMap.get(tableName).put(inverseJoinColumn,
					constraint.insert(0, inverseJoinColumnType).toString());
		}
	}

	/**
	 * 获取基本字段定义
	 * 
	 * @return
	 */
	private static void initTableBaseColumnMap(
			Map<String, Map<String, String>> tableMap,
			Set<Class<?>> entityClassSet) {
		for (Class<?> entityClass : entityClassSet) {
			String tableName = ENTITY_CLASS_MAP.get(entityClass).getTableName();
			for (Method method : entityClass.getDeclaredMethods()) {
				if (method.getName().startsWith("get")) {
					initBaseColumnProperty(entityClass, method,
							tableMap.get(tableName));
				}
			}
		}
	}

	/**
	 * 获取基本字段定义
	 * 
	 * @return
	 */
	private static void initTableConstraintColumnMap(
			Map<String, Map<String, String>> tableMap,
			Set<Class<?>> entityClassSet) {
		for (Class<?> entityClass : entityClassSet) {
			String tableName = ENTITY_CLASS_MAP.get(entityClass).getTableName();
			for (Method method : entityClass.getDeclaredMethods()) {
				if (method.getName().startsWith("get")) {
					initConstraintColumn(entityClass, method,
							tableMap.get(tableName), tableMap, tableName);

				}
			}
		}
	}

	/**
	 * 获取方法上对字段的描述
	 * 
	 * @param sqlSource
	 * @throws ColumnException
	 */
	private static void initBaseColumnProperty(Class<?> entityClass,
			Method method, Map<String, String> map) {
		String defaultColumnName = firstToLower(method.getName().replaceFirst(
				"get", ""));
		String columnPropertyName = defaultColumnName;
		StringBuilder constraint = new StringBuilder();
		// 获取方法返回值以及各大注解值
		Class<?> returnType = method.getReturnType();
		Column column = method.getAnnotation(Column.class);
		Id id = method.getAnnotation(Id.class);
		GeneratedValue generatedValue = method
				.getAnnotation(GeneratedValue.class);

		if (returnType.isPrimitive()
				|| Number.class.isAssignableFrom(returnType)
				|| Boolean.class.equals(returnType)
				|| Character.class.equals(returnType)
				|| String.class.equals(returnType)
				|| Byte.class.equals(returnType)
				|| Date.class.equals(returnType)) {
			// 如果是java基本属性以及一些常用的基本对象类型
			if ((String.class.equals(returnType) && generatedValue != null)) {
				// 如果注解不匹配该普通类型
				LOGGER.error("the Method " + method.getName()
						+ " has incorrect annocation");
				throw new RuntimeException("the Method " + method.getName()
						+ " has incorrect annocation generatedValue");
			}
			int defaultColumnLength = 0;
			if (id != null) {
				constraint.append(" PRIMARY KEY ");
				ENTITY_CLASS_MAP.get(entityClass).setIdName(defaultColumnName);
			}
			if (column != null) {
				String columnName = column.name();
				String columnDefinition = column.columnDefinition();
				boolean nullable = column.nullable();
				boolean unique = column.unique();
				int columnLength = column.length();

				if (columnName.length() > 0) {
					// column注解定义的字段名取代默认字段名
					defaultColumnName = columnName;
				}

				if (columnDefinition.length() > 0) {
					// 排挤其它字段定义，包括Id
					map.put(defaultColumnName, columnDefinition);
					ENTITY_CLASS_MAP.get(entityClass).putTableMapping(
							columnPropertyName, defaultColumnName);
					return;
				}
				if (columnLength > 0) {
					defaultColumnLength = columnLength;
				}
				if (!nullable) {
					constraint.append(" NOT NULL ");
				}
				if (unique) {
					constraint.append(" UNIQUE ");
				}
			}
			if (generatedValue != null) {
				// 这里简单做就只用这个AUTO_INCREMENT
				constraint.append(" AUTO_INCREMENT ");
			}
			// 这里定义类型
			String type = returnType.getSimpleName().toLowerCase();
			if (returnType.equals(String.class)) {
				type = "varchar";
			}
			if (defaultColumnLength <= 0) {
				if (returnType.equals(String.class)) {
					type = type + "(" + 255 + ")";
				}
			} else {
				type = type + "(" + defaultColumnLength + ")";
			}
			map.put(defaultColumnName, type + constraint.toString());
			ENTITY_CLASS_MAP.get(entityClass).putTableMapping(
					columnPropertyName, defaultColumnName);
		} else if (Collection.class.isAssignableFrom(returnType)) {

		} else if (!returnType.isAnnotationPresent(Entity.class)) {
			// 关联关系出错
			LOGGER.error("the Method " + method.getName()
					+ " can't find the mapping entity");
			throw new RuntimeException("the Method " + method.getName()
					+ " can't find the mapping entity");
		}
	}

	/**
	 * 确定要创建的表的名字数量 获取表名集合
	 * 
	 * @param entityClassSet
	 * @return
	 */
	private static Map<String, Map<String, String>> getTableNameMap(
			Set<Class<?>> entityClassSet) {
		Map<String, Map<String, String>> tableMap = new HashMap<String, Map<String, String>>();
		/** 获取表名 **/
		for (Class<?> entityClass : entityClassSet) {
			// 检查是否get和set方法配对

			String tableName = null;
			if (entityClass.isAnnotationPresent(Table.class)) {
				Table tableAnnotation = entityClass.getAnnotation(Table.class);
				tableName = tableAnnotation.name();
			}
			if (tableName == null) {
				String entityName = entityClass.getSimpleName();
				tableName = firstToLower(entityName);
			}

			EntityProperty entityProperty = new EntityProperty();
			entityProperty.setEntityClass(entityClass);
			entityProperty.setEntityName(entityClass.getSimpleName());
			entityProperty.setTableName(tableName);

			ENTITY_CLASS_MAP.put(entityClass, entityProperty);
			ENTITY_NAME_MAP.put(entityProperty.getEntityName(), entityProperty);

			check(entityClass);
			tableMap.put(tableName, new HashMap<String, String>());
			for (Method method : entityClass.getDeclaredMethods()) {
				if (method.getName().length() > 3
						&& method.getName().startsWith("get")) {
					JoinTable joinTable = method.getAnnotation(JoinTable.class);
					if (joinTable != null) {
						tableMap.put(joinTable.tableName(),
								new HashMap<String, String>());
					}
				}
			}

		}
		return tableMap;
	}

	/**
	 * 检查是否有齐全的get和set方法
	 * 
	 * @param entityClass
	 */
	private static void check(Class<?> entityClass) {
		Map<String, PropertyMethod> map = new HashMap<String, PropertyMethod>();
		/* 遍历注解方法，获取要生成的表结构信息 */
		// 遍历get和set方法
		for (Method method : entityClass.getDeclaredMethods()) {
			String methodName = method.getName();
			if (methodName.length() <= 3) {
				continue;
			}
			if (methodName.startsWith("get")) {
				if (method.getParameterCount() != 0) {
					continue;
				}

				String columnName = method.getName().replaceFirst("get", "");
				if (map.get(columnName) == null) {
					map.put(columnName, new PropertyMethod());
				}
				map.get(columnName).setGetMethod(method);
				ENTITY_CLASS_MAP.get(entityClass).setGetMethod(
						firstToLower(columnName), method);

			} else if (methodName.startsWith("set")) {
				if (method.getParameterCount() != 1) {
					continue;
				}
				String columnName = method.getName().replaceFirst("set", "");
				if (map.get(columnName) == null) {
					map.put(columnName, new PropertyMethod());
				}
				map.get(columnName).setSetMethod(method);
				ENTITY_CLASS_MAP.get(entityClass).setSetMethod(
						firstToLower(columnName), method);
			}
		}
		// 遍历map
		// 判断是否有元素只有set或者get方法
		for (Entry<String, PropertyMethod> entry : map.entrySet()) {
			PropertyMethod propertyMethod = entry.getValue();
			if (propertyMethod.getGetMethod() == null
					|| propertyMethod.getSetMethod() == null) {
				// 错误发生，只有单个get或set方法
				LOGGER.error("the entity " + entityClass.getSimpleName()
						+ " has single get or set Method");
				throw new RuntimeException();
			}
		}
	}

	/**
	 * 获取带entity注解的实体类
	 */
	private static Set<Class<?>> getEntityClassSet() {
		Set<Class<?>> entityClassSet = new HashSet<Class<?>>();
		for (Class<?> cls : getEntityPackageClassSet()) {
			if (cls.isAnnotationPresent(Entity.class)) {
				entityClassSet.add(cls);
			}
		}
		return entityClassSet;
	}

	/**
	 * 获取Entity包下的的Class
	 * 
	 * @return
	 */
	private static Set<Class<?>> getEntityPackageClassSet() {
		return ClassUtil.getClassSet(ConfigHelper.getAppEntityPackage());
	}

	/**
	 * 字符串首字母变为小写
	 * 
	 * @param str
	 * @return
	 */
	private static String firstToLower(String str) {
		String first = str.substring(0, 1);
		String toLowerFirst = first.toLowerCase();
		return str.replaceFirst(first, toLowerFirst);
	}
}

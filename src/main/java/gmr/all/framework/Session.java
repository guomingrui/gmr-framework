package gmr.all.framework;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gmr.all.framework.annotation.GeneratedValue;
import gmr.all.framework.bean.EntityProperty;
import gmr.all.framework.bean.Property;
import gmr.all.framework.bean.TableJoinProperty;
import gmr.all.framework.hellper.DBInitHelper;
import gmr.all.framework.util.ConnectionUtil;

/**
 * 未把字符串模版缓存起来
 * 
 * @author gmr
 *
 */
public class Session {
	private static Logger LOGGER = LoggerFactory.getLogger(Session.class);
	private static final Map<Class<?>, String> GET_ENTITY_SQL_CASH = new HashMap<Class<?>, String>();
	private static final Map<Class<?>, String> SAVE_ENTITY_SQL_CASH = new HashMap<Class<?>, String>();
	private Connection connection = null;
	boolean isBeginTraction = false;
	boolean isClose = false;

	public Session() {
	}

	public Connection getConnection() {
		try {
			if (isClose == true) {
				throw new RuntimeException("session is closed");
			}
			if (this.connection == null || this.connection.isClosed()) {
				this.connection = ConnectionUtil.getConnection();
				this.connection.setAutoCommit(!isBeginTraction);
			}
			return this.connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将对象保存到对应的数据库表中 暂时不支持级联保存对象
	 * 
	 * @param saveObject
	 */
	public void save(Object saveObject) {
		Class<?> entityClass = saveObject.getClass();
		EntityProperty entityProperty = DBInitHelper
				.getEntityProperty(entityClass);
		if (entityProperty == null) {
			LOGGER.error(entityClass + " is not an entity");
			throw new RuntimeException(entityClass + " is not an entity");
		}
		List<String> propertyList = new ArrayList<String>();
		Object id = null;

		String saveBaseSql = getSaveBaseSql(entityClass, propertyList,
				entityProperty.getTableEntityMap());
		// 保存基本对象,未增加表关联
		try {
			PreparedStatement preparedStatement = getConnection()
					.prepareStatement(saveBaseSql,
							Statement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < propertyList.size(); i++) {
				preparedStatement.setObject(i + 1, entityProperty
						.getPropertyValue(propertyList.get(i), saveObject));
			}
			preparedStatement.executeUpdate();
			ResultSet rs = null;
			try {
				rs = preparedStatement.getGeneratedKeys();
				if (rs.next()) {
					Class<?> idType = entityProperty.getGetIdMethod()
							.getReturnType();
					id = rs.getObject(1);
					if (id.getClass().equals(Long.class)
							&& (idType.equals(Integer.class) || idType
									.equals(int.class))) {
						entityProperty.setObject(entityProperty.getIdName(),
								saveObject, (int) (long) (Long) id);
					} else if (id.getClass().equals(Long.class)
							&& (idType.equals(short.class) || idType
									.equals(Short.class))) {
						entityProperty.setObject(entityProperty.getIdName(),
								saveObject, (short) (long) (Long) id);
					} else {
						entityProperty
								.setObject(entityProperty.getIdName(),
										saveObject,
										(int) (long) (Long) rs.getObject(1));
					}
				} else {
					id = entityProperty.getIdValue(saveObject);
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
		} catch (SQLException e) {
			return;
		}
		// 生成表关联语句
		Map<String, TableJoinProperty> constraintMap = getConstraintMap(
				entityProperty, entityProperty.getTableName(),
				entityProperty.getEntityMappingColumn(entityProperty
						.getIdName()));
		for (Entry<String, Class<?>> constraintEntry : entityProperty
				.getConstraintEntityMap().entrySet()) {
			Method getMethod = entityProperty.getGetPropertyMap().get(
					constraintEntry.getKey());
			Class<?> constraintClass = constraintEntry.getValue();
			String propertyName = constraintEntry.getKey();
			Object constraint = null;
			try {
				constraint = getMethod.invoke(saveObject);
			} catch (Exception e) {
			}
			if (constraint == null) {
				continue;
			}
			if (Collection.class.isAssignableFrom(constraint.getClass())) {
				// 集合对象
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) entityProperty
						.getPropertyValue(propertyName, saveObject);
				for (Object obj : collection) {
					try {
						bulidConstraint(constraintMap.get(propertyName), id,
								DBInitHelper.getEntityProperty(constraintClass)
										.getIdValue(obj));
					} catch (SQLException e) {
						LOGGER.error("bulid constraint error", e);
						throw new RuntimeException("bulid constraint error", e);
					}
				}

			} else {
				// 非集合对象
				try {
					bulidConstraint(constraintMap.get(propertyName), id,
							DBInitHelper.getEntityProperty(constraintClass)
									.getIdValue(constraint));
				} catch (SQLException e) {
					LOGGER.error("bulid constraint error", e);
					throw new RuntimeException("bulid constraint error", e);
				}

			}

		}
	}

	/**
	 * 建立关联关系
	 * 
	 * @throws SQLException
	 */
	public void bulidConstraint(TableJoinProperty tableJoinProperty,
			Object joinId, Object inverseJoinId) throws SQLException {
		PreparedStatement preparedStatement = null;
		StringBuilder sqlBuilder = new StringBuilder();
		// 下面三个暂未改成模版
		if (tableJoinProperty.getMainTableName().equals(
				tableJoinProperty.getMiddleTableName())) {
			// 中间表与主表同表
			sqlBuilder.append("UPDATE ")
					.append(tableJoinProperty.getMainTableName())
					.append(" SET ")
					.append(tableJoinProperty.getInverseJoinColumn())
					.append("=").append("?").append(" WHERE ")
					.append(tableJoinProperty.getMaintTableIdName())
					.append("=").append("?");
			preparedStatement = getConnection().prepareStatement(
					sqlBuilder.toString());
			preparedStatement.setObject(1, inverseJoinId);
			preparedStatement.setObject(2, joinId);
		} else if (tableJoinProperty.getTargetTableName().equals(
				tableJoinProperty.getMiddleTableName())) {
			// 目标表与中间表相同
			sqlBuilder.append("UPDATE ")
					.append(tableJoinProperty.getTargetTableName())
					.append(" SET ").append(tableJoinProperty.getJoinColumn())
					.append("=").append("?").append(" WHERE ")
					.append(tableJoinProperty.getTargetTableIdName())
					.append("=").append("?");
			preparedStatement = getConnection().prepareStatement(
					sqlBuilder.toString());
			preparedStatement.setObject(1, joinId);
			preparedStatement.setObject(2, inverseJoinId);
		} else {
			// 中间表格独立
			sqlBuilder.append("INSERT INTO ")
					.append(tableJoinProperty.getMiddleTableName()).append("(")
					.append(tableJoinProperty.getJoinColumn()).append(",")
					.append(tableJoinProperty.getInverseJoinColumn())
					.append(")").append(" VALUES(?,?)");
			preparedStatement = getConnection().prepareStatement(
					sqlBuilder.toString());
			preparedStatement.setObject(1, joinId);
			preparedStatement.setObject(2, inverseJoinId);
		}
		preparedStatement.executeUpdate();
	}

	/**
	 * 获取id对应的实体对象 暂时只支持EAGGER，暂时不支持栏加载
	 * 
	 * @param entityClass
	 * @param id
	 * @return
	 */
	public Object get(Class<?> entityClass, int id) {
		return getByObject(entityClass, id);
	}

	/**
	 * 获取id对应的实体对象
	 * 
	 * @param entityClass
	 * @param id
	 * @return
	 */
	public Object get(Class<?> entityClass, String id) {
		return getByObject(entityClass, id);
	}

	/**
	 * 获取关联对象
	 * 
	 * @param instance
	 * @param entityClass
	 * @param existsObjectMap
	 */
	public void getMappingEntity(Object instance, Class<?> entityClass,
			Map<Class<?>, Map<Object, Object>> existsObjectMap) {

		EntityProperty entityProperty = DBInitHelper
				.getEntityProperty(entityClass);
		String tableName = entityProperty.getTableName();
		String idName = entityProperty.getEntityMappingColumn(entityProperty
				.getIdName());
		Map<String, TableJoinProperty> constraintMap = getConstraintMap(
				entityProperty, tableName, idName);
		try {
			// 里面集合了所有中间表的信息
			Map<String, Class<?>> collectionPropertyMap = getNumberToManyProperty(entityClass);
			// 查询的字段集合生成
			Set<String> collePropNames = collectionPropertyMap.keySet();

			// 生成不包括查询集合对象的sql语句
			String baseConstraintSql = getJoinSql(tableName, collePropNames,
					constraintMap, idName, false);
			setEntites(baseConstraintSql, constraintMap, entityProperty,
					instance, existsObjectMap, false, collectionPropertyMap);
			// 生成集合对象的sql语句
			String manyConstraintSql = getJoinSql(tableName, collePropNames,
					constraintMap, idName, true);
			setEntites(manyConstraintSql, constraintMap, entityProperty,
					instance, existsObjectMap, true, collectionPropertyMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启事务
	 */
	public void beginTranstaction() {
		try {
			getConnection().setAutoCommit(false);
			this.isBeginTraction = true;
		} catch (SQLException e) {
			LOGGER.error("begin traction error", e);
			throw new RuntimeException("begin traction error", e);
		}
	}

	/**
	 * 关闭事务
	 */
	public void endTranstaction() {
		try {
			getConnection().setAutoCommit(true);
			this.isBeginTraction = false;
		} catch (SQLException e) {
			LOGGER.error("begin traction error", e);
			throw new RuntimeException("begin traction error", e);
		}
	}

	/**
	 * 事务回滚
	 */
	public void rollback() {
		try {
			getConnection().rollback();
		} catch (SQLException e) {
			LOGGER.error("rollback error", e);
			throw new RuntimeException("rollback error", e);
		}
	}

	/**
	 * 关闭session
	 */
	public void close() {
		try {
			getConnection().close();
			isClose = true;
		} catch (SQLException e) {
			LOGGER.error("close session error", e);
			throw new RuntimeException("close session error", e);
		}
	}

	private Map<String, TableJoinProperty> getConstraintMap(
			EntityProperty entityProperty, String tableName, String idName) {
		Map<String, TableJoinProperty> constraintMap = new HashMap<String, TableJoinProperty>();
		for (Entry<String, String> middleTableEntry : entityProperty
				.getMiddleTableNameMap().entrySet()) {

			// 连接所需变量
			EntityProperty targetEntityProperty = DBInitHelper
					.getEntityProperty(entityProperty
							.getConstraintEntityClass(middleTableEntry.getKey()));
			Property joinProperty = entityProperty
					.getMiddleColumns(middleTableEntry.getKey());

			// 创建带各种关联表参数的tableJoinProperty实例
			TableJoinProperty tableJoinProperty = new TableJoinProperty(
					tableName, middleTableEntry.getValue(),
					targetEntityProperty.getTableName(), joinProperty.getKey(),
					joinProperty.getValue(), targetEntityProperty
							.getTableEntityMap().values(),
					targetEntityProperty
							.getEntityMappingColumn(targetEntityProperty
									.getIdName()), idName);
			constraintMap.put(middleTableEntry.getKey(), tableJoinProperty);
		}
		return constraintMap;
	}

	public Query createQuery(String hql){
		return new Query(hql, this);
	}
	/**
	 * 获取基本的EntityClass,不加关联关系所映射的对象
	 * 
	 * @param id
	 * @param entityClass
	 * @param existsObjectMap
	 * @return
	 */
	private Object getBaseEntityObject(Object id, Class<?> entityClass,
			Map<Class<?>, Map<Object, Object>> existsObjectMap) {

		Object baseEntity = null;
		EntityProperty entityProperty = DBInitHelper
				.getEntityProperty(entityClass);
		String baseSql = getBaseSql(entityClass);

		if (existsObjectMap.get(entityClass) == null) {
			existsObjectMap.put(entityClass, new HashMap<Object, Object>());
		}
		ResultSet rs = null;
		try {
			PreparedStatement preparedStatement = getConnection()
					.prepareStatement(baseSql);
			preparedStatement.setObject(1, id);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				try {
					baseEntity = entityClass.newInstance();
				} catch (Exception e) {
					LOGGER.error("new Object error", e);
					throw e;
				}
				for (Entry<String, String> entry : entityProperty
						.getTableEntityMap().entrySet()) {
					if (entityProperty.getIdName().equals(entry.getKey())) {
						Object value = rs.getObject(entry.getValue());
						existsObjectMap.get(entityClass).put(value, baseEntity);
						entityProperty.setObject(entry.getKey(), baseEntity,
								value);

					} else {
						entityProperty.setObject(entry.getKey(), baseEntity,
								rs.getObject(entry.getValue()));
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("get Method error", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw new RuntimeException("resultSet close error", e);
				}
			}
		}
		return baseEntity;
	}

	/**
	 * 获取生成的saveSql
	 * 
	 * @return
	 */
	private String getSaveBaseSql(Class<?> entityClass,
			List<String> propertyList, Map<String, String> mappingMap) {
		List<String> columnList = new ArrayList<String>();
		EntityProperty entityProperty = DBInitHelper
				.getEntityProperty(entityClass);
		String tableName = entityProperty.getTableName();
		
		//获取缓存的对应实体的sql语句
		String baseSql = SAVE_ENTITY_SQL_CASH.get(entityClass);
		Set<String> propertySet = mappingMap.keySet();
		
		//判断是否自动生成IDName
		if (entityProperty.getGetIdMethod().isAnnotationPresent(
				GeneratedValue.class)) {
			propertySet.remove(entityProperty.getIdName());
		}
		for (String property : propertySet) {
			propertyList.add(property);
			columnList.add(entityProperty.getEntityMappingColumn(property));
		}
		if (baseSql == null) {
			baseSql = generateSaveSql(columnList, tableName);
			SAVE_ENTITY_SQL_CASH.put(entityClass, baseSql);
		}

		return baseSql;
	}

	/**
	 * 生成保存的sql语句
	 * 
	 * @param columns
	 * @param tableName
	 * @return
	 */
	private String generateSaveSql(Collection<String> columns, String tableName) {
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(
				tableName).append("(");

		for (String columnName : columns) {
			sqlBuilder.append(columnName).append(",");
		}
		sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
		sqlBuilder.append(") VALUES(");
		for (int i = 0; i < columns.size(); i++) {
			sqlBuilder.append("?,");
		}
		sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
		sqlBuilder.append(")");

		return sqlBuilder.toString();
	}

	/**
	 * get方法 获取对象
	 * 
	 * @param entityClass
	 * @param id
	 * @return
	 */
	private Object getByObject(Class<?> entityClass, Object id) {

		Map<Class<?>, Map<Object, Object>> existsObjectMap = new HashMap<Class<?>, Map<Object, Object>>();
		Object baseEntity = getBaseEntityObject(id, entityClass,
				existsObjectMap);
		if (baseEntity == null) {
			return null;
		}
		try {
			getMappingEntity(baseEntity, entityClass, existsObjectMap);
		} catch (Exception e) {
			throw new RuntimeException("get getMappingEntity error", e);
		}
		return baseEntity;

	}

	/**
	 * 放置基础和关联属性
	 * 
	 * @param baseConstraintSql
	 * @param tableName
	 * @param idName
	 * @param collePropNames
	 * @param constraintMap
	 * @param entityProperty
	 * @param instance
	 * @param existsObjectMap
	 */
	private void setEntites(String baseConstraintSql,
			Map<String, TableJoinProperty> constraintMap,
			EntityProperty entityProperty, Object instance,
			Map<Class<?>, Map<Object, Object>> existsObjectMap, boolean isMany,
			Map<String, Class<?>> collectionPropertyMap) {
		if (baseConstraintSql != null) {
			try {
				PreparedStatement preparedStatement = getConnection()
						.prepareStatement(baseConstraintSql);
				try {
					preparedStatement.setObject(1, entityProperty
							.getGetIdMethod().invoke(instance));
				} catch (Exception e) {
				}
				ResultSet resultSet = null;
				// 读取并set进对象中，不包括集合属性
				try {
					resultSet = preparedStatement.executeQuery();
					Map<String, Collection<?>> manyMap = new HashMap<String, Collection<?>>();
					while (resultSet.next()) {
						for (Entry<String, TableJoinProperty> constraintEntry : constraintMap
								.entrySet()) {
							if (collectionPropertyMap
									.containsKey(constraintEntry.getKey()) != isMany) {
								continue;
							}

							if (collectionPropertyMap
									.containsKey(constraintEntry.getKey())) {

								// 检查并创建集合属性
								if (!manyMap.containsKey(constraintEntry
										.getKey())) {
									Class<?> collectionType = collectionPropertyMap
											.get(constraintEntry.getKey());

									if (collectionType.isInterface()) {
										if (List.class.equals(collectionType)) {
											collectionType = ArrayList.class;
										} else if (Set.class
												.equals(collectionType)) {
											collectionType = HashSet.class;
										} else {
											throw new RuntimeException(
													"not support entity Collection"
															+ collectionType);
										}
									}
									Collection<?> collection = null;
									try {
										collection = (Collection<?>) collectionType
												.newInstance();
									} catch (Exception e) {
										throw new RuntimeException(
												"not support entity Collection"
														+ collectionType);
									}
									manyMap.put(constraintEntry.getKey(),
											collection);
									// 将创建集合set进实体属性
									entityProperty.setObject(
											constraintEntry.getKey(), instance,
											collection);
								}
								// 集合对象
								setManyEntites(
										entityProperty.getConstraintEntityClass(constraintEntry
												.getKey()),
										constraintEntry.getValue(),
										manyMap.get(constraintEntry.getKey()),
										resultSet, existsObjectMap);
							} else {
								// 非集合对象将属性放入对象
								resultSet.getObject(1);
								setBaseEntites(constraintEntry.getKey(),
										constraintEntry.getValue(),
										entityProperty, instance, resultSet,
										existsObjectMap);
							}
						}
						if (!isMany) {
							break;
						}
					}
				} finally {
					if (resultSet != null) {
						resultSet.close();
					}
				}
			} catch (SQLException e) {
				throw new RuntimeException("can't finish the constraint sql", e);
			}
		}
	}

	/**
	 * 获取连接sql
	 * 
	 * @param tableName
	 * @param collePropNames
	 * @param constraintMap
	 * @param idName
	 * @return
	 */
	private String getJoinSql(String tableName, Set<String> collePropNames,
			Map<String, TableJoinProperty> constraintMap, String idName,
			boolean isMany) {
		StringBuilder columnsBuilder = new StringBuilder("SELECT ");
		StringBuilder joinSqlBuilder = new StringBuilder(" FROM ")
				.append(tableName).append(" _").append(tableName);

		boolean isBaseConstraint = false;
		for (Entry<String, TableJoinProperty> constraintEntry : constraintMap
				.entrySet()) {
			if (isMany == collePropNames.contains(constraintEntry.getKey())) {
				TableJoinProperty tableJoinProperty = constraintEntry
						.getValue();
				columnsBuilder.append(tableJoinProperty.getColumns());
				joinSqlBuilder.append(tableJoinProperty.getJoinSql());
				isBaseConstraint = true;
			}
		}

		if (isBaseConstraint) {
			columnsBuilder.deleteCharAt(columnsBuilder.length() - 1);
			joinSqlBuilder.append(" WHERE ").append("_").append(tableName)
					.append(".").append(idName).append(" = ?");
			return columnsBuilder.toString() + joinSqlBuilder.toString();
		} else {
			return null;
		}
	}

	/**
	 * 设置集合对象
	 * 
	 * @param constraintClass
	 * @param joinTableProperty
	 * @param collection
	 * @param resultSet
	 * @param existsObjectMap
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setManyEntites(Class<?> constraintClass,
			TableJoinProperty joinTableProperty, Collection collection,
			ResultSet resultSet,
			Map<Class<?>, Map<Object, Object>> existsObjectMap)
			throws SQLException {

		Object idObject = resultSet.getObject(joinTableProperty
				.getTargetTableAlias()
				+ "__"
				+ joinTableProperty.getTargetTableIdName());
		// 判断是否回环关联
		if (idObject == null) {
		} else if (existsObjectMap.get(constraintClass) != null
				&& existsObjectMap.get(constraintClass).get(idObject) != null) {
			// 封装已有对象
			collection.add(existsObjectMap.get(constraintClass).get(idObject));
		} else {
			Object constraintInstance = null;
			try {
				constraintInstance = constraintClass.newInstance();
				EntityProperty constraintEntyty = DBInitHelper
						.getEntityProperty(constraintClass);
				for (Entry<String, String> constraintColumn : constraintEntyty
						.getTableEntityMap().entrySet()) {
					Object setParam = resultSet.getObject(joinTableProperty
							.getTargetTableAlias()
							+ "__"
							+ constraintColumn.getValue());
					constraintEntyty.setObject(constraintColumn.getKey(),
							constraintInstance, setParam);
				}
			} catch (Exception e) {
				throw new RuntimeException(constraintClass
						+ " can't get instance by construct", e);
			}
			if (existsObjectMap.get(constraintClass) == null) {
				existsObjectMap.put(constraintClass,
						new HashMap<Object, Object>());
			}
			existsObjectMap.get(constraintClass).put(idObject,
					constraintInstance);
			collection.add(existsObjectMap.get(constraintClass).get(idObject));
			getMappingEntity(constraintInstance, constraintClass,
					existsObjectMap);
		}
	}

	/**
	 * 获取基础实体
	 * 
	 * @param propertyName
	 * @param joinTableProperty
	 * @param entityProperty
	 * @param instance
	 * @param resultSet
	 * @param existsObjectMap
	 * @throws SQLException
	 */
	private void setBaseEntites(String propertyName,
			TableJoinProperty joinTableProperty, EntityProperty entityProperty,
			Object instance, ResultSet resultSet,
			Map<Class<?>, Map<Object, Object>> existsObjectMap)
			throws SQLException {
		Class<?> constraintClass = entityProperty
				.getConstraintEntityClass(propertyName);

		Object idObject = resultSet.getObject(joinTableProperty
				.getTargetTableAlias()
				+ "__"
				+ joinTableProperty.getTargetTableIdName());
		// 判断是否回环关联
		if (idObject == null) {
		} else if (existsObjectMap.get(constraintClass) != null
				&& existsObjectMap.get(constraintClass).get(idObject) != null) {
			entityProperty.setObject(propertyName, instance, existsObjectMap
					.get(constraintClass).get(idObject));
		} else {
			Object constraintInstance = null;
			try {
				constraintInstance = constraintClass.newInstance();
				EntityProperty constraintEntyty = DBInitHelper
						.getEntityProperty(constraintClass);
				for (Entry<String, String> constraintColumn : constraintEntyty
						.getTableEntityMap().entrySet()) {
					Object setParam = resultSet.getObject(joinTableProperty
							.getTargetTableAlias()
							+ "__"
							+ constraintColumn.getValue());
					constraintEntyty.setObject(constraintColumn.getKey(),
							constraintInstance, setParam);
				}
			} catch (Exception e) {
				throw new RuntimeException(constraintClass
						+ " can't get instance by construct", e);
			}
			// 将生成对象加入回环对象集
			if (existsObjectMap.get(constraintClass) == null) {
				existsObjectMap.put(constraintClass,
						new HashMap<Object, Object>());
			}
			existsObjectMap.get(constraintClass).put(idObject,
					constraintInstance);
			entityProperty
					.setObject(propertyName, instance, constraintInstance);
			getMappingEntity(constraintInstance, constraintClass,
					existsObjectMap);
		}
	}

	/**
	 * 获取实体中一对多或者多对多的属性
	 * 
	 * @param entityClass
	 * @return
	 */
	private Map<String, Class<?>> getNumberToManyProperty(Class<?> entityClass) {
		Map<String, Class<?>> collectionPropertyMap = new HashMap<String, Class<?>>();
		EntityProperty entityProperty = DBInitHelper
				.getEntityProperty(entityClass);
		/*
		 * set 方法抽出集合对多映射
		 */
		for (Entry<String, Method> setPropertyEntry : entityProperty
				.getSetPropertyMap().entrySet()) {
			if (Collection.class.isAssignableFrom(setPropertyEntry.getValue()
					.getParameterTypes()[0])) {
				collectionPropertyMap.put(setPropertyEntry.getKey(),
						setPropertyEntry.getValue().getParameterTypes()[0]);
			}
		}
		return collectionPropertyMap;
	}

	/**
	 * 获取生成基础实体类的sql
	 * 
	 * @param entityClass
	 * @return
	 */
	private String getBaseSql(Class<?> entityClass) {
		EntityProperty entityProperty = DBInitHelper
				.getEntityProperty(entityClass);
		String tableName = entityProperty.getTableName();
		String idColumn = entityProperty.getEntityMappingColumn(entityProperty
				.getIdName());
		String baseSql = GET_ENTITY_SQL_CASH.get(entityClass);

		if (baseSql == null) {
			baseSql = generateGetSqlById(entityProperty.getTableEntityMap()
					.values(), tableName, idColumn);
			GET_ENTITY_SQL_CASH.put(entityClass, baseSql);
		}
		return baseSql;
	}

	/**
	 * 生成sql语句
	 * 
	 * @param columns
	 * @param tableName
	 * @return
	 */
	private String generateGetSql(Collection<String> columns, String tableName) {
		StringBuilder sqlBuilder = new StringBuilder("SELECT ");

		for (String columnName : columns) {
			sqlBuilder.append(columnName).append(",");
		}

		sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
		sqlBuilder.append(" FROM ").append(tableName);

		return sqlBuilder.toString();
	}

	/**
	 * 生成带id的第一个实体对象
	 * 
	 * @param columns
	 * @param tableName
	 * @param idColumnName
	 * @return
	 */
	private String generateGetSqlById(Collection<String> columns,
			String tableName, String idColumnName) {
		return generateGetSql(columns, tableName) + " WHERE " + idColumnName
				+ " = ?";
	}

	public void commit() {
		try {
			getConnection().commit();
		} catch (Exception e) {
			throw new RuntimeException("commit error", e);
		}
	}

}

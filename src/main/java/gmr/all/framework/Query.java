package gmr.all.framework;

import gmr.all.framework.bean.EntityProperty;
import gmr.all.framework.hellper.DBInitHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * 只支持单体单层属性查询
 * 
 * 暂不缓存
 * 
 * @author gmr
 */
public class Query {
	private String hql;
	private EntityProperty entityProperty;
	private PreparedStatement preparedStatement;
	private String sql;
	private Session session;

	public Query(String hql, Session session) {
		this.hql = hql;
		this.session = session;
		resolveHql();
		try {
			this.preparedStatement = session.getConnection().prepareStatement(
					sql);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从0开始
	 * 
	 * @param position
	 */
	public Query setDate(int position, Object value) {
		try {
			this.preparedStatement.setObject(position + 1, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从0开始
	 * 
	 * @param position
	 */
	public Query setDate(int position, Date value) {
		try {
			this.preparedStatement.setDate(position + 1, new java.sql.Date(
					value.getTime()));
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从0开始
	 * 
	 * @param position
	 */
	public Query setString(int position, String value) {
		try {
			this.preparedStatement.setString(position + 1, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从0开始
	 * 
	 * @param position
	 */
	public Query setInteger(int position, int value) {
		try {
			this.preparedStatement.setInt(position + 1, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 去除hql中的双空格
	 */
	private String deleteTwoSpace() {
		String str = this.hql;
		while (str.contains("  ")) {
			str.replace("  ", " ");
		}
		return str;
	}

	private Object getUniqueObject() {
		Map<Class<?>, Map<Object, Object>> existsObjectMap = new HashMap<Class<?>, Map<Object, Object>>();
		Object baseObject = null;
		try {
			baseObject = getBaseUniqueObject(existsObjectMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (baseObject == null) {
			return null;
		}
		this.session.getMappingEntity(baseObject,
				this.entityProperty.getEntityClass(), existsObjectMap);
		return baseObject;
	}

	/**
	 * 取唯一结果
	 * 
	 * @return
	 * @throws Exception
	 */
	private Object getBaseUniqueObject(
			Map<Class<?>, Map<Object, Object>> existsObjectMap)
			throws Exception {
		Object baseEntity = null;
		Class<?> entityClass = entityProperty.getEntityClass();
		// 存放已存在对象的map
		// 实体class,实体id值，对应实体
		if (existsObjectMap.get(entityClass) == null) {
			existsObjectMap.put(entityClass, new HashMap<Object, Object>());
		}
		ResultSet rs = preparedStatement.executeQuery();
		rs.last();
		if (rs.getRow() >= 2) {
			throw new RuntimeException("the result not unique");
		}
		if (rs.first()) {
			baseEntity = entityClass.newInstance();
			setIntoObject(baseEntity, entityClass, rs, existsObjectMap);
		}
		return baseEntity;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List getBaseListObject(
			Map<Class<?>, Map<Object, Object>> existsObjectMap)
			throws Exception {
		// Object baseEntity = null;
		Class<?> entityClass = entityProperty.getEntityClass();
		List list = new ArrayList();
		// 存放已存在对象的map
		// 实体class,实体id值，对应实体
		if (existsObjectMap.get(entityClass) == null) {
			existsObjectMap.put(entityClass, new HashMap<Object, Object>());
		}
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			Object baseEntity = null;
			baseEntity = entityClass.newInstance();
			setIntoObject(baseEntity, entityClass, rs, existsObjectMap);
			list.add(baseEntity);
		}
		return list;
	}

	/**
	 * 将结果集里的值放入
	 * 
	 * @param baseEntity
	 * @param entityClass
	 * @param rs
	 * @param existsObjectMap
	 * @throws Exception
	 */
	private void setIntoObject(Object baseEntity, Class<?> entityClass,
			ResultSet rs, Map<Class<?>, Map<Object, Object>> existsObjectMap)
			throws Exception {
		for (Entry<String, String> entry : entityProperty.getTableEntityMap()
				.entrySet()) {
			if (entityProperty.getIdName().equals(entry.getKey())) {
				Object value = rs.getObject(entry.getValue());
				existsObjectMap.get(entityClass).put(value, baseEntity);
				entityProperty.setObject(entry.getKey(), baseEntity, value);

			} else {
				entityProperty.setObject(entry.getKey(), baseEntity,
						rs.getObject(entry.getValue()));
			}
		}
	}

	public Object uniqueResult() {
		return getUniqueObject();
	}

	/**
	 * 解析hql成sql
	 */
	private void resolveHql() {
		// select,from,where,limit,order by,group by这六种分段关键词
		String singleSpaceHql = deleteTwoSpace().trim();
		String lowerHql = singleSpaceHql.toLowerCase();
		int selectIndex = lowerHql.indexOf("select");
		int fromIndex = lowerHql.indexOf("from");
		int whereIndex = lowerHql.indexOf("where");
		int limitIndex = lowerHql.indexOf("limit");
		int orderIndex = lowerHql.indexOf("order by");
		int groupIndex = lowerHql.indexOf("group by");
		if (lowerHql.contains("select")) {
			// 报错
			throw new RuntimeException("can't contain select ");
		}
		if (fromIndex == -1) {
			// 报错
			throw new RuntimeException("is not corrected hql ");
		}
		TreeMap<Integer, String> indexMap = new TreeMap<Integer, String>(
				new Comparator<Integer>() {
					public int compare(Integer a, Integer b) {
						return a.compareTo(b);
					}
				});
		HashMap<String, String> cutMap = new HashMap<String, String>();
		indexMap.put(fromIndex, "from");
		if (selectIndex != -1) {
			indexMap.put(selectIndex, "select");
		}
		if (whereIndex != -1) {
			indexMap.put(whereIndex, "where");
		}
		if (limitIndex != -1) {
			indexMap.put(limitIndex, "limit");
		}
		if (orderIndex != -1) {
			indexMap.put(selectIndex, "order");
		}
		if (groupIndex != -1) {
			indexMap.put(selectIndex, "group");
		}
		// cutMap
		int lastIndex = -1;
		String lastName = null;
		for (Entry<Integer, String> entry : indexMap.entrySet()) {
			if (lastIndex != -1) {
				cutMap.put(
						lastName,
						singleSpaceHql.substring(
								lastIndex + lastName.length() + 1,
								entry.getKey()).trim());
			}
			lastIndex = entry.getKey();
			lastName = entry.getValue();
		}
		cutMap.put(lastName,
				singleSpaceHql.substring(lastIndex + lastName.length() + 1)
						.trim());

		// 检查并生成所需的实体
		EntityProperty entityProp = DBInitHelper.getEntityProperty(cutMap.get(
				"from").trim());

		// 获取查询字段
		// 通查
		Map<String, String> tableEntityMap = entityProp.getTableEntityMap();
		Set<String> columns = new HashSet<String>();
		// 替换属性成表字段
		for (Entry<String, String> entry : tableEntityMap.entrySet()) {
			singleSpaceHql = singleSpaceHql.replace(entry.getKey(),
					entry.getValue());
			columns.add(entry.getValue());
		}
		singleSpaceHql = singleSpaceHql.replace(entityProp.getEntityName(),
				entityProp.getTableName());
		this.entityProperty = entityProp;
		this.sql = generateGetSql(columns, singleSpaceHql);
	}

	/**
	 * 生成sql语句
	 * 
	 * @param columns
	 * @param tableName
	 * @return
	 */
	public String generateGetSql(Collection<String> columns, String hqlToSql) {
		StringBuilder sqlBuilder = new StringBuilder("SELECT ");

		for (String columnName : columns) {
			sqlBuilder.append(columnName).append(",");
		}

		sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
		sqlBuilder.append(" ").append(hqlToSql);

		return sqlBuilder.toString();
	}

	@SuppressWarnings("rawtypes")
	public List list() {
		return getListResult();
	}

	@SuppressWarnings("rawtypes")
	private List getListResult() {
		Map<Class<?>, Map<Object, Object>> existsObjectMap = new HashMap<Class<?>, Map<Object, Object>>();
		List baseObjectList = null;
		try {
			baseObjectList = getBaseListObject(existsObjectMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (baseObjectList.size() == 0) {
			return baseObjectList;
		}
		for (Object baseObject : baseObjectList) {
			// 这里绝对不行
			// 严重需要改正
			// 先放着这样子先
			this.session.getMappingEntity(baseObject,
					this.entityProperty.getEntityClass(), existsObjectMap);
		}

		return baseObjectList;
	}
}

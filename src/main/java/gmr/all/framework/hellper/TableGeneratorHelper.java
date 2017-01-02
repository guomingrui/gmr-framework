package gmr.all.framework.hellper;

import gmr.all.framework.bean.Property;
import gmr.all.framework.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 建表语句生成助手
 */
public class TableGeneratorHelper {

	/**
	 * 创建sql语句表
	 * 
	 * @param tableMap
	 * @param referenceMap
	 * @throws Exception
	 */
	public static void creating(Map<String, Map<String, String>> tableMap,
			Map<String, Property> referenceMap) throws Exception {
		Connection connection = ConnectionUtil.getConnection();
		connection.setAutoCommit(false);
		Set<String> existsTables = new HashSet<String>();
		ResultSet resultSet = null;
		try {
			resultSet = connection.prepareStatement("SHOW TABLES")
					.executeQuery();
			while (resultSet.next()) {
				existsTables.add(resultSet.getString(1));
			}

			for (Entry<String, Map<String, String>> tableEntry : tableMap
					.entrySet()) {

				if (existsTables.contains(tableEntry.getKey())) {
					continue;
				}
				StringBuilder createSqlBuilder = new StringBuilder(
						"CREATE table IF NOT EXISTS ");// 创表语句

				createSqlBuilder.append(tableEntry.getKey()).append("(");

				for (Entry<String, String> columnEntry : tableEntry.getValue()
						.entrySet()) {
					createSqlBuilder.append(columnEntry.getKey()).append(" ")
							.append(columnEntry.getValue()).append(",");

				}
				createSqlBuilder.deleteCharAt(createSqlBuilder.length() - 1);
				createSqlBuilder.append(")");

				// 如果表不存在，则建表
				PreparedStatement createStatement = connection
						.prepareStatement(createSqlBuilder.toString());
				createStatement.execute();
			}
			for (Entry<String, Property> referenceEntry : referenceMap
					.entrySet()) {
				if (!existsTables.contains(referenceEntry.getKey())) {
					// 添加表与表之间的关联
					StringBuilder referenceSqlBuilder = new StringBuilder(
							"ALTER TABLE ");
					referenceSqlBuilder.append(referenceEntry.getKey());
					if (referenceEntry.getValue().getKey() != null) {
						referenceSqlBuilder.append(" ADD ")
								.append(referenceEntry.getValue().getKey())
								.append(",");
					}
					if (referenceEntry.getValue().getValue() != null) {
						referenceSqlBuilder.append(" ADD ")
								.append(referenceEntry.getValue().getValue())
								.append(",");
					}
					referenceSqlBuilder.deleteCharAt(referenceSqlBuilder
							.length() - 1);
					connection.prepareStatement(referenceSqlBuilder.toString())
							.execute();
				}
			}
			connection.commit();
		} catch (Exception e) {
			connection.rollback();
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (connection != null) {
				connection.close();
			}
		}

	}
}

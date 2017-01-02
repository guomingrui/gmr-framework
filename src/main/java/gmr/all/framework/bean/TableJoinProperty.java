package gmr.all.framework.bean;

import java.util.Collection;

public class TableJoinProperty {
	String mainTableName;// 主表名
	String middleTableName;// 中间表名字
	String middleTableAlias;// 中间表别名
	String targetTableAlias;// 连接表别名
	String targetTableName;// 目标表名字
	String joinColumn;// 中间表映射主表的字段
	String inverseJoinColumn;// 中间表映射目标表的字段
	Collection<String> targetTableColumns;// 目标表蕴含的字段
	String targetTableIdName;// 目标表的主键名称
	String maintTableIdName;// 主表的主键名称
	String mainTableAlis;

	public TableJoinProperty() {
		setAlias();
	}

	private void setAlias() {
		this.targetTableAlias = "a"
				+ String.valueOf(System.currentTimeMillis()) + this.hashCode();
		this.mainTableAlis = "_" + mainTableName;
		if (this.mainTableName.equals(this.middleTableName)) {
			// 主表与中间表相同
			this.middleTableAlias = this.mainTableAlis;
		} else if (this.targetTableName.equals(this.middleTableName)) {
			// 目标表与中间表相同
			this.middleTableAlias = this.targetTableAlias;
		} else {
			// 中间表独立
			this.middleTableAlias = "a" + String.valueOf(this.hashCode())
					+ System.currentTimeMillis();
		}
	}

	public TableJoinProperty(String mainTableName, String middleTableName,
			String targetTableName, String joinColumn,
			String inverseJoinColumn, Collection<String> targetTableColumns,
			String targetTableIdName, String maintTableIdName) {
		this.mainTableName = mainTableName;
		this.middleTableName = middleTableName;
		this.targetTableName = targetTableName;
		this.joinColumn = joinColumn;
		this.inverseJoinColumn = inverseJoinColumn;
		this.targetTableColumns = targetTableColumns;
		this.targetTableIdName = targetTableIdName;
		this.maintTableIdName = maintTableIdName;
		setAlias();
	}

	/**
	 * 返回select部分的表字段字符串,逗号结尾
	 * 
	 * @return
	 */
	public String getColumns() {
		StringBuilder columnsBuilder = new StringBuilder();
		for (String tableColumn : this.targetTableColumns) {
			columnsBuilder.append(" ").append(this.targetTableAlias)
					.append(".").append(tableColumn).append(" ")
					.append(this.targetTableAlias).append("__")
					.append(tableColumn).append(",");
		}
		if (this.mainTableName.equals(this.middleTableName)) {
			// 主表与中间表相同
			columnsBuilder.append(this.mainTableAlis).append(".")
					.append(this.inverseJoinColumn).append(" ")
					.append(this.mainTableAlis).append("__")
					.append(this.inverseJoinColumn).append(",");
		} else if (this.targetTableName.equals(this.mainTableName)) {
			// 目标表与中间表相同
			columnsBuilder.append(this.targetTableAlias).append(".")
					.append(this.joinColumn).append(" ")
					.append(this.targetTableAlias).append("__")
					.append(this.joinColumn).append(",");
		} else {
			// 中间表独立
			columnsBuilder.append(this.middleTableAlias).append(".")
					.append(this.inverseJoinColumn).append(" ")
					.append(this.middleTableAlias).append("__")
					.append(this.inverseJoinColumn).append(this.joinColumn)
					.append(",").append(this.middleTableAlias).append(".")
					.append(this.joinColumn).append(" ")
					.append(this.middleTableAlias).append("__")
					.append(this.joinColumn).append(",");
		}
		return columnsBuilder.toString();
	}

	public String getMainTableAlis() {
		return mainTableAlis;
	}

	public String getJoinSql() {
		StringBuilder joinSqlBuilder = new StringBuilder();
		if (middleTableName.equals(mainTableName)) {
			// 中间表就是主表
			joinSqlBuilder.append(" LEFT OUTER JOIN ").append(targetTableName)
					.append(" ").append(this.targetTableAlias).append(" ON ")
					.append(this.targetTableAlias).append(".")
					.append(this.targetTableIdName).append(" = ")
					.append(this.mainTableAlis).append(".")
					.append(this.inverseJoinColumn);

		} else if (middleTableName.equals(targetTableIdName)) {
			// 中间表是目标表
			joinSqlBuilder.append(" LEFT OUTER JOIN ").append(targetTableName)
					.append(" ").append(this.targetTableAlias).append(" ON ")
					.append(this.targetTableAlias).append(".")
					.append(this.joinColumn).append(" = ")
					.append(this.mainTableAlis).append(".")
					.append(this.maintTableIdName);
		} else {
			// 中间表独立
			joinSqlBuilder.append(" LEFT OUTER JOIN ")
					.append(this.middleTableName).append(" ")
					.append(this.middleTableAlias).append(" ON ")
					.append(this.middleTableAlias).append(".")
					.append(this.joinColumn).append(" = ")
					.append(this.mainTableAlis).append(".")
					.append(this.maintTableIdName).append(" LEFT OUTER JOIN ")
					.append(this.targetTableName).append(" ")
					.append(this.targetTableAlias).append(" ON ")
					.append(this.targetTableAlias).append(".")
					.append(this.targetTableIdName).append(" = ")
					.append(this.middleTableAlias).append(".")
					.append(this.inverseJoinColumn);
		}
		return joinSqlBuilder.toString();
	}

	public Collection<String> getTargetTableColumns() {
		return targetTableColumns;
	}

	public void setTargetTableColumns(Collection<String> targetTableColumns) {
		this.targetTableColumns = targetTableColumns;
	}

	public String getTargetTableIdName() {
		return targetTableIdName;
	}

	public void setTargetTableIdName(String targetTableIdName) {
		this.targetTableIdName = targetTableIdName;
	}

	public String getMaintTableIdName() {
		return maintTableIdName;
	}

	public void setMaintTableIdName(String maintTableIdName) {
		this.maintTableIdName = maintTableIdName;
	}

	public String getMainTableName() {
		return mainTableName;
	}

	public void setMainTableName(String mainTableName) {
		this.mainTableName = mainTableName;
	}

	public String getMiddleTableName() {
		return middleTableName;
	}

	public void setMiddleTableName(String middleTableName) {
		this.middleTableName = middleTableName;
	}

	public String getMiddleTableAlias() {
		return middleTableAlias;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	public String getTargetTableAlias() {
		return targetTableAlias;
	}

	public String getJoinColumn() {
		return joinColumn;
	}

	public void setJoinColumn(String joinColumn) {
		this.joinColumn = joinColumn;
	}

	public String getInverseJoinColumn() {
		return inverseJoinColumn;
	}

	public void setInverseJoinColumn(String inverseJoinColumn) {
		this.inverseJoinColumn = inverseJoinColumn;
	}

}

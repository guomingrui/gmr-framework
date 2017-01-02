package gmr.all.framework.util;

import gmr.all.framework.hellper.ConfigHelper;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionUtil {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConnectionUtil.class);
	private static final ComboPooledDataSource DATA_SOURCE = new ComboPooledDataSource();
	static {
		try {
			initDataSource();
		} catch (PropertyVetoException e) {
			LOGGER.error("datasource init error", e);
			throw new RuntimeException();
		}
	}
	
	/**
	 * 初始化数据源配置信息
	 * 
	 * @throws PropertyVetoException
	 */
	public static void initDataSource() throws PropertyVetoException {
		DATA_SOURCE.setDriverClass(ConfigHelper.getJdbcDriver());
		DATA_SOURCE.setUser(ConfigHelper.getJdbcUsername());
		DATA_SOURCE.setPassword(ConfigHelper.getJdbcPassword());
		DATA_SOURCE.setJdbcUrl(ConfigHelper.getJdbcUrl());
	}
	
	/**
	 * 获取数据库连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		return DATA_SOURCE.getConnection();
	}

}

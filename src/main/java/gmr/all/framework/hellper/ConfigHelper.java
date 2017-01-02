package gmr.all.framework.hellper;

import gmr.all.framework.ConfigBaseParamter;
import gmr.all.framework.util.PropUtil;

import java.util.Properties;

/**
 * @author gmr
 *
 */
public class ConfigHelper {
	private static final Properties CONFIG_PROPS = PropUtil
			.loadProperties(ConfigBaseParamter.CONFIG_FILE);

	/**
	 * 获取JDBC连接驱动
	 * @return
	 */
	public static  String getJdbcDriver() {
		return PropUtil.getString(CONFIG_PROPS, ConfigBaseParamter.JDBC_DRIVER);
	}

	/**
	 * 获取JDBC连接路径
	 * @return
	 */
	public static  String getJdbcUrl() {
		return PropUtil.getString(CONFIG_PROPS, ConfigBaseParamter.JDBC_URL);
	}

	
	/**
	 * 获取JDBC连接用户名
	 * @return
	 */
	public static  String getJdbcUsername() {
		return PropUtil.getString(CONFIG_PROPS,
				ConfigBaseParamter.JDBC_USERNAME);
	}

	/**
	 * 获取JDBC连接密码
	 * @return
	 */
	public static  String getJdbcPassword() {
		return PropUtil.getString(CONFIG_PROPS,
				ConfigBaseParamter.JDBC_PASSWORD);
	}


	/**
	 * 获取App基础包路径
	 * @return
	 */
	public static  String getAppBasePackage() {
		return PropUtil.getString(CONFIG_PROPS,
				ConfigBaseParamter.APP_BASE_PACKAGE);
	}
	
	/**
	 * 获取App Entity包路径
	 * @return
	 */
	public static  String getAppEntityPackage() {
		return PropUtil.getString(CONFIG_PROPS,
				ConfigBaseParamter.APP_ENTITY_PACKAGE);
	}

	/**
	 * 获取JSP路径
	 * @return
	 */
	public static  String getAppJspPath() {
		return PropUtil
				.getString(CONFIG_PROPS, ConfigBaseParamter.APP_JSP_PATH, "/WEB-INF/view/");
	}
	
	public static String getFrameworkPackage(){
		return ConfigBaseParamter.FRAMEWORK_PACKGE;
	}
	
	/**
	 * 获取App静态资源路径
	 * 
	 * @return
	 */
	public static  String getAppAssetPath() {
		return PropUtil.getString(CONFIG_PROPS,
				ConfigBaseParamter.APP_ASSET_PATH, "/asset/");
	}
}

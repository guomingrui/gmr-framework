package gmr.all.framework;

/**
 * @author gmr
 * 基础配置属性接口
 */
public interface ConfigBaseParamter {
	String FRAMEWORK_PACKGE = "gmr.all"; 
	String CONFIG_FILE = "gmr.properties";//配置文件路径值(下面全都是属性名)
	String JDBC_DRIVER = "gmr.framework.jdbc.driver";//驱动属性名
	String JDBC_URL = "gmr.framework.jdbc.url";//驱动路径
	String JDBC_USERNAME = "gmr.framework.jdbc.username";//数据库连接用户名
	String JDBC_PASSWORD = "gmr.framework.jdbc.password";//数据库连接密码
	String APP_BASE_PACKAGE = "gmr.framework.app.base_package";//基础包路径
	String APP_ENTITY_PACKAGE = "gmr.framework.app.entity_package";//基础包路径属性名
	String APP_JSP_PATH = "gmr.framework.app.jsp_path";//jsp路径属性名
	String APP_ASSET_PATH = "gmr.framework.app.asset_path";//静态资源路径属性名
}

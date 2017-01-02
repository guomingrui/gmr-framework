package gmr.all.framework;

import gmr.all.framework.hellper.AopHelper;
import gmr.all.framework.hellper.BeanHelper;
import gmr.all.framework.hellper.ClassHelper;
import gmr.all.framework.hellper.ControllerHelper;
import gmr.all.framework.hellper.DBInitHelper;
import gmr.all.framework.hellper.IocHelper;
import gmr.all.framework.util.ClassUtil;

/**
 * 助手类加载器
 * 
 * @author gmr
 *
 */
public class HelperLoader {
	public static void init() {
		//注意加载顺序
		Class<?>[] classes = { ClassHelper.class, BeanHelper.class, AopHelper.class,ControllerHelper.class,IocHelper.class,DBInitHelper.class};
		for (Class<?> c : classes) {
			ClassUtil.loadClass(c.getName(), true);
		}
	}
}

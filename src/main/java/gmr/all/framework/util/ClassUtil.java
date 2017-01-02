package gmr.all.framework.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类相关操作工具
 * 
 * @author gmr
 *
 */
public class ClassUtil {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CastUtil.class);

	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * 加载类
	 * 
	 * @param className
	 * @param isInited
	 * @return
	 */
	public static Class<?> loadClass(String className, boolean isInited) {
		Class<?> c = null;
		try {
			c = Class.forName(className, isInited, getClassLoader());
		} catch (ClassNotFoundException e) {
			LOGGER.error(className + " not found", e);
			throw new RuntimeException();
		}
		return c;
	}

	/**
	 * 获取包下的所有class集合
	 * 
	 * @param packageName
	 * @return
	 */
	public static Set<Class<?>> getClassSet(String packageName) {
		Set<Class<?>> classSet  = new HashSet<Class<?>>();
		try {
			Enumeration<URL> enums = getClassLoader().getResources(
					packageName.replace(".", "/"));
			while (enums.hasMoreElements()) {
				URL url = enums.nextElement();
				if (url != null) {
					/*
					 * 协议处理
					 */
					if (url.getProtocol().equals("file")) {
						// 替换url的空格的unicode编码成空格
						// url--->Path
						// 增加该路径下的类
						addClass(classSet, url.getPath().replace("%20", " "),
								packageName);
					} else if (url.getProtocol().equals("jar")) {
						// 读取jar包文件
						JarURLConnection jarURLConnection = (JarURLConnection) url
								.openConnection();
						if (jarURLConnection != null) {
							JarFile jarFile = jarURLConnection.getJarFile();
							if (jarFile != null) {
								/* 遍历jar里的文件 */
								Enumeration<JarEntry> jarEnums = jarFile
										.entries();
								while (jarEnums.hasMoreElements()) {
									JarEntry jarEntry = jarEnums.nextElement();
									String jarEntryName = jarEntry.getName();
									if (jarEntryName.endsWith(".class")) {
										String className = jarEntryName
												.substring(
														0,
														jarEntryName
																.lastIndexOf("."))
												.replace("/", ".");
										doAddClass(className, classSet);

									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("get class set failure", e);
			e.printStackTrace();
		}

		return classSet;
	}

	/**
	 * @param classSet
	 *            class集合
	 * @param packagePath
	 *            包路径
	 * @param packageName
	 *            包名
	 */
	private static void addClass(Set<Class<?>> classSet, String packagePath,
			String packageName) {
		File[] files = new File(packagePath).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (file.getName().endsWith(".class") && file.isFile())
						|| file.isDirectory();
			}
		});
		for (File file : files) {
			if (file.isFile()) {
				String className = file.getName().substring(0,
						file.getName().lastIndexOf("."));
				if (packageName != null && packageName.trim().length() > 0) {
					className = packageName + "." + className;
				}
				doAddClass(className, classSet);
			} else {
				String nextPackagePath = file.getName();
				String nextPackageName = file.getName();
				if (packagePath != null && packagePath.trim().length() > 0) {
					nextPackagePath = packagePath + "/" + file.getName();
				}
				if (packageName != null && packageName.trim().length() > 0) {
					nextPackageName = packageName + "." + file.getName();
				}
				addClass(classSet, nextPackagePath, nextPackageName);
			}
		}
	}

	/**
	 * 寻找并添加class到class集合中
	 * 
	 * @param className
	 * @param classSet
	 */
	private static void doAddClass(String className, Set<Class<?>> classSet) {
		Class<?> c = loadClass(className, false);
		classSet.add(c);
	}

}

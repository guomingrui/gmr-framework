package gmr.all.framework.hellper;

import gmr.all.framework.annotation.Repository;
import gmr.all.framework.annotation.Resolver;
import gmr.all.framework.annotation.Controller;
import gmr.all.framework.annotation.Service;
import gmr.all.framework.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * 获取Class集合
 * 
 * @author gmr
 *
 */
public class ClassHelper {
	private static final Set<Class<?>> CLASS_SET;
	static {
		/*
		 * 从包路径加载class
		 */
		CLASS_SET = ClassUtil.getClassSet(ConfigHelper.getAppBasePackage());
		CLASS_SET.addAll(ClassUtil.getClassSet(ConfigHelper
				.getFrameworkPackage()));
	}

	public static Set<Class<?>> getClassSet() {
		return CLASS_SET;
	}

	/**
	 * 获取包下某类及某类的子类
	 * 
	 * @param superClass
	 * @return
	 */
	public static Set<Class<?>> getClassSetBySuper(Class<?> superClass) {
		Set<Class<?>> superClassSet = new HashSet<Class<?>>();
		for (Class<?> c : getClassSet()) {
			if (superClass.isAssignableFrom(c)) {
				superClassSet.add(c);
			}
		}
		return superClassSet;
	}

	/**
	 * 获取包下带有指定注解的class
	 * 
	 * @param annotationClass
	 * @return
	 */
	public static Set<Class<?>> getClassSetByAnnotation(
			Class<? extends Annotation> annotationClass) {
		Set<Class<?>> annotationClassSet = new HashSet<Class<?>>();
		for (Class<?> c : getClassSet()) {
			if (c.isAnnotationPresent(annotationClass)) {
				annotationClassSet.add(c);
			}
		}
		return annotationClassSet;
	}

	/**
	 * 获取包下带有Controller的Class
	 * 
	 * @return
	 */
	public static Set<Class<?>> getControllerClasses() {
		return getClassSetByAnnotation(Controller.class);
	}

	/**
	 * 获取包下带有Controller的Service
	 * 
	 * @return
	 */
	public static Set<Class<?>> getServiceClasses() {
		return getClassSetByAnnotation(Service.class);
	}

	/**
	 * 获取包下带有Resolvert的Service
	 * 
	 * @return
	 */
	public static Set<Class<?>> getResolverClasses() {
		return getClassSetByAnnotation(Resolver.class);
	}

	public static Set<Class<?>> getRepositoryClass() {
		return getClassSetByAnnotation(Repository.class);
	}

	/**
	 * 获取包下带有gmr-framework注解的Bean Class
	 * 
	 * @return
	 */
	public static Set<Class<?>> getBeanClasses() {

		Set<Class<?>> allSet = new HashSet<Class<?>>();
		allSet.addAll(getControllerClasses());
		allSet.addAll(getServiceClasses());
		allSet.addAll(getRepositoryClass());
		// 先只能定义一个异常解析器，暂时不支持嵌套
		allSet.addAll(getResolverClasses());
		return allSet;
	}

}

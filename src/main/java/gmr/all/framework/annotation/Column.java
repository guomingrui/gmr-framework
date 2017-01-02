package gmr.all.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	public abstract String name() default "";

	public abstract boolean unique() default false;

	public abstract boolean nullable() default true;

	public abstract String columnDefinition() default "";

	public abstract int length() default 0;

}

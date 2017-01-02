package gmr.all.framework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
//	public abstract FetchType fetch();
	public Class<?> entityClass();
}

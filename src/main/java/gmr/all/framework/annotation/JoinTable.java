package gmr.all.framework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 中间表映射
 */
@Target({ java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {
	public String tableName();

	public String joinColumn();

	public String inverseJoinColumn();
}

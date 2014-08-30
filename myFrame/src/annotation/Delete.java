package annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Delete {
	
	/**
	 * 列名
	 * @return
	 */
	String column() default "";

	/**
	 * 默认值
	 * @return
	 */
	String value() default "";
}

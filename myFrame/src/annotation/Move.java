package annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从某一字段将数据移动到别一个字段时，可用此注解标示
 * @author huangjp
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Move {
	
	/**
	 * 默认值 
	 * @return
	 */
	String value() default "";

	/**
	 * 去向
	 * @return
	 */
	String into() default "";

	/**
	 * 来自
	 * @return
	 */
	String from() default "";
	
	/**
	 * 表名
	 * @return
	 */
	String table() default "";
}

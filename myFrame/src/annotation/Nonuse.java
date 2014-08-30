package annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对一个过去曾使用过的字段，在有了新的字段时可用此注解标示其为不使用
 * @author huangjp
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Nonuse {
	
	/**
	 * 默认值 
	 * @return
	 */
	String value() default "";
	
	/**
	 * 是否删除数据库该字段,数据库该字段中若有数据，将不会删除
	 * @return
	 */
	boolean isDel() default false;
}

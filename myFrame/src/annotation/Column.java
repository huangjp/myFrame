package annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import Enum.Tpye;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
	
	/**
	 * 列名
	 * @return
	 */
	String name() default "";
	
	/**
	 * 列类型，默认为字符串
	 * @return
	 */
	Tpye type() default Tpye.DEFAULT;
	
	/**
	 * 定义长度
	 * @return
	 */
	int length() default 0;
	
	/**
	 * 定义精度
	 * @return
	 */
	int precision() default 0;
	
	/**
	 * 定义宽度
	 * @return
	 */
	int scale() default 0;
	
	/**
	 * 是否主键，有此标识则默认为主键
	 * @return
	 */
	boolean primary() default false;
	
	/**
	 * 默认值
	 * @return
	 */
	String defaultValue() default "default ";
	
	/**
	 * 是否唯一
	 * @return
	 */
	boolean identity() default false;
	
	/**
	 * 是否外键，有此标识则默认为外键
	 * @return
	 */
	boolean foreign() default false;
	
	/**
	 * 是否允许为空
	 * @return
	 */
	boolean isNull() default true;
	
	/**
	 * 是否自增
	 * @return
	 */
	boolean auto_increment() default false;
	
	/**
	 * 描述
	 * @return
	 */
	String comment() default "";
	
	String value() default "";
}

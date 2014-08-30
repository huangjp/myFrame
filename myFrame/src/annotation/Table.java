package annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Target(ElementType.TYPE)   //接口、类、枚举、注解
 * @Target(ElementType.FIELD) //字段、枚举的常量
 * @Target(ElementType.METHOD) //方法
 * @Target(ElementType.PARAMETER) //方法参数
 * @Target(ElementType.CONSTRUCTOR)  //构造函数
 * @Target(ElementType.LOCAL_VARIABLE)//局部变量
 * @Target(ElementType.ANNOTATION_TYPE)//注解
 * @Target(ElementType.PACKAGE) ///包 
 * 
 * @author huangjp
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
	
	/**
	 * 默认值
	 * @return
	 */
	String value();
	
	/**
	 * 表名
	 * @return
	 */
	String name() default "";
	
	/**
	 * 自增初始值
	 * @return
	 */
	int auto_increment() default 0;
	
	/**
	 * 表描述
	 * @return
	 */
	String comment() default "";
	
	/**
	 * 表编码
	 * @return
	 */
	String charset() default "utf8";
	
	/**
	 * 数据库引擎，默认行级锁
	 * @return
	 */
	String engine() default "InnoDB";
}

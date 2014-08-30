package util.mysql;

import java.util.List;

import util.mysql.entity.Table;

public class MySQL {

	/**
	 * 生成的实体类将依据数据库表名做实体类的类名，若您要自定义类名，可将类名传入，调用四参的该方法
	 * @param DBName 数据库名
	 * @param name table名
	 * @param physicalPath 生成实体类的物理存放路径
	 * @return boolean
	 */
	public static boolean tableToClassByHibernate(String DBName, String name, String physicalPath) {
		TableUtilByHibernate table = new TableUtilByHibernate();
		return table.tableToClass(DBName, name, physicalPath);
	}
	
	/**
	 * 生成的实体类将依据数据库表名做实体类的类名，若您要自定义类名，可将类名传入，调用四参的该方法
	 * @param DBName 数据库名
	 * @param name table名
	 * @param physicalPath 生成实体类的物理存放路径
	 * @return boolean
	 */
	public static boolean tableToClassByMyBatis(String DBName, String name, String physicalPath) {
		TableUtilByMyBatis table = new TableUtilByMyBatis();
		return table.tableToClass(DBName, name, physicalPath);
	}
	
	/**
	 * 生成的实体类将依据数据库表名做实体类的类名，若您要自定义类名，可将类名传入，调用四参的该方法
	 * @param DBName 数据库名
	 * @param name table名
	 * @param physicalPath 生成实体类的物理存放路径
	 * @return boolean
	 */
	public static boolean tableToClassByJDBC(String DBName, String name, String physicalPath) {
		TableUtilByJDBC table = new TableUtilByJDBC();
		return table.tableToClass(DBName, name, physicalPath);
	}
	
	/**
	 * 生成实体类
	 * @param DBName 数据库名
	 * @param name table名
	 * @param className class名
	 * @param physicalPath 生成实体类的物理存放路径
	 * @return boolean
	 */
	public static boolean tableToClassByHibernate(String DBName, String name, String className,
			String physicalPath) {
		TableUtilByHibernate table = new TableUtilByHibernate();
		return table.tableToClass(DBName, name, className, physicalPath);
	}
	
	/**
	 * 生成实体类
	 * @param DBName 数据库名
	 * @param name table名
	 * @param className class名
	 * @param physicalPath 生成实体类的物理存放路径
	 * @return boolean
	 */
	public static boolean tableToClassByMyBatis(String DBName, String name, String className,
			String physicalPath) {
		TableUtilByMyBatis table = new TableUtilByMyBatis();
		return table.tableToClass(DBName, name, className, physicalPath);
	}
	
	/**
	 * 生成实体类
	 * @param DBName 数据库名
	 * @param name table名
	 * @param className class名
	 * @param physicalPath 生成实体类的物理存放路径
	 * @return boolean
	 */
	public static boolean tableToClassByJDBC(String DBName, String name, String className,
			String physicalPath) {
		TableUtilByJDBC table = new TableUtilByJDBC();
		return table.tableToClass(DBName, name, className, physicalPath);
	}
	
	/**
	 * 批量生成
	 * @param DBName 数据库名称
	 * @param physicalPath 实体类存放路径
	 * @return
	 */
	public static boolean batchToClassByHibernate(String DBName, String physicalPath) {
		try {
			List<Table> list = TableUtilByHibernate.getTables(DBName);
			for (Table t : list) {
				TableUtilByHibernate table = new TableUtilByHibernate();
				if(!table.batchToClass(t, physicalPath)) {
					System.out.println("表格并没有生成完成，中间异常");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			System.out.println("数据库连接异常");
			return false;
		}
	}
	
	/**
	 * 批量生成
	 * @param DBName 数据库名称
	 * @param physicalPath 实体类存放路径
	 * @return
	 */
	public static boolean batchToClassByMyBatis(String DBName, String physicalPath) {
		try {
			List<Table> list = TableUtilByMyBatis.getTables(DBName);
			for (Table t : list) {
				TableUtilByMyBatis table = new TableUtilByMyBatis();
				if(!table.batchToClass(t, physicalPath)) {
					System.out.println("表格并没有生成完成，中间异常");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			System.out.println("数据库连接异常");
			return false;
		}
	}
	
	/**
	 * 批量生成
	 * @param DBName 数据库名称
	 * @param physicalPath 实体类存放路径
	 * @return
	 */
	public static boolean batchToClassByJDBC(String DBName, String physicalPath) {
		try {
			List<Table> list = TableUtilByJDBC.getTables(DBName);
			for (Table t : list) {
				TableUtilByJDBC table = new TableUtilByJDBC();
				if(!table.batchToClass(t, physicalPath)) {
					System.out.println("表格并没有生成完成，中间异常");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			System.out.println("数据库连接异常");
			return false;
		}
	}
}

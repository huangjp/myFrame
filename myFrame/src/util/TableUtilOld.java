package util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Enum.Tpye;
import annotation.Column;
import annotation.Delete;
import annotation.Table;

public class TableUtilOld {
	
	public static <T> boolean consistencyByClass(Class<T> c) {
		try {
			String tablename = getTableName(c);
			String sql = "select * from " + tablename;
			PreparedStatement ps = DBUtil.getPS(sql);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				if(deleteColumn(c, f)) continue;
				Column col = f.getAnnotation(Column.class);
				String colnum = getTableFieldName(col, f);
				String type = getTableFieldType(col, f);
				List<Integer> lengths = getTableFieldLength(col, f);
				boolean bool = false;
				for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
					String fieldName = rsmd.getColumnLabel(i);
					String fieldType = rsmd.getColumnTypeName(i);
					Integer length = rsmd.getPrecision(i);
					if(colnum.equalsIgnoreCase(fieldName)) {
						if(type.equalsIgnoreCase(fieldType)) {
							for (int j = 0; j < lengths.size(); j++) {
								if(length.equals(lengths.get(j))) {
									bool = true;
									break;
								}
							}
						}
					}
				}
				if(!bool) {
					addColumn(c, f);
				}
			}
			CreateClass1.create(tablename);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.closeCon();
		}
	}
	
	//添加表格中表结构的某一个字段column
	private static <T> boolean addColumn(Class<T> c, Field field) {
		try {
			String tablename = getTableName(c);
			Column col = field.getAnnotation(Column.class);
			String colname = getTableFieldName(col, field);
			String type = getTableFieldType(col, field);
			List<Integer> lengths = getTableFieldLength(col, field);
			StringBuilder sb = new StringBuilder();
			sb.append("ALTER TABLE `" + tablename + "` add COLUMN `" + colname + "` " + type);
			if(lengths.size() == 1) {
				sb.append("(" + lengths.get(0) + ")");
			} else if(lengths.size() == 2){
				sb.append("(" + lengths.get(0) + "," + lengths.get(1) + ")");
			}
			if(col.isNull()) sb.append(" NOT NULL");
			sb.append(" " + col.defaultValue());
			sb.append(" " + col.comment());
			DBUtil.getPS(sb.toString()).execute();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			DBUtil.closeCon();
		}
	}
	
	//删除表格中表结构的某一个字段column
	private static <T> boolean deleteColumn(Class<T> c, Field field) {
		try {
			Delete del = field.getAnnotation(Delete.class);
			if(del == null) return false;
			String tablename = getTableName(c);
			String colname = field.getName();
			if(del.column() != "") colname = del.column();
			if(del.value() != "") colname = del.value();
			String sql = "ALTER TABLE `" + tablename + "` DROP COLUMN `" + colname + "`";
			DBUtil.getPS(sql).execute();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			DBUtil.closeCon();
		}
	}
	
	//删除表格
	private static <T> boolean deleteTable(Class<T> c) {
		Table table = c.getAnnotation(Table.class);
		StringBuilder sb = new StringBuilder();
		String tablename = c.getSimpleName();
		if(table.value() != "" && table.name() == "") tablename = table.value();
		if(table.value() == "" && table.name() != "") tablename = table.value();
		sb.append("DROP TABLE IF EXISTS `" + tablename + "`;\n");
		try {
			DBUtil.getPS(sb.toString()).execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.closeCon();
		}
	}
	//创建数据库表格
	public static <T> boolean create(Class<T> c) {
		Table table = c.getAnnotation(Table.class);
		if(!deleteTable(c))return false;
		StringBuilder sb = new StringBuilder();
		String tablename = getTableName(c);
		sb.append("CREATE TABLE `" + tablename + "` (\n");
		Field[] fields = c.getDeclaredFields();
		String primary = "";
		for (Field f : fields) {
			Column col = f.getAnnotation(Column.class);
			if(col != null) {
				sb.append(" `" + col.name() + "` " + col.type().name());
//				if(col.length1() != 0 && col.length2() != 0) {
//					sb.append(" (" + col.length1() + ", " + col.length2() + ")");
//				} else if(col.length1() != 0){
//					sb.append(" (" + col.length1() + ")");
//				}
				if(col.primary()) {
					primary = col.name();
					sb.append(" NOT NULL");
					sb.append(" " + col.defaultValue());
				} else {
					if(col.identity()) {
						sb.append(" UNIQUE");
					} else {
						if(col.foreign()) sb.append("");
					}
					sb.append(" " + col.defaultValue());
					if(col.isNull()) sb.append(" NOT NULL");
				}
				if(col.auto_increment()) sb.append(" AUTO_INCREMENT");
				sb.append(" " + col.comment() + ",\n");
			} else {
				sb.append(" " + f.getName() + " NULL");
				Type type = f.getType();
				if(type == String.class) {
					sb.append(" TEXT NULL,");
				} else if(type == Integer.class) {
					sb.append(" INT NULL,");
				} else if(type == Boolean.class) {
					sb.append(" BIT(1) NULL,");
				} else if(type == Date.class) {
					sb.append(" DATETIME NULL,");
				}
			}
		}
		if(primary != "") sb.append(" PRIMARY KEY(" + primary + ")\n");
		sb.append(") ENGINE=" + table.engine() + " AUTO_INCREMENT=" + table.auto_increment());
		sb.append(" DEFAULT CHARSET=" + table.charset());
		System.out.println(sb.toString());
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		try {
			ps.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.closeCon();
		}
	}
	
	//获得数据库表中字段类型的集合
	public static <T> List<String> getTableFieldTypes(Class<T> c) {
		List<String> types = new ArrayList<String>();
		String tablename = getTableName(c);
		String sql = "select * from " + tablename;
		PreparedStatement ps = DBUtil.getPS(sql);
		try {
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int size = rsmd.getColumnCount();
			for (int i = 1; i < size + 1; i++) {
				String str = rsmd.getColumnTypeName(i);
				types.add(str);
			}
			return types;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			DBUtil.closeCon();
		}
	}
	
	//获得数据库表中字段集合
	public static <T> List<String> getTableFields(Class<T> c) {
		List<String> attrs = new ArrayList<String>();
		String tablename = getTableName(c);
		String sql = "select * from " + tablename;
		PreparedStatement ps = DBUtil.getPS(sql);
		try {
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int size = rsmd.getColumnCount();
			for (int i = 1; i < size + 1; i++) {
				String str = rsmd.getColumnName(i);
				attrs.add(str);
			}
			return attrs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			DBUtil.closeCon();
		}
	}
	/*
	 * 获取实体类中每个属性对应的表字段在数据库的字节长度,依据Colnum注解数据来定义,
	 * 若注解中没有，测S
	 */
	public static List<Integer> getTableFieldLength(Column col, Field field) {
		Type type = field.getType();
		List<Integer> lengths = new ArrayList<Integer>();
		if(type == String.class) lengths.add(20);
		if(type == Integer.class || type == int.class) lengths.add(11);
//		if(col.length1() != 0 && col.length2() != 0) {
//			lengths.add(col.length1());
//			lengths.add(col.length2());
//		} else if(col.length1() != 0){
//			lengths.add(col.length1());
//		}
		return lengths;
	}
	//获取实体类中每个属性对应的表字段名的类型
	public static String getTableFieldType(Column col, Field field) {
		String type = field.getType().getSimpleName();
		if(col.type() != Tpye.DEFAULT) type = col.type().name();
		return type;
	}
	
	//获取实体类中每个属性对应的表字段名
	public static String getTableFieldName(Column col, Field field) {
		String fieldname = field.getName();
		if(col.value() != "" && col.name() == "") fieldname = col.value();
		if(col.value() == "" && col.name() != "") fieldname = col.name();
		return fieldname;
	}
	
	//获取实体类中的表名
	public static <T> String getTableName(Class<T> c) {
		Table table = c.getAnnotation(Table.class);
		String tablename = c.getSimpleName();
		if(table.value() != "" && table.name() == "") tablename = table.value();
		if(table.value() == "" && table.name() != "") tablename = table.name();
		return tablename;
	}
	
	//获取实体类中属性集合
	public static <T> List<String> getClassAttr(Class<T> c) {
		List<String> list = new ArrayList<String>();
		Field[] fields = c.getDeclaredFields();
		for (Field f : fields) {
			list.add(f.getName());
		}
		return list;
	}
	
	//获取实体类中属性类型集合
	public static <T> List<String> getAttrType(Class<T> c) {
		List<String> list = new ArrayList<String>();
		Field[] fields = c.getDeclaredFields();
		for (Field f : fields) {
			list.add(f.getType().getClass().getSimpleName());
		}
		return list;
	}
	
	/**
	 * 保留方法
	 */
	public void test() throws Exception {
        String sql = "select * from class";
        PreparedStatement stmt = DBUtil.getPS(sql);
        try {
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData data = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= data.getColumnCount(); i++) {
                    // 获得所有列的数目及实际列数
                    int columnCount = data.getColumnCount();
                    // 获得指定列的列名
                    String columnName = data.getColumnName(i);
                    // 获得指定列的列值
                    String columnValue = rs.getString(i);
                    // 获得指定列的数据类型
                    int columnType = data.getColumnType(i);
                    // 获得指定列的数据类型名
                    String columnTypeName = data.getColumnTypeName(i);
                    // 所在的Catalog名字
                    String catalogName = data.getCatalogName(i);
                    // 对应数据类型的类
                    String columnClassName = data.getColumnClassName(i);
                    // 在数据库中类型的最大字符个数
                    int columnDisplaySize = data.getColumnDisplaySize(i);
                    // 默认的列的标题
                    String columnLabel = data.getColumnLabel(i);
                    // 获得列的模式
                    String schemaName = data.getSchemaName(i);
                    // 某列类型的精确度(类型的长度)
                    int precision = data.getPrecision(i);
                    // 小数点后的位数
                    int scale = data.getScale(i);
                    // 获取某列对应的表名
                    String tableName = data.getTableName(i);
                    // 是否自动递增
                    boolean isAutoInctement = data.isAutoIncrement(i);
                    // 在数据库中是否为货币型
                    boolean isCurrency = data.isCurrency(i);
                    // 是否为空
                    int isNullable = data.isNullable(i);
                    // 是否为只读
                    boolean isReadOnly = data.isReadOnly(i);
                    // 能否出现在where中
                    boolean isSearchable = data.isSearchable(i);
                    System.out.println(columnCount);
                    System.out.println("获得列" + i + "的字段名称:" + columnName);
                    System.out.println("获得列" + i + "的字段值:" + columnValue);
                    System.out.println("获得列" + i + "的类型,返回SqlType中的编号:"
                            + columnType);
                    System.out.println("获得列" + i + "的数据类型名:" + columnTypeName);
                    System.out.println("获得列" + i + "所在的Catalog名字:"
                            + catalogName);
                    System.out.println("获得列" + i + "对应数据类型的类:"
                            + columnClassName);
                    System.out.println("获得列" + i + "在数据库中类型的最大字符个数:"
                            + columnDisplaySize);
                    System.out.println("获得列" + i + "的默认的列的标题:" + columnLabel);
                    System.out.println("获得列" + i + "的模式:" + schemaName);
                    System.out
                            .println("获得列" + i + "类型的精确度(类型的长度):" + precision);
                    System.out.println("获得列" + i + "小数点后的位数:" + scale);
                    System.out.println("获得列" + i + "对应的表名:" + tableName);
                    System.out.println("获得列" + i + "是否自动递增:" + isAutoInctement);
                    System.out.println("获得列" + i + "在数据库中是否为货币型:" + isCurrency);
                    System.out.println("获得列" + i + "是否为空:" + isNullable);
                    System.out.println("获得列" + i + "是否为只读:" + isReadOnly);
                    System.out.println("获得列" + i + "能否出现在where中:"
                            + isSearchable);
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败");
        } finally {
        	DBUtil.closeCon();
        }
	}
}

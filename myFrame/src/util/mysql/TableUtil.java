package util.mysql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.CreateClass1;
import util.DBUtil;
import util.MyUtil;
import util.PrivilegeReader;
import util.mysql.entity.TableStructure;
import util.mysql.entity.TableTypeLength;
import Enum.Tpye;
import annotation.Column;
import annotation.Delete;
import annotation.Table;

public class TableUtil {
	private boolean f_util = false;
	private boolean f_sql = false;
	private String path = "";
	private String entityPath = "";
	private String DBName = "";
	private List<TableStructure> colNames = new ArrayList<TableStructure>();
	
	public TableUtil() {
		super();
	}
	
	public boolean tableToClass(String DBName, String name, String physicalPath) {
		try {
			path = physicalPath;
			ParsePath(physicalPath);
			this.DBName = DBName;
			getColumn(name.toUpperCase());
			System.out.println("表已经生成了，在"+entityPath+"路径下");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean tableToClass(String DBName, String name, String className, String physicalPath) {
		try {
			path = physicalPath;
			ParsePath(physicalPath);
			this.DBName = DBName;
			getColumn(name.toUpperCase(),className);
			System.out.println("表已经生成了，在"+entityPath+"路径下");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchToClass(util.mysql.entity.Table table, String physicalPath) {
		try {
			path = physicalPath;
			ParsePath(physicalPath);
			this.DBName = table.getDBName();
			colNames = table.getTableStructures();
			giveValue(table.getName().toUpperCase().trim());
			System.out.println("表已经生成了，在"+entityPath+"路径下");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void ParsePath(String path) {
//		String p = "E:/Workspaces/MyEclipse Professional/myFrame/src/entity/";
		String[] strings = path.split("/src/");
		String root = strings[0];
		String p = strings[1].replace("/", ".");
		entityPath = p.substring(0, p.length() - 1);
		List<String> paths = PrivilegeReader.getRootPaths(root);
		for (String s : paths) {
			if(path.contains(s)) {
				strings = path.split(s+"/");
				p = strings[1].replace("/", ".");
				entityPath = p.substring(0, p.length() - 1);
				continue;
			}
		}
	}
	
	/**
	 * 获得数据库中所有表的集合
	 * @param DBName 数据库名，区分大小写
	 * @return
	 * @throws Exception
	 */
	public static List<util.mysql.entity.Table> getTables(String DBName) throws Exception {
		String sql = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_DEFAULT," +
				"IS_NULLABLE,DATA_TYPE,COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT "
				+ "from information_schema.columns where table_schema = '"+DBName+"'";
		PreparedStatement ps = DBUtil.getPS(sql);
		ResultSet rs = ps.executeQuery();
		List<util.mysql.entity.Table> list = new ArrayList<util.mysql.entity.Table>();
		List<TableStructure> colNames = new ArrayList<TableStructure>();
		String tableName = "";
		while (rs.next()) {
			TableStructure ts = new TableStructure();
			java.lang.reflect.Field[] fields = ts.getClass().getDeclaredFields();
			for (java.lang.reflect.Field f : fields) {
				Type type = f.getType();
				String name = f.getName();
				Method m = ts.getClass().getDeclaredMethod("set" + MyUtil.initcap(name), (Class<?>) type);
				m.invoke(ts,rs.getString(name));
			}
			if(!tableName.equalsIgnoreCase(ts.getTable_name()) && tableName != "") {
				util.mysql.entity.Table table = new util.mysql.entity.Table();
				table.setDBName(DBName);
				table.setName(tableName);
				table.setTableStructures(colNames);
				list.add(table);
				colNames = new ArrayList<TableStructure>();
			}
			colNames.add(ts);
			tableName = ts.getTable_name();
		}
		DBUtil.closeCon();
		return list;
	}
	
//	Schema schema = new Schema();
//	java.lang.reflect.Field[] fields = schema.getClass().getDeclaredFields();
//	for (java.lang.reflect.Field f : fields) {
//		Type type = f.getType();
//		String name = f.getName();
//		Method m = schema.getClass().getDeclaredMethod("set" + MyUtil.initcap(name), (Class<?>) type);
//		List<TableStructure> colNames = new ArrayList<TableStructure>();
//		if("tableStructures".equalsIgnoreCase(name.trim())) {
//			TableStructure ts = new TableStructure();
//			java.lang.reflect.Field[] fields1 = ts.getClass().getDeclaredFields();
//			for (java.lang.reflect.Field f1 : fields1) {
//				Type type1 = f1.getType();
//				String name1 = f1.getName();
//				Method m1 = 
//						ts.getClass().getDeclaredMethod("set" + MyUtil.initcap(name1), (Class<?>) type1);
//				m1.invoke(ts,rs.getString(name));
//			}
//			colNames.add(ts);
//			m.invoke(schema,ts);
//		} else {
//			m.invoke(schema,rs.getString(name));
//		}
//	}
//	list.add(schema);
	
	//新版开始
	private void getColumn(String tableName,Object... className) throws Exception {
		String sql = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_DEFAULT," +
				"IS_NULLABLE,DATA_TYPE,COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT "
				+ "from information_schema.columns where table_name='"+tableName
				+ "' and table_schema = '"+DBName+"'";
		PreparedStatement ps = DBUtil.getPS(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			TableStructure ts = new TableStructure();
			java.lang.reflect.Field[] fields = ts.getClass().getDeclaredFields();
			for (java.lang.reflect.Field f : fields) {
				Type type = f.getType();
				String name = f.getName();
				Method m = ts.getClass().getDeclaredMethod("set" + MyUtil.initcap(name), (Class<?>) type);
				m.invoke(ts,rs.getString(name));
			}
			colNames.add(ts);
		}
		giveValue(tableName,className);
		DBUtil.closeCon();
	}

	//为判断条件赋值
	private void giveValue(String tableName,Object... className) {
		for (int i = 1; i < colNames.size(); i++) {
			String type = colNames.get(i).getData_type().trim();
			if("datetime".equalsIgnoreCase(type) || "date".equalsIgnoreCase(type) || 
					"timestamp".equalsIgnoreCase(type)) {
				f_util = true;
			}
			if("image".equalsIgnoreCase(type) || "text".equalsIgnoreCase(type) ||
					"blob".equalsIgnoreCase(type)) {
				f_sql = true;
			}
		}
		if(className.length != 0) {
			writeFile(writeJavaString(tableName,className),MyUtil.humpcap(className[0].toString()));
		} else {
			writeFile(writeJavaString(tableName),MyUtil.humpcap(tableName));
		}
	}
	private void writeAllMethod(StringBuilder sb) {
		for (int i = 0; i < colNames.size(); i++) {
			String name = colNames.get(i).getColumn_name().trim();
			String type = colNames.get(i).getData_type().trim();
			String attr = MyUtil.initsmallcap(MyUtil.humpcap(name));
			sb.append("\t\r\n\tpublic void set" + MyUtil.humpcap(name) + "(" +
					castTpye(type) + " " + attr + "){\r\n");
			sb.append("\t\tthis." + attr + "=" + attr +";\r\n");
			sb.append("\t}\r\n");
			sb.append("\t\r\n\tpublic " + castTpye(type) + " get" + MyUtil.humpcap(name)+"(){\r\n");
			sb.append("\t\treturn " + attr + ";\r\n");
			sb.append("\t}\r\n");
		}
	}
	private void writeAllAttrs(StringBuilder sb) {
		for (int i = 0; i < colNames.size(); i++) {
			TableTypeLength ttl = getTpye(colNames.get(i).getColumn_type().trim());
			String name = colNames.get(i).getColumn_name().trim();
			String type = ttl.getType().trim();
			boolean isNull = "NO".equalsIgnoreCase(colNames.get(i).getIs_nullable().trim()) ? false : true;
			Object defVal = colNames.get(i).getColumn_default();
			String comment = colNames.get(i).getColumn_comment().trim();
			Integer length = ttl.getLength();
			String key = colNames.get(i).getColumn_key().trim();
			String str = castTpye(type);
			String attr = MyUtil.initsmallcap(MyUtil.humpcap(name));
			if("PRI".equalsIgnoreCase(key)) {
				sb.append("\t@Id\r\n");
				sb.append("\t@GeneratedValue(strategy = GenerationType.IDENTITY)\r\n");
				sb.append("\t@Column(name=\""+name+"\""+
						(length==null?"":",length="+length)+",nullable="+isNull+")"+
						(defVal==null?"":"//default=\""+defVal+"\"")+"\r\n");
			}  else {
				boolean b = "UNI".equalsIgnoreCase(key);
				if("decimal".equalsIgnoreCase(type) || 
						"numeric".equalsIgnoreCase(type) || "smallmoney".equalsIgnoreCase(type) || 
						"real".equalsIgnoreCase(type) || "double".equalsIgnoreCase(type) || 
						"number".equalsIgnoreCase(type) || "binary_double".equalsIgnoreCase(type) || 
						"money".equalsIgnoreCase(type)) {
					Integer pre = ttl.getPrecision();
					if(pre != null && pre != 0) {
						sb.append("\t@Column(name=\""+name+"\",precision="+pre+",scale="+
								ttl.getScale()+",nullable="+isNull+
								(b==true?",unique=true":"")+")"+
								(defVal==null?"":"//default=\""+defVal+"\"")+"\r\n");
					} else {
						sb.append("\t@Column(name=\""+name+"\",nullable="+isNull+
								(b==true?",unique=true":"")+")"+
								(defVal==null?"":"//default=\""+defVal+"\"")+"\r\n");
					}
				} else {
					sb.append("\t@Column(name=\""+name+"\""+
							(length == null?"":",length="+length)+",nullable="+isNull+
							(b==true?",unique=true":"")+")"+
							(defVal==null?"":"//default=\""+defVal+"\"")+"\r\n");
				}
			}
			sb.append("\tprivate "+str+" "+ attr +";"+
					(comment.trim()=="" || comment == null?"":"//"+comment)+"\r\n\n");
		}
	}
	//新版结束
	
	private void writeFile(String str,String javaLocationAndName) {
		FileWriter fw;
		try {
			File file = new File(path+javaLocationAndName+".java");
			fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(str);
			pw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//生成JAVA文件
	private String writeJavaString(String tableName,Object... className) {
		StringBuilder sb = new StringBuilder();
		sb.append("package " + entityPath + ";\r\n\n");
		if(f_util) {
			sb.append("import java.sql.Date;\r\n");
			sb.append("import java.sql.Timestamp;\r\n");
			sb.append("import java.text.ParseException;\r\n");
			sb.append("import java.text.SimpleDateFormat;\r\n");
		}
		if(f_sql) {
			sb.append("import java.sql.*;\r\n");
		}
		sb.append("import javax.persistence.Column;\r\n");
		sb.append("import javax.persistence.Entity;\r\n");
		sb.append("import javax.persistence.GeneratedValue;\r\n");
		sb.append("import javax.persistence.GenerationType;\r\n");
		sb.append("import javax.persistence.Id;\r\n");
		sb.append("import javax.persistence.Table;\r\n\n");
		sb.append("@Entity\r\n");
		sb.append("@Table(name=\"" + tableName + "\")\r\n");
		if(className.length != 0) {
			sb.append("public class " + MyUtil.humpcap(className[0].toString()) + " {\r\n\n");
		} else {
			sb.append("public class " + MyUtil.humpcap(tableName) + " {\r\n\n");
		}
		writeAllAttrs(sb);
		writeAllMethod(sb);
		if(className.length != 0) {
			writeHashCodeAndEquals(sb,MyUtil.humpcap(className[0].toString()));
		} else {
			writeHashCodeAndEquals(sb,MyUtil.humpcap(tableName));
		}
		sb.append("}\r\n");
		return sb.toString();
	}
	
	//数据库类型转换转换为java包装类型
	private String castTpye(String s) {
		if(s.equalsIgnoreCase("bit") || s.equalsIgnoreCase("boolean")) {
			return "Boolean";
		}else if(s.equalsIgnoreCase("tinyint") || s.equalsIgnoreCase("blob") || s.equalsIgnoreCase("byte")) {
			return "Integer";
		}else if(s.equalsIgnoreCase("smallint") || s.equalsIgnoreCase("short")) {
			return "Integer";
		}else if(s.equalsIgnoreCase("int") || s.equalsIgnoreCase("number1") || s.equalsIgnoreCase("integer")) {
			return "Integer";
		}else if(s.equalsIgnoreCase("bigint") || s.equalsIgnoreCase("long")) {
			return "Long";
		}else if(s.equalsIgnoreCase("float") || s.equalsIgnoreCase("binary_float")) {
			return "Float";
		}else if(s.equalsIgnoreCase("decimal") || s.equalsIgnoreCase("numeric") || s.equalsIgnoreCase("real") ||
				s.equalsIgnoreCase("double")||s.equalsIgnoreCase("number")||s.equalsIgnoreCase("binary_double")) {
			return "Double";
		}else if(s.equalsIgnoreCase("money") || 
				s.equalsIgnoreCase("smallmoney")) {
			return "Double";
		}else if(s.equalsIgnoreCase("varchar") || s.equalsIgnoreCase("text") || s.equalsIgnoreCase("tinytext") ||
				s.equalsIgnoreCase("char") || s.equalsIgnoreCase("varchar2") ||s.equalsIgnoreCase("nvarchar") ||
				s.equalsIgnoreCase("nchar") || s.equalsIgnoreCase("String")){
			return "String";
		}else if(s.equalsIgnoreCase("datetime") || s.equalsIgnoreCase("timestamp")) {
			return "Timestamp";
		}else if(s.equalsIgnoreCase("date")) {
			return "Date";
		} else if(s.equalsIgnoreCase("image") || s.equalsIgnoreCase("blob")) {
			return "Blob";
		}else if(s.equalsIgnoreCase("clob")) {
			return "Clob";
		}
		return null;
	}
	
	private static TableTypeLength getTpye(String str) {
		TableTypeLength ttl = new TableTypeLength();
		int i = str.indexOf("(");
		i = i == -1 ? str.length() : i;
		ttl.setType(str.substring(0, i));
		if(i != str.length()) {
			str = str.substring(i + 1 , str.indexOf(")"));
			if(str.contains(",")) {
				String[] sssss = str.split(",");
				ttl.setPrecision(Integer.parseInt(sssss[0]));
				ttl.setScale(Integer.parseInt(sssss[1]));
			} else {
				ttl.setLength(Integer.parseInt(str));
			}
		}
		return ttl;
	}
	
	private void writeHashCodeAndEquals(StringBuilder sb,String className) {
		for (int i = 0; i < colNames.size(); i++) {
			String key = colNames.get(i).getColumn_key().trim();
			String name = colNames.get(i).getColumn_name().trim();
			String attr = MyUtil.initsmallcap(MyUtil.humpcap(name));
			if("PRI".equalsIgnoreCase(key)) {
				sb.append("\t@Override");
				sb.append("\tpublic int hashCode() {\r\n");
				sb.append("\t\tfinal int prime = 31;\r\n");
				sb.append("\t\tint result = 1;\r\n");
				sb.append("\t\tresult = prime * result + (("+
						attr+" == null) ? 0 : "+attr+".hashCode());\r\n");
				sb.append("\t\treturn result;\r\n");
				sb.append("\t}\r\n\n");
				sb.append("\t@Override");
				sb.append("\tpublic boolean equals(Object obj) {\r\n");
				sb.append("\t\tif (this == obj) return true;\r\n");
				sb.append("\t\tif (obj == null) return false;\r\n");
				sb.append("\t\tif (getClass() != obj.getClass()) return false;\r\n");
				sb.append("\t\t"+className+" other = ("+className+") obj;\r\n");
				sb.append("\t\tif ("+attr+" == null) {\r\n\t\t\tif (other."+
						attr+" != null) return false;\r\n");
				sb.append("\t\t} else if (!"+attr+".equals(other."+
						attr+")) return false;\r\n");
				sb.append("\t\treturn true;\r\n");
				sb.append("\t}\r\n\n");
				break;
			}
		}
	}
	
	public <T> boolean consistencyByClass(Class<T> c) {
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
	private <T> boolean addColumn(Class<T> c, Field field) {
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
	private <T> boolean deleteColumn(Class<T> c, Field field) {
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
	private <T> boolean deleteTable(Class<T> c) {
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
	public <T> boolean create(Class<T> c) {
		Table table = c.getAnnotation(Table.class);
		if(!deleteTable(c))return false;
		StringBuilder sb = new StringBuilder();
		String tablename = getTableName(c);
		sb.append("CREATE TABLE `" + tablename + "` (\n");
		Field[] fields = c.getDeclaredFields();
		String primary = "";
		for (Field f : fields) {
			Type type = f.getType();
			Column col = f.getAnnotation(Column.class);
			if(col != null) {
				if(type == double.class || type == Double.class) {
					sb.append(" `" + col.name() + "` " + col.type().name());
					if(col.precision() != 0 && col.scale() != 0) {
						sb.append(" (" + col.precision() + ", " + col.scale() + ")");
					}
				} else {
					sb.append(" `" + col.name() + "` " + col.type().name());
					if(col.length() != 0) {
						sb.append(" (" + col.length() + ")");
					}
				}
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
	public <T> List<String> getTableFieldTypes(Class<T> c) {
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
	public <T> List<String> getTableFields(Class<T> c) {
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
	public List<Integer> getTableFieldLength(Column col, Field field) {
		Type type = field.getType();
		List<Integer> lengths = new ArrayList<Integer>();
		if(type == Double.class || type == double.class) {
			int precision = col.precision();
			int scale = col.scale();
			if(precision != 0) {
				lengths.add(precision);
				lengths.add(scale);
			}
		} else {
			int i = col.length();
			if(i != 0) {
				lengths.add(col.length());
			} else {
				if(type == String.class) lengths.add(45);
			}
		}
		return lengths;
	}
	//获取实体类中每个属性对应的表字段名的类型
	public String getTableFieldType(Column col, Field field) {
		String type = field.getType().getSimpleName();
		if(col.type() != Tpye.DEFAULT) type = col.type().name();
		return type;
	}
	
	//获取实体类中每个属性对应的表字段名
	public String getTableFieldName(Column col, Field field) {
		String fieldname = field.getName();
		if(col.value() != "" && col.name() == "") fieldname = col.value();
		if(col.value() == "" && col.name() != "") fieldname = col.name();
		return fieldname;
	}
	
	//获取实体类中的表名
	public <T> String getTableName(Class<T> c) {
		Table table = c.getAnnotation(Table.class);
		String tablename = c.getSimpleName();
		if(table.value() != "" && table.name() == "") tablename = table.value();
		if(table.value() == "" && table.name() != "") tablename = table.name();
		return tablename;
	}
	
	//获取实体类中属性集合
	public <T> List<String> getClassAttr(Class<T> c) {
		List<String> list = new ArrayList<String>();
		Field[] fields = c.getDeclaredFields();
		for (Field f : fields) {
			list.add(f.getName());
		}
		return list;
	}
	
	//获取实体类中属性类型集合
	public <T> List<String> getAttrType(Class<T> c) {
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


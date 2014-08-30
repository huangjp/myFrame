package util.mysql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import util.DBUtil;
import util.MyUtil;
import util.PrivilegeReader;
import util.mysql.entity.Table;
import util.mysql.entity.TableStructure;
import util.mysql.entity.TableTypeLength;

public class TableUtilByJDBC {
	private boolean f_util = false;
	private boolean f_sql = false;
	private String path = "";
	private String entityPath = "";
	private String DBName = "";
	private List<TableStructure> colNames = new ArrayList<TableStructure>();
	
	public TableUtilByJDBC() {
		super();
	}
	
	public boolean tableToClass(String DBName, String name, String physicalPath) {
		try {
			path = physicalPath;
			ParsePath(physicalPath);
			this.DBName = DBName;
			getColumns(name.toUpperCase());
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
			getColumns(name.toUpperCase(),className);
			System.out.println("表已经生成了，在"+entityPath+"路径下");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchToClass(Table table, String physicalPath) {
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
	public static List<Table> getTables(String DBName) throws Exception {
		String sql = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_DEFAULT," +
				"IS_NULLABLE,DATA_TYPE,COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT "
				+ "from information_schema.columns where table_schema = '"+DBName+"'";
		PreparedStatement ps = DBUtil.getPS(sql);
		ResultSet rs = ps.executeQuery();
		List<Table> list = new ArrayList<Table>();
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
				Table table = new Table();
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
	
	private void getColumns(String tableName,Object... className) throws Exception {
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
			writeFile(generateJavaString(tableName,className),MyUtil.humpcap(className[0].toString())+".java");
		} else {
			writeFile(generateJavaString(tableName),MyUtil.humpcap(tableName)+".java");
		}
	}
	
	private void writeFile(String str,String javaLocationAndName) {
		FileWriter fw;
		try {
			File file = new File(path+javaLocationAndName);
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
	
	private void generateAllMethod(StringBuilder sb) {
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
	
	private void generateAllAttrs(StringBuilder sb) {
		for (int i = 0; i < colNames.size(); i++) {
			TableTypeLength ttl = getTpye(colNames.get(i).getColumn_type().trim());
			String name = colNames.get(i).getColumn_name().trim();
			String type = ttl.getType().trim();
			String comment = colNames.get(i).getColumn_comment().trim();
			String str = castTpye(type);
			String attr = MyUtil.initsmallcap(MyUtil.humpcap(name));
			sb.append("\tprivate "+str+" "+ attr +";"+
					(comment.trim()=="" || comment == null?"":"//"+comment)+"\r\n\n");
		}
	}
	
	//生成JAVA文件
	private String generateJavaString(String tableName,Object... className) {
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
		sb.append("import java.io.Serializable;\r\n");
		sb.append("import javax.persistence.Column;\r\n");
		sb.append("import javax.persistence.Entity;\r\n");
		sb.append("import javax.persistence.GeneratedValue;\r\n");
		sb.append("import javax.persistence.GenerationType;\r\n");
		sb.append("import javax.persistence.Id;\r\n");
		sb.append("import javax.persistence.Transient;\r\n");
		sb.append("import javax.persistence.Table;\r\n\n");
		if(className.length != 0) {
			sb.append("public class " + MyUtil.humpcap(className[0].toString()) + 
					" implements Serializable {\r\n\n");
		} else {
			sb.append("public class " + MyUtil.humpcap(tableName) + " implements Serializable {\r\n\n");
		}
		sb.append("\tprivate static final long serialVersionUID = 1L;\r\n\n");
		generateAllAttrs(sb);
		generateAllMethod(sb);
		if(className.length != 0) {
			generateHashCodeAndEquals(sb,MyUtil.humpcap(className[0].toString()));
		} else {
			generateHashCodeAndEquals(sb,MyUtil.humpcap(tableName));
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
				s.equalsIgnoreCase("nchar") || s.equalsIgnoreCase("String") || s.equalsIgnoreCase("LONGTEXT")){
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
	
	private void generateHashCodeAndEquals(StringBuilder sb,String className) {
		for (int i = 0; i < colNames.size(); i++) {
			String key = colNames.get(i).getColumn_key().trim();
			String name = colNames.get(i).getColumn_name().trim();
			String attr = MyUtil.initsmallcap(MyUtil.humpcap(name));
			if("PRI".equalsIgnoreCase(key)) {
				sb.append("\t@Override\r\n");
				sb.append("\tpublic int hashCode() {\r\n");
				sb.append("\t\tfinal int prime = 31;\r\n");
				sb.append("\t\tint result = 1;\r\n");
				sb.append("\t\tresult = prime * result + (("+
						attr+" == null) ? 0 : "+attr+".hashCode());\r\n");
				sb.append("\t\treturn result;\r\n");
				sb.append("\t}\r\n\n");
				sb.append("\t@Override\r\n");
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
}


package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateClass1 {
	private static List<String> colNames = new ArrayList<String>();
	private static List<String> colTypes = new ArrayList<String>();
	private static List<String> types = new ArrayList<String>();
	private static boolean f_util = false;
	private static boolean f_sql = false;
	
	public CreateClass1() {
		super();
	}
	
	public static <T> boolean create(String tableName) {
		try {
			getColumn(tableName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static <T> boolean create(String tableName, String className) {
		try {
			getColumn(tableName,className);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static void getColumn(String tableName,String... className) throws SQLException {
		String sql = "select * from " + tableName;
		PreparedStatement ps = DBUtil.getPS(sql);
		ResultSet rs = ps.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int size = rsmd.getColumnCount();
		for (int i = 0; i < size; i++) {
			colNames.add(MyUtil.smallcap(rsmd.getColumnName(i+1)));
			String str = rsmd.getColumnTypeName(i+1);
			if(str.equalsIgnoreCase("number")) {
				if(rs.next()) {
					if(rs.getInt(i+1) == rs.getDouble(i+1)) {
						str = str + "1";
					}
				}
			}
			colTypes.add(MyUtil.smallcap(str));
		}
		giveValue(tableName,className);
		DBUtil.closeCon();
	}
	private static void giveValue(String tableName,String... className) {
		for (int i = 1; i < colNames.size(); i++) {
			if(colTypes.get(i).equalsIgnoreCase("datetime") || 
					colTypes.get(i).equalsIgnoreCase("date") || colTypes.get(i).equalsIgnoreCase("timestamp")) {
				f_util = true;
			}
			if(colTypes.get(i).equalsIgnoreCase("image") || colTypes.get(i).equalsIgnoreCase("text") ||
					colTypes.get(i).equalsIgnoreCase("blob")) {
				f_sql = true;
			}
		}
		if(className != null) {
			writerfile(parse(tableName,className),MyUtil.initcap(className[0]));
		} else {
			writerfile(parse(tableName),MyUtil.initcap(tableName));
		}
	}
	private static void writerfile(String str,String javaLocationAndName) {
		FileWriter fw;
		try {
			File file = new File("D:/huangjp.success/Workspaces/MyEclipse Professional/myAnnotation/src/entity/"+javaLocationAndName+".java");
			fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(str);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String parse(String tableName,String... className) {
		StringBuffer sb = new StringBuffer();
		sb.append("package entity;\r\n\n");
		if(f_util) {
			sb.append("import java.util.Date;\r\n");
			sb.append("import java.text.ParseException;\r\n");
			sb.append("import java.text.SimpleDateFormat;\r\n");
		}
		if(f_sql) {
			sb.append("import java.sql.*;\r\n");
		}
		if(className != null) {
			sb.append("import annotation.Table;\r\n\n");
			sb.append("@Table(\"" + tableName + "\")\r\n");
			sb.append("public class " + MyUtil.initcap(className[0]) + " {\r\n");
		} else {
			sb.append("public class " + MyUtil.initcap(tableName) + " {\r\n");
		}
		processAllAttrs(sb);
		processAllMethod(sb);
		sb.append("}\r\n");
		return sb.toString();
	}
	private static void processAllMethod(StringBuffer sb) {
		for (int i = 0; i < colNames.size(); i++) {
			sb.append("\tpublic void set" + MyUtil.initcap(colNames.get(i)) + "(" +
					sqlType2JavaType(colTypes.get(i)) + " " + 
					MyUtil.initcap(colNames.get(i)) + "){\r\n");
			sb.append("\t\tthis."+colNames.get(i)+"="+
					MyUtil.initcap(colNames.get(i))+";\r\n");
			sb.append("\t}\r\n");
			sb.append("\tpublic "+sqlType2JavaType(colTypes.get(i))+" get"+
					MyUtil.initcap(colNames.get(i))+"(){\r\n");
			sb.append("\t\treturn "+colNames.get(i)+";\r\n");
			sb.append("\t}\r\n");
		}
	}
	private static void processAllAttrs(StringBuffer sb) {
		for (int i = 0; i < colNames.size(); i++) {
			String str = sqlType2JavaType(colTypes.get(i));
			sb.append("\tprivate "+str+" "+
					colNames.get(i)+";\r\n");
			types.add(str);
		}
	}
	private static String sqlType2JavaType(String s) {
		if(s.equalsIgnoreCase("bit") || s.equalsIgnoreCase("boolean")) {
			return "boolean";
		}else if(s.equalsIgnoreCase("tinyint") ||
				s.equalsIgnoreCase("blob") || s.equalsIgnoreCase("byte")) {
			return "byte";
		}else if(s.equalsIgnoreCase("smallint") || s.equalsIgnoreCase("short")) {
			return "short";
		}else if(s.equalsIgnoreCase("int") || s.equalsIgnoreCase("number1") || s.equalsIgnoreCase("integer")) {
			return "Integer";
		}else if(s.equalsIgnoreCase("bigint") || s.equalsIgnoreCase("long")) {
			return "long";
		}else if(s.equalsIgnoreCase("float") || s.equalsIgnoreCase("binary_float")) {
			return "Float";
		}else if(s.equalsIgnoreCase("decimal") || s.equalsIgnoreCase("numeric") ||
				s.equalsIgnoreCase("real") ||
				s.equalsIgnoreCase("double") || s.equalsIgnoreCase("number") ||
				s.equalsIgnoreCase("binary_double")) {
			return "Double";
		}else if(s.equalsIgnoreCase("money") || 
				s.equalsIgnoreCase("smallmoney")) {
			return "double";
		}else if(s.equalsIgnoreCase("varchar") || s.equalsIgnoreCase("text") || s.equalsIgnoreCase("tinytext") || 
				s.equalsIgnoreCase("char") ||
				s.equalsIgnoreCase("varchar2") ||
				s.equalsIgnoreCase("nvarchar") ||
				s.equalsIgnoreCase("nchar") || s.equalsIgnoreCase("String")){
			return "String";
		}else if(s.equalsIgnoreCase("datetime") ||
				s.equalsIgnoreCase("date") ||
				s.equalsIgnoreCase("timestamp")) {
			return "Date";
		}else if(s.equalsIgnoreCase("image") || s.equalsIgnoreCase("blob")) {
			return "Blob";
		}else if(s.equalsIgnoreCase("clob")) {
			return "Clob";
		}
		return null;
	}
}


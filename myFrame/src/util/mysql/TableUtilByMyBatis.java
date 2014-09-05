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

public class TableUtilByMyBatis {
	private boolean f_util = false;
	private boolean f_sql = false;
	private String path = "";
	private String entityPath = "";
	private String DBName = "";
	private List<TableStructure> colNames = new ArrayList<TableStructure>();
	
	public TableUtilByMyBatis() {
		super();
	}
	
	public boolean tableToClass(String DBName, String name, String physicalPath) {
		try {
			path = physicalPath;
			ParsePath(physicalPath);
			this.DBName = DBName;
			getColumns(name);
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
			getColumns(name,className);
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
			writeFile(generateEntityMapper(tableName, className), "I"+MyUtil.humpcap(className[0].toString())+"Mapper.java");
			writeFile(generateEntityXML(tableName, className), "I"+MyUtil.humpcap(className[0].toString())+"Mapper.xml");
			writeFile(generateEntityPage(tableName, className), MyUtil.humpcap(className[0].toString())+"Page.java");
			String name = MyUtil.humpcap(className[0].toString()).replace("Entity", "");
			writeFile(generateIService(tableName, className), "I"+name+"Service.java");
			writeFile(generateService(tableName, className), name+"Service.java");
		} else {
			writeFile(generateJavaString(tableName),MyUtil.humpcap(tableName)+".java");
			writeFile(generateEntityMapper(tableName),"I"+MyUtil.humpcap(tableName)+"Mapper.java");
			writeFile(generateEntityXML(tableName),"I"+MyUtil.humpcap(tableName)+"Mapper.xml");
			writeFile(generateEntityPage(tableName),MyUtil.humpcap(tableName)+"Page.java");
			String name = MyUtil.humpcap(tableName).replace("Entity", "");
			writeFile(generateIService(tableName), "I"+name+"Service.java");
			writeFile(generateService(tableName), name+"Service.java");
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
		sb.append("import java.io.Serializable;\r\n\n");
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
		}else if(s.equalsIgnoreCase("text")) {
			return "String";
		} else if(s.equalsIgnoreCase("varchar") || s.equalsIgnoreCase("tinytext") ||
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
	
	/**
	 * mapper entity
	 */
	private String generateEntityMapper(String tableName,Object... className) {
		String name = "";
//		String type = "";
//		for (int i = 0; i < colNames.size(); i++) {
//			TableTypeLength ttl = getTpye(colNames.get(i).getColumn_type().trim());
//			
//		}
		if(className.length == 0) {
			name = MyUtil.humpcap(tableName);
		} else {
			name = MyUtil.humpcap(className[0].toString().trim());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("package " + entityPath + ";\r\n\n");
		sb.append("import com.szyungu.ecmros.common.base.IBaseMapper;\r\n");
		sb.append("import " + entityPath + "." + name + ";\r\n\n");
		sb.append("public interface I"+name+"Mapper extends IBaseMapper<"+name+", Long> {\r\n\n");
		sb.append("\tInteger selectDataCount();\r\n");
		sb.append("}");
		return sb.toString();
	}
	
	private String generateIService(String tableName,Object... className) {
		String name = "";
		if(className.length == 0) {
			name = MyUtil.humpcap(tableName);
		} else {
			name = MyUtil.humpcap(className[0].toString().trim());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("package " + entityPath + ";\r\n\n");
		sb.append("import com.szyungu.ecmros.common.base.IBaseMapper;\r\n");
		sb.append("import com.szyungu.ecmros.common.base.IBaseService;\r\n");
		sb.append("import com.szyungu.ecmros.app.book.model." + name + ";\r\n");
		sb.append("import com.szyungu.ecmros.app.book.model." + name + "Page;\r\n\n");
		String name1 = name.replace("Entity", "");
		sb.append("public interface I"+name1+"Service extends IBaseService<"+name+", Long> {\r\n\n");
		sb.append("\tInteger selectDataCount();\r\n\n");
		sb.append("}");
		return sb.toString();
	}
	
	private String generateService(String tableName,Object... className) {
		String name = "";
		if(className.length == 0) {
			name = MyUtil.humpcap(tableName);
		} else {
			name = MyUtil.humpcap(className[0].toString().trim());
		}
		StringBuilder sb = new StringBuilder();
		String name1 = name.replace("Entity", "");
		sb.append("package " + entityPath + ";\r\n\n");
		sb.append("import org.springframework.beans.factory.annotation.Autowired;\r\n");
		sb.append("import com.szyungu.ecmros.common.base.IBaseMapper;\r\n");
		sb.append("import com.szyungu.ecmros.common.base.BaseServiceImpl;\r\n");
		sb.append("import com.szyungu.ecmros.app.book.model." + name + ";\r\n");
		sb.append("import com.szyungu.ecmros.app.book.model.I" + name + "Mapper;\r\n");
		sb.append("import com.szyungu.ecmros.app.book.model." + name + "Page;\r\n");
		sb.append("import com.szyungu.ecmros.app.book.model.I" + name1 + "Service;\r\n\n");
		sb.append("public class "+name1+"Service extends BaseServiceImpl<"+name+
				", Long> implements I"+name1+"Service {\r\n\n");
//		sb.append("\tInteger selectDataCount();\r\n\n");
		sb.append("\t@Autowired\r\n");
		sb.append("\tprivate I"+name+"Mapper mapper;\r\n\n");
		sb.append("\t@Override\r\n");
		sb.append("\tpublic IBaseMapper<"+name+", Long> getMapper() {\r\n");
		sb.append("\t\treturn mapper;\r\n");
		sb.append("\t}\r\n\n");
		sb.append("\t@Override\r\n");
		sb.append("\tpublic Integer selectDataCount() {\r\n");
		sb.append("\t\treturn this.mapper.selectDataCount();\r\n");
		sb.append("\t}\r\n\n");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * mapper entity.xml
	 */
	private String generateEntityXML(String tableName,Object... className) {
		String pk = "";
		String name = "";
		String attr = "";
		for (int i = 0; i < colNames.size(); i++) {
			if("PRI".equalsIgnoreCase(colNames.get(i).getColumn_key().trim())) {
				String col = colNames.get(i).getColumn_name().trim();
				pk = MyUtil.humpcap(col);
				attr = MyUtil.initsmallcap(pk);
				break;
			}
		}
		if(className.length == 0) {
			name = MyUtil.humpcap(tableName);
		} else {
			name = MyUtil.humpcap(className[0].toString().trim());
		}
		int i = entityPath.lastIndexOf(".");
		String mapperPath = entityPath.substring(0, i == -1 ? entityPath.length() : i);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
		sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\r\n");
		sb.append("<mapper namespace=\""+mapperPath+".dao.mapper.I"+name+"Mapper\" >\r\n");
		sb.append("\t<resultMap id=\"BaseResultMap\" type=\""+entityPath+"."+name+"\" >\r\n");
		generateXMLAttr(sb);//属性映射方法
		sb.append("\t</resultMap>\r\n");
		sb.append("\t<sql id=\"Base_Column_List\" >\r\n");
		generateColNames(sb);//字段方法
		sb.append("\t</sql>\r\n");
		sb.append("\t<select id=\"selectByPrimaryKey\" resultMap=\"BaseResultMap\" parameterType=\"java.lang.Long\" >\r\n");
		generateSelectByPrimaryKey(sb);//select 方法
		sb.append("\t</select>\r\n");
		sb.append("\t<select id=\"selectDataCount\" resultType=\"java.lang.Integer\">\r\n");
		generateSelectLeaveCount(sb);
		sb.append("\t</select>\r\n");
		sb.append("\t<select id=\"list\" resultMap=\"BaseResultMap\" parameterType=\""+mapperPath+".page."+name+"Page\">\r\n");
		generateList(sb,tableName);//method
		sb.append("\t</select>\r\n");
		sb.append("\t<delete id=\"deleteByPrimaryKey\" parameterType=\"java.lang.Long\" >\r\n");
		generateDeleteByPrimaryKey(sb);//deleteByPrimaryKey method
		sb.append("\t</delete>\r\n");
		sb.append("\t<insert id=\"insert\" useGeneratedKeys=\"true\" keyProperty=\""+attr+"\"  parameterType=\""+entityPath+"."+name+"\" >\r\n");
		generateInsert(sb,pk,tableName);//insert method
		sb.append("\t</insert>\r\n");
		sb.append("\t<insert id=\"insertSelective\" parameterType=\""+entityPath+"."+name+"\" >\r\n");
		generateInsertSelective(sb,tableName);//insertSelective method
		sb.append("\t</insert>\r\n");
		sb.append("\t<update id=\"updateByPrimaryKeySelective\" parameterType=\""+entityPath+"."+name+"\" >\r\n");
		generateUpdateByPrimaryKeySelective(sb,tableName);//updateByPrimaryKeySelective method
		sb.append("\t</update>\r\n");
		sb.append("\t<update id=\"updateByPrimaryKey\" parameterType=\""+entityPath+"."+name+"\" >\r\n");
		generateUpdateByPrimaryKey(sb,tableName);//updateByPrimaryKey method
		sb.append("\t</update>\r\n");
		sb.append("</mapper>");
		return sb.toString();
	}
	
	private StringBuilder generateUpdateByPrimaryKey(StringBuilder sb, String name) {
		String pk = "";
		String attr = "";
		String type = "";
		sb.append("\t\tupdate "+name+" set \r\n");
		for (int i = 0; i < colNames.size(); i++) {
			pk = pk != "" ? pk : colNames.get(i).getColumn_key().trim();
			String col = colNames.get(i).getColumn_name().trim();
			attr = MyUtil.initsmallcap(MyUtil.humpcap(col));
			type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			if("PRI".equalsIgnoreCase(pk)) {
				pk = "\t\twhere "+col+" = #{"+attr+",jdbcType="+type+"}\r\n";
			} else {
				if(i != colNames.size() - 1) {
					sb.append("\t\t\t"+col+"=#{"+attr+",jdbcType="+type+"},\r\n");
				} else {
					sb.append("\t\t\t"+col+"=#{"+attr+",jdbcType="+type+"}\r\n");
				}
			}
		}
		sb.append(pk);
		return sb;
	}
	
	private StringBuilder generateUpdateByPrimaryKeySelective(StringBuilder sb, String name) {
		String pk = "";
		String attr = "";
		String type = "";
		sb.append("\t\tupdate "+name+" \r\n");
		sb.append("\t\t<set>\r\n");
		for (int i = 0; i < colNames.size(); i++) {
			pk = pk != "" ? pk : colNames.get(i).getColumn_key().trim();
			String col = colNames.get(i).getColumn_name().trim();
			attr = MyUtil.initsmallcap(MyUtil.humpcap(col));
			type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			if("PRI".equalsIgnoreCase(pk)) {
				pk = "\t\twhere "+col+" = #{"+attr+",jdbcType="+type+"}\r\n";
			} else {
				sb.append("\t\t\t<if test=\""+attr+"!= null\" >"+col+"=#{"+attr+",jdbcType="+type+"},</if>\r\n");
			}
		}
		sb.append("\t\t</set>\r\n");
		sb.append(pk);
		return sb;
	}
	
	private StringBuilder generateInsertSelective(StringBuilder sb,String name) {
		String attr = "";
		String type = "";
		sb.append("\t\tinsert into "+name+" \r\n");
		sb.append("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >\r\n");
		StringBuilder sb1 = new StringBuilder();
		for (int i = 0; i < colNames.size(); i++) {
			String col = colNames.get(i).getColumn_name().trim();
			attr = MyUtil.initsmallcap(MyUtil.humpcap(col));
			type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			sb.append("\t\t\t<if test=\""+attr+"!= null\" >"+col+",</if>\r\n");
			sb1.append("\t\t\t<if test=\""+attr+"!= null\" >#{"+attr+",jdbcType="+type+"},</if>\r\n");
		}
		sb.append("\t\t</trim>\r\n");
		sb.append("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >\r\n");
		sb.append(sb1);
		sb.append("\t\t</trim>\r\n");
		return sb;
	}
	
	private StringBuilder generateInsert(StringBuilder sb, String pk, String name) {
		// TODO 需要找到PK主键的类型
		String attr = MyUtil.initsmallcap(MyUtil.humpcap(pk));
		String type = "";
		sb.append("\t\t<selectKey resultType=\"java.lang.Long\" order=\"AFTER\" keyProperty=\""+attr+"\">\r\n");
		sb.append("\t\t\tSELECT LAST_INSERT_ID() AS " + pk + " \r\n");
		sb.append("\t\t</selectKey>\r\n");
		sb.append("\t\tinsert into "+name+" (\r\n");
		StringBuilder sb1 = new StringBuilder();
		for (int i = 0; i < colNames.size(); i++) {
			String col = colNames.get(i).getColumn_name().trim();
			attr = MyUtil.initsmallcap(MyUtil.humpcap(col));
			type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			if(i != colNames.size() - 1) {
				sb.append("\t\t\t"+col+",\r\n");
				sb1.append("\t\t\t#{"+attr+",jdbcType="+type+"},\r\n");
			} else {
				sb.append("\t\t\t"+col+"\r\n");
				sb1.append("\t\t\t#{"+attr+",jdbcType="+type+"}\r\n");
			}
		}
		sb.append("\t\t) values (\r\n");
		sb.append(sb1);
		sb.append("\t\t)\r\n");
		return sb;
	}
	
	private StringBuilder generateDeleteByPrimaryKey(StringBuilder sb) {
		String name = "";
		String col = "";
		String attr = "";
		String type = "";
		for (int i = 0; i < colNames.size(); i++) {
			String pri = colNames.get(i).getColumn_key().trim();
			col = colNames.get(i).getColumn_name().trim();
			attr = MyUtil.initsmallcap(MyUtil.humpcap(col));
			type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			name = colNames.get(i).getTable_name();
			if("PRI".equalsIgnoreCase(pri)) break;
		}
		sb.append("\t\tdelete from "+name+" \r\n");
		sb.append("\t\twhere " + col + " = #{"+attr+",jdbcType="+type+"}\r\n");
		return sb;
	}
	
	private StringBuilder generateList(StringBuilder sb, String name) {
		sb.append("\t\tselect <include refid=\"Base_Column_List\" /> from "+name+" \r\n");
		return sb;
	}
	
	private StringBuilder generateSelectLeaveCount(StringBuilder sb) {
		String name = "";
		for (int i = 0; i < colNames.size(); i++) {
			String pk = colNames.get(i).getColumn_key().trim();
			name = colNames.get(i).getTable_name();
			if("PRI".equalsIgnoreCase(pk)) break;
		}
		sb.append("\t\tselect count(1) from "+name+"\r\n");
		return sb;
	}
	
	private StringBuilder generateSelectByPrimaryKey(StringBuilder sb) {
		String name = "";
		String col = "";
		String attr = "";
		String type = "";
		for (int i = 0; i < colNames.size(); i++) {
			String pri = colNames.get(i).getColumn_key().trim();
			col = colNames.get(i).getColumn_name().trim();
			attr = MyUtil.initsmallcap(MyUtil.humpcap(col));
			type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			name = colNames.get(i).getTable_name();
			if("PRI".equalsIgnoreCase(pri)) break;
		}
		sb.append("\t\tselect <include refid=\"Base_Column_List\" /> from "+name+" \r\n");
		sb.append("\t\twhere " + col + " = #{"+attr+",jdbcType="+type+"}\r\n");
		return sb;
	}
	
	private StringBuilder generateColNames(StringBuilder sb) {
		for (int i = 0; i < colNames.size(); i++) {
			String name = colNames.get(i).getColumn_name().trim();
			sb.append("\t\t");
			if(i != colNames.size() - 1) {
				sb.append(name+",");
			} else {
				sb.append(name);
			}
			sb.append("\r\n");
		}
		return sb;
	}
	
	private StringBuilder generateXMLAttr(StringBuilder sb) {
		for (int i = 0; i < colNames.size(); i++) {
			String pk = colNames.get(i).getColumn_key().trim();
			String name = colNames.get(i).getColumn_name().trim();
			String type = colNames.get(i).getData_type().trim().toUpperCase();
			if("INT".equalsIgnoreCase(type)) type = "INTEGER";
			String attr = MyUtil.initsmallcap(MyUtil.humpcap(name));
			if("PRI".equalsIgnoreCase(pk)) {
				sb.append("\t\t<id column= \""+name+"\" property=\""+ attr +"\" jdbcType=\""+type+"\" />\r\n");
			} else {
				sb.append("\t\t<result column= \""+name+"\" property=\""+ attr +"\" jdbcType=\""+type+"\" />\r\n");
			}
		}
		return sb;
	}
	
	private String generateEntityPage(String tableName, Object... className) {
		String name = "";
		if(className.length == 0) {
			name = MyUtil.humpcap(tableName);
		} else {
			name = MyUtil.humpcap(className[0].toString().trim());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("package " + entityPath + ";\r\n\n");
		sb.append("import com.szyungu.ecmros.common.base.PageEntity;\r\n");
		sb.append("import " + entityPath + "." + name + ";\r\n\n");
		sb.append("public class "+name+"Page extends PageEntity {\r\n\n");
		sb.append("\t//private Integer currentPage;// 当前第几页\r\n");
		sb.append("\t//private Integer pageSize;// 页面大小\r\n\n");
		sb.append("}");
		return sb.toString();
	}
}
package util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * bbs项目中针对各种实体类对象与Map之间的转换、String的各种操作、路径检查纠正等，不建议写入其它代码
 * 
 * hjp 2013-12-2 
 */
public class MyUtil {
	
	/**
	 * 实体类转换为map，但是实体类中的8种基本类型的变量不建议转换成Key:value，请单独map.put(),
	 * 或将基本类型换成其包装类型
	 */
	public static <T> Map<String, Object> castMap(T entity)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] field = entity.getClass().getDeclaredFields();
		for (Field field2 : field) {
			String fieldName = field2.getName();
			Method m = entity.getClass().getDeclaredMethod(
					"get" + initcap(fieldName));
			if (m != null) {
				Object o = m.invoke(entity);
				if (o != null && m.getReturnType() != boolean.class) {
					map.put(fieldName, o);
				}
			}
		}
		return map;
	}

	/**
	 * 将Map<String,Object>转换为实体类(测试一次通过,待继续测试）
	 */
	public static <T> T castEntity(Class<T> c, Map<String, Object> map)
			throws Exception {
		T t = c.newInstance();
		Field[] field = c.getDeclaredFields();
		for (Field f : field) {
			Type type = f.getType();
			String fieldname = f.getName();
			for (String key : map.keySet()) {
				if (fieldname.toLowerCase().equals( key.toLowerCase()) ){
					Method m = t.getClass().getDeclaredMethod(
							"set"+ initcap(fieldname), (Class<?>) type);
					if (m != null){
						if("class java.lang.String".equals(type.toString())) {
							m.invoke(t, map.get(key).toString());
						} else {
							m.invoke(t, map.get(key));
						}
					}
					break;
				}
			}
		}
		return t;
	}
	
	public static <T> List<T> castList(Class<T> c, T[] ts) {
		if(ts == null) return new ArrayList<T>();
		List<T> list = new ArrayList<T>();
		for (T t : ts) {
			list.add(t);
		}
		return list;
	}
//	public static <T> T castEntity(Class<T> c, Map<String, Object> map)
//			throws SecurityException, NoSuchMethodException,
//			InstantiationException, IllegalAccessException,
//			IllegalArgumentException, InvocationTargetException {
//		T t = c.newInstance();
//		Field[] field = c.getDeclaredFields();
//		for (Field f : field) {
//			Type type = f.getType();
//			String fieldname = f.getName();
//			for (String key : map.keySet()) {
//				if (fieldname.toLowerCase().equals( key.toLowerCase()) ){
//					String methodName = "set"+ initcap(fieldname);
//					Method m = t.getClass().getDeclaredMethod(
//							methodName, (Class<?>) type);
//					if (m != null){
//						if("class java.lang.String".equals(type.toString())) {
//							m.invoke(t, map.get(key).toString());
//						} else {
//							m.invoke(t, map.get(key));
//						}
//					}
//					break;
//				}
//			}
//		}
//		return t;
//	}

	/**
	 * 获得实体类中每个非Transient注解字段的连接串用于select * from语句中代替“*”
	 */
	public static <T> String getClassAttrs(Class<T> c) {
		Field[] field = c.getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Field f : field) {
			String name = f.getName();
			Annotation[] annotations = f.getAnnotations();
			if(!findAnnotation(annotations)) {
				i++;
				if(i == 1) {
					sb.append(name);
				} else {
					sb.append("," + name);
				}
			}
		}
		return sb.toString() == "" ? "*" : sb.toString();
	}
	
	/**
	 * 获得map中key的连接串用于select * from语句中代替“*”
	 */
	public static String getMapKeys(Map<String, Object> map) {
		if(map == null) return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : map.keySet()) {
			i++;
			if(i == 1) {
				sb.append(key);
			} else {
				sb.append("," + key);
			}
		}
		return sb.toString() == "" ? "*" : sb.toString();
	}
	
	/**
	 * 获得map中key的连接串用于select * from语句中代替“*”
	 */
	public static String getMapKeys(List<Map<String, Object>> list) {
		if(list == null || list.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : list.get(0).keySet()) {
			i++;
			if(i == 1) {
				sb.append(key);
			} else {
				sb.append("," + key);
			}
		}
		return sb.toString() == "" ? "*" : sb.toString();
	}
	
	/** 
     * 查询字段或是属性上面有没有有效annotation 
     */  
    public static boolean findAnnotation(Annotation[] annotations) {
        if (annotations.length == 0) return false;
        for (Annotation annotation : annotations) {  
        	System.out.println(annotation.toString());
            if (annotation instanceof Transient) {
            	return true;
            }
        }
        return false;
    }
	
	/**
	 * 将大写字母转换成小写
	 * 
	 * @param str
	 * @return
	 */
	public static String smallcap(String str) {
		if(str == null) return null;
		if(str == "") return "";
		char[] ch = str.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			if (ch[i] >= 'A' && ch[i] <= 'Z') {
				ch[i] = (char) (ch[i] + 32);
			}
		}
		return new String(ch);
	}

	/**
	 * 首字母大写
	 * 
	 * @param str
	 * @return
	 */
	public static String initcap(String str) {
		if(str == null) return null;
		if(str == "") return "";
		char[] ch = str.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}
		return new String(ch);
	}
	
	/**
	 * 驼峰字符串(支持"_"," ","-"连接的字符串)
	 * @param str
	 * @return
	 */
	public static String humpcap(String str) {
		if(str == null) return null;
		if(str == "") return "";
		String[] strings = str.split("_");
		if(str.contains(" ")) {
			strings = str.split(" ");
		}
		if(str.contains("-")) {
			strings = str.split("-");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			sb.append(initcap(smallcap(strings[i])));
		}
		return sb.toString();
	}

	/**
	 * 首字母小写
	 * 
	 * @param str
	 * @return
	 */
	public static String initsmallcap(String str) {
		if(str == null) return null;
		if(str == "") return "";
		char[] ch = str.toCharArray();
		if (ch[0] >= 'A' && ch[0] <= 'Z') {
			ch[0] = (char) (ch[0] + 32);
		}
		return new String(ch);
	}

	/**
	 * 对于String进行加"'"单引号，但凡是String串中有带"+","-"或已经包了“'”引号的将不会返回单引号包字符串
	 *  也可以针对日期类型进行格式化，格式化为"yyyy-MM-dd HH:mm:ss",暂不支持其它格式
	 */
	public static Object getString(Object o) {
		Object object = o;
		String typename = object.getClass().getSimpleName();
		if ("Date".equals(typename)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			object = "'" + sdf.format(o) + "'";
			return object;
		}
		int i = object.toString().trim().length();
		if (object.toString().substring(0, 1).contains("'")
				&& object.toString().substring(i - 1, i).contains("'"))
			return object.toString().trim();
		if (object.toString().contains("+") || object.toString().contains("-"))
			return object.toString().trim();
		if("".equals(object.toString().trim())) return "''";
		object.toString().substring(0, 1).replace("'", "");
		object.toString().substring(i - 1, i).replace("'", "");
		if ("String".equals(typename))
			object = "'" + object.toString().trim() + "'";
		return object;
	}
	
	/**
	 * 将一个字符串的首字母改为大写或者小写
	 * 
	 * @param srcString 源字符串
	 * @param flag 大小写标识，ture小写，false大些
	 * @return 改写后的新字符串
	 */
	public static String toLowerCaseInitial(String srcString, boolean flag) {
		StringBuilder sb = new StringBuilder();
		if (flag) {
			sb.append(Character.toLowerCase(srcString.charAt(0)));
		} else {
			sb.append(Character.toUpperCase(srcString.charAt(0)));
		}
		sb.append(srcString.substring(1));
		return sb.toString();
	}

	/**
	 * 将一个字符串按照句点（.）分隔，返回最后一段
	 * 
	 * @param clazzName 源字符串
	 * @return 句点（.）分隔后的最后一段字符串
	 */
	public static String getLastName(String clazzName) {
		String[] ls = clazzName.split("\\.");
		return ls[ls.length - 1];
	}

	/**
	 * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的"/"符号。
	 * 
	 * @param path 文件路径
	 * @return 格式化后的文件路径
	 */
	public static String formatPath(String path) {
		String reg0 = "\\\\＋";
		String reg = "\\\\＋|/＋";
		String temp = path.trim().replaceAll(reg0, "/");
		temp = temp.replaceAll(reg, "/");
		if (temp.endsWith("/")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		if (System.getProperty("file.separator").equals("\\")) {
			temp = temp.replace('/', '\\');
		}
		return temp;
	}

	/**
	 * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的"/"符号
	 * (适用于FTP远程文件路径或者Web资源的相对路径)。
	 * 
	 * @param path
	 *            文件路径
	 * @return 格式化后的文件路径
	 */
	public static String formatPath4Ftp(String path) {
		String reg0 = "\\\\＋";
		String reg = "\\\\＋|/＋";
		String temp = path.trim().replaceAll(reg0, "/");
		temp = temp.replaceAll(reg, "/");
		if (temp.endsWith("/")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		return temp;
	}

	/**
	 * 获取文件父路径
	 * 
	 * @param path 文件路径
	 * @return 文件父路径
	 */
	public static String getParentPath(String path) {
		return new File(path).getParent();
	}

	/**
	 * 获取相对路径
	 * 
	 * @param fullPath 全路径
	 * @param rootPath 根路径
	 * @return 相对根路径的相对路径
	 */
	public static String getRelativeRootPath(String fullPath, String rootPath) {
		String relativeRootPath = null;
		String _fullPath = formatPath(fullPath);
		String _rootPath = formatPath(rootPath);

		if (_fullPath.startsWith(_rootPath)) {
			relativeRootPath = fullPath.substring(_rootPath.length());
		} else {
			throw new RuntimeException("要处理的两个字符串没有包含关系，处理失败！");
		}
		if (relativeRootPath == null)
			return null;
		else
			return formatPath(relativeRootPath);
	}

	/**
	 * 获取当前系统换行符
	 * 
	 * @return 系统换行符
	 */
	public static String getSystemLineSeparator() {
		return System.getProperty("line.separator");
	}

	/**
	 * 将用“|”分隔的字符串转换为字符串集合列表，剔除分隔后各个字符串前后的空格
	 * 
	 * @param series 将用“|”分隔的字符串
	 * @return 字符串集合列表
	 */
	public static List<String> series2List(String series) {
		return series2List(series, "\\|");
	}

	/**
	 * 将用正则表达式regex分隔的字符串转换为字符串集合列表，剔除分隔后各个字符串前后的空格
	 * 
	 * @param series 用正则表达式分隔的字符串
	 * @param regex 分隔串联串的正则表达式
	 * @return 字符串集合列表
	 */
	private static List<String> series2List(String series, String regex) {
		List<String> result = new ArrayList<String>();
		if (series != null && regex != null) {
			for (String s : series.split(regex)) {
				if (s.trim() != null && !s.trim().equals(""))
					result.add(s.trim());
			}
		}
		return result;
	}

	/**
	 * @param strList 字符串集合列表
	 * @return 通过“|”串联为一个字符串
	 */
	public static String list2series(List<String> strList) {
		StringBuffer series = new StringBuffer();
		for (String s : strList) {
			series.append(s).append("|");
		}
		return series.toString();
	}

	/**
	 * 将字符串的首字母转为小写
	 * 
	 * @param resStr 源字符串
	 * @return 首字母转为小写后的字符串
	 */
	public static String firstToLowerCase(String resStr) {
		if (resStr == null) {
			return null;
		} else if ("".equals(resStr.trim())) {
			return "";
		} else {
			StringBuffer sb = new StringBuffer();
			Character c = resStr.charAt(0);
			if (Character.isLetter(c)) {
				if (Character.isUpperCase(c))
					c = Character.toLowerCase(c);
				sb.append(resStr);
				sb.setCharAt(0, c);
				return sb.toString();
			}
		}
		return resStr;
	}

	/**
	 * 将字符串的首字母转为大写
	 * 
	 * @param resStr 源字符串
	 * @return 首字母转为大写后的字符串
	 */
	public static String firstToUpperCase(String resStr) {
		if (resStr == null) {
			return null;
		} else if ("".equals(resStr.trim())) {
			return "";
		} else {
			StringBuffer sb = new StringBuffer();
			Character c = resStr.charAt(0);
			if (Character.isLetter(c)) {
				if (Character.isLowerCase(c))
					c = Character.toUpperCase(c);
				sb.append(resStr);
				sb.setCharAt(0, c);
				return sb.toString();
			}
		}
		return resStr;
	}
	
	/**
	 * 获得一个简易的Map<String,Object>对象，封装传入的一组数据
	 */
	public static Map<String, Object> getMap(String key,Object value) {
		if(key != null && key != "" && value != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(key, value);
			return map;
		} else {
			return null;
		}
	}

	/**
	 * @author liaoyq
	 * @param map
	 * @return
	 * 把map中的值转化成 sql语句中的条件字句
	 */
	public static String getSqlStringFromMap(Map<String,Object> map){//注意map中的Object类型只能是基本数据类型
		if(map==null){
			return "";
		}
		//where id =1 and name = '';
		String str = "";
		Set<String> keySet = map.keySet();
		Iterator<String> keyIt = keySet.iterator();
		while(keyIt.hasNext()){
			String key = keyIt.next();
			str += key;
			str += "=";
			Object value = map.get(key).toString();
			
			str +=value;
			if(keyIt.hasNext()){//不是最后一个
				str +=" and ";
			}
		}
		return str;
	}
	
	/**
	 * @author liaoyq
	 * @param map
	 * @return
	 * 把map中的值转化成模糊 sql语句中的条件字句
	 */
	public static String getFuzzySqlStringFromMap(Map<String,Object> map){//注意map中的Object类型只能是基本数据类型
		if(map==null){
			return "";
		}
		//where id =1 and name = '';
		String str = "";
		Set<String> keySet = map.keySet();
		Iterator<String> keyIt = keySet.iterator();
		while(keyIt.hasNext()){
			String key = keyIt.next();
			str += key;
			str = str +" like '%";
			Object value = map.get(key).toString();
			
			str +=value;
			str +="%' ";
			if(keyIt.hasNext()){//不是最后一个
				str +=" and ";
			}
		}
		return str;
	}
	
	/**
	 * 获得hibernate注解为table的name（即表名）
	 * @param c
	 * @return
	 */
	public static <T> String getTableName(Class<T> c) {
		Table table = c.getAnnotation(Table.class);
		return table.name();
	}
	
	/**
	 * 获得实体类中每个非Transient注解字段的连接串用于select * from语句中代替“*”
	 */
	public static <T> String getTableField(Class<T> c) {
		Field[] fields = c.getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Field f : fields) {
			Column col = f.getAnnotation(Column.class);
			if(f.getAnnotation(Transient.class) == null) {
				i++;
				if(i == 1) {
					if(col != null) {
						sb.append("`" + col.name() + "`");
					} else {
						sb.append("`" + f.getName() + "`");
					}
				} else {
					if(col != null) {
						sb.append(", `" + col.name() + "`");
					} else {
						sb.append(",`" + f.getName() + "`");
					}
				}
				
			}
		}
		return sb.toString() == "" ? "*" : sb.toString();
	}
	
	/**
	 * 依据传入的道具路径和key，返回对就应类型
	 * @param propertiesPath
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Class<?> getClass(String propertiesPath,String name) 
			throws IOException, ClassNotFoundException {
		Properties pr = new Properties();
		pr.load(MyUtil.class.getClassLoader().getResourceAsStream(propertiesPath));
		String classDefinde = pr.getProperty(name);
		Class<?> c = Class.forName(classDefinde);
		return c;
	}
	
	/**
	 * 暂用方法
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Class<?> getClass(String name) 
			throws IOException, ClassNotFoundException {
		Properties pr = new Properties();
		String path = "com/ecloud/flow/formmanagement/config/flow.properties";
		pr.load(MyUtil.class.getClassLoader().getResourceAsStream(path));
		String classDefinde = pr.getProperty(name);
		Class<?> c = Class.forName(classDefinde);
		return c;
	}
	
	/**
	 * 暂用方法
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static String getValue(String name) 
			throws IOException, ClassNotFoundException {
		Properties pr = new Properties();
		String path = "com/ecloud/flow/formmanagement/config/flow.properties";
		pr.load(MyUtil.class.getClassLoader().getResourceAsStream(path));
		String classDefinde = pr.getProperty(name);
		return classDefinde;
	}
}
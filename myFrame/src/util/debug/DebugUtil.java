/*
 * 文件名：DebugUtil.java	 
 * 时     间：下午5:04:05
 * 作     者：huangjp       
 * 版     权： 2014-2022  智慧园区, 公司保留所有权利.
 * 联     系：www.szyungu.com
 */
package util.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @ClassName: DebugUtil
 * @Description: (调试使用)
 * @author huangjp
 * @date 2014年3月31日 下午5:04:05
 */
public class DebugUtil {
	public static void main(String[] args) throws IOException {
//		System.out.println(getJavaFile(LeaveApplyEntity.class));
//		System.out.println(getRequestMapping(LeaveControl.class));
	}

	/**
	 * 获取实体类中属性字段及字段注释
	 * @param c
	 * @return
	 */
	public static <T> String getJavaFile(Class<T> c, HttpServletRequest... request) {
		try {
			return readFile(getPath(c, request));
		} catch (IOException e) {
			return "error";
		}
	}

	/**
	 * 获取control.java中的请求路径和注释
	 * @param c
	 * @return
	 */
	public static <T> String getRequestMapping(Class<T> c, HttpServletRequest... request) {
		StringBuilder sb = new StringBuilder();
		RequestMapping r = c.getAnnotation(RequestMapping.class);
		String basePath = getRequestPath(r);
		ClassPool pool = ClassPool.getDefault();  
		try {
			pool.insertClassPath(new ClassClassPath(DebugUtil.class));
			InputStream in = new FileInputStream(getPath(c,request));
			Map<String, String> map = getMethodComment(in);
			CtClass cc = pool.get(c.getName());
			CtMethod[] cm = cc.getDeclaredMethods();
			for (CtMethod m : cm) {
				RequestMapping rm = (RequestMapping) m.getAnnotation(RequestMapping.class);
				if(rm != null) {
					String s = getRequestPath(rm);
					for(String key : map.keySet()) {
						if(s.contains(key)) {
							sb.append("</br>\r\n</br>");
							sb.append(map.get(key));
						}
					}
					sb.append("<h3>");
					if(request.length > 0) getIP(sb, request[0]);
					sb.append(basePath);
					sb.append(s);
					sb.append(" 参数:");
					getParameterVariableName(sb, m);
					sb.append("</h3>\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	private static void getIP(StringBuilder sb,HttpServletRequest request) {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString();// 获得本机IP
//			String address = addr.getHostName().toString();// 获得本机名称
			sb.append(ip);
			sb.append(":8080");
			sb.append(request.getContextPath());
		} catch (Exception e) {
			System.out.println("Bad IP Address!" + e);
		}
	}
	
	private static <T> void getParameterVariableName(StringBuilder sb, CtMethod cm) {
		try {
			MethodInfo methodInfo = cm.getMethodInfo();  
			CodeAttribute codeAttribute = methodInfo.getCodeAttribute();  
			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);  
			if (attr == null)  {
			    //exception
			}
			CtClass[] paramTypes = cm.getParameterTypes();
			String[] paramNames = new String[cm.getParameterTypes().length];  
			int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;  
			for (int i = 0; i < paramNames.length; i++)  
			    paramNames[i] = attr.variableName(i + pos);
			merge(paramNames, paramTypes, sb);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void merge(String[] paramNames, CtClass[] paramTypes, StringBuilder sb) {
		sb.append("(");
		for (int i = 0; i < paramTypes.length; i++) {
			if (i > 0) sb.append(" , "); // 如果有多个参数，中间则用逗号隔开，否则直接打印参数
			sb.append("[");
			sb.append(paramTypes[i].getSimpleName());
			sb.append(" ");
			sb.append(paramNames[i]);
			sb.append("]");
		}
		sb.append(")");
	}

	private static String getRequestPath(RequestMapping rm) {
		if(rm == null) return "";
		StringBuilder sb = new StringBuilder();
		String[] s = rm.value();
		for (String str : s) {
			sb.append(str);
		}
		RequestMethod[] ss = rm.method();
		for (RequestMethod r : ss) {
			sb.append(" (");
			sb.append(r);
			sb.append(")");
		}
		return sb.toString();
	}
	
	private static <T> Map<String, String> getMethodComment(InputStream in) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String line; // 用来保存每行读取的内容
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		line = reader.readLine(); // 读取第一行
		StringBuilder sb = new StringBuilder();
		boolean bool = false;
		while (line != null) {
			String s1 = "";
			if (!line.trim().isEmpty()) {
				String s = line.trim();
				s1 = s.length() > 2 ? s.substring(0, 3) : s.substring(0,s.length());
			}
			if (s1.contains("/**") || s1.contains("/*") || s1.contains("//")) {
				bool = true;
				sb = new StringBuilder();
			}
			if(bool) {
				sb.append(line.trim());
				sb.append("</br>\r\n");
			}
			if (s1.contains("*/")) bool = false;
			if(!bool && line.contains("\t@RequestMapping")) {
				int i = line.indexOf(".") != -1 ? line.indexOf(".") : (
					line.indexOf(",")-1 != -2 ? line.indexOf(",")-1 : (
						line.indexOf(")")-1 != -2 ? line.indexOf(")")-1 : 0));
				String s = line.substring(line.indexOf("/"), i);
				map.put(s, sb.toString());
			} else if(bool && !line.contains("*")) bool = false; 
			line = reader.readLine(); // 读取下一行
		}
		reader.close();
		in.close();
		return map;
	}

	/**
	 * 获取文件信息
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStream in = new FileInputStream(path);
		String line; // 用来保存每行读取的内容
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		line = reader.readLine(); // 读取第一行
		boolean bool = false;
		List<String> list = new ArrayList<String>();
		List<String> list1 = new ArrayList<String>();
		StringBuilder sb1 = new StringBuilder();
		while (line != null) { // 如果 line 为空说明读完了
			String s1 = "";
			if (!line.trim().isEmpty()) {
				String s = line.trim();
				s1 = s.length() > 2 ? s.substring(0, 3) : s.substring(0,
						s.length());
			}
			if (line.contains("{"))
				list.add("{");
			if (mark(list) && (s1.contains("/**") || s1.contains("/*")))
				list1.add("1");
			if (!list1.isEmpty() && list1.size() >= 2 && can(list1)) {
				if (bool && mark(list) && !line.trim().isEmpty()
						&& !line.contains("}") && line.trim() != "") {
					sb.append(sb1);
				}
			} else if (!list1.isEmpty() && s1.contains("*")) {
				sb1.append(line.trim());
				sb1.append("</br>\r\n");
			}
			if (bool && mark(list) && !line.trim().isEmpty()
					&& !line.contains("}") && line.trim() != ""
					&& !line.contains("Override")
					&& !line.contains("serialVersionUID")) {
				sb.append(line.trim()); // 将读到的内容添加到 buffer 中
				sb.append("</br>\r\n"); // 添加换行符
			}
			if (line.contains("public class"))
				bool = true;
			if (line.contains("}"))
				list.add("}");
			if (s1.contains("*/"))
				list1.add("2");
			line = reader.readLine(); // 读取下一行
		}
		reader.close();
		in.close();
		return sb.toString();
	}

	private static boolean can(List<String> list) {
		List<String> _1 = new ArrayList<String>();
		List<String> _2 = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).contains("1"))
				_1.add(list.get(i));
			if (list.get(i).contains("2"))
				_2.add(list.get(i));
		}
		if (_1.size() == _2.size()) {
			list = new ArrayList<String>();
			return true;
		} else {
			return false;
		}
	}

	private static boolean mark(List<String> list) {
		List<String> _1 = new ArrayList<String>();
		List<String> _2 = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).contains("{"))
				_1.add(list.get(i));
			if (list.get(i).contains("}"))
				_2.add(list.get(i));
		}
		if (_1.size() - _2.size() == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取.java文件的物理路径
	 * 
	 * @param c
	 * @return
	 */
	public static <T> String getWebPath(Class<T> c, HttpServletRequest... request) {
		URL url = DebugUtil.class.getClassLoader().getResource("/");
		String str = url.getPath();
		str = str.substring(0, str.indexOf("/."));
		String projectPath = request[0].getContextPath();
		File f = new File(c.getName());
		File file = new File(str + projectPath + "/src");
		List<String> list = fileList(
				new File(file.getAbsoluteFile().toString()),
				new ArrayList<String>());
		String path = "";
		for (String s : list) {
			if (s.contains(f.toString().replace(".", "\\"))) {
				path = s.replace("\\.\\", "\\");
			}
		}
		return path.replace("\\", "/");
	}

	/**
	 * 获取.java文件的物理路径
	 * 
	 * @param c
	 * @return
	 */
	public static <T> String getPath(Class<T> c, HttpServletRequest... request) {
		File f = new File(c.getName());
		File file = new File(".\\src");
		List<String> list = fileList(
				new File(file.getAbsoluteFile().toString()),
				new ArrayList<String>());
		String path = "";
		for (String s : list) {
			if (s.contains(f.toString().replace(".", "\\"))) {
				path = s.replace("\\.\\", "\\");
			}
		}
		if(path == "") {
			path = getWebPath(c,request);
		}
		return path.replace("\\", "/");
	}

	/**
	 * 找到file下所有文件夹和文件
	 * 
	 * @param file
	 * @param list
	 * @return
	 */
	public static List<String> fileList(File file, List<String> list) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				list.add(f.getPath());
				fileList(f, list);
			}
		}
		return list;
	}
}

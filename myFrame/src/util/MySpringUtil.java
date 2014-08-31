package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

public class MySpringUtil {
	public static <T> String getClassAttrs(Class<T> c) {
		Field[] field = c.getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Field f : field) {
			String name = f.getName();
			Annotation[] annotations = f.getAnnotations();
			if (!findAnnotation(annotations)) {
				i++;
				if (i == 1)
					sb.append(name);
				else {
					sb.append("," + name);
				}
			}
		}
		return sb.toString() == "" ? "*" : sb.toString();
	}

	public static boolean findAnnotation(Annotation[] annotations) {
		if (annotations.length == 0)
			return false;
		Annotation[] arrayOfAnnotation = annotations;
		int j = annotations.length;
		for (int i = 0; i < j; i++) {
			Annotation annotation = arrayOfAnnotation[i];
			System.out.println(annotation.toString());
			if ((annotation instanceof Transient)) {
				return true;
			}
		}
		return false;
	}

	public static <T> String getTableName(Class<T> c) {
		Table table = (Table) c.getAnnotation(Table.class);
		return table.name();
	}

	public static <T> String getTableField(Class<T> c) {
		Field[] fields = c.getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Field f : fields) {
			Column col = (Column) f.getAnnotation(Column.class);
			if (f.getAnnotation(Transient.class) == null) {
				i++;
				if (i == 1) {
					if (col != null)
						sb.append("`" + col.name() + "`");
					else {
						sb.append("`" + f.getName() + "`");
					}
				} else if (col != null)
					sb.append(", `" + col.name() + "`");
				else {
					sb.append(",`" + f.getName() + "`");
				}
			}

		}

		return sb.toString() == "" ? "*" : sb.toString();
	}
}
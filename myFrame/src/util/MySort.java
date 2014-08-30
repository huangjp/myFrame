package util;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * 排序专用类,用于排序字段和升降序(目前只针对List、Map两种集合),默认为降序，若要升序，请给
 * 参数whatOrder 设置为false,可用构造函数伟入，参数type、attr必须有
 * 目前只能排日期类型和整形
 * @author huangjp 2013-12-3
 */
public class MySort implements Comparator<Object> {
	private String type;//排序的类型
	private String attr;//排序的字段
	private boolean whatOrder = true;//true为降，false为升
	
	public MySort() {
	}

	public MySort(String type, String name) {
		super();
		this.type = type;
		this.attr = name;
	}

	public MySort(String type, String name, boolean whatOrder) {
		super();
		this.type = type;
		this.attr = name;
		this.whatOrder = whatOrder;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return attr;
	}

	public void setName(String name) {
		this.attr = name;
	}

	public boolean isWhatOrder() {
		return whatOrder;
	}
	
	public void setWhatOrder(boolean whatOrder) {
		this.whatOrder = whatOrder;
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		String name = o1.getClass().getSimpleName();
		if(name != "Map") {
			int index = ListCompare(o1, o2);
			return index;
		} else {
			try {
				int index = MapCompare(MyUtil.castMap(o1), MyUtil.castMap(o2));
				return index;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			} 
		}
	}
	
	/**
	 * 针对List<T>进行排序
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int ListCompare(Object o1, Object o2) {
		Method m = null;
		try {
			m = o1.getClass().getDeclaredMethod("get" + MyUtil.initcap(attr.toString()));
			if("Date".equals(type)) {
				return myDateCompare(o1, o2, m);
			} else if("Integer".equals(type) || "int".equals(type)) {
				return myIntegerCompare(o1, o2, m);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 处理Date类型的比较
	 * @param o1
	 * @param o2
	 * @param m
	 * @return
	 * @throws Exception
	 */
	private int myDateCompare(Object o1, Object o2, Method m) throws Exception {
		Date date1 = (Date) m.invoke(o1);
		Date date2 = (Date) m.invoke(o2);
		if (whatOrder) {
			if((date1 == null && date2 == null) || (date1 != null && date2 == null)) {
				return 0;
			} else if(date1 == null && date2 != null) {
				return 1;
			}
			return  date1.getTime() > date2.getTime() ? -1
					: (date1.getTime() == date2.getTime() ? 0 : 1);
		} else {
			if((date1 == null && date2 == null) || (date1 != null && date2 == null)) {
				return 0;
			} else if(date1 == null && date2 != null) {
				return -1;
			} 
			return date1.getTime() > date2.getTime() ? 1
					: (date1.getTime() == date2.getTime() ? 0 : -1);
		}
	}
	
	/**
	 * 处理int类型的比较
	 * @param o1
	 * @param o2
	 * @param m
	 * @return
	 * @throws Exception
	 */
	private int myIntegerCompare(Object o1, Object o2, Method m) throws Exception  {
		int int1 = Integer.parseInt(m.invoke(o1).toString());
		int int2 = Integer.parseInt(m.invoke(o2).toString());
		if (whatOrder) {
			return  int1 > int2 ? -1 : (int1 == int2 ? 0 : 1);
		} else {
			return int1 > int2 ? 1 : (int1 == int2 ? 0 : -1);
		}
	}
	
	/**
	 * 针对Map类型的排序
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int MapCompare(Map<String, Object> o1, Map<String, Object> o2) {
		try {
			if("Date".equals(type)) {
				return myDateCompare(o1, o2);
			} else if("Integer".equals(type) || "int".equals(type)) {
				return myIntegerCompare(o1, o2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 处理Date类型的比较
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int myDateCompare(Map<String, Object> o1, Map<String, Object> o2){
		if(whatOrder) {
			return (Integer) o1.get(attr) > (Integer) o2.get(attr) ? -1
					: ((Integer) o1.get(attr) == (Integer) o2.get(attr) ? 0 : 1);
		} else {
			return (Integer) o1.get(attr) > (Integer) o2.get(attr) ? 1
					: ((Integer) o1.get(attr) == (Integer) o2.get(attr) ? 0 : -1);
		}
	}
	
	/**
	 * 处理 int 类型的比较
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int myIntegerCompare(Map<String, Object> o1, Map<String, Object> o2) {
		if(whatOrder) {
			return (Integer) o1.get(attr) > (Integer) o2.get(attr) ? -1
					: ((Integer) o1.get(attr) == (Integer) o2.get(attr) ? 0 : 1);
		} else {
			return (Integer) o1.get(attr) > (Integer) o2.get(attr) ? 1
					: ((Integer) o1.get(attr) == (Integer) o2.get(attr) ? 0 : -1);
		}
	}
	
	/**
	 * 静态内部类实现依据list集合中对象的id字段进行排序。依据whatOrder确定升降序(测试一次通过)
	 */
	public static class ListSortById implements Comparator<Object> {
		private boolean whatOrder = true;//true为升，false为降
		public ListSortById() {
		}
		public ListSortById(boolean whatOrder) {
			this.whatOrder = whatOrder;
		}
		@Override
		public int compare(Object o1, Object o2) {
			Method m = null;
			try {
				m = o1.getClass().getDeclaredMethod("getId");
				if (whatOrder) {
					return (Integer) m.invoke(o1) > (Integer) m.invoke(o2) ? -1
							: ((Integer) m.invoke(o1) == (Integer) m.invoke(o2) ? 0	: 1);
				} else {
					return (Integer) m.invoke(o1) > (Integer) m.invoke(o2) ? 1
							: ((Integer) m.invoke(o1) == (Integer) m.invoke(o2) ? 0	: -1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	/**
	 * 静态内部类实现依据map中的key为id的字段进行排序。依据whatOrder确定升降序(未测试)
	 */
	public static class MapSortById implements Comparator<Map<String, Object>> {
		private boolean whatOrder = true;//true为升，false为降
		public MapSortById() {
		}
		public MapSortById(boolean whatOrder) {
			this.whatOrder = whatOrder;
		}
		@Override
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			if(whatOrder) {
				return (Integer) o1.get("id") > (Integer) o2.get("id") ? -1
						: ((Integer) o1.get("id") == (Integer) o2.get("id") ? 0 : 1);
			} else {
				return (Integer) o1.get("id") > (Integer) o2.get("id") ? 1
						: ((Integer) o1.get("id") == (Integer) o2.get("id") ? 0 : -1);
			}
		}
	}
}

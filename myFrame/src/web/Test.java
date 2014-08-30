package web;

import util.mysql.MySQL;

public class Test {
	public static void main(String[] args) {
		String path = "D:/workspace/ecloud/src/main/java/cost/com/ecloud/cost/eneity/";
		MySQL.tableToClassByMyBatis("eCloud_All", "COST_PERMISSION_T", "PERMISSION_ENTITY", path);
	}
}

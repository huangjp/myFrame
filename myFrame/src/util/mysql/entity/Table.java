package util.mysql.entity;

import java.util.List;

public class Table {
	private String DBName;
	private String name;
	private List<TableStructure> tableStructures;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TableStructure> getTableStructures() {
		return tableStructures;
	}

	public void setTableStructures(List<TableStructure> ts) {
		this.tableStructures = ts;
	}

	public String getDBName() {
		return DBName;
	}

	public void setDBName(String dBName) {
		DBName = dBName;
	}
}

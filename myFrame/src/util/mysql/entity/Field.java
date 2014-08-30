package util.mysql.entity;



public class Field {
	private String field;
	private Boolean nullable;
	private String defaultValue;
	private TableTypeLength type;
	private String comment;
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public TableTypeLength getType() {
		return type;
	}
	public void setType(TableTypeLength type) {
		this.type = type;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Override
	public String toString() {
		return "Field [field=" + field + ", nullable=" + nullable
				+ ", defaultValue=" + defaultValue + ", type=" + type
				+ ", comment=" + comment + "]";
	}
}

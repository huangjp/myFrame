package util.mysql.entity;

public class TableTypeLength {
	private String type;
	private Integer precision;//精度
	private Integer scale;//大小
	private Integer length;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getPrecision() {
		return precision;
	}
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	public Integer getScale() {
		return scale;
	}
	public void setScale(Integer scale) {
		this.scale = scale;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	@Override
	public String toString() {
		return "TableTypeLength [type=" + type + ", precision=" + precision
				+ ", scale=" + scale + ", length=" + length + "]";
	}
	
}

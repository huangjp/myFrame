package base;

import java.io.Serializable;

public class BaseEntity implements Serializable {

	private static final long serialVersionUID = -780076237439125492L;

	/**
	 * 1表示正常的数据 0表示已经删除的数据
	 */
	private String isDelete = "1";

	/**
	 * 创建人
	 */
	private String createPerson;

	/**
	 * 创建日期
	 */
	private String createDate;

	/**
	 * 最后更新人
	 */
	private String lastUpdatePerson;

	/**
	 * 最后更新日期
	 */
	private String lastUpdateDate;

	/**
	 * 备注
	 */
	private String remarks;

	/**
	 * 预留字段1
	 */
	private String attribute1;

	/**
	 * 预留字段2
	 */
	private String attribute2;

	/**
	 * 预留字段3
	 */
	private String attribute3;

	/**
	 * 预留字段4
	 */
	private String attribute4;

	/**
	 * 预留字段5
	 */
	private String attribute5;

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}

	public String getCreatePerson() {
		return createPerson;
	}

	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}

	public String getLastUpdatePerson() {
		return lastUpdatePerson;
	}

	public void setLastUpdatePerson(String lastUpdatePerson) {
		this.lastUpdatePerson = lastUpdatePerson;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(String attribute1) {
		this.attribute1 = attribute1;
	}

	public String getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(String attribute2) {
		this.attribute2 = attribute2;
	}

	public String getAttribute3() {
		return attribute3;
	}

	public void setAttribute3(String attribute3) {
		this.attribute3 = attribute3;
	}

	public String getAttribute4() {
		return attribute4;
	}

	public void setAttribute4(String attribute4) {
		this.attribute4 = attribute4;
	}

	public String getAttribute5() {
		return attribute5;
	}

	public void setAttribute5(String attribute5) {
		this.attribute5 = attribute5;
	}

}

package base;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
public class PageEntity {
	
//	private static final Logger logger = Logger.getLogger(PageEntity.class);
	
	//当前第几页
	private Integer currentPage;
	
	//页面大小
	private Integer pageSize;

	//总记录数
	private Integer dataCount;

	//查询条件
	@SuppressWarnings("rawtypes")
	private Map filters;
	
	private Integer companyId;
	
	private Integer projectId;
	
	private String keyWords;
	
	//各页开始的记录数 返回0为第一条记录，1为第二条记录
	public Integer getStartRow(){
		if(getCurrentPage() == 0 || getCurrentPage() == 1) {
			return 0;
		}
		return (getCurrentPage() - 1) * getPageSize();
	}
	
	//总页数
	public Integer getPageCount(){
		Integer totalElements = getDataCount();
		Integer pageSize = getPageSize();
		int result = totalElements  % pageSize == 0 ? 
				totalElements / pageSize 
				: totalElements / pageSize + 1;
		if(result <= 1)
			result = 1;
		return result;
	}
	
	public Integer getDataCount() {
		if(dataCount == null){
//			if(logger.isDebugEnabled()){
//				logger.debug("未传入记录总数！");
//			}
		}
		return dataCount;
	}

	public void setDataCount(Integer dataCount) {
		this.dataCount = dataCount;
	}



	public PageEntity(Integer currentPage, Integer pageSize){
		this.currentPage=currentPage;
		this.pageSize=pageSize;
	}

	public PageEntity(){
	}

	public Integer getCurrentPage() {
		if(currentPage== null || currentPage == 0 ) return 1;
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getPageSize() {
		if(pageSize == null) return 15;
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	@SuppressWarnings("rawtypes")
	public Map getFilters() {
		if(filters == null) {
			filters = new HashMap(0);
		}
		return filters;
	}

	public void setFilters(@SuppressWarnings("rawtypes") Map filters) {
		this.filters = filters;
	}
	
	public void setPageAttribute(Map<String, Object> map){
		map.put("dataCount",   dataCount); 
		map.put("currentPage",   getCurrentPage()); 
		map.put("pageSize",   pageSize); 
		map.put("pageCount",   getPageCount());
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	
	

}


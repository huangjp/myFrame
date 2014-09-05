package base;

import java.util.List;

public interface IBaseMapper<T, PK> {

	// 增
	int insert(T obj);

	// 有一个增一个
	int insertSelective(T obj);

	// 删
	int deleteByPrimaryKey(PK id);

	// 改
	int updateByPrimaryKey(T obj);

	// 有一个改一个
	int updateByPrimaryKeySelective(T obj);

	// 查1个
	T selectByPrimaryKey(PK id);

	// 分页查多个
	List<T> list(PageEntity page);

	// 查表共有多少条数据
	int count();

	// 根据查询条件得到查询结果的记录总数
	int count(PageEntity page);

	List<T> queryAllEntities();
}

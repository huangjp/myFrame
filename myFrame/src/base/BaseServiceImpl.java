package base;

import java.io.Serializable;
import java.util.List;

public abstract class BaseServiceImpl<T, PK extends Serializable> implements
		IBaseService<T, PK> {

	public abstract IBaseMapper<T, PK> getMapper();

	// 增
	public int add(T obj) {
		return getMapper().insert(obj);
	}

	// 有一个增一个
	public int insertSelective(T obj) {
		return getMapper().insertSelective(obj);
	}

	// 有一个增一个 2014-4-21
	public int addSelective(T obj) {
		return getMapper().insertSelective(obj);
	}

	// 删
	public int deleteByPrimaryKey(PK id) {
		return getMapper().deleteByPrimaryKey(id);
	}

	// 改
	public int updateByPrimaryKey(T obj) {
		return getMapper().updateByPrimaryKey(obj);
	}

	// 改
	public int updateByPrimaryKeySelective(T obj) {
		return getMapper().updateByPrimaryKeySelective(obj);
	}

	// 查1个
	public T get(PK id) {
		return getMapper().selectByPrimaryKey(id);
	}

	// 查1个 2014-4-21
	public T find(PK id) {
		return getMapper().selectByPrimaryKey(id);
	}

	// 分页查多个
	public List<T> list(PageEntity page) {
		return getMapper().list(page);
	}

	public List<T> findAllEntities() {
		return getMapper().queryAllEntities();
	}

	// 查表共有多少条数据
	public int count() {
		return getMapper().count();
	}

	// 根据查询条件得到查询结果的记录总数
	public int count(PageEntity page) {
		return getMapper().count(page);
	}
}

package dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IPublicDao {

	/**
	 * 获得实例化空对象
	 * 
	 * @param c
	 * @return
	 */
	public <T> T getInstance(Class<T> c);

	/**
	 * 删除单个对象
	 * 
	 * @param c
	 * @param entity
	 */
	public <T> void deleteEntity(T entity);
	

	/**
	 * 多字段删除某个对象：map不能为空，也不能只有id字段，而其它为空； map的值类似于实体类，key对应数据
	 * 为对应字段，value对应key的值；
	 * 
	 * @param c
	 * @param map
	 */
	public <T> void deleteEntityFieldsById(Class<T> c, Map<String, Object> map);
	
	/**
	 * 多字段删除某个对象,当通过一个实体类的多个字段能确定出另一个实体类的某一个数据时，可用该方法
	 * （未测试）
	 * 
	 * @param c 要删除的类型
	 * @param entity 删除时依据的数据,可以与删除类型不一致
	 */
	public <T> void deleteEntityFieldsById(T entity);
	
	/**
	 * 批量删除，方法有误，暂未测试
	 * 
	 * @param entities
	 */
	public <T extends Collection<T>> void deleteByCollection(List<T> entities);

	/**
	 * 覆盖对象数据，并对修改的内容更新
	 * 
	 * @param c
	 * @param entity
	 */
	public <T> void updeteEntity(T entity);

	/**
	 * 更新单个对象：依据id对单个字段值进行更改,需要传入字段值和字段名
	 * 
	 * @param c
	 * @param id
	 * @param field
	 * @param fieldvalue
	 */
	public <T> void updateEntityById(Class<T> c, int id, String field,
			Object fieldvalue);

	/**
	 * 新增单个对象
	 * 
	 * @param c
	 * @param entity
	 * @return
	 */
	public <T> Serializable saveEntity(T entity);
	public <T> void saveOrUpdateEntity(T entity);
	
	/**
	 * 批量插入对象
	 * @param c
	 * @param list
	 * @return
	 */
	public <T> List<Serializable> saveEntities(List<T> list);

	/**
	 * 查询对象集合：不分页，通过类型查询
	 * 
	 * @param c
	 * @return
	 */
	public <T> List<T> getEntities(Class<T> c);

	/**
	 * 查询树结构分类列表：只能针对一张表即param c类型为实体类的数据中存在分级情况的查询，通常我们把这些类
	 * 表叫做分类表或是类别表的情况，比如表内一个字段里的两行单独的id，其中一个id为另一个id的父类。且该tab
	 * le中必须有层级关系ID（形状如"0-1-5",意思是"0-1(第一级类别的id)-5(第二级类别的id)-...依此延续"）,
	 * 而将这个ID做为 param map对象中的key=value形式传入方法中
	 * 
	 * @param c
	 *            必须传入实体类
	 * @param map
	 *            该MAP中只有两条数据，一条必须以"fields"(String类型)为key,其value =
	 *            "一个或多个字段，字段间以','号隔开", 另一条对应where关键字后的(id=1)形状的(key=value)
	 * @return 返回tierid（层级ID）所在的当前类别的所有祖先类别，每一个祖先类别入在一个MAP中，因此返回LIST
	 */
	public <T> List<Map<String, Object>> getEntityContent(Class<T> c,
			Map<String, Object> map);

	/**
	 * 查询实体类集合：依据实体类和传入的hql查询列表，需要给出hql语句
	 * 
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesByAttr(Class<T> c, String hql);

	/**
	 * 查询实体类集合：依据Map<String, Object>对象中的key value查询数据库
	 * 
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesByAttrAnd(Class<T> c, Map<String, Object> map);
	public <T> List<T> getEntitiesByAttrOr(Class<T> c, Map<String, Object> map);
	
	/**
	 * 查询实体类集合：依据list对象中的值对field字段进行and或者or的数据查询(待测试)
	 * 
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesByAttrAnd(Class<T> c, String field, List<Object> list);
	public <T> List<T> getEntitiesByAttrOr(Class<T> c, String field, List<Object> list);
	
	/**
	 * 模糊查询实体类集合：依据Map<String, Object>对象中的key value查询数据库
	 * 
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesByAttrLike(Class<T> c, Map<String, Object> map);
	
	/**
	 * 查询实体类集合：依据entity对象中给定的属性查询数据库(一个或多个属性)
	 * 
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesByAttr(T entity);
	
	/**
	 * 模糊查询实体类集合：依据entity对象中给定的属性查询数据库(一个或多个属性)
	 * 
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesByAttrLike(T entity);
	
	/**
	 * 查询实体类集合：通过ID查询对象
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesById(Class<T> c, int id);
	
	/**
	 * 查询实体类集合：通过entity中的ID查询
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> List<T> getEntitiesById(T entity);
	
	/**
	 * 多字段更新：map不能为空，也不能只有id字段，而其它为空； map的值类似于实体类，key对应数据
	 * 为对应字段，value对应key的值；
	 * 
	 * @param c
	 * @param map
	 */
	public <T> void updateEntityFieldsById(T entity);
	public <T> void updateEntityFieldsById(Class<T> c, Map<String, Object> map);

	/**
	 * 非id查询对象：当不清楚对象id，但是确可以通过其它两个或多个field确定一条数据库记录时可
	 * 以将多个字段封装成MAP形式参数并调用本方法，可以返回该条记录
	 * 多用于查询某条关联记录时通过用户ID和文档ID来确认文档与用户是否关联过。
	 * map不能为空。
	 * 
	 * @param c
	 * @param map
	 * @return
	 */
	public <T> T getEntityByAttrs(Class<T> c, Map<String, Object> map);
	
	/**
	 * 非id查询对象：当不清楚对象id，但是确可以通过其它两个或多个field确定一条数据库记录时可
	 * 以将多个字段封装成MAP形式参数并调用本方法，可以返回该条记录
	 * 多用于查询某条关联记录时通过用户ID和文档ID来确认文档与用户是否关联过。
	 * map不能为空。
	 * 
	 * @param c
	 * @param map
	 * @return
	 */
	public <T> T getEntityByAttrs(T entity);
	
	/**
	 * 序列ID查询单行记录
	 * @param c
	 * @param id
	 * @return
	 */
	public <T> T getEntityBySerializable(Class<T> c, Serializable id);
	
	/**
	 * 批量更新：要求每一个map里面都必须有且只能有一个id字段的key。最好该map都是通过实体类转型而来的
	 * (该方法未测试)
	 * 
	 * @param c
	 * @param list
	 */
	public <T> void updateEntitiesById(Class<T> c, List<Map<String, Object>> list);
	
	/**
	 * 通过实体类和传入的hql获取单个对象
	 * 
	 * @param c
	 * @param id
	 * @return
	 * @throws DataAccessException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T getEntityById(Class<T> c, int id);
	
	/**
	 * 单表分页查询
	 * @param c
	 * @param rows
	 * @param page
	 * @return
	 */
	public <T> List<T> getEntityForPage(Class<T> c, int rows, int page);
	
	/**
	 * 多表、多字段分页查询(实现方法,待测)
	 * @param cs
	 * @param rows
	 * @param page
	 * @return
	 */
//	public <T> List<T> getEntitiesForPage(final String hql, final PageModel pageModel);
	
	/**
	 * 查询数据库实体类的行数
	 * @param c
	 * @return
	 */
	public <T> int getEntityRows(Class<T> c);
	
	/**
	 * （未完成实现）
	 * @param c
	 * @param map
	 * @return
	 */
	public <T> StringBuilder getEntityTree(Class<T> c, Map<String, Object> map);
	
	/**
	 * 更新并返回更新后的当前对象
	 * @param entity
	 * @return
	 */
	public <T> T updateAndGetEntityById(T entity);
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public <T> List<Map<String,Object>> getEntitiesByJDBCAttr(String sql);
	
	/**
	 * 获得指定类在数据库表中的最大ID
	 */
	public <T> Integer getEntityMaxId(Class<T> c);
}

package dao;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Delete;

import util.DBUtil;
import util.MyUtil;

@SuppressWarnings({ "unchecked" })
public class PublicDao implements IPublicDao {
	
	@Override
	public <T> T getInstance(Class<T> c){
		try {
			return c.newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public <T> void deleteEntity(T entity){
		String tablename = entity.getClass().getSimpleName();
		try {
			Method m = entity.getClass().getDeclaredMethod("getId");
			int id = (Integer) m.invoke(entity);
			String sql = "delete from " + tablename + " where id = " + id;
			PreparedStatement ps = DBUtil.getPS(sql);
			ps.executeUpdate();
			DBUtil.closeCon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T extends Collection<T>> void deleteByCollection(List<T> entities) {
	}

	@Override
	public <T> void deleteEntityFieldsById(Class<T> c, Map<String, Object> map) {
		String name = MyUtil.smallcap(c.getSimpleName());
		StringBuilder sb = new StringBuilder();
		sb.append("delete from " + name + " where ");
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			if(count == 1) {
				sb.append(key + " = " + MyUtil.getString(map.get(key)));
			}else {
				sb.append(" and " + key + " = " + MyUtil.getString(map.get(key)));
			}
		}
		try {
			DBUtil.getPS(sb.toString()).executeUpdate();//可能返回int
		} catch (SQLException e) {
			System.out.println("PublicDao.deleteEntityFieldsById()异常");
		} finally {
			DBUtil.closeCon();
		}
	}

	@Override
	public <T> void deleteEntityFieldsById(T entity) {
		try {
			Map<String, Object> map = MyUtil.castMap(entity);
			deleteEntityFieldsById(entity.getClass(), map);
		} catch (Exception e) {}
	}

	@Override
	public <T> T getEntityById(Class<T> c, int id){
		try {
			if(id == 0) return c.newInstance();
		} catch (Exception e) {}
		String sql = "select " + MyUtil.getClassAttrs(c) + " from "+c.getSimpleName()+" where id="+id;
		PreparedStatement ps = DBUtil.getPS(sql);
		List<T> list = setT(c, ps);
		DBUtil.closeCon();
		return list.isEmpty()?null:list.get(0);
	}

	@Override
	public <T> void updeteEntity(T entity) {
		String tablename = entity.getClass().getSimpleName();
		StringBuilder sb = new StringBuilder();
		sb.append("update " + tablename + " set ");
		try {
			Map<String, Object> map = MyUtil.castMap(entity);
			int count = 0;
			for (String key : map.keySet()) {
				count++;
				if (count == 1) {
					sb.append("" + key + " = " + map.get(key));
				} else {
					sb.append("," + key + " = " + map.get(key));
				}
			}
			sb.append(" where id = " + map.get("id"));
			PreparedStatement ps = DBUtil.getPS(sb.toString());
			ps.executeUpdate();
			DBUtil.closeCon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> void updateEntityById(Class<T> c, int id, String field,
			Object fieldvalue) {
//		String name = MyUtil.smallcap(c.getSimpleName());
//		jdbcTamplate.execute(SqlConstant.updateByIdToField(name, id, field,
//				fieldvalue));
	}

	@Override
	public <T> Serializable saveEntity(T entity) {
		String tablename = entity.getClass().getSimpleName();
		String files = MyUtil.getClassAttrs(entity.getClass());
		StringBuilder sb = new StringBuilder();
		sb.append("insert into "+tablename+" ("+files+") values(");
//		sb.append(getFieldValue(entity));
		sb.append(");");
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		try {
			int id = ps.executeUpdate();
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeCon();
		}
		return null;
	}

	@Override
	public <T> void saveOrUpdateEntity(T entity) {
//		hibernateTemplate.saveOrUpdate(entity);
	}

	@Override
	public <T> List<T> getEntities(Class<T> c) {
		String tablename = c.getSimpleName();
		String sql = "select " + MyUtil.getClassAttrs(c) + " from " + tablename;
		PreparedStatement ps = DBUtil.getPS(sql);
		List<T> list = setT(c, ps);
		DBUtil.closeCon();
		return list == null?new ArrayList<T>():list;
	}

	@Override
	public <T> List<Map<String, Object>> getEntityContent(Class<T> c,
			Map<String, Object> map) {
		if(map == null) return new ArrayList<Map<String,Object>>();
		String name = MyUtil.smallcap(c.getSimpleName());
		StringBuilder sb = new StringBuilder();
		sb.append("select " + map.get("fields") + " from " + name + " where ");
		for (String key : map.keySet()) {
			String str = (String) map.get(key);
			if (!("fields".equals(key))) {
				String[] strs = str.split("-");
				sb.append("" + key + " = " + strs[0]);
				for (int i = 1; i < strs.length; i++) {
					sb.append(" or " + key + " = " + strs[i]);
				}
			}
		}
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		List<Map<String, Object>> list = setMap(c, ps);
		DBUtil.closeCon();
		return list == null?new ArrayList<Map<String,Object>>():list;
	}

	@Override
	public <T> List<T> getEntitiesByAttr(Class<T> c, String sql) {
		PreparedStatement ps = DBUtil.getPS(sql);
		List<T> list = setT(c, ps);
		DBUtil.closeCon();
		return list == null?new ArrayList<T>():list;
	}
	
	@Override
	public <T> List<T> getEntitiesByAttrAnd(Class<T> c, Map<String, Object> map) {
		if(map == null) return new ArrayList<T>();
		StringBuilder sb = new StringBuilder();
		sb.append("select " + MyUtil.getClassAttrs(c) + " from " + c.getSimpleName() + " where ");
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			if (count == 1) {
				sb.append("" + key + " = " + map.get(key));
			} else {
				sb.append(" and " + key + " = " + map.get(key));
			}
		}
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		List<T> list = setT(c, ps);
		DBUtil.closeCon();
		return list == null?new ArrayList<T>():list;
	}
	
	@Override
	public <T> List<T> getEntitiesByAttrOr(Class<T> c, Map<String, Object> map) {
		if(map == null) return new ArrayList<T>();
		StringBuilder sb = new StringBuilder();
		sb.append("select " + MyUtil.getClassAttrs(c) + " from " + c.getSimpleName() + " where ");
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			if (count == 1) {
				sb.append("" + key + " = " + map.get(key));
			} else {
				sb.append(" or " + key + " = " + map.get(key));
			}
		}
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		List<T> list = setT(c, ps);
		DBUtil.closeCon();
		return list == null?new ArrayList<T>():list;
	}

	@Override
	public <T> List<T> getEntitiesByAttrAnd(Class<T> c, String field, List<Object> list) {
		if(list == null || list.isEmpty()) return new ArrayList<T>();
		StringBuilder sb = new StringBuilder();
		sb.append("select " + MyUtil.getClassAttrs(c) + " from " + c.getSimpleName() + " where ");
		int count = 0;
		for (Object key : list) {
			count++;
			if (count == 1) {
				sb.append("" + field + " = " + key);
			} else {
				sb.append(" and " + field + " = " + key);
			}
		}
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		List<T> ts = setT(c, ps);
		DBUtil.closeCon();
		return ts == null?new ArrayList<T>():ts;
	}

	@Override
	public <T> List<T> getEntitiesByAttrOr(Class<T> c, String field, List<Object> list) {
		if(list == null || list.isEmpty()) return new ArrayList<T>();
		StringBuilder sb = new StringBuilder();
		sb.append("select " + MyUtil.getClassAttrs(c) + " from " + c.getSimpleName() + " where ");
		int count = 0;
		for (Object key : list) {
			count++;
			if (count == 1) {
				sb.append("" + field + " = " + key);
			} else {
				sb.append(" or " + field + " = " + key);
			}
		}
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		List<T> ts = setT(c, ps);
		DBUtil.closeCon();
		return ts == null?new ArrayList<T>():ts;
	}

	@Override
	public <T> List<T> getEntitiesById(Class<T> c, int id) {
		String sql = "select " + MyUtil.getClassAttrs(c) + " from "+c.getSimpleName()+" where id="+id;
		PreparedStatement ps = DBUtil.getPS(sql);
		List<T> ts = setT(c, ps);
		DBUtil.closeCon();
		return ts == null?new ArrayList<T>():ts;
	}

	@Override
	public <T> List<T> getEntitiesById(T entity) {
		if(entity == null) new ArrayList<T>();
		try {
			Method m = entity.getClass().getMethod("getId");
			int id = (Integer) m.invoke(entity);
			String sql = "select " + MyUtil.getClassAttrs(entity.getClass()) + 
					"from "+entity.getClass().getSimpleName()+" where id="+id;
			PreparedStatement ps = DBUtil.getPS(sql);
			List<T> ts = (List<T>) setT(entity.getClass(), ps);
			DBUtil.closeCon();
			return ts == null?new ArrayList<T>():ts;
		} catch (Exception e) {
			return new ArrayList<T>();
		}
	}

	@Override
	public <T> void updateEntityFieldsById(T entity) {
		try {
			Map<String, Object> map = MyUtil.castMap(entity);
			updateEntityFieldsById(entity.getClass(), map);
		} catch (Exception e) {}
	}

	@Override
	public <T> void updateEntityFieldsById(Class<T> c, Map<String, Object> map) {
		try {
			String name = MyUtil.smallcap(c.getSimpleName());
			StringBuilder sb = new StringBuilder();
			sb.append("update " + name + " set ");
			int count = 0;
			for (String key : map.keySet()) {
				if ("id".equals(key)) continue;
				count++;
				if(count == 1) {
					sb.append("" + key + " = " + MyUtil.getString(map.get(key)));
				} else {
					sb.append("," + key + " = " + MyUtil.getString(map.get(key)));
				}
			}
			sb.append(" where id = " + map.get("id"));
//			jdbcTamplate.execute(sb.toString());
		} catch (Exception e) {}
	}

	@Override
	public <T> T getEntityByAttrs(Class<T> c, Map<String, Object> map) {
		try {
			if(map == null) return c.newInstance();
		} catch (Exception e) {} 
		StringBuilder sb = new StringBuilder();
		sb.append("from " + c.getSimpleName() + " where ");
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			if (count == 1) {
				sb.append("" + key + " = " + map.get(key));
			} else {
				sb.append(" and " + key + " = " + map.get(key));
			}
		}
		PreparedStatement ps = DBUtil.getPS(sb.toString());
		List<T> ts = setT(c, ps);
		DBUtil.closeCon();
		return ts == null?null:ts.get(0);
	}

	@Override
	public <T> void updateEntitiesById(Class<T> c,List<Map<String, Object>> list) {
		String name = MyUtil.smallcap(c.getSimpleName());
		for (int i = 0; i < list.size(); i++) {
			StringBuilder sb = new StringBuilder();
			Map<String, Object> map = list.get(i);
			sb.append("update " + name + " set ");
			int count = 0;
			for (String key : map.keySet()) {
				count++;
				if (count == 1) {
					sb.append("" + key + " = " + map.get(key));
				} else {
					sb.append("," + key + " = " + map.get(key));
				}
			}
			sb.append(" where id = " + map.get("id"));
//			jdbcTamplate.execute(sb.toString());
		}
	}

	@Override
	public <T> List<Serializable> saveEntities(List<T> list) {
		List<Serializable> ser = new ArrayList<Serializable>();
//		for (int i = 0; i < list.size(); i++) {
//			Serializable s = hibernateTemplate.save(list.get(i));
//			if(Integer.parseInt(s.toString()) > 0) ser.add(s);
//		}
//		hibernateTemplate.saveOrUpdateAll(list);
		return ser;
	}

	@Override
	public <T> T getEntityBySerializable(Class<T> c, Serializable id) {
		try {
			if(id == null) return c.newInstance();
		} catch (Exception e) {}
//		return this.hibernateTemplate.get(c, id);
		return null;
	}

	@Override
//	@Transactional
	public <T> List<T> getEntityForPage(Class<T> c, int rows, int page) {
//		Session session = hibernateTemplate.getSessionFactory().getCurrentSession();
//		Query query = session.createQuery("from " + MyUtil.smallcap(c.getSimpleName())); 
//		query.setFirstResult((page - 1) * rows);
//		query.setMaxResults(rows);
//		HibernateSessionFactory.closeSession();
//		return query.list();
		return null;
	}

//	@Override
//	public <T> List<T> getEntitiesForPage(final String hql,
//			final PageModel pageModel) {
//		List<T> list = this.hibernateTemplate.executeFind(new HibernateCallback<Object>() {
//			public Object doInHibernate(Session session)
//					throws HibernateException, SQLException {
//				Query query = session.createQuery(hql);
//				Integer pageNow = pageModel.getPageNow() - 1;
//				query.setFirstResult(pageNow * pageModel.getPageSize());
//				query.setMaxResults(pageModel.getPageSize());
//				List<T> list = query.list();
//				return list;
//			}
//		});
//		return list;
//	}

	@Override
	public <T> int getEntityRows(Class<T> c) {
		
		return 0;
	}

	@Override
	public <T> StringBuilder getEntityTree(Class<T> c, Map<String, Object> map) {
		if(map == null) return new StringBuilder();
		String name = MyUtil.smallcap(c.getSimpleName());
		StringBuilder sb = new StringBuilder();
		sb.append("select " + map.get("fields") + " from " + name + " where ");
		for (String key : map.keySet()) {
			String str = (String) map.get(key);
			if (!("fields".equals(key))) {
				String[] strs = str.split("-");
				sb.append("" + key + " = " + strs[0]);
				for (int i = 1; i < strs.length; i++) {
					sb.append(" or " + key + " = " + strs[i]);
				}
			}
		}
		return sb;
	}

	@Override
	public <T> T updateAndGetEntityById(T entity) {
		updateEntityFieldsById(entity);
		try {
			Method method = entity.getClass().getMethod("getId");
			Integer id = (Integer) method.invoke(entity);
			return (T) getEntityById(entity.getClass(), id);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public <T> List<T> getEntitiesByAttr(T entity) {
		if(entity == null) return new ArrayList<T>();
		try {
			return (List<T>)getEntitiesByAttrAnd(
					entity.getClass(), MyUtil.castMap(entity));
		} catch (Exception e) {
			return new ArrayList<T>();
		}
	}

	@Override
	public <T> T getEntityByAttrs(T entity) {
		try {
			Map<String, Object> map = MyUtil.castMap(entity);
			return (T) getEntityByAttrs(entity.getClass(), map);
		} catch (Exception e) {
			return entity;
		}
	}

	@Override
	public <T> List<Map<String, Object>> getEntitiesByJDBCAttr(String sql) {
//		return jdbcTamplate.queryForList(sql);
		return null;
	}

	@Override
	public <T> List<T> getEntitiesByAttrLike(Class<T> c, Map<String, Object> map) {
		if(map == null) return new ArrayList<T>();
		StringBuilder sb = new StringBuilder();
		String names = MyUtil.getClassAttrs(c);
		sb.append("select " + names + " from " + MyUtil.smallcap(c.getSimpleName()) + " where ");
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			if (count == 1) {
				sb.append("" + key + " like '%" + map.get(key) + "%'");
			} else {
				sb.append(" and " + key + " like '%" + map.get(key) + "%'");
			}
		}
//		List<Map<String, Object>> mList = (List<Map<String,Object>>)jdbcTamplate.queryForList(sb.toString());
		List<T> list = new ArrayList<T>();
//		for (int i = 0; i < mList.size(); i++) {
//			try {
//				list.add(MyUtil.castEntity(c, mList.get(i)));
//			} catch (Exception e) {System.out.println("异常了");}
//		}
		return list;
	}

	@Override
	public <T> List<T> getEntitiesByAttrLike(T entity) {
		try {
			return (List<T>) getEntitiesByAttrLike(
					entity.getClass(), MyUtil.castMap(entity));
		} catch (Exception e) { return new ArrayList<T>();}
	}

	@Override
	public <T> Integer getEntityMaxId(Class<T> c) {
//		String sql = "select max(t.id) from " + MyUtil.smallcap(c.getSimpleName()) + " as t";
//		return jdbcTamplate.queryForInt(sql);
		return null;
	}
	
	/**
	 * 处理查询数据库后的返回值并封装进实体类
	 * @param c
	 * @param ps
	 * @return
	 */
	private <T> List<T> setT(Class<T> c,PreparedStatement ps) {
		try {
			ResultSet rs = ps.executeQuery();
			List<T> list = new ArrayList<T>();
			while (rs.next()) {
				T t = c.newInstance();
				Field[] fields = t.getClass().getDeclaredFields();
				for (Field f : fields) {
					Type type = f.getType();
					String fieldName = f.getName();
					String methodName = MyUtil.initcap(fieldName);
					Method m = t.getClass().getDeclaredMethod("set" + methodName, (Class<?>)type);
					if(m != null) {
						m.invoke(t, rs.getString(fieldName));
					}
				}
				list.add(t);
			}
			return list;
		} catch (Exception e) {
			return null;
		}
	}
	
	public <T> Boolean alterTable(Class<T> c) {
		try {
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				Annotation an = f.getAnnotation(Delete.class);
				if(an == null) {
				} else {
					
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 处理查询数据库后的返回值并封装进实体类
	 * @param c
	 * @param ps
	 * @return
	 */
	private <T> List<Map<String, Object>> setMap(Class<T> c,PreparedStatement ps) {
		try {
			ResultSet rs = ps.executeQuery();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				T t = c.newInstance();
				Field[] fields = t.getClass().getDeclaredFields();
				for (Field f : fields) {
					map.put(f.getName(), rs.getString(f.getName()));
				}
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			return null;
		}
	}
	
//	private <T> boolean updateT(T t, PreparedStatement ps) {
//		try {
//			String sql = "update " + MyUtil.smallcap(t.getClass().getSimpleName()) + " set ";
//			Field[] fields = t.getClass().getDeclaredFields();
//			for (Field f : fields) {
//				Type type = f.getType();
//				String methodName = MyUtil.initcap(f.getName());
//				Method m = t.getClass().getDeclaredMethod("get" + methodName);
//				Object o = m.invoke(t);
//				if(o != null) {
//					if(fields.length > 1) {
//						sql += f.getName() + " = ?";
//					} else {
//						sql += "," + f.getName() + " = ?";
//					}
//				}
//				ps.setObject(0, o);
//			}
//			return true;
//		} catch (Exception e) {
//			return false;
//		}
//	}
//	
//	private <T> StringBuilder getFieldValue(T entity) {
//		Field[] fields = entity.getClass().getDeclaredFields();
//		StringBuilder sb = new StringBuilder();
//		for (Field f : fields) {
//			String name = f.getName();
//			Type type = f.getType();
//			try {
//				Method m = entity.getClass().getDeclaredMethod("get" + MyUtil.initcap(name));
//				Object o = m.invoke(entity);
//				if(!"id".equals(name)) {
//					if(fields.length == 1) {
//						sb.append(o);
//					} else {
//						sb.append("," + o);
//					}
//				}
//			} catch (Exception e) {
//				return null;
//			} 
//		}
//		return sb;
//	}
}


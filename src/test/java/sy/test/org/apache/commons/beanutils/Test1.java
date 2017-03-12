package sy.test.org.apache.commons.beanutils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.beanutils.BeanUtils;

import sy.model.base.Syresource;

public class Test1 {

	public static void main(String[] args) throws IllegalAccessException, InvocationTargetException {
		Syresource r = new Syresource();
		Field[] fields = Syresource.class.getDeclaredFields();
		for (Field field : fields) {
			System.out.println(field);
			System.out.println(field.getClass());
		}
		Map m = new HashMap();
		m.put("name", "测试");
		m.put("syresourcetype.name", "类型");
		//BeanUtils.populate(r, m);
		System.out.println(r.getName());
		System.out.println(r.getSyresourcetype().getName());
	}

}

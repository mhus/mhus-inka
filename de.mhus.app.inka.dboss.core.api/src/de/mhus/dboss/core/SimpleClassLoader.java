package de.mhus.dboss.core;

public class SimpleClassLoader implements ILoader {

	@Override
	public Object newInstance(String name, Object... objects) throws DBossException {
		
		try {
			Class<?>[] classes = new Class<?>[objects.length];
			
			for (int i = 0; i < objects.length; i++)
				classes[i] = findInterface(objects[i].getClass());
			
			Object obj = Class.forName(name).getConstructor(classes).newInstance(objects);
			return obj;
		} catch (Exception e) {
			throw new DBossException(e);
		}
	}

	private Class<?> findInterface(Class<?> clazz) {
		
		for (Class<?> interf : clazz.getInterfaces() ) {
			if (interf.getSimpleName().startsWith("I"))
				return interf;
		}
		
		return clazz;
	}

}

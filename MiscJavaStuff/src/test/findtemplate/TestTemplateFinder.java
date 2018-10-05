package test.findtemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class TestTemplateFinder {

	public static void main(String[] args) {
		Class<?> testy = StringValue.class;
		System.out.println( getTemplateCanonicalName(testy, 0) );
		System.out.println( getTemplateCanonicalName(testy, 1) );
		System.out.println( getTemplateCanonicalName( new Template<Integer>().getClass(), 0 ) );
		System.out.println( getTemplateCanonicalName( (new Template<Integer>() {}).getClass(), 0 ) );
		System.out.println( getTemplateCanonicalName(String.class, 0) );
	}

	// https://stackoverflow.com/questions/3437897/how-to-get-a-class-instance-of-generics-type-t#3437930
	private static String getTemplateCanonicalName(Class<?> clazz, int index) {
		Type mySuperclass = clazz.getGenericSuperclass();
		if (mySuperclass instanceof ParameterizedType) {
			Type[] templates = ((ParameterizedType)mySuperclass).getActualTypeArguments();
			if (index >= templates.length) return null;
			Type tType = templates[index];
			String templName = tType.getTypeName();
			return templName;
		}
		return null;
	}

}

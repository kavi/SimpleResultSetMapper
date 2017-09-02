package dk.javacode.srsm.util;

import java.lang.reflect.Field;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.javacode.srsm.exceptions.MappingException;

public class ReflectionUtil {
	
	private static Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

	public static Field getField(Class<?> clazz, String name) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			if (name.equalsIgnoreCase(f.getName())) {
				return f;
			}
		}
		log.debug("No field with name: " + name + " on class " + clazz.getName());
		return null;
	}
	
	public static <E> Object invokeMethod(E target, Method method) throws MappingException {
		try {
			return method.invoke(target, new Object[] { });
		} catch (IllegalArgumentException e) {
			throw new MappingException("Unable to invoke method (" + method.getName() + ") with no arguments.", e);
		} catch (IllegalAccessException e) {
			throw new MappingException("Method (" + method.getName() + ") not accessible.", e);
		} catch (InvocationTargetException e) {
			throw new MappingException("Error in method: " + method.getName(), e);
		}
	}
	
	public static <E> void invokeMethod(E target, Method method, Object value) throws MappingException {
		try {
			method.invoke(target, new Object[] { value });
		} catch (IllegalArgumentException e) {
			throw new MappingException("Unable to invoke method (" + method.getName() + ") with arguments: " + value, e);
		} catch (IllegalAccessException e) {
			throw new MappingException("Method (" + method.getName() + ") not accessible.", e);
		} catch (InvocationTargetException e) {
			throw new MappingException("Error in method: " + method.getName(), e);
		}
	}
	
	public static Method getSetMethodForField(Class<?> clazz, Field field) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equalsIgnoreCase("set" + field.getName())) {
				return m;
			}
		}
		return null;
	}
	
	public static Method getAddMethodForCollection(Class<?> clazz, Field field) {
		String fieldName = field.getName();
		String addName = "add" + fieldName;
		String addNameShort = "add" + fieldName.substring(0, fieldName.length() - 1);
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if ((m.getName().equalsIgnoreCase(addNameShort) || m.getName().equalsIgnoreCase(addName)) && m.getParameterTypes().length == 1) {
				return m;
			}
		}
		return null;
	}
	
	public static Method getGetMethodForField(Class<?> clazz, Field field) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equalsIgnoreCase("get" + field.getName()) || m.getName().equalsIgnoreCase("is" + field.getName()) || m.getName().equalsIgnoreCase("has" + field.getName())) {
				return m;
			}
		}
		return null;
	}

	public static <E> E getNewInstance(Class<E> clazz) throws MappingException {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			// Consider catching the individual exceptions - then again. The causing exception will be in the stacktrace...
			throw new MappingException("Unable to initialize class: " + clazz.getName(), e);
		}
	}
}

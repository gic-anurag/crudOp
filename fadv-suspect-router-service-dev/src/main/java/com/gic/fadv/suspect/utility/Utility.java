package com.gic.fadv.suspect.utility;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class Utility {

	private static final Logger logger = LoggerFactory.getLogger(Utility.class);

	public static void printGettersSetters(Class<?> aClass) {
		Method[] methods = aClass.getMethods();

		for (Method method : methods) {
			if (isGetter(method))
				logger.info(Marker.ANY_MARKER,"getter: ", method);

			if (isSetter(method))
				logger.info(Marker.ANY_MARKER,"setter: ", method);
		}
	}

	public static boolean isGetter(Method method) {
		if (!method.getName().startsWith("get"))
			return false;
		if (method.getParameterTypes().length != 0)
			return false;
		if (void.class.equals(method.getReturnType()))
			return false;
		return true;
	}

	public static boolean isSetter(Method method) {
		if (!method.getName().startsWith("set"))
			return false;
		if (method.getParameterTypes().length != 1)
			return false;
		return true;
	}
}

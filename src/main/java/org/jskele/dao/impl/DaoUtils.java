package org.jskele.dao.impl;

import org.jskele.dao.impl.params.ParameterExtractor;
import org.springframework.core.ResolvableType;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class DaoUtils {
    public static boolean isBean(Class<?> paramType) {
        return beanConstructor(paramType) != null;
    }

    public static String[] beanProperties(Class<?> paramType) {
        Constructor<?> constructor = beanConstructor(paramType);
        if (constructor == null) {
            return null;
        }

        return constructor.getAnnotation(ConstructorProperties.class).value();
    }

    public static Constructor<?> beanConstructor(Class<?> beanClass) {
        Constructor<?>[] constructors = beanClass.getConstructors();

        Constructor<?> found = null;

        for (Constructor<?> constructor : constructors) {
            ConstructorProperties annotation = constructor.getAnnotation(ConstructorProperties.class);
            if (annotation == null) {
                continue;
            }

            if (found != null) {
                return null;
            }

            found = constructor;
        }

        return found;
    }

    public static Class<?> rowClass(Method method, Class<?> daoClass) {
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method, daoClass);

        if (resolvableType.hasGenerics()) {
            ResolvableType[] generics = resolvableType.getGenerics();
            checkArgument(generics.length == 1);
            return generics[0].resolve();
        }

        return resolvableType.resolve();
    }

    public static Class<?> beanClass(Method method, Class<?> daoClass) {
        if (method.getParameterCount() != 1) {
            return null;
        }

        ResolvableType resolvableType = ResolvableType.forMethodParameter(method, 0, daoClass);

        if (resolvableType.hasGenerics()) {
            ResolvableType[] generics = resolvableType.getGenerics();
            checkArgument(generics.length == 1);
            return beanClass(generics[0].resolve());
        }

        return beanClass(resolvableType.resolve());
    }

    private static Class<?> beanClass(Class<?> potentialBeanClass) {
        if (isBean(potentialBeanClass)) {
            return potentialBeanClass;
        }

        return null;
    }

    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        return method.getAnnotation(annotationClass) != null;
    }

    public static boolean hasReturnType(Method method, Class<?> returnType) {
        return method.getReturnType().equals(returnType);
    }

    public static Map<String, Object> getParamValuesByName(Object[] args, ParameterExtractor extractor) {
        String[] paramNames = extractor.names();
        Object[] paramValues = extractor.values(args);
        // Not using
        // `IntStream.range(0, paramNames.length).boxed().collect(toMap(i -> paramNames[i], i -> paramValues[i]));`
        // as it would throw NPE when value is null
        Map<String, Object> paramValuesByName = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramValuesByName.put(paramNames[i], paramValues[i]) != null) {
                throw new IllegalStateException("Duplicate parameter name!");
            }
        }
        return paramValuesByName;
    }
}

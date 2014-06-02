package com.owlike.genson.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RenamingPropertyNameResolver implements PropertyNameResolver {

    private final String field;
    private final Class<?> fromClass;
    private final Class<?> ofType;
    private final String toName;

    public RenamingPropertyNameResolver(String field, Class<?> fromClass, Class<?> ofType, String toName) {
        this.field = field;
        this.fromClass = fromClass;
        this.ofType = ofType;
        this.toName = toName;
    }

    @Override
    public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
        return null;
    }

    @Override
    public String resolve(int parameterIdx, Method fromMethod) {
        return null;
    }

    @Override
    public String resolve(Field fromField) {
        return tryToRename(fromField.getName(), fromField.getDeclaringClass(),
                fromField.getType());
    }

    @Override
    public String resolve(Method fromMethod) {
        String name = fromMethod.getName();
        if (name.startsWith("is") && name.length() > 2) {
            return tryToRename(name.substring(2), fromMethod.getDeclaringClass(),
                    fromMethod.getReturnType());
        }
        if (name.length() > 3) {
            if (name.startsWith("get"))
                return tryToRename(name.substring(3), fromMethod.getDeclaringClass(),
                        fromMethod.getReturnType());
            if (name.startsWith("set") && fromMethod.getParameterTypes().length == 1)
                return tryToRename(name.substring(3), fromMethod.getDeclaringClass(),
                        fromMethod.getParameterTypes()[0]);
        }
        return null;
    }

    private String tryToRename(String actualName, Class<?> declaringClass,
                               Class<?> propertyType) {
        if ((field == null || actualName.equalsIgnoreCase(field))
                && (fromClass == null || fromClass.isAssignableFrom(declaringClass))
                && (ofType == null || ofType.isAssignableFrom(propertyType)))
            return toName;
        return null;
    }
}

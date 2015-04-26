package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.owlike.genson.*;
import com.owlike.genson.stream.ObjectWriter;

public abstract class PropertyAccessor extends BeanProperty implements Comparable<PropertyAccessor> {
  Serializer<Object> propertySerializer;

  protected PropertyAccessor(String name, Type type, Class<?> declaringClass, Class<?> concreteClass,
                             Annotation[] annotations, int modifiers) {
    super(name, type, declaringClass, concreteClass, annotations, modifiers);
  }

  public void serialize(Object propertySource, ObjectWriter writer, Context ctx) {
    Object propertyValue = access(propertySource);
    writer.writeName(name);
    try {
      propertySerializer.serialize(propertyValue, writer, ctx);
    } catch (Throwable th) {
      throw couldNotSerialize(th);
    }
  }

  public abstract Object access(final Object target);

  public int compareTo(PropertyAccessor o) {
    return o.priority() - priority();
  }

  protected JsonBindingException couldNotAccess(Exception e) {
    return new JsonBindingException("Could not access value of property named '"
      + name + "' using accessor " + signature() + " from class "
      + declaringClass.getName(), e);
  }

  protected JsonBindingException couldNotSerialize(Throwable e) {
    return new JsonBindingException("Could not serialize property '" + name
      + "' from class " + declaringClass.getName(), e);
  }

  public static class MethodAccessor extends PropertyAccessor {
    protected final Method _getter;

    public MethodAccessor(String name, Method getter, Type type, Class<?> concreteClass) {
      super(name, type, getter.getDeclaringClass(), concreteClass, getter.getAnnotations(), getter.getModifiers());
      this._getter = getter;
      if (!_getter.isAccessible()) {
        _getter.setAccessible(true);
      }
    }

    @Override
    public Object access(final Object target) {
      try {
        return _getter.invoke(target);
      } catch (IllegalArgumentException e) {
        throw couldNotAccess(e);
      } catch (IllegalAccessException e) {
        throw couldNotAccess(e);
      } catch (InvocationTargetException e) {
        throw couldNotAccess(e);
      }
    }

    @Override
    String signature() {
      return _getter.toGenericString();
    }

    @Override
    int priority() {
      return 100;
    }
  }

  public static class FieldAccessor extends PropertyAccessor {
    protected final Field _field;

    public FieldAccessor(String name, Field field, Type type, Class<?> concreteClass) {
      super(name, type, field.getDeclaringClass(), concreteClass, field.getAnnotations(), field.getModifiers());
      this._field = field;
      if (!_field.isAccessible()) {
        _field.setAccessible(true);
      }
    }

    @Override
    public Object access(final Object target) {
      try {
        return _field.get(target);
      } catch (IllegalArgumentException e) {
        throw couldNotAccess(e);
      } catch (IllegalAccessException e) {
        throw couldNotAccess(e);
      }
    }

    @Override
    public String signature() {
      return _field.toGenericString();
    }

    @Override
    public int priority() {
      return 50;
    }
  }
}

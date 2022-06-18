package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.owlike.genson.*;
import com.owlike.genson.stream.ObjectReader;

public abstract class PropertyMutator extends BeanProperty implements Comparable<PropertyMutator> {
  Deserializer<Object> propertyDeserializer;

  protected PropertyMutator(String name, Type type, Class<?> declaringClass, Class<?> concreteClass,
                            Annotation[] annotations, int modifiers) {
    super(name, type, declaringClass, concreteClass, annotations, modifiers);
  }

  public Object deserialize(ObjectReader reader, Context ctx) {
    try {
      return propertyDeserializer.deserialize(reader, ctx);
    } catch (Throwable th) {
      throw couldNotDeserialize(th);
    }
  }

  public void deserialize(Object into, ObjectReader reader, Context ctx) {
    Object propValue = null;
    try {
      propValue = propertyDeserializer.deserialize(reader, ctx);
    } catch (Throwable th) {
      throw couldNotDeserialize(th);
    }
    mutate(into, propValue);
  }

  public abstract void mutate(Object target, Object value);

  public int compareTo(PropertyMutator o) {
    return o.priority() - priority();
  }

  protected JsonBindingException couldNotMutate(Exception e) {
    return new JsonBindingException("Could not mutate value of property named '"
      + name + "' using mutator " + signature(), e);
  }

  protected JsonBindingException couldNotDeserialize(Throwable e) {
    return new JsonBindingException("Could not deserialize to property '" + name + "' of class " + declaringClass, e);
  }

  public static class MethodMutator extends PropertyMutator {
    protected final Method _setter;
    protected boolean accessible = true;

    public MethodMutator(String name, Method setter, Type type, Class<?> concreteClass) {
      super(name, type, setter.getDeclaringClass(), concreteClass, setter.getAnnotations(), setter.getModifiers());
      this._setter = setter;
      if (!_setter.isAccessible()) {
        try{
          _setter.setAccessible(true);
        }
        catch(Exception e){
          accessible = false;
        }
      }
    }

    @Override
    public void mutate(Object target, Object value) {
      try {
        if(accessible){
          _setter.invoke(target, value);
        }
      } catch (IllegalArgumentException e) {
        throw couldNotMutate(e);
      } catch (IllegalAccessException e) {
        throw couldNotMutate(e);
      } catch (InvocationTargetException e) {
        throw couldNotMutate(e);
      }
    }

    @Override
    public String signature() {
      return _setter.toGenericString();
    }

    @Override
    public int priority() {
      return 100;
    }
  }

  public static class FieldMutator extends PropertyMutator {
    protected final Field _field;
    protected boolean accessible = true;

    public FieldMutator(String name, Field field, Type type, Class<?> concreteClass) {
      super(name, type, field.getDeclaringClass(), concreteClass, field.getAnnotations(), field.getModifiers());
      this._field = field;
      if (!_field.isAccessible()) {
        try{
          _field.setAccessible(true);
        }
        catch(Exception e){
          accessible = false;
        }
      }
    }

    @Override
    public void mutate(Object target, Object value) {
      try {
        if(accessible){
          _field.set(target, value);
        }
      } catch (IllegalArgumentException e) {
        throw couldNotMutate(e);
      } catch (IllegalAccessException e) {
        throw couldNotMutate(e);
      }
    }

    @Override
    public String signature() {
      return _field.toGenericString();
    }

    @Override
    public int priority() {
      return 0;
    }
  }
}

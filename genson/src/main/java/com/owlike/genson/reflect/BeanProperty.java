package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a bean property, in practice it can be an object field, method (getter/setter) or
 * constructor parameter.
 *
 * @author eugen
 */
public abstract class BeanProperty {
  protected final String name;
  protected final Type type;
  protected final Class<?> declaringClass;
  protected final Class<?> concreteClass;
  protected Annotation[] annotations;
  protected final int modifiers;

  protected BeanProperty(String name, Type type, Class<?> declaringClass,
                         Class<?> concreteClass, Annotation[] annotations, int modifiers) {
    this.name = name;
    this.type = type;
    this.declaringClass = declaringClass;
    this.concreteClass = concreteClass;
    this.annotations = annotations;
    this.modifiers = modifiers;
  }

  /**
   * @return The class in which this property is declared
   */
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  /**
   * @return The final concrete class from which this property has been resolved.
   * For example if this property is defined in class Root but was resolved for class Child extends Root,
   * then getConcreteClass would return Child class and getDeclaringClass would return Root class.
   */
  public Class<?> getConcreteClass() { return concreteClass; }

  /**
   * The name of this property (not necessarily the original one).
   */
  public String getName() {
    return name;
  }

  /**
   * @return the type of the property
   */
  public Type getType() {
    return type;
  }

  public Class<?> getRawClass() {
    return TypeUtil.getRawClass(type);
  }

  public int getModifiers() {
    return modifiers;
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    for (Annotation ann : annotations)
      if (annotationClass.isInstance(ann)) return annotationClass.cast(ann);
    return null;
  }

  void updateWith(BeanProperty otherBeanProperty) {
    // we don't care for duplicate annotations as it should not change the behaviour
    if (otherBeanProperty.annotations.length > 0) {
      Annotation[] mergedAnnotations = new Annotation[annotations.length + otherBeanProperty.annotations.length];
      System.arraycopy(annotations, 0, mergedAnnotations, 0, annotations.length);
      System.arraycopy(otherBeanProperty.annotations, 0, mergedAnnotations, annotations.length, otherBeanProperty.annotations.length);

      this.annotations = mergedAnnotations;
    }
  }

  /**
   * Used to give priority to implementations, for example by default a method would have a higher
   * priority than a field because it can do some logic. The greater the priority value is the
   * more important is this BeanProperty.
   *
   * @return the priority of this BeanProperty
   */
  abstract int priority();

  abstract String signature();
}

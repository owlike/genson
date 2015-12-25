package com.owlike.genson.reflect;

import static com.owlike.genson.reflect.TypeUtil.getRawClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.owlike.genson.Genson;

public interface BeanPropertyFactory {
  PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson);

  PropertyAccessor createAccessor(String name, Method method, Type ofType, Genson genson);

  BeanCreator createCreator(Type ofType, Constructor<?> ctr, String[] resolvedNames,
                                   Genson genson);

  BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
                                   Genson genson);

  PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson);

  PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson);

  class CompositeFactory implements BeanPropertyFactory {
    private final List<BeanPropertyFactory> factories;

    public CompositeFactory(List<? extends BeanPropertyFactory> factories) {
      this.factories = new ArrayList<BeanPropertyFactory>(factories);
    }

    @Override
    public PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson) {
      for (BeanPropertyFactory factory : factories) {
        PropertyAccessor accessor = factory.createAccessor(name, field, ofType, genson);
        if (accessor != null) return accessor;
      }
      throw new RuntimeException("Failed to create a accessor for field " + field);
    }

    @Override
    public PropertyAccessor createAccessor(String name, Method method, Type ofType,
                                           Genson genson) {
      for (BeanPropertyFactory factory : factories) {
        PropertyAccessor accessor = factory.createAccessor(name, method, ofType, genson);
        if (accessor != null) return accessor;
      }
      throw new RuntimeException("Failed to create a accessor for method " + method);
    }

    @Override
    public BeanCreator createCreator(Type ofType, Constructor<?> ctr, String[] resolvedNames,
                                     Genson genson) {
      for (BeanPropertyFactory factory : factories) {
        BeanCreator creator = factory.createCreator(ofType, ctr, resolvedNames, genson);
        if (creator != null) return creator;
      }
      throw new RuntimeException("Failed to create a BeanCreator for constructor " + ctr);
    }

    @Override
    public BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
                                     Genson genson) {
      for (BeanPropertyFactory factory : factories) {
        BeanCreator creator = factory.createCreator(ofType, method, resolvedNames, genson);
        if (creator != null) return creator;
      }
      throw new RuntimeException("Failed to create a BeanCreator for method " + method);
    }

    @Override
    public PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson) {
      for (BeanPropertyFactory factory : factories) {
        PropertyMutator mutator = factory.createMutator(name, field, ofType, genson);
        if (mutator != null) return mutator;
      }
      throw new RuntimeException("Failed to create a mutator for field " + field);
    }

    @Override
    public PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson) {
      for (BeanPropertyFactory factory : factories) {
        PropertyMutator mutator = factory.createMutator(name, method, ofType, genson);
        if (mutator != null) return mutator;
      }
      throw new RuntimeException("Failed to create a mutator for method " + method);
    }
  }

  class StandardFactory implements BeanPropertyFactory {
    public PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson) {
      Class<?> ofClass = getRawClass(ofType);
      Type expandedType = TypeUtil.expandType(field.getGenericType(), ofType);
      return new PropertyAccessor.FieldAccessor(name, field, expandedType, ofClass);
    }

    public PropertyAccessor createAccessor(String name, Method method, Type ofType,
                                           Genson genson) {
      Type expandedType = TypeUtil.expandType(method.getGenericReturnType(), ofType);
      return new PropertyAccessor.MethodAccessor(name, method, expandedType,
        getRawClass(ofType));
    }

    public PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson) {
      Class<?> ofClass = getRawClass(ofType);
      Type expandedType = TypeUtil.expandType(field.getGenericType(), ofType);
      return new PropertyMutator.FieldMutator(name, field, expandedType, ofClass);
    }

    public PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson) {
      Type expandedType = TypeUtil.expandType(method.getGenericParameterTypes()[0], ofType);
      return new PropertyMutator.MethodMutator(name, method, expandedType,
        getRawClass(ofType));
    }

    // ofClass is not necessarily of same type as method return type, as ofClass corresponds to
    // the declaring class!
    public BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
                                     Genson genson) {
      return new BeanCreator.MethodBeanCreator(method, resolvedNames, expandTypes(
        method.getGenericParameterTypes(), ofType), getRawClass(ofType));
    }

    public BeanCreator createCreator(Type ofType, Constructor<?> ctr, String[] resolvedNames,
                                     Genson genson) {
      return new BeanCreator.ConstructorBeanCreator(getRawClass(ofType), ctr, resolvedNames,
        expandTypes(ctr.getGenericParameterTypes(), ofType));
    }

    public Type[] expandTypes(Type[] typesToExpand, Type inContext) {
      Type[] expandedTypes = new Type[typesToExpand.length];
      for (int i = 0; i < typesToExpand.length; i++) {
        expandedTypes[i] = TypeUtil.expandType(typesToExpand[i], inContext);
      }
      return expandedTypes;
    }
  }
}

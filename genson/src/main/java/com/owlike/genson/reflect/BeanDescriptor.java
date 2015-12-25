package com.owlike.genson.reflect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.owlike.genson.*;
import com.owlike.genson.reflect.BeanCreator.BeanCreatorProperty;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

/**
 * BeanDescriptors are used to serialize/deserialize objects based on their fields, methods and
 * constructors. By default it is supposed to work on JavaBeans, however it can be configured and
 * extended to support different kind of objects.
 * <p/>
 * In most cases BeanDescriptors should not be used directly as it is used internally to support
 * objects not handled by the default Converters. The most frequent case when you will use directly
 * a BeanDescriptor is when you want to deserialize into an existing instance. Here is an example :
 * <p/>
 * <pre>
 * Genson genson = new Genson();
 * BeanDescriptorProvider provider = genson.getBeanDescriptorProvider();
 * BeanDescriptor&lt;MyClass&gt; descriptor = provider.provide(MyClass.class, genson);
 *
 * MyClass existingInstance = descriptor.deserialize(existingInstance, new JsonReader(&quot;{}&quot;),
 * 		new Context(genson));
 * </pre>
 *
 * @param <T> type that this BeanDescriptor can serialize and deserialize.
 * @author eugen
 * @see BeanDescriptorProvider
 */
public class BeanDescriptor<T> implements Converter<T> {
  final Class<?> fromDeclaringClass;
  final Class<T> ofClass;
  final Map<String, PropertyMutator> mutableProperties;
  final List<PropertyAccessor> accessibleProperties;
  final boolean failOnMissingProperty;

  final BeanCreator creator;
  private final boolean _noArgCtr;

  private static final Object MISSING = new Object();
  // Used as a cache so we just copy it instead of recreating and assigning the default values
  private Object[] globalCreatorArgs;

  private final static Comparator<BeanProperty> _readablePropsComparator = new Comparator<BeanProperty>() {
    public int compare(BeanProperty o1, BeanProperty o2) {
      return o1.name.compareToIgnoreCase(o2.name);
    }
  };

  public BeanDescriptor(Class<T> forClass, Class<?> fromDeclaringClass,
                        List<PropertyAccessor> readableBps,
                        Map<String, PropertyMutator> writableBps, BeanCreator creator,
                        boolean failOnMissingProperty) {
    this.ofClass = forClass;
    this.fromDeclaringClass = fromDeclaringClass;
    this.creator = creator;
    this.failOnMissingProperty = failOnMissingProperty;
    mutableProperties = writableBps;

    Collections.sort(readableBps, _readablePropsComparator);

    accessibleProperties = Collections.unmodifiableList(readableBps);
    if (this.creator != null) {
      _noArgCtr = this.creator.parameters.size() == 0;
      globalCreatorArgs = new Object[creator.parameters.size()];
      Arrays.fill(globalCreatorArgs, MISSING);
    } else {
      _noArgCtr = false;
    }
  }

  public boolean isReadable() {
    return !accessibleProperties.isEmpty();
  }

  public boolean isWritable() {
    return creator != null;
  }

  public void serialize(T obj, ObjectWriter writer, Context ctx) {
    writer.beginObject();
    RuntimePropertyFilter runtimePropertyFilter = ctx.genson.runtimePropertyFilter();
    for (PropertyAccessor accessor : accessibleProperties) {
      if (runtimePropertyFilter.shouldInclude(accessor, ctx)) accessor.serialize(obj, writer, ctx);
    }
    writer.endObject();
  }

  public T deserialize(ObjectReader reader, Context ctx) {
    T bean = null;
    // optimization for default ctr
    if (_noArgCtr) {
      bean = ofClass.cast(creator.create());
      deserialize(bean, reader, ctx);
    } else {
      if (creator == null)
        throw new JsonBindingException("No constructor has been found for type "
          + ofClass);
      bean = _deserWithCtrArgs(reader, ctx);
    }
    return bean;
  }

  public void deserialize(T into, ObjectReader reader, Context ctx) {
    reader.beginObject();
    RuntimePropertyFilter runtimePropertyFilter = ctx.genson.runtimePropertyFilter();
    for (; reader.hasNext(); ) {
      reader.next();
      String propName = reader.name();
      PropertyMutator mutator = mutableProperties.get(propName);
      if (mutator != null) {
        if (runtimePropertyFilter.shouldInclude(mutator, ctx)) {
          mutator.deserialize(into, reader, ctx);
        } else {
          reader.skipValue();
        }
      } else if (failOnMissingProperty) throw missingPropertyException(propName);
      else reader.skipValue();
    }
    reader.endObject();
  }


  protected T _deserWithCtrArgs(ObjectReader reader, Context ctx) {
    List<String> names = new ArrayList<String>();
    List<Object> values = new ArrayList<Object>();
    RuntimePropertyFilter runtimePropertyFilter = ctx.genson.runtimePropertyFilter();

    reader.beginObject();
    for (; reader.hasNext(); ) {
      reader.next();
      String propName = reader.name();
      PropertyMutator muta = mutableProperties.get(propName);

      if (muta != null) {
        if (runtimePropertyFilter.shouldInclude(muta, ctx)) {
          Object param = muta.deserialize(reader, ctx);
          names.add(propName);
          values.add(param);
        } else {
          reader.skipValue();
        }
      } else if (failOnMissingProperty) throw missingPropertyException(propName);
      else reader.skipValue();
    }

    int size = names.size();
    int foundCtrParameters = 0;
    Object[] creatorArgs = globalCreatorArgs.clone();
    String[] newNames = new String[size];
    Object[] newValues = new Object[size];

    for (int i = 0, j = 0; i < size; i++) {
      BeanCreatorProperty mp = creator.paramsAndAliases.get(names.get(i));
      if (mp != null) {
        creatorArgs[mp.index] = values.get(i);
        foundCtrParameters++;
      } else {
        newNames[j] = names.get(i);
        newValues[j] = values.get(i);
        j++;
      }
    }

    if (foundCtrParameters < creator.parameters.size()) updateWithDefaultValues(creatorArgs, ctx.genson);

    T bean = ofClass.cast(creator.create(creatorArgs));
    for (int i = 0; i < size; i++) {
      PropertyMutator property = mutableProperties.get(newNames[i]);
      if (property != null) property.mutate(bean, newValues[i]);
    }
    reader.endObject();
    return bean;
  }

  private void updateWithDefaultValues(Object[] creatorArgs, Genson genson) {
    for (int i = 0; i < creatorArgs.length; i++) {
      if (creatorArgs[i] == MISSING) {
        for (BeanCreatorProperty property : creator.parameters.values()) {
          if (property.index == i) {
            creatorArgs[i] = genson.defaultValue(property.getRawClass());
            break;
          }
        }
      }
    }
  }

  public Class<T> getOfClass() {
    return ofClass;
  }

  private JsonBindingException missingPropertyException(String name) {
   return new JsonBindingException("No matching property in " + getOfClass() + " for key " + name);
  }
}
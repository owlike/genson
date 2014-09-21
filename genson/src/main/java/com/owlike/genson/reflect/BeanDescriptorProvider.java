package com.owlike.genson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.owlike.genson.Genson;

/**
 * Interface implemented by classes who want to provide {@link BeanDescriptor} instances for the
 * specified type.
 *
 * @author eugen
 */
public interface BeanDescriptorProvider {
  /**
   * Provides a BeanDescriptor for "type" using current Genson instance.
   *
   * @param type for which we need a BeanDescriptor.
   * @param genson current instance.
   * @return A BeanDescriptor instance able to serialize/deserialize objects of type T.
   */
  public <T> BeanDescriptor<T> provide(Class<T> type, Genson genson);

  /**
   * Provides a BeanDescriptor that can serialize/deserialize "ofClass" type, based on "type"
   * argument. The arguments "ofClass" and "type" will be the same in most cases, but for example
   * in BeanViews ofClass will correspond to the parameterized type and "type" to the BeanView
   * implementation.
   *
   * @param ofClass is the Class for which we need a BeanDescriptor that will be able to
   *                serialize/deserialize objects of that type;
   * @param type    to use to build this descriptor (use its declared methods, fields, etc).
   * @param genson  is the current Genson instance.
   * @return A BeanDescriptor instance able to serialize/deserialize objects of type ofClass.
   */
  public <T> BeanDescriptor<T> provide(Class<T> ofClass, Type type, Genson genson);


  public static class CompositeBeanDescriptorProvider implements BeanDescriptorProvider {
    private final List<BeanDescriptorProvider> providers;

    private final ConcurrentHashMap<Type, BeanDescriptor<?>> cache = new ConcurrentHashMap<Type, BeanDescriptor<?>>();

    public CompositeBeanDescriptorProvider(List<BeanDescriptorProvider> providers) {
      this.providers = new ArrayList<BeanDescriptorProvider>(providers);
    }

    @Override
    public <T> BeanDescriptor<T> provide(Class<T> ofClass, Genson genson) {
      return provide(ofClass, ofClass, genson);
    }

    @Override
    public <T> BeanDescriptor<T> provide(Class<T> ofClass, Type type, Genson genson) {
      BeanDescriptor<T> desc = (BeanDescriptor<T>) cache.get(type);
      if (desc == null) {
        for (BeanDescriptorProvider provider : providers) {
          desc = provider.provide(ofClass, type, genson);
          if (desc != null) break;
        }

        cache.putIfAbsent(type, desc);
      }

      return desc;
    }
  }
}

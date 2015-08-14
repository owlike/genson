package com.owlike.genson.ext.jaxrs;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.ext.jaxb.JAXBBundle;

import javax.ws.rs.ext.ContextResolver;
import java.util.HashSet;
import java.util.Set;

public final class GensonJaxRSFeature implements ContextResolver<GensonJaxRSFeature> {

  private static final Genson _defaultGenson = new GensonBuilder()
      .withBundle(new JAXBBundle())
      .useConstructorWithArguments(true)
      .create();

  private boolean enabled = true;
  private Set<Class<?>> notSerializableTypes = new HashSet<Class<?>>();
  private Set<Class<?>> notDeserializableTypes = new HashSet<Class<?>>();
  private Genson genson = _defaultGenson;

  @Override
  public GensonJaxRSFeature getContext(Class<?> type) {
    return this;
  }

  public GensonJaxRSFeature disable() {
    this.enabled = false;
    return this;
  }

  public GensonJaxRSFeature enable() {
    this.enabled = true;
    return this;
  }

  public GensonJaxRSFeature use(Genson genson) {
    this.genson = genson;
    return this;
  }

  public Genson genson() {
    return genson;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public GensonJaxRSFeature disableSerializationFor(Class<?> type, Class<?>... types) {
    notSerializableTypes.add(type);
    for (Class<?> t : types) notSerializableTypes.add(t);
    return this;
  }

  public GensonJaxRSFeature disableDeserializationFor(Class<?> type, Class<?>... types) {
    notDeserializableTypes.add(type);
    for (Class<?> t : types) notDeserializableTypes.add(t);
    return this;
  }

  public boolean isSerializable(Class<?> type) {
    return !notSerializableTypes.contains(type);
  }

  public boolean isDeserializable(Class<?> type) {
    return !notDeserializableTypes.contains(type);
  }
}

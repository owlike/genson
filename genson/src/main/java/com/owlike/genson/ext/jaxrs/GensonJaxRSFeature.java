package com.owlike.genson.ext.jaxrs;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.ext.jaxb.JAXBBundle;

import javax.ws.rs.ext.ContextResolver;

public final class GensonJaxRSFeature implements ContextResolver<GensonJaxRSFeature> {

  private static final Genson _defaultGenson = new GensonBuilder()
      .withBundle(new JAXBBundle())
      .useConstructorWithArguments(true)
      .create();

  private boolean enabled = true;
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

}

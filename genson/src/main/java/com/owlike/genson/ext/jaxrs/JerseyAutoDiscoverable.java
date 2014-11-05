package com.owlike.genson.ext.jaxrs;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.ws.rs.core.FeatureContext;

@Priority(AutoDiscoverable.DEFAULT_PRIORITY - 100)
public class JerseyAutoDiscoverable implements AutoDiscoverable {

  @Override
  public void configure(FeatureContext context) {
    context.register(GensonJsonConverter.class);
  }
}

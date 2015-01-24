package com.owlike.genson.ext.jaxrs;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

@Priority(AutoDiscoverable.DEFAULT_PRIORITY - 100)
public class JerseyAutoDiscoverable implements AutoDiscoverable {

  @Override
  public void configure(FeatureContext context) {
    Configuration config = context.getConfiguration();
    Object gensonDisabled = config.getProperty("jersey.genson.disable");
    boolean disabled = gensonDisabled != null && "true".equalsIgnoreCase(gensonDisabled.toString());

    if (!config.isRegistered(GensonJsonConverter.class) && !disabled)
      context.register(GensonJsonConverter.class);
  }
}

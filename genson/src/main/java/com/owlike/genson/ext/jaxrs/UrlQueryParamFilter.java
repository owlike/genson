package com.owlike.genson.ext.jaxrs;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import com.owlike.genson.Context;
import com.owlike.genson.ThreadLocalHolder;
import com.owlike.genson.reflect.BeanProperty;
import com.owlike.genson.reflect.RuntimePropertyFilter;

/**
 * This class will include or exclude (depending on how it is configured) the properties during ser/de based on the
 * content of the query string.
 */
public class UrlQueryParamFilter implements RuntimePropertyFilter, ContainerRequestFilter {
  private String paramName = "filter";
  // By default we exclude all and include only what is present in the query params
  private boolean inclusionFilter = true;
  private String splitBy;

  @Override
  public boolean shouldInclude(BeanProperty property, Context ctx) {
    Set<String> properties = ThreadLocalHolder.get("_jaxrs_params_to_filter", Set.class);

    if (inclusionFilter) return properties.contains(property.getName());
    else return !properties.contains(property.getName());
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    List<String> properties = containerRequestContext.getUriInfo().getQueryParameters().get(paramName);
    Set<String> propertiesToFilter = new HashSet<String>();
    if (properties != null) {
      for (String v : properties) {
        if (splitBy != null) Collections.addAll(propertiesToFilter, v.split(splitBy)); else propertiesToFilter.add(v);
      }
    }

    ThreadLocalHolder.store("_jaxrs_params_to_filter", propertiesToFilter);
  }

  /**
   * If true this will behave as an inclusion filter. Meaning that only properties that are present in the query params
   * will be included during ser/de. If false all properties are included by default and only the ones from the query
   * params are excluded. True by default.
   */
  public UrlQueryParamFilter inclusionFilter(boolean yes) {
    inclusionFilter = yes;
    return this;
  }

  /**
   * The name of the query parameter to get the properties to filter. Called "filter" by default.
   */
  public UrlQueryParamFilter paramName(String queryParamName) {
    paramName = queryParamName;
    return this;
  }

  /**
   * When defined it will be used to split every param value into property names.
   * This allows to use the filter in a non standard way like : localhost/foo?filter=age,name,gender
   */
  public UrlQueryParamFilter splitBy(String pattern) {
    splitBy = pattern;
    return this;
  }
}

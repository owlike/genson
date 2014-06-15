package com.owlike.genson.ext.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.owlike.genson.*;
import com.owlike.genson.annotation.WithBeanView;
import com.owlike.genson.ext.jaxb.JAXBBundle;
import com.owlike.genson.stream.JsonStreamException;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json", "application/*+json"})
@Produces({MediaType.APPLICATION_JSON, "text/json", "application/*+json"})
public class GensonJsonConverter implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
  private static class GensonStandardResolver implements ContextResolver<Genson> {
    private final Genson genson;

    public GensonStandardResolver() {
      this.genson = createDefaultInstance();
    }

    @Override
    public Genson getContext(Class<?> type) {
      return genson;
    }

    private final Genson createDefaultInstance() {
      return new GensonBuilder().withBundle(new JAXBBundle()).useBeanViews(true)
        .useConstructorWithArguments(true).create();
    }
  }

  private final ContextResolver<Genson> _gensonResolver;

  public GensonJsonConverter() {
    this(new GensonStandardResolver());
  }

  public GensonJsonConverter(@javax.ws.rs.core.Context Providers providers) {
    ContextResolver<Genson> gensonResolver = providers.getContextResolver(Genson.class, null);
    if (gensonResolver == null)
      _gensonResolver = new GensonStandardResolver();
    else
      _gensonResolver = gensonResolver;
  }

  public GensonJsonConverter(ContextResolver<Genson> gensonResolver) {
    this._gensonResolver = gensonResolver;
  }

  private Genson getInstance(Class<?> type) {
    Genson genson = _gensonResolver.getContext(type);
    if (genson == null)
      throw new NullPointerException("Could not resolve a Genson instance for type " + type
        + " using ContextResolver " + _gensonResolver.getClass());
    return genson;
  }

  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType) {
    return true;
  }

  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations,
                      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    Genson genson = getInstance(type);
    ObjectWriter writer = genson.createWriter(new OutputStreamWriter(entityStream, "UTF-8"));
    try {
      genson.serialize(t, rawIfNullGenericType(type, genericType), writer, createContext(annotations, genson));
      writer.flush();
    } catch (JsonBindingException e) {
      throw new WebApplicationException(e);
    } catch (JsonStreamException jse) {
      throw new WebApplicationException(jse);
    }
  }

  private Context createContext(Annotation[] annotations, Genson genson) {
    WithBeanView viewAnno = find(WithBeanView.class, annotations);
    Context context = null;
    if (viewAnno != null)
      context = new Context(genson, Arrays.asList(viewAnno.views()));
    else
      context = new Context(genson);
    return context;
  }

  private <T extends Annotation> T find(Class<T> annotationClass, Annotation[] inAnnotations) {
    for (Annotation anno : inAnnotations)
      if (annotationClass.isInstance(anno)) return annotationClass.cast(anno);
    return null;
  }

  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType) {
    return true;
  }

  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                         MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                         InputStream entityStream) throws IOException, WebApplicationException {
    try {
      Genson genson = getInstance(type);
      ObjectReader reader = genson.createReader(new InputStreamReader(entityStream, "UTF-8"));
      return genson.deserialize(GenericType.of(rawIfNullGenericType(type, genericType)), reader, createContext(annotations, genson));
    } catch (JsonBindingException e) {
      throw new WebApplicationException(e);
    } catch (JsonStreamException jse) {
      throw new WebApplicationException(jse);
    }
  }

  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations,
                      MediaType mediaType) {
    return -1;
  }

  private Type rawIfNullGenericType(Class<?> rawType, Type genericType) {
    return genericType != null ? genericType : rawType;
  }
}

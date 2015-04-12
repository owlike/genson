package com.owlike.genson.ext.jaxrs;

import java.io.*;
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

  private final ContextResolver<GensonJaxRSFeature> _gensonResolver;

  public GensonJsonConverter() {
    this(new GensonJaxRSFeature());
  }

  public GensonJsonConverter(@javax.ws.rs.core.Context Providers providers) {
    ContextResolver<GensonJaxRSFeature> gensonResolver = providers.getContextResolver(GensonJaxRSFeature.class, null);
    if (gensonResolver == null) {
      // This allows us to remain compatible with existing user code that would register a custom resolver.
      ContextResolver<Genson> oldResolver = providers.getContextResolver(Genson.class, null);
      if (oldResolver != null) {
        gensonResolver = new GensonJaxRSFeature().use(oldResolver.getContext(Object.class));
      }
    }

    if (gensonResolver == null)
      _gensonResolver = new GensonJaxRSFeature();
    else
      _gensonResolver = gensonResolver;
  }

  public GensonJsonConverter(ContextResolver<GensonJaxRSFeature> gensonResolver) {
    this._gensonResolver = gensonResolver;
  }

  private Genson getInstance(Class<?> type) {
    Genson genson = _gensonResolver.getContext(type).genson();
    if (genson == null)
      throw new NullPointerException("Could not resolve a Genson instance for type " + type
        + " using ContextResolver " + _gensonResolver.getClass());
    return genson;
  }

  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType) {
    return _gensonResolver.getContext(type).isEnabled();
  }

  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations,
                      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    Genson genson = getInstance(type);
    String charset = mediaType.getParameters().get("charset");
    if (charset == null) charset = "UTF-8";
    if (!charset.equalsIgnoreCase("UTF-8")
      && !charset.equalsIgnoreCase("UTF-16BE") && !charset.equalsIgnoreCase("UTF-16LE")
      && !charset.equalsIgnoreCase("UTF-32BE") && !charset.equalsIgnoreCase("UTF-32LE"))
      throw new UnsupportedEncodingException("JSON spec allows only UTF-8/16/32 encodings.");

    ObjectWriter writer = genson.createWriter(new OutputStreamWriter(entityStream, charset));
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
    if (inAnnotations != null) {
      for (Annotation anno : inAnnotations)
        if (annotationClass.isInstance(anno))
          return annotationClass.cast(anno);
    }
    return null;
  }

  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType) {
    return _gensonResolver.getContext(type).isEnabled();
  }

  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                         MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                         InputStream entityStream) throws IOException, WebApplicationException {
    try {
      Genson genson = getInstance(type);
      ObjectReader reader = genson.createReader(entityStream);
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

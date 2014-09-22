package com.owlike.genson.ext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.Test;

import static org.junit.Assert.*;

import com.owlike.genson.ext.jaxrs.GensonJsonConverter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONWithPadding;

public class JaxRSIntegrationTest {
  private Server server;

  @Test
  public void testJerseyJsonConverter() throws Exception {
    Map<String, String> jerseyParams = new HashMap<String, String>();
    jerseyParams.put("com.sun.jersey.config.property.resourceConfigClass",
      "com.sun.jersey.api.core.PackagesResourceConfig");
    jerseyParams.put("com.sun.jersey.config.property.packages", "com.owlike.genson.ext");
    startServer(ServletContainer.class, jerseyParams);
    try {
      testIntegration();
    } finally {
      stopServer();
    }
  }

  @Test
  public void testJerseyJsonPConverter() throws Exception {
    Map<String, String> jerseyParams = new HashMap<String, String>();
    jerseyParams.put("com.sun.jersey.config.property.resourceConfigClass",
      "com.sun.jersey.api.core.PackagesResourceConfig");
    jerseyParams.put("com.sun.jersey.config.property.packages", "com.owlike.genson.ext");
    startServer(ServletContainer.class, jerseyParams);
    try {
      ClientConfig cfg = new DefaultClientConfig(GensonJsonConverter.class);
      Client client = Client.create(cfg);
      assertEquals("someCallback([1,2,3])", client.resource("http://localhost:9999/get")
        .accept("application/x-javascript").get(String.class));
    } finally {
      stopServer();
    }
  }

  @Test
  public void testResteasyJsonConverter() throws Exception {
    Map<String, String> resteasy = new HashMap<String, String>();
    resteasy.put("resteasy.scan", "true");
    resteasy.put("javax.ws.rs.Application", RestEasyApp.class.getName());
    startServer(HttpServletDispatcher.class, resteasy);
    try {
      testIntegration();
    } finally {
      stopServer();
    }
  }

  private void testIntegration() {
    ClientConfig cfg = new DefaultClientConfig(GensonJsonConverter.class);
    Client client = Client.create(cfg);
    @SuppressWarnings("unchecked")
    Map<String, Long> map =
      client.resource("http://localhost:9999/get").accept(MediaType.APPLICATION_JSON)
        .get(Map.class);
    assertEquals(map.get("key1"), new Long(1));
    assertEquals(map.get("key2"), new Long(2));
  }

  private void startServer(Class<? extends Servlet> jaxrsProvider, Map<String, String> initParams)
    throws Exception {
    server = new Server(9999);
    ServletHolder servletHolder = new ServletHolder(jaxrsProvider);
    servletHolder.setInitParameters(initParams);
    ServletContextHandler ctxHandler = new ServletContextHandler();
    ctxHandler.addServlet(servletHolder, "/*");
    ctxHandler.setContextPath("/");
    server.setHandler(ctxHandler);
    server.start();
  }

  public void stopServer() throws Exception {
    server.stop();
  }

  public static class RestEasyApp extends Application {
    private static final Set<Class<?>> CLASSES;

    static {
      Set<Class<?>> tmp = new HashSet<Class<?>>();
      tmp.add(DummyRessource.class);

      CLASSES = Collections.unmodifiableSet(tmp);
    }

    @Override
    public Set<Class<?>> getClasses() {

      return CLASSES;
    }
  }

  @Path("/get")
  public static class DummyRessource {
    public DummyRessource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Dummy get() {
      Dummy dummy = new Dummy();
      dummy.setKey1(1);
      dummy.setKey2(2);
      return dummy;
    }

    @GET
    @Produces("application/x-javascript")
    public JSONWithPadding getJSONP() {
      return new JSONWithPadding(Arrays.asList(1, 2, 3), "someCallback");
    }
  }

  public static class Dummy {
    private int key1;
    private int key2;

    public int getKey1() {
      return key1;
    }

    public void setKey1(int key1) {
      this.key1 = key1;
    }

    public int getKey2() {
      return key2;
    }

    public void setKey2(int key2) {
      this.key2 = key2;
    }
  }
}

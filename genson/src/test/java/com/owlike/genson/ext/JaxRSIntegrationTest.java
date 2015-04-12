package com.owlike.genson.ext;

import java.util.*;

import javax.servlet.Servlet;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.owlike.genson.ext.jaxrs.GensonJaxRSFeature;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.JSONP;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.Test;

import static org.junit.Assert.*;

import com.owlike.genson.ext.jaxrs.GensonJsonConverter;

public class JaxRSIntegrationTest {
  private Server server;

  @Test
  public void testJerseyJsonConverter() throws Exception {
    Map<String, String> jerseyParams = new HashMap<String, String>();
    jerseyParams.put("javax.ws.rs.Application", RestEasyApp.class.getName());
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
    jerseyParams.put("javax.ws.rs.Application", RestEasyApp.class.getName());
    startServer(ServletContainer.class, jerseyParams);
    try {
      ClientConfig cfg = new ClientConfig(GensonJsonConverter.class);
      Client client = ClientBuilder.newClient(cfg);
      assertEquals("someCallback([1,2,3])", client.target("http://localhost:9999/get")
        .request("application/x-javascript").get(String.class));
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

  @Test
  public void testResteasyThrowException() throws Exception {
    Map<String, String> resteasy = new HashMap<String, String>();
    resteasy.put("resteasy.scan", "true");
    resteasy.put("javax.ws.rs.Application", RestEasyApp.class.getName());
    startServer(HttpServletDispatcher.class, resteasy);
    try {
      ClientConfig cfg = new ClientConfig(GensonJsonConverter.class);
      Client client = ClientBuilder.newClient(cfg);
      @SuppressWarnings("unchecked")
      Map<String, Long> map =
        client.target("http://localhost:9999/get/throwException").request(MediaType.APPLICATION_JSON)
          .get(Map.class);
      assertEquals(map.get("key1"), new Long(1));
      assertEquals(map.get("key2"), new Long(2));
    } finally {
      stopServer();
    }
  }

  @Test
  public void testDisableGenson() throws Exception {
    ResourceConfig serverCfg = new ResourceConfig()
        .register(new DummyRessource())
        .register(new GensonJaxRSFeature().disable())
        .register(new ExceptionMapper<Throwable>() {
          @Override
          public Response toResponse(Throwable exception) {
            return Response.ok(exception.getCause().getMessage()).build();
          }
        });

    ServletHolder servletHolder = new ServletHolder(new ServletContainer(serverCfg));
    ServletContextHandler ctxHandler = new ServletContextHandler();
    ctxHandler.addServlet(servletHolder, "/*");
    ctxHandler.setContextPath("/");

    server = new Server(9999);
    server.setHandler(ctxHandler);
    server.start();

    try {
      ClientConfig clientCfg = new ClientConfig(GensonJsonConverter.class);
      Client client = ClientBuilder.newClient(clientCfg);

      String res = client.target("http://localhost:9999/get").request(MediaType.APPLICATION_JSON).get(String.class);
      assertTrue(res.contains("MessageBodyWriter not found for media type=application/json"));
    } finally {
      stopServer();
    }
  }

  private void testIntegration() {
    ClientConfig cfg = new ClientConfig(GensonJsonConverter.class);
    Client client = ClientBuilder.newClient(cfg);
    @SuppressWarnings("unchecked")
    Map<String, Long> map =
      client.target("http://localhost:9999/get").request(MediaType.APPLICATION_JSON)
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
    @Path("/throwException")
    @Produces(MediaType.APPLICATION_JSON)
    public Dummy throwException() {

      Dummy dummy = new Dummy();
      dummy.setKey1(1);
      dummy.setKey2(2);

      throw new WebApplicationException(Response.ok(dummy).build());
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Dummy get() {
      Dummy dummy = new Dummy();
      dummy.setKey1(1);
      dummy.setKey2(2);
      return dummy;
    }

    @JSONP(callback = "someCallback")
    @GET
    @Produces("application/x-javascript")
    public List<Integer> getJSONP() {
      return Arrays.asList(1, 2, 3);
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

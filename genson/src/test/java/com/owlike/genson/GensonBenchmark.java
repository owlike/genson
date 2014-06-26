package com.owlike.genson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.owlike.genson.bean.ComplexObject;
import com.owlike.genson.bean.Primitives;

/*
======= Serialization Bench ======
Genson global serialization time=26.245 s
Genson avg serialization time=0.5249 ms

Jackson global serialization time=22.878 s
Jackson avg serialization time=0.45756 ms

Gson global serialization time=40.077 s
Gson avg serialization time=0.80154 ms

======= Deserialization Bench ======
Genson global deserialization time=33.629 s
Genson avg deserialization time=0.67258 ms

Genson custom double parse global deserialization time=15.969 s
Genson custom double parse avg deserialization time=0.31938 ms

Jackson global deserialization time=31.427 s
Jackson avg deserialization time=0.62854 ms

Gson global deserialization time=42.501 s
Gson avg deserialization time=0.85002 ms

=================================
 */
public class GensonBenchmark {
  final int ITERATION_CNT = 50000;
  private Genson genson = new GensonBuilder().useStrictDoubleParse(true).create();
  private Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
  private ObjectMapper om = new ObjectMapper();
  private Map<String, Object> map;
  private String json;

  public static void main(String[] args) throws IOException {
    GensonBenchmark bench = new GensonBenchmark();
    bench.setUp();
    bench.benchSerialization();
    bench.benchDeserialization();
  }

  public void setUp() {
    map = new HashMap<String, Object>();
    Primitives p1 = new Primitives(923456789, new Integer(56884646),
      16737897023.96909986098180546, new Double(54657750.9988904315),
      "TEXT ...  HEY\\\"\\\"\\\"\\\"ads dd qdqsdq!", true, new Boolean(false));
    Primitives p2 = new Primitives(923456789, new Integer(861289603), 54566544.0998891, null,
      null, false, true);

    List<Primitives> longList = new ArrayList<Primitives>(100);
    for (int i = 0; i < 100; i++)
      longList.add(p1);
    for (int i = 100; i < 200; i++)
      longList.add(p2);
    List<Primitives> shortList = Arrays.asList(p1, p1, p2, p2);

    Primitives[] mediumArray = new Primitives[]{p1, p2, p1, p2, p2, p2, p1, p1, p2};

    ComplexObject coBig = new ComplexObject(p1, longList, mediumArray);
    ComplexObject coSmall = new ComplexObject(p1, shortList, mediumArray);
    ComplexObject coEmpty = new ComplexObject(null, new ArrayList<Primitives>(),
      new Primitives[]{});

    // Serialization object
    for (int i = 0; i < 1; i++) {
      map.put("textValue" + i, "some text ...");
      map.put("intValue" + i, 44542365);
      map.put("doubleValue" + i, 7.5);
      map.put("falseValue" + i, false);
      map.put("trueValueObj" + i, Boolean.TRUE);
      map.put("nullValue" + i, null);

      map.put("mediumArray" + i, mediumArray);

      map.put("complexBigObject" + i, coBig);
      map.put("complexSmallObject" + i, coSmall);
      map.put("complexEmptyObject" + i, coEmpty);
    }

    // Deserialization json string
    StringBuilder sb = new StringBuilder().append('[').append(coBig.jsonString()).append(',')
      .append(coSmall.jsonString()).append(',').append(coEmpty.jsonString()).append(']');
    json = sb.toString();
  }

  public void benchSerialization() throws IOException {
    // warm up
    for (int i = 0; i < 35; i++) {
      om.writeValueAsString(map);
      genson.serialize(map);
      gson.toJson(map);
    }
    freeMem();
    System.out.println("======= Serialization Bench ======");

    @SuppressWarnings("unused")
    String dummyJson;
    Timer globalTimer = new Timer();
    Timer moyTimer = new Timer();

    globalTimer.start();
    moyTimer.start();
    for (int i = 0; i < ITERATION_CNT; i++) {
      dummyJson = genson.serialize(map);
      moyTimer.cumulate();
    }
    System.out.println("Genson global serialization time=" + globalTimer.stop().printS());
    System.out.println("Genson avg serialization time=" + moyTimer.stop().printMS() + "\n");
    freeMem();

    globalTimer.start();
    moyTimer.start();
    for (int i = 0; i < ITERATION_CNT; i++) {
      dummyJson = om.writeValueAsString(map);
      moyTimer.cumulate();
    }
    System.out.println("Jackson global serialization time=" + globalTimer.stop().printS());
    System.out.println("Jackson avg serialization time=" + moyTimer.stop().printMS() + "\n");
    freeMem();

    globalTimer.start();
    moyTimer.start();
    for (int i = 0; i < ITERATION_CNT; i++) {
      dummyJson = gson.toJson(map);
      moyTimer.cumulate();
    }
    System.out.println("Gson global serialization time=" + globalTimer.stop().printS());
    System.out.println("Gson avg serialization time=" + moyTimer.stop().printMS() + "\n");
  }

  public void benchDeserialization() throws IOException {
    // warm up
    for (int i = 0; i < 35; i++) {
      om.readValue(json, ComplexObject[].class);
      gson.fromJson(json, ComplexObject[].class);
      genson.deserialize(json, ComplexObject[].class);
    }
    freeMem();

    System.out.println("======= Deserialization Bench ======");
    @SuppressWarnings("unused")
    ComplexObject[] cos;
    Timer globalTimer = new Timer();
    Timer moyTimer = new Timer();

    globalTimer.start();
    moyTimer.start();
    for (int i = 0; i < ITERATION_CNT; i++) {
      cos = genson.deserialize(json, ComplexObject[].class);
      moyTimer.cumulate();
    }
    System.out.println("Genson global deserialization time=" + globalTimer.stop().printS());
    System.out.println("Genson avg deserialization time=" + moyTimer.stop().printMS() + "\n");
    freeMem();

    globalTimer.start();
    moyTimer.start();
    for (int i = 0; i < ITERATION_CNT; i++) {
      cos = om.readValue(json, ComplexObject[].class);
      moyTimer.cumulate();
    }
    System.out.println("Jackson global deserialization time=" + globalTimer.stop().printS());
    System.out.println("Jackson avg deserialization time=" + moyTimer.stop().printMS() + "\n");
    freeMem();

    globalTimer.start();
    moyTimer.start();
    for (int i = 0; i < ITERATION_CNT; i++) {
      cos = gson.fromJson(json, ComplexObject[].class);
      moyTimer.cumulate();
    }
    System.out.println("Gson global deserialization time=" + globalTimer.stop().printS());
    System.out.println("Gson avg deserialization time=" + moyTimer.stop().printMS() + "\n");

    System.out.println("=================================");
  }

  public void freeMem() {
    System.gc();
    synchronized (this) {
      try {
        this.wait(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}

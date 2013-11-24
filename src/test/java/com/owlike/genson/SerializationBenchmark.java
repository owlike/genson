package com.owlike.genson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Locale;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.bean.Feed;
import com.owlike.genson.bean.Tweet;

/**
 * Serialization bench, based on same data as DeserializeBenchmark. Jackson is slightly faster.
 * 
 * @author eugen
 *
 */
public class SerializationBenchmark {
	private final int ITER = 5000;
	private final int WARMUP_ITER = 50;
	private ObjectMapper mapper;
	private Genson genson;
	private Gson gson;
	private Tweet[] tweets;
	private Feed shortFeed;
	private Feed longFeed;
	
	public SerializationBenchmark() throws TransformationException, IOException {
		setUp();
	}

	private void setUp() throws TransformationException, IOException {
		genson = new Genson.Builder().setDateFormat(
				new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US)).create();
		gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy").serializeNulls().create();
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
		mapper.setDateFormat(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US));

		tweets = genson.deserialize(
				new InputStreamReader(ClassLoader.class.getResourceAsStream("/TWEETS.json")),
				Tweet[].class);
		shortFeed = genson.deserialize(
				new InputStreamReader(ClassLoader.class.getResourceAsStream("/READER_SHORT.json")),
				Feed.class);
		longFeed = genson.deserialize(
				new InputStreamReader(ClassLoader.class.getResourceAsStream("/READER_LONG.json")),
				Feed.class);
	}

	public void go() throws JsonGenerationException, JsonMappingException, IOException,
			TransformationException {
		Timer timer = new Timer();
		// warmup
		jacksonWrite(WARMUP_ITER, tweets);
		gensonWrite(WARMUP_ITER, tweets);
		gsonWrite(WARMUP_ITER, tweets);
		// clean
		freeMem();
		// bench
		timer.start();
		jacksonWrite(ITER, tweets);
		System.out.println("Jackson tweets:" + timer.stop().printS());
		freeMem();
		timer.start();
		gensonWrite(ITER, tweets);
		System.out.println("Genson tweets:" + timer.stop().printS());
		freeMem();
		timer.start();
		gsonWrite(ITER, tweets);
		System.out.println("Gson tweets:" + timer.stop().printS());
		System.out.println("**************************");

		// warmup
		jacksonWrite(WARMUP_ITER, shortFeed);
		gensonWrite(WARMUP_ITER, shortFeed);
		gsonWrite(WARMUP_ITER, shortFeed);
		// clean
		freeMem();
		// bench
		timer.start();
		jacksonWrite(ITER, shortFeed);
		System.out.println("Jackson shortFeed:" + timer.stop().printS());
		freeMem();
		timer.start();
		gensonWrite(ITER, shortFeed);
		System.out.println("Genson shortFeed:" + timer.stop().printS());
		freeMem();
		timer.start();
		gsonWrite(ITER, shortFeed);
		System.out.println("Gson shortFeed:" + timer.stop().printS());
		System.out.println("**************************");

		// warmup
		jacksonWrite(WARMUP_ITER, longFeed);
		gensonWrite(WARMUP_ITER, longFeed);
		gsonWrite(WARMUP_ITER, longFeed);
		// clean
		freeMem();
		// bench
		timer.start();
		jacksonWrite(ITER, longFeed);
		System.out.println("Jackson longFeed:" + timer.stop().printS());
		freeMem();
		timer.start();
		gensonWrite(ITER, longFeed);
		System.out.println("Genson longFeed:" + timer.stop().printS());
		freeMem();
		timer.start();
		gsonWrite(ITER, longFeed);
		System.out.println("Gson longFeed:" + timer.stop().printS());
	}

	public <T> void jacksonWrite(int iter, T object) throws JsonGenerationException,
			JsonMappingException, IOException {
		for (int i = 0; i < iter; i++) {
			mapper.writeValueAsString(object);
		}
	}

	public <T> void gensonWrite(int iter, T object) throws TransformationException, IOException {
		for (int i = 0; i < iter; i++) {
			genson.serialize(object);
		}
	}

	public <T> void gsonWrite(int iter, T object) throws TransformationException, IOException {
		for (int i = 0; i < iter; i++) {
			gson.toJson(object);
		}
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

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException,
			IOException, TransformationException {
		new SerializationBenchmark().go();
	}
}

package com.owlike.genson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * Deserialization benchmark based on performance benchmark from gson (metrics project ParseBenchmark class).
 * According to this benchmark, Genson performances are very close to Jacksons,
 * except in some cases where Genson is faster (the long reader input data).
 * {@link SerializationBenchmark} measures serialization performances, in this case Jackson
 * seems to be slightly faster.
 * 
 * @author eugen
 *
 */
public class DeserializeBenchmark {
	private final int ITER = 5000;
	private final int WARMUP_ITER = 50;

	private String tweets;
	private String shortReader;
	private String longReader;
	private Genson genson = new Genson.Builder().setDateFormat(
			new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US)).create();
	private Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy").create();
	private ObjectMapper mapper = new ObjectMapper();

	TypeReference<List<Tweet>> tweetsType = new TypeReference<List<Tweet>>() {};
	TypeReference<Feed> feedType = new TypeReference<Feed>() {};
	GenericType<Feed> genericFeedType = new GenericType<Feed>() {};
	GenericType<List<Tweet>> genericTweetsType = new GenericType<List<Tweet>>() {};

	public DeserializeBenchmark() throws Exception {
		setUp();
	}

	private void setUp() throws Exception {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
		mapper.setDateFormat(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US));

		tweets = resourceToString("/TWEETS.json");
		shortReader = resourceToString("/READER_SHORT.json");
		longReader = resourceToString("/READER_LONG.json");
	}

	private void go() throws JsonParseException, JsonMappingException, IOException,
			TransformationException {

		// warmup
		jacksonParse(WARMUP_ITER, tweets, tweetsType);
		gensonParse(WARMUP_ITER, tweets, genericTweetsType);
		gsonParse(WARMUP_ITER, tweets, tweetsType);
		freeMem();
		Timer timer = new Timer().start();
		// genson
		gensonParse(ITER, tweets, genericTweetsType);
		System.out.println("Genson tweets:" + timer.stop().printS());
		// jackson
		freeMem();
		timer.start();
		jacksonParse(ITER, tweets, tweetsType);
		System.out.println("Jackson tweets:" + timer.stop().printS());freeMem();
		// gson
		freeMem();
		timer.start();
		gsonParse(ITER, tweets, tweetsType);
		System.out.println("Gson tweets:" + timer.stop().printS());
		System.out.println("*****************");

		jacksonParse(WARMUP_ITER, shortReader, feedType);
		gensonParse(WARMUP_ITER, shortReader, genericFeedType);
		gsonParse(WARMUP_ITER, shortReader, feedType);
		freeMem();
		timer.start();
		gensonParse(ITER, shortReader, genericFeedType);
		System.out.println("Genson shortReader:" + timer.stop().printS());
		freeMem();
		timer.start();
		jacksonParse(ITER, shortReader, feedType);
		System.out.println("Jackson shortReader:" + timer.stop().printS());
		freeMem();
		timer.start();
		gsonParse(ITER, shortReader, feedType);
		System.out.println("Gson shortReader:" + timer.stop().printS());
		System.out.println("*****************");

		jacksonParse(WARMUP_ITER, longReader, feedType);
		gensonParse(WARMUP_ITER, longReader, genericFeedType);
		gsonParse(WARMUP_ITER, longReader, feedType);
		freeMem();
		timer.start();
		gensonParse(ITER, longReader, genericFeedType);
		System.out.println("Genson longReader:" + timer.stop().printS());
		freeMem();
		timer.start();
		jacksonParse(ITER, longReader, feedType);
		System.out.println("Jackson longReader:" + timer.stop().printS());
		freeMem();
		timer.start();
		gsonParse(ITER, longReader, feedType);
		System.out.println("Gson longReader:" + timer.stop().printS());
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

	public <T> void jacksonParse(int iter, String source, TypeReference<T> type)
			throws JsonParseException, JsonMappingException, IOException {
		for (int i = 0; i < iter; i++) {
			mapper.readValue(source, type);
		}
	}

	public <T> void gensonParse(int iter, String source, GenericType<T> type)
			throws TransformationException, IOException {
		for (int i = 0; i < iter; i++) {
			genson.deserialize(source, type);
		}
	}
	
	public <T> void gsonParse(int iter, String source, TypeReference<T> type)
			throws TransformationException, IOException {
		for (int i = 0; i < iter; i++) {
			gson.fromJson(source, type.getType());
		}
	}

	private static String resourceToString(String path) throws Exception {
		InputStream in = ClassLoader.class.getResourceAsStream(path);
		if (in == null) {
			throw new IllegalArgumentException("No such file: " + path);
		}

		Reader reader = new InputStreamReader(in, "UTF-8");
		char[] buffer = new char[8192];
		StringWriter writer = new StringWriter();
		int count;
		while ((count = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, count);
		}
		reader.close();
		return writer.toString();
	}

	public static void main(String[] args) throws Exception {
		DeserializeBenchmark bench = new DeserializeBenchmark();
		bench.go();
	}

}

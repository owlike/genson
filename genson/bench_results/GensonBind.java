package serializers.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.genson.Context;
import org.genson.Genson;
import org.genson.stream.JsonWriter;
import org.genson.stream.ObjectWriter;

import data.media.MediaContent;
import serializers.JavaBuiltIn;
import serializers.Serializer;
import serializers.TestGroups;

/**
 * Genson benchmark class for jvm-serializers project
 * 
 * @author eugen
 *
 */
public class GensonBind {
	public static void register(TestGroups groups) {
		groups.media.add(JavaBuiltIn.mediaTransformer, new GensonSerializer<MediaContent>(MediaContent.class));
	}
	
	static class GensonSerializer<T> extends Serializer<T> {
		private final Genson genson = new Genson();
		private final Class<T> tClass;
		public GensonSerializer(Class<T> tClass) {
			this.tClass = tClass;
		}
		
		@Override
		public T deserialize(byte[] array) throws Exception {
			return genson.deserialize(new String(array, "UTF-8"), tClass);
		}

		@Override
		public byte[] serialize(T content) throws Exception {
			return genson.serialize(content).getBytes("UTF-8");
		}

		@Override
		public void serializeItems(T[] items, OutputStream out) throws Exception {
			ObjectWriter writer = new JsonWriter(new OutputStreamWriter(out));
			for (int i = 0, len = items.length; i < len; ++i) {
				genson.serialize(items[i], tClass, writer, new Context(genson));
			}
			writer.flush();
			out.close();
		}

		@Override
		public T[] deserializeItems(InputStream in, int numberOfItems)
				throws Exception {
			@SuppressWarnings("unchecked")
			T[] result = (T[]) new Object[numberOfItems];
			Reader reader = new InputStreamReader(in);
			for (int i = 0; i < numberOfItems; ++i) {
				result[i] = genson.deserialize(reader, tClass);
			}
			reader.close();
			return result;
		}

		@Override
		public String getName() {
			return "json/genson/databind";
		}
	}
}

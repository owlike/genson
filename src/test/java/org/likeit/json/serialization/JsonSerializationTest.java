package org.likeit.json.serialization;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import org.likeit.json.bean.Primitives;
import org.likeit.transformation.ObjectTransformer;
import org.likeit.transformation.TransformationException;

public class JsonSerializationTest {
	private Primitives p = new Primitives(1, new Integer(10), 1.00001, new Double(
			0.00001), "TEXT ...  HEY!", true, new Boolean(false));
	
	@Test
	public void testJsonPrimitiveObject() throws TransformationException, IOException {
		ObjectTransformer ot = new ObjectTransformer();
		String json = ot.serialize(p);
		assertEquals(json, p.jsonString());
	}
	
	@Test public void testRootArray() {
		
	}
}

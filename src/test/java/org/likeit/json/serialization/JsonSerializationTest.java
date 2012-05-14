package org.likeit.json.serialization;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import org.likeit.json.bean.ComplexObject;
import org.likeit.json.bean.Primitives;
import org.likeit.transformation.ObjectTransformer;
import org.likeit.transformation.TransformationException;

public class JsonSerializationTest {
	ObjectTransformer ot = new ObjectTransformer();
	
	@Test
	public void testJsonPrimitiveObject() throws TransformationException, IOException {
		Primitives p = createPrimitives();
		String json = ot.serialize(p);
		assertEquals(json, p.jsonString());
	}
	
	@Test public void testJsonArrayOfPrimitives() throws TransformationException, IOException {
		String expected = "[\"a\",1,3.2,null,true]";
		Object[] array = new Object[]{"a", 1, 3.2, null, true};
		String json = ot.serialize(array);
		assertEquals(json, expected);
	}
	
	@Test public void testJsonArrayOfMixedContent() throws TransformationException, IOException {
		Primitives p = createPrimitives();
		p.setIntPrimitive(-88); p.setDoubleObject(null);
		String expected = "[\"a\"," + p.jsonString() + ",1,3.2,null,false," + p.jsonString() + "]";
		Object[] array = new Object[]{"a", p, 1, 3.2, null, false, p};
		String json = ot.serialize(array);
		assertEquals(json, expected);
	}
	
	@Test public void testJsonComplexObject() throws TransformationException, IOException {
		Primitives p = createPrimitives();
		List<Primitives> list = Arrays.asList(p, p, p ,p ,p);
		ComplexObject co = new ComplexObject(p, list, list.toArray(new Primitives[list.size()]));
		String json = ot.serialize(co);
		assertEquals(json, co.jsonString());
	}
	
	private Primitives createPrimitives() {
		return new Primitives(1, new Integer(10), 1.00001, new Double(
				0.00001), "TEXT ...  HEY!", true, new Boolean(false));
	}
}

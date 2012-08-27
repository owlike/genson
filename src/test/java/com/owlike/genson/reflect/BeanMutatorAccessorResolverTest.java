package com.owlike.genson.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.owlike.genson.Trilean;

import static com.owlike.genson.reflect.BeanMutatorAccessorResolver.*;

public class BeanMutatorAccessorResolverTest {
	@Test
	public void testCustomResolver() throws SecurityException, NoSuchFieldException {
		List<BeanMutatorAccessorResolver> resolvers = new ArrayList<BeanMutatorAccessorResolver>();
		resolvers.add(new BaseResolver() {
			@Override
			public Trilean isAccessor(Field field, Class<?> fromClass) {
				return MyProxy.class.equals(field.getType()) ? Trilean.FALSE : Trilean.UNKNOWN;
			}
		});
		resolvers.add(new StandardMutaAccessorResolver());
		
		CompositeResolver composite = new CompositeResolver(resolvers);

		assertEquals(Trilean.FALSE, composite.isAccessor(MyPojo.class.getDeclaredField("proxy"), MyPojo.class));
		assertEquals(Trilean.TRUE, composite.isAccessor(MyPojo.class.getDeclaredField("aString"), MyPojo.class));
	}
	
	private class MyPojo {
		private MyProxy proxy;
		public String aString;
	}

	private class MyProxy {
	}
}

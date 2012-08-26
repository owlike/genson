package com.owlike.genson.spring;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.owlike.genson.Genson;
import com.owlike.genson.bean.Primitives;
import com.owlike.genson.ext.spring.ExtendedReqRespBodyMethodProcessor;
import com.owlike.genson.ext.spring.GensonMessageConverter;

public class SpringIntegrationTest {
	@Test public void testFromAndTwoJson() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse resp = new MockHttpServletResponse();
		resp.setContentType("application/json");
		
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new GensonMessageConverter(new Genson.Builder().setSkipNull(false).create()));
		ExtendedReqRespBodyMethodProcessor handler = new ExtendedReqRespBodyMethodProcessor(converters);
		handler.handleReturnValue(get(), new MethodParameter(SpringIntegrationTest.class.getMethod("get"), -1), new ModelAndViewContainer(), new ServletWebRequest(req, resp));
		assertEquals(get().jsonString(), resp.getContentAsString());
		
		req = new MockHttpServletRequest();
		req.setContent("[1,2,3]".getBytes());
		req.setContentType("application/json");
		resp = new MockHttpServletResponse();
		Object o = handler.resolveArgument(new MethodParameter(SpringIntegrationTest.class.getMethod("set", int[].class), 0), new ModelAndViewContainer(), new ServletWebRequest(req, resp), new DefaultDataBinderFactory(null));
		assertArrayEquals(new int[]{1, 2, 3}, (int[])o);
	}

	public void set(@RequestBody int[] data) {
		
	}
	
	public @ResponseBody
	Primitives get() {
		return new Primitives(923456789, new Integer(861289603), 54566544.0998891, null, null,
				false, true);
	}
}

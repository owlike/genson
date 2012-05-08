package org.likeit.transformation.spring;

import java.io.IOException;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

public class ExtendedReqRespBodyMethodProcessor extends RequestResponseBodyMethodProcessor {
	private final static ThreadLocal<MethodParameter> _localReturnType = new ThreadLocal<MethodParameter>();
	
	public ExtendedReqRespBodyMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException {
		
		try {
			_localReturnType.set(returnType);
			super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
		} finally {
			_localReturnType.remove();
		}
	}
	
	static MethodParameter getCurrentMethodParameter() {
		return _localReturnType.get();
	}
}

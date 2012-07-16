package org.genson.spring;

import java.io.IOException;
import java.util.List;

import org.genson.ThreadLocalHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

public class ExtendedReqRespBodyMethodProcessor extends RequestResponseBodyMethodProcessor {

	public ExtendedReqRespBodyMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Object object = null;
		try {
			ThreadLocalHolder.store("__GENSON$method_param", parameter);
			object = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
		} finally {
			ThreadLocalHolder.remove("__GENSON$method_param");
		}

		return object;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException,
			HttpMediaTypeNotAcceptableException {

		try {
			ThreadLocalHolder.store("__GENSON$return_param", returnType);
			super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
		} finally {
			ThreadLocalHolder.remove("__GENSON$return_param");
		}
	}
}

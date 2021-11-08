package com.gic.fadv.verification.keycloak.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import com.gic.fadv.verification.keycloak.exception.NotFoundException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

	@Override
	public boolean hasError(ClientHttpResponse httpResponse) throws IOException {

		return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
				|| httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
	}

	@Override
	public void handleError(ClientHttpResponse httpResponse) throws IOException {

		if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
			// Handle SERVER_ERROR
			throw new HttpClientErrorException(httpResponse.getStatusCode());
		} else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
				&& httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
			// Handle CLIENT_ERROR
			throw new NotFoundException();
		}
	}
}
package com.workday.community.aem.core.utils.restclient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class APIRequest.
 */
public class APIRequest {

	/** The url of request. */
	String url;

	/** The request body. */
	String body;

	/** The request headers. */
	HashMap<String, String> headers;

	/** The method used in request. */
	String method;

	/** The request content type. */
	String contentType;

	/**
	 * Getter for body.
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Getter for method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Setter for method.
	 *
	 * @param method request method.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Getter for Url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter for Url.
	 *
	 * @param url request URL.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Getter for Uri.
	 */
	public URI getUri() {
		return URI.create(url);
	}

	/**
	 * Adds a header in the request headers.
	 *
	 * @param header the current header name.
	 * @param value  the header value.
	 */
	public void addHeader(String header, String value) {
		if (this.headers != null)
			headers.put(header, value);
		else {
			headers = new HashMap<String, String>();
			headers.put(header, value);
		}
	}

	/**
	 * Getter for Headers.
	 */
	public Map<String, String> getHeaders() {
		return this.headers;
	}
}

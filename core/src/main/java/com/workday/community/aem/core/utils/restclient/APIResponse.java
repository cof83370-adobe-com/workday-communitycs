package com.workday.community.aem.core.utils.restclient;

/**
 * The Class APIResponse.
 */
public class APIResponse {

	/** The response body. */
	String responseBody;

	/** The response code. */
	int responseCode;

	/**
	 * Getter for response body.
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * Setter for response body.
	 *
	 * @param responseBody response body.
	 */
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	/**
	 * Setter for response code.
	 *
	 * @param responseCode response code.
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

}

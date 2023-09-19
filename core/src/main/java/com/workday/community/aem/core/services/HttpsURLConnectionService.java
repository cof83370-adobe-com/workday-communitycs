package com.workday.community.aem.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.constants.HttpConstants;

/**
 * The Class HttpsURLConnectionService.
 */
@Component(service = HttpsURLConnectionService.class, immediate = true)
public class HttpsURLConnectionService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Set up http url connection.
     *
     * @param apiUrl The api url
     * @return The HttpsURLConnection
     */
    protected HttpsURLConnection getHttpsURLConnection(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        return (HttpsURLConnection) url.openConnection();
    }

    /**
     * Send the api request.
     *
     * @param url        The api url
     * @param header     The api header
     * @param httpMethod The api call method
     * @param payload    The api call payload
     * @return The api response
     */
    public HashMap<String, Object> send(String url, HashMap<String, String> header, String httpMethod, String payload) {
        HashMap<String, Object> apiResponse = new HashMap<>();

        try {
            HttpsURLConnection request = this.getHttpsURLConnection(url);
            request.setConnectTimeout(HttpConstants.HTTP_TIMEMOUT);
            request.setReadTimeout(HttpConstants.HTTP_TIMEMOUT);
            request.setRequestMethod(httpMethod);
            if (!header.isEmpty()) {
                for (HashMap.Entry<String, String> entry : header.entrySet()) {
                    request.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (!payload.isEmpty()) {
                request.setDoOutput(true);
                OutputStream os = request.getOutputStream();
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
                os.close();
            }
            apiResponse.put("statusCode", request.getResponseCode());
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            apiResponse.put("response", response.toString());
            request.disconnect();
            return apiResponse;
        } catch (IOException e) {
            logger.error("Rest api call in HttpsURLConnectionService failed: {}", e.getMessage());
            if (!apiResponse.containsKey("statusCode")) {
                apiResponse.put("statusCode", HttpStatus.SC_BAD_REQUEST);
            }
            if (!apiResponse.containsKey("response")) {
                apiResponse.put("response", e.getMessage());
            }
            return apiResponse;
        }
    }
}
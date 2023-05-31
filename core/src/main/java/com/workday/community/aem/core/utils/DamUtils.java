package com.workday.community.aem.core.utils;

import com.day.cq.dam.api.Asset;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for read files from DAM.
 */
public class DamUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DamUtils.class);

	public static JsonObject readJsonFromDam(ResourceResolver resourceResolver, String path) throws RuntimeException {
		try {
			Resource resource = resourceResolver.getResource(path);
			Asset asset = resource.adaptTo(Asset.class);
			Resource original = asset.getOriginal();
			InputStream content = original.adaptTo(InputStream.class);
			if (content == null) {
				LOGGER.error("Empty json file.");
				return new JsonObject();
			}

			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));

			while (true) {
				if ((line = br.readLine()) == null) {
					break;
				}
				sb.append(line);
			}
			content.close();
			br.close();

			// Gson object for json handling.
			Gson gson = new Gson();
			return gson.fromJson(sb.toString(), JsonObject.class);
		}  catch (IOException | SlingException e) {
			throw new RuntimeException();
		}
	}
    
}

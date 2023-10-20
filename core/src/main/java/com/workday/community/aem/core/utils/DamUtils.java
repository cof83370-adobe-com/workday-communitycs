package com.workday.community.aem.core.utils;

import static java.util.Objects.requireNonNull;

import com.day.cq.dam.api.Asset;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Utility class for read files from DAM.
 */
@Slf4j
public class DamUtils {

  /**
   * Get file content as json object.
   *
   * @param resourceResolver The Resource Resolver object.
   * @param path             The file path.
   * @return Json object of file content.
   * @throws DamException The DamException object.
   */
  public static JsonObject readJsonFromDam(ResourceResolver resourceResolver, String path)
      throws DamException {
    try {
      log.debug("readJsonFromDam for {}", path);
      Resource resource = resourceResolver.getResource(path);
      Asset asset = requireNonNull(resource).adaptTo(Asset.class);
      Resource original = requireNonNull(asset).getOriginal();
      InputStream content = original.adaptTo(InputStream.class);
      if (content == null) {
        log.error("Empty json file.");
        return new JsonObject();
      }

      StringBuilder sb = new StringBuilder();
      String line;
      BufferedReader br =
          new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));

      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      content.close();
      br.close();

      // Gson object for json handling.
      Gson gson = new Gson();
      return gson.fromJson(sb.toString(), JsonObject.class);
    } catch (IOException | SlingException e) {
      throw new DamException();
    }
  }

}

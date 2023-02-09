package com.workday.community.aem.migration.services.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.workday.community.aem.migration.models.PageNameBean;
import com.workday.community.aem.migration.services.PageNameFinderService;

/**
 * The Class PageNameFinderServiceImpl.
 */
@Component(immediate = true, service = PageNameFinderService.class)
@ServiceDescription("Workday - Page Name Finder Service Provider")
public class PageNameFinderServiceImpl implements PageNameFinderService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Gets the page name.
     *
     * @param resolver the resolver
     * @param eventPageJsonDamPath the event page json dam path
     * @return the page name
     */
    @Override
    public List<PageNameBean> getPageName(ResourceResolver resolver, final String eventPageJsonDamPath) {
        return readJsonResourceFromDam(resolver, eventPageJsonDamPath);
    }

    /**
     * Read json resource from dam.
     *
     * @param resolver the resolver
     * @param damPath the dam path
     * @return the list
     */
    private List<PageNameBean> readJsonResourceFromDam(ResourceResolver resolver, final String damPath) {
        List<PageNameBean> pageNamesLists = null;
        try {
            Resource resource = resolver.getResource(damPath);
            Asset asset = resource.adaptTo(Asset.class);
            Resource original = asset.getOriginal();
            InputStream content = original.adaptTo(InputStream.class);

            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            Type listType = new TypeToken<List<PageNameBean>>() {
            }.getType();
            pageNamesLists = (List<PageNameBean>) Optional.ofNullable(new Gson().fromJson(sb.toString(), listType))
                    .orElseGet(Collections::emptyList);
        } catch (Exception exec) {
            logger.info("Exception occurred at readJsonResourceFromDam method of PageNameFinderServiceImpl:{}",
                    exec.getMessage());
        }
        return pageNamesLists;
    }
}
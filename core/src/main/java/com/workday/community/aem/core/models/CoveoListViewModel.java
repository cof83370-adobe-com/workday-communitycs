package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.impl.CoveoFilterModelImpl;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The sling model for Coveo list View component.
 */
public interface CoveoListViewModel {
    public JsonObject getSearchConfig();

    public boolean getDisplayMetadata();

    public boolean getDisplayTags();

    @Inject
    public List<CoveoFilterModelImpl> getCategories();

    public Map<String, Objects> getCategoryConfiguration();
}

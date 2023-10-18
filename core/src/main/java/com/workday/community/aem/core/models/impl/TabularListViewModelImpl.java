package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.FeedTabModel;
import com.workday.community.aem.core.models.TabularListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Model(
        adaptables = {
                Resource.class,
                SlingHttpServletRequest.class
        },
        adapters = { TabularListViewModel.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TabularListViewModelImpl implements TabularListViewModel {
    private static final String MODEL_CONFIG_FILE = "/content/dam/workday-community/resources/tab-list-criteria-data.json";
    @Self
    private SlingHttpServletRequest request;

    @ChildResource
    @Named("searches/.")
    private List<FeedTabModel> searches;

    /**
     * SearchConfig service object.
     */
    @OSGiService
    private SearchApiConfigService searchConfigService;

    @OSGiService
    private UserService userService;

    /**
     * The snap service object.
     */
    @OSGiService
    private SnapService snapService;

    private JsonObject searchConfig;

    private JsonObject modelConfig;

    public void init(SlingHttpServletRequest request) {
        if (request != null) {
            this.request = request;
        }
    }

    @Override
    public List<FeedTabModel> getSearches() {
        return searches == null ? new ArrayList<>() : new ArrayList<>(searches);
    }

    @Override
    public JsonObject getSearchConfig() {
        if (this.searchConfig == null) {
            this.searchConfig = CoveoUtils.getSearchConfig(
                    searchConfigService,
                    request,
                    snapService,
                    userService);
        }
        return this.searchConfig;
    }

    @Override
    public String getExtraCriteria() throws DamException {
        return getModelConfig().getAsJsonObject("extraCriteria").get("value").getAsString();

    }

    @Override
    public String getFeedUrlBase() throws DamException {
        return getModelConfig().getAsJsonObject("feedUrlBase").get("value").getAsString();
    }

    private JsonObject getModelConfig() throws DamException {
        if (this.modelConfig == null) {
            this.modelConfig = DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
        }
        return this.modelConfig;
    }

    @Override
    public JsonArray getSelectedFields() throws DamException {
        JsonArray fields = new JsonArray();
        for(int i=0;i<searches.size();i++) {
            String tagQuery = searches.get(i).getTagQuery();
            String dataExpression = searches.get(i).getSelectedFieldsData().get("dataExpression").getAsString();
            searches.get(i).getSelectedFieldsData().addProperty("dataExpression", dataExpression+tagQuery+getExtraCriteria());
            fields.add(searches.get(i).getSelectedFieldsData());
        }
        return fields;
    }
}
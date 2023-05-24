package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.CoveoFilterModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Resource;
import javax.inject.Inject;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoFilterModelImpl{
    @Inject
    private String category;

    public String getCategory() {
        return category;
    }
}

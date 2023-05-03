package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.CoveoListView;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

/**
 * The Class CoveoListViewImpl.
 */
@Model(
        adaptables = Resource.class,
        adapters = {CoveoListView.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoListViewImpl implements CoveoListView {
    public String getMessage() {
        return "Test string";
    }
}

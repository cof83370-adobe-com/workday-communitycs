package com.workday.community.aem.core.models;
import java.util.List;

public interface CoveoListViewModel {
    public boolean getDisplayTags();


    public boolean getDisplayMetadata();

    public List<CoveoFilterModel> getCategories();
}
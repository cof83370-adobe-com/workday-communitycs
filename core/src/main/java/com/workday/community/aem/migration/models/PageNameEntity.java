package com.workday.community.aem.migration.models;

import java.util.ArrayList;
import java.util.List;

public class PageNameEntity {
    private List<PageNameBean> pageNameList = new ArrayList<>();

    public List<PageNameBean> getPageNameList() {
        return pageNameList;
    }

    public void setPageNameList(List<PageNameBean> pageNameList) {
        this.pageNameList = pageNameList;
    }
}

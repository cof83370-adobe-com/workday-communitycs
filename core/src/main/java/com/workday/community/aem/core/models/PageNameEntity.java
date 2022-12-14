package com.workday.community.aem.core.models;

import java.util.ArrayList;
import java.util.List;

public class PageNameEntity {
    private List<PageNameBean> pageNameList = new ArrayList<PageNameBean>();

    public List<PageNameBean> getPageNameList() {
        return pageNameList;
    }

    public void setPageNameList(List<PageNameBean> pageNameList) {
        this.pageNameList = pageNameList;
    }
}

package com.workday.community.aem.core.models;

import com.workday.community.aem.core.pojos.BookPageBean;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface TocModel {
    List<BookPageBean> bookPageList();
    Resource bookResource();
}

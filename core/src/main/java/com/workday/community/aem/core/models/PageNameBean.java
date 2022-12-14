package com.workday.community.aem.core.models;

public class PageNameBean {
    private String nodeId;
    private String title;

    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return "PageNameBean [nodeId=" + nodeId + ", title=" + title + "]";
    }
}

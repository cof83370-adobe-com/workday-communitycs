package com.workday.community.aem.core.pojos;

import java.util.List;

public class CategoryFacet {
    String field = null;

    String label = null;

    List<String> categories = List.of();

    void setField(String field) {
        this.field = field;
    }

    String getField() {
        return field;
    }

    void setLabel(String label) {
        this.label = label;
    }

    String getLabel() {
        return label;
    }

    void setCategories(List<String> categories) {
        this.categories = categories;
    }

    List<String> getCategories() {
        return categories;
    }
}

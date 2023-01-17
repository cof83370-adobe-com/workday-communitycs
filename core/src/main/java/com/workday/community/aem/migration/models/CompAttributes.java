package com.workday.community.aem.migration.models;

import java.util.Comparator;

public class CompAttributes implements Comparable<CompAttributes> {
    private int id;
    private String titleVal;
    private String textVal;

    public CompAttributes(int id, String titleVal, String textVal) {
        this.id = id;
        this.titleVal = titleVal;
        this.textVal = textVal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitleVal() {
        return titleVal;
    }

    public void setTitleVal(String titleVal) {
        this.titleVal = titleVal;
    }

    public String getTextVal() {
        return textVal;
    }

    public void setTextVal(String textVal) {
        this.textVal = textVal;
    }

    @Override
    public int compareTo(CompAttributes comparecomp) {
        int compareId = ((CompAttributes) comparecomp).getId();
        return this.getId() - compareId;
    }

    /**
     * Comparator to sort CompAttributes list or array in order of ID
     */
    public static Comparator<CompAttributes> IdComparator = new Comparator<CompAttributes>() {
        @Override
        public int compare(CompAttributes e1, CompAttributes e2) {
            return e1.getId() - e2.getId();
        }
    };

    @Override
    public String toString() {
        return "CompAttributes [id=" + id + ", titleVal=" + titleVal + ", textVal=" + textVal + "]";
    }
}

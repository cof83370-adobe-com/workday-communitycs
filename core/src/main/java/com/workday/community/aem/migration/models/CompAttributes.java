package com.workday.community.aem.migration.models;

import java.util.Comparator;

/**
 * The Class CompAttributes.
 * 
 * 
 * @author palla.pentayya
 */
public class CompAttributes implements Comparable<CompAttributes> {
    
    /** The id. */
    private int id;
    
    /** The title val. */
    private String titleVal;
    
    /** The text val. */
    private String textVal;

    /**
     * Instantiates a new comp attributes.
     *
     * @param id the id
     * @param titleVal the title val
     * @param textVal the text val
     */
    public CompAttributes(int id, String titleVal, String textVal) {
        this.id = id;
        this.titleVal = titleVal;
        this.textVal = textVal;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the title val.
     *
     * @return the title val
     */
    public String getTitleVal() {
        return titleVal;
    }

    /**
     * Sets the title val.
     *
     * @param titleVal the new title val
     */
    public void setTitleVal(String titleVal) {
        this.titleVal = titleVal;
    }

    /**
     * Gets the text val.
     *
     * @return the text val
     */
    public String getTextVal() {
        return textVal;
    }

    /**
     * Sets the text val.
     *
     * @param textVal the new text val
     */
    public void setTextVal(String textVal) {
        this.textVal = textVal;
    }

    /**
     * Compare to.
     *
     * @param compareComp the compare comp
     * @return the int
     */
    @Override
    public int compareTo(CompAttributes compareComp) {
        int compareId = ((CompAttributes) compareComp).getId();
        return this.getId() - compareId;
    }

    /** Comparator to sort CompAttributes list or array in order of ID. */
    public static Comparator<CompAttributes> idComparator = new Comparator<CompAttributes>() {
        @Override
        public int compare(CompAttributes e1, CompAttributes e2) {
            return e1.getId() - e2.getId();
        }
    };

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "CompAttributes [id=" + id + ", titleVal=" + titleVal + ", textVal=" + textVal + "]";
    }
}

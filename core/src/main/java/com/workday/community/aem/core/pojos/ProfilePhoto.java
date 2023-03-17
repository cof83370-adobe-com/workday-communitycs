package com.workday.community.aem.core.pojos;

public final class ProfilePhoto {
    /**
     * The file name with extension.
     */
    String fileNameWithExtension;
    /**
     * The Profile photo description.
     */
    String description;
    /**
     * The Profile user id.
     */
    String contactId;
    /**
     * The photo content.
     */
    String base64content;
    /**
     * The photo version id.
     */
    String photoVersionId;

    /**
     * Getter method.
     *
     * @return The file name with extension.
     */
    public String getFileNameWithExtension() {
        return fileNameWithExtension;
    }

    /**
     * Accessor method.
     *
     * @param fileNameWithExtension The file name with extension.
     */
    public void setFileNameWithExtension(String fileNameWithExtension) {
        this.fileNameWithExtension = fileNameWithExtension;
    }

    /**
     * Getter method.
     *
     * @return The Profile photo description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Accessor method.
     *
     * @param description The Profile photo description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter method.
     *
     * @return The Profile user id.
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * Accessor method.
     *
     * @param contactId The Profile user id.
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * Getter method.
     *
     * @return The photo content.
     */
    public String getBase64content() {
        return base64content;
    }

    /**
     * Accessor method.
     *
     * @param base64content The photo content.
     */
    public void setBase64content(String base64content) {
        this.base64content = base64content;
    }

    /**
     * Getter method.
     *
     * @return The photo version id.
     */
    public String getPhotoVersionId() {
        return photoVersionId;
    }

    /**
     * Accessor method.
     *
     * @param photoVersionId The photo version id.
     */
    public void setPhotoVersionId(String photoVersionId) {
        this.photoVersionId = photoVersionId;
    }
}
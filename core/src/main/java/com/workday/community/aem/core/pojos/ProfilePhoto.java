package com.workday.community.aem.core.pojos;

public final class ProfilePhoto {
    /**
     * The file name with extension.
     */
    String fileNameWithExtension;

    /**
     * The success.
     */
    String success;

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
     * @return The success Confirmation.
     */
    public String getSuccess() {
        return success;
    }

	
    /**
     * Accessor method.
     *
     * @param success The success Confirmation.
     */
    public void setSuccess(String success) {
        this.success = success;
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
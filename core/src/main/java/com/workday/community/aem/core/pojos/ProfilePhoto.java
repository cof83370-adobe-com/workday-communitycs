package com.workday.community.aem.core.pojos;

public final class ProfilePhoto {
    String fileNameWithExtension;
    String description;
    String contactId;
    String base64content;
    String photoVersionId;

    public String getFileNameWithExtension() {
        return fileNameWithExtension;
    }

    public void setFileNameWithExtension(String fileNameWithExtension) {
        this.fileNameWithExtension = fileNameWithExtension;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getBase64content() {
        return base64content;
    }

    public void setBase64content(String base64content) {
        this.base64content = base64content;
    }

    public String getPhotoVersionId() {
        return photoVersionId;
    }

    public void setPhotoVersionId(String photoVersionId) {
        this.photoVersionId = photoVersionId;
    }


}
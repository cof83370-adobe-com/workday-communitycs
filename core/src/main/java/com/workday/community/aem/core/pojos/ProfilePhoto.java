package com.workday.community.aem.core.pojos;

import lombok.Data;

@Data
public class ProfilePhoto {
    String fileNameWithExtension;
    String description;
    String contactId;
    String base64content;
    String photoVersionId;
}
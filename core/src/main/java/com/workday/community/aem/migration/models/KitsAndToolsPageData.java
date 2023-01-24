package com.workday.community.aem.migration.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class KitsAndToolsPageData.
 * 
 * 
 * @author palla.pentayya
 */
@XmlRootElement(name = "row")
@XmlAccessorType(XmlAccessType.FIELD)
public class KitsAndToolsPageData {

    /** The product. */
    @XmlElement(name = "product")
    private String product;

    /** The article type. */
    @XmlElement(name = "article_type")
    private String articleType;

    /** The author. */
    @XmlElement(name = "author")
    private String author;

    /** The drupal access control. */
    @XmlElement(name = "access_control")
    private String drupalAccessControl;

    /** The drupal node id. */
    @XmlElement(name = "nid")
    private String drupalNodeId;

    /** The description. */
    @XmlElement(name = "description")
    private String description;

    /** The industry. */
    @XmlElement(name = "industry")
    private String industry;

    /** The title. */
    @XmlElement(name = "title")
    private String title;

    /** The video attachment. */
    @XmlElement(name = "video_attachment")
    private String videoAttachment;

    /** The show ask related question. */
    @XmlElement(name = "show_ask_related_question")
    private String showAskRelatedQuestion;

    /** The file attachment. */
    @XmlElement(name = "file_attachment")
    private String fileAttachment;

    /** The program type. */
    @XmlElement(name = "program_type")
    private String programType;

    /** The retirement date. */
    @XmlElement(name = "retirement_date")
    private String retirementDate;

    /** The content type. */
    @XmlElement(name = "content_type")
    private String contentType;

    /** The updated date. */
    @XmlElement(name = "updated_date")
    private String updatedDate;

    /** The using workday. */
    @XmlElement(name = "using_workday")
    private String usingWorkday;

    /** The posted date. */
    @XmlElement(name = "posted_date")
    private String postedDate;

    /** The release tag. */
    @XmlElement(name = "release_tag")
    private String releaseTag;

    /** The read count. */
    @XmlElement(name = "read_count")
    private String readCount;

    /**
     * Gets the product.
     *
     * @return the product
     */
    public String getProduct() {
        if (product.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return product;
    }

    /**
     * Sets the product.
     *
     * @param product the new product
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * Gets the article type.
     *
     * @return the article type
     */
    public String getArticleType() {
        if (articleType.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return articleType;
    }

    /**
     * Sets the article type.
     *
     * @param articleType the new article type
     */
    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    /**
     * Gets the author.
     *
     * @return the author
     */
    public String getAuthor() {
        if (author.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the new author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the drupal access control.
     *
     * @return the drupal access control
     */
    public String getDrupalAccessControl() {
        if (drupalAccessControl.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return drupalAccessControl;
    }

    /**
     * Sets the drupal access control.
     *
     * @param drupalAccessControl the new drupal access control
     */
    public void setDrupalAccessControl(String drupalAccessControl) {
        this.drupalAccessControl = drupalAccessControl;
    }

    /**
     * Gets the drupal node id.
     *
     * @return the drupal node id
     */
    public String getDrupalNodeId() {
        if (drupalNodeId.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return drupalNodeId;
    }

    /**
     * Sets the drupal node id.
     *
     * @param drupalNodeId the new drupal node id
     */
    public void setDrupalNodeId(String drupalNodeId) {
        this.drupalNodeId = drupalNodeId;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        if (description.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the industry.
     *
     * @return the industry
     */
    public String getIndustry() {
        if (industry.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return industry;
    }

    /**
     * Sets the industry.
     *
     * @param industry the new industry
     */
    public void setIndustry(String industry) {
        this.industry = industry;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        if (title.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the video attachment.
     *
     * @return the video attachment
     */
    public String getVideoAttachment() {
        if (videoAttachment.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return videoAttachment;
    }

    /**
     * Sets the video attachment.
     *
     * @param videoAttachment the new video attachment
     */
    public void setVideoAttachment(String videoAttachment) {
        this.videoAttachment = videoAttachment;
    }

    /**
     * Gets the show ask related question.
     *
     * @return the show ask related question
     */
    public String getShowAskRelatedQuestion() {
        if (showAskRelatedQuestion.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return showAskRelatedQuestion;
    }

    /**
     * Sets the show ask related question.
     *
     * @param showAskRelatedQuestion the new show ask related question
     */
    public void setShowAskRelatedQuestion(String showAskRelatedQuestion) {
        this.showAskRelatedQuestion = showAskRelatedQuestion;
    }

    /**
     * Gets the file attachment.
     *
     * @return the file attachment
     */
    public String getFileAttachment() {
        if (fileAttachment.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return fileAttachment;
    }

    /**
     * Sets the file attachment.
     *
     * @param fileAttachment the new file attachment
     */
    public void setFileAttachment(String fileAttachment) {
        this.fileAttachment = fileAttachment;
    }

    /**
     * Gets the program type.
     *
     * @return the program type
     */
    public String getProgramType() {
        if (programType.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return programType;
    }

    /**
     * Sets the program type.
     *
     * @param programType the new program type
     */
    public void setProgramType(String programType) {
        this.programType = programType;
    }

    /**
     * Gets the retirement date.
     *
     * @return the retirement date
     */
    public String getRetirementDate() {
        if (retirementDate.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return retirementDate;
    }

    /**
     * Sets the retirement date.
     *
     * @param retirementDate the new retirement date
     */
    public void setRetirementDate(String retirementDate) {
        this.retirementDate = retirementDate;
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        if (contentType.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the updated date.
     *
     * @return the updated date
     */
    public String getUpdatedDate() {
        if (updatedDate.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return updatedDate;
    }

    /**
     * Sets the updated date.
     *
     * @param updatedDate the new updated date
     */
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * Gets the using workday.
     *
     * @return the using workday
     */
    public String getUsingWorkday() {
        if (usingWorkday.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return usingWorkday;
    }

    /**
     * Sets the using workday.
     *
     * @param usingWorkday the new using workday
     */
    public void setUsingWorkday(String usingWorkday) {
        this.usingWorkday = usingWorkday;
    }

    /**
     * Gets the posted date.
     *
     * @return the posted date
     */
    public String getPostedDate() {
        if (postedDate.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return postedDate;
    }

    /**
     * Sets the posted date.
     *
     * @param postedDate the new posted date
     */
    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    /**
     * Gets the release tag.
     *
     * @return the release tag
     */
    public String getReleaseTag() {
        if (releaseTag.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return releaseTag;
    }

    /**
     * Sets the release tag.
     *
     * @param releaseTag the new release tag
     */
    public void setReleaseTag(String releaseTag) {
        this.releaseTag = releaseTag;
    }

    /**
     * Gets the read count.
     *
     * @return the read count
     */
    public String getReadCount() {
        if (readCount.equalsIgnoreCase("NULL")) {
            return StringUtils.EMPTY;
        }
        return readCount;
    }

    /**
     * Sets the read count.
     *
     * @param readCount the new read count
     */
    public void setReadCount(String readCount) {
        this.readCount = readCount;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "KitsAndToolsPageData [product=" + product + ", articleType=" + articleType + ", author=" + author
                + ", drupalAccessControl=" + drupalAccessControl + ", drupalNodeId=" + drupalNodeId + ", description="
                + description + ", industry=" + industry + ", title=" + title + ", videoAttachment=" + videoAttachment
                + ", showAskRelatedQuestion=" + showAskRelatedQuestion + ", fileAttachment=" + fileAttachment
                + ", programType=" + programType + ", retirementDate=" + retirementDate + ", contentType=" + contentType
                + ", updatedDate=" + updatedDate + ", usingWorkday=" + usingWorkday + ", postedDate=" + postedDate
                + ", releaseTag=" + releaseTag + ", readCount=" + readCount + "]";
    }

}

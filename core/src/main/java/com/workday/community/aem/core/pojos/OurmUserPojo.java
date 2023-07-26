package com.workday.community.aem.core.pojos;

/**
 * The Class OurmUserPojo.
 */
public class OurmUserPojo {

    /** The profile image data. */
    String profileImageData;

    /** The username. */
    String username;

    /** The first name. */
    String firstName;

    /** The last name. */
    String lastName;

    /** The email. */
    String email;

    /** The sf id. */
    String sfId;

    public OurmUserPojo() {};

    public OurmUserPojo(String profileImageData, String username, String firstName, String lastName, String email, String sfId) {
        this.profileImageData = profileImageData;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email= email;
        this.sfId = sfId;
      }

    /**
     * Gets the profile image data.
     *
     * @return the profile image data
     */
    public String getProfileImageData() {
        return profileImageData;
    }

    /**
     * Sets the profile image data.
     *
     * @param profileImageData the new profile image data
     */
    public void setProfileImageData(String profileImageData) {
        this.profileImageData = profileImageData;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the sf id.
     *
     * @return the sf id
     */
    public String getSfId() {
        return sfId;
    }

    /**
     * Sets the sf id.
     *
     * @param sfId the new sf id
     */
    public void setSfId(String sfId) {
        this.sfId = sfId;
    }

}

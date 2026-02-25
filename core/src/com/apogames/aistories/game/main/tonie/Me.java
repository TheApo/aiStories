package com.apogames.aistories.game.main.tonie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Me {
    private String email;
    private String uuid;
    private String firstName;
    private String lastName;
    private String sex;
    private boolean acceptedTermsOfUse;
    private boolean tracking;
    private String authCode;
    private String profileImage;
    @JsonProperty("isVerified")
    private boolean verified;
    private String locale;
    @JsonProperty("isEduUser")
    private boolean eduUser;
    private int notificationCount;
    private boolean requiresVerificationToUpload;

    public Me() {
    }

    public String getEmail() {
        return this.email;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getSex() {
        return this.sex;
    }

    public boolean isAcceptedTermsOfUse() {
        return this.acceptedTermsOfUse;
    }

    public boolean isTracking() {
        return this.tracking;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public String getProfileImage() {
        return this.profileImage;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public String getLocale() {
        return this.locale;
    }

    public boolean isEduUser() {
        return this.eduUser;
    }

    public int getNotificationCount() {
        return this.notificationCount;
    }

    public boolean isRequiresVerificationToUpload() {
        return this.requiresVerificationToUpload;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setAcceptedTermsOfUse(boolean acceptedTermsOfUse) {
        this.acceptedTermsOfUse = acceptedTermsOfUse;
    }

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @JsonProperty("isVerified")
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @JsonProperty("isEduUser")
    public void setEduUser(boolean eduUser) {
        this.eduUser = eduUser;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public void setRequiresVerificationToUpload(boolean requiresVerificationToUpload) {
        this.requiresVerificationToUpload = requiresVerificationToUpload;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Me)) {
            return false;
        } else {
            Me other = (Me)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isAcceptedTermsOfUse() != other.isAcceptedTermsOfUse()) {
                return false;
            } else if (this.isTracking() != other.isTracking()) {
                return false;
            } else if (this.isVerified() != other.isVerified()) {
                return false;
            } else if (this.isEduUser() != other.isEduUser()) {
                return false;
            } else if (this.getNotificationCount() != other.getNotificationCount()) {
                return false;
            } else if (this.isRequiresVerificationToUpload() != other.isRequiresVerificationToUpload()) {
                return false;
            } else {
                label122: {
                    Object this$email = this.getEmail();
                    Object other$email = other.getEmail();
                    if (this$email == null) {
                        if (other$email == null) {
                            break label122;
                        }
                    } else if (this$email.equals(other$email)) {
                        break label122;
                    }

                    return false;
                }

                Object this$uuid = this.getUuid();
                Object other$uuid = other.getUuid();
                if (this$uuid == null) {
                    if (other$uuid != null) {
                        return false;
                    }
                } else if (!this$uuid.equals(other$uuid)) {
                    return false;
                }

                Object this$firstName = this.getFirstName();
                Object other$firstName = other.getFirstName();
                if (this$firstName == null) {
                    if (other$firstName != null) {
                        return false;
                    }
                } else if (!this$firstName.equals(other$firstName)) {
                    return false;
                }

                Object this$lastName = this.getLastName();
                Object other$lastName = other.getLastName();
                if (this$lastName == null) {
                    if (other$lastName != null) {
                        return false;
                    }
                } else if (!this$lastName.equals(other$lastName)) {
                    return false;
                }

                label94: {
                    Object this$sex = this.getSex();
                    Object other$sex = other.getSex();
                    if (this$sex == null) {
                        if (other$sex == null) {
                            break label94;
                        }
                    } else if (this$sex.equals(other$sex)) {
                        break label94;
                    }

                    return false;
                }

                Object this$authCode = this.getAuthCode();
                Object other$authCode = other.getAuthCode();
                if (this$authCode == null) {
                    if (other$authCode != null) {
                        return false;
                    }
                } else if (!this$authCode.equals(other$authCode)) {
                    return false;
                }

                Object this$profileImage = this.getProfileImage();
                Object other$profileImage = other.getProfileImage();
                if (this$profileImage == null) {
                    if (other$profileImage != null) {
                        return false;
                    }
                } else if (!this$profileImage.equals(other$profileImage)) {
                    return false;
                }

                Object this$locale = this.getLocale();
                Object other$locale = other.getLocale();
                if (this$locale == null) {
                    if (other$locale != null) {
                        return false;
                    }
                } else if (!this$locale.equals(other$locale)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof Me;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isAcceptedTermsOfUse() ? 79 : 97);
        result = result * 59 + (this.isTracking() ? 79 : 97);
        result = result * 59 + (this.isVerified() ? 79 : 97);
        result = result * 59 + (this.isEduUser() ? 79 : 97);
        result = result * 59 + this.getNotificationCount();
        result = result * 59 + (this.isRequiresVerificationToUpload() ? 79 : 97);
        Object $email = this.getEmail();
        result = result * 59 + ($email == null ? 43 : $email.hashCode());
        Object $uuid = this.getUuid();
        result = result * 59 + ($uuid == null ? 43 : $uuid.hashCode());
        Object $firstName = this.getFirstName();
        result = result * 59 + ($firstName == null ? 43 : $firstName.hashCode());
        Object $lastName = this.getLastName();
        result = result * 59 + ($lastName == null ? 43 : $lastName.hashCode());
        Object $sex = this.getSex();
        result = result * 59 + ($sex == null ? 43 : $sex.hashCode());
        Object $authCode = this.getAuthCode();
        result = result * 59 + ($authCode == null ? 43 : $authCode.hashCode());
        Object $profileImage = this.getProfileImage();
        result = result * 59 + ($profileImage == null ? 43 : $profileImage.hashCode());
        Object $locale = this.getLocale();
        result = result * 59 + ($locale == null ? 43 : $locale.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getEmail();
        return "Me(email=" + var10000 + ", uuid=" + this.getUuid() + ", firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ", sex=" + this.getSex() + ", acceptedTermsOfUse=" + this.isAcceptedTermsOfUse() + ", tracking=" + this.isTracking() + ", authCode=" + this.getAuthCode() + ", profileImage=" + this.getProfileImage() + ", verified=" + this.isVerified() + ", locale=" + this.getLocale() + ", eduUser=" + this.isEduUser() + ", notificationCount=" + this.getNotificationCount() + ", requiresVerificationToUpload=" + this.isRequiresVerificationToUpload() + ")";
    }
}

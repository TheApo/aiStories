package com.apogames.aistories.game.main.tonie.amazon;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldsBean {
    private String key;
    private String policy;
    @JsonProperty("x-amz-algorithm")
    private String xAmzAlgorithm;
    @JsonProperty("x-amz-credential")
    private String xAmzCredential;
    @JsonProperty("x-amz-date")
    private String xAmzDate;
    @JsonProperty("x-amz-signature")
    private String xAmzSignature;
    @JsonProperty("x-amz-security-token")
    private String xAmzSecurityToken;

    public FieldsBean() {
    }

    public String getKey() {
        return this.key;
    }

    public String getPolicy() {
        return this.policy;
    }

    public String getXAmzAlgorithm() {
        return this.xAmzAlgorithm;
    }

    public String getXAmzCredential() {
        return this.xAmzCredential;
    }

    public String getXAmzDate() {
        return this.xAmzDate;
    }

    public String getXAmzSignature() {
        return this.xAmzSignature;
    }

    public String getXAmzSecurityToken() {
        return this.xAmzSecurityToken;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @JsonProperty("x-amz-algorithm")
    public void setXAmzAlgorithm(String xAmzAlgorithm) {
        this.xAmzAlgorithm = xAmzAlgorithm;
    }

    @JsonProperty("x-amz-credential")
    public void setXAmzCredential(String xAmzCredential) {
        this.xAmzCredential = xAmzCredential;
    }

    @JsonProperty("x-amz-date")
    public void setXAmzDate(String xAmzDate) {
        this.xAmzDate = xAmzDate;
    }

    @JsonProperty("x-amz-signature")
    public void setXAmzSignature(String xAmzSignature) {
        this.xAmzSignature = xAmzSignature;
    }

    @JsonProperty("x-amz-security-token")
    public void setXAmzSecurityToken(String xAmzSecurityToken) {
        this.xAmzSecurityToken = xAmzSecurityToken;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FieldsBean)) {
            return false;
        } else {
            FieldsBean other = (FieldsBean)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label95: {
                    Object this$key = this.getKey();
                    Object other$key = other.getKey();
                    if (this$key == null) {
                        if (other$key == null) {
                            break label95;
                        }
                    } else if (this$key.equals(other$key)) {
                        break label95;
                    }

                    return false;
                }

                Object this$policy = this.getPolicy();
                Object other$policy = other.getPolicy();
                if (this$policy == null) {
                    if (other$policy != null) {
                        return false;
                    }
                } else if (!this$policy.equals(other$policy)) {
                    return false;
                }

                Object this$xAmzAlgorithm = this.getXAmzAlgorithm();
                Object other$xAmzAlgorithm = other.getXAmzAlgorithm();
                if (this$xAmzAlgorithm == null) {
                    if (other$xAmzAlgorithm != null) {
                        return false;
                    }
                } else if (!this$xAmzAlgorithm.equals(other$xAmzAlgorithm)) {
                    return false;
                }

                label74: {
                    Object this$xAmzCredential = this.getXAmzCredential();
                    Object other$xAmzCredential = other.getXAmzCredential();
                    if (this$xAmzCredential == null) {
                        if (other$xAmzCredential == null) {
                            break label74;
                        }
                    } else if (this$xAmzCredential.equals(other$xAmzCredential)) {
                        break label74;
                    }

                    return false;
                }

                label67: {
                    Object this$xAmzDate = this.getXAmzDate();
                    Object other$xAmzDate = other.getXAmzDate();
                    if (this$xAmzDate == null) {
                        if (other$xAmzDate == null) {
                            break label67;
                        }
                    } else if (this$xAmzDate.equals(other$xAmzDate)) {
                        break label67;
                    }

                    return false;
                }

                Object this$xAmzSignature = this.getXAmzSignature();
                Object other$xAmzSignature = other.getXAmzSignature();
                if (this$xAmzSignature == null) {
                    if (other$xAmzSignature != null) {
                        return false;
                    }
                } else if (!this$xAmzSignature.equals(other$xAmzSignature)) {
                    return false;
                }

                Object this$xAmzSecurityToken = this.getXAmzSecurityToken();
                Object other$xAmzSecurityToken = other.getXAmzSecurityToken();
                if (this$xAmzSecurityToken == null) {
                    if (other$xAmzSecurityToken != null) {
                        return false;
                    }
                } else if (!this$xAmzSecurityToken.equals(other$xAmzSecurityToken)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FieldsBean;
    }

    public int hashCode() {
        int result = 1;
        Object $key = this.getKey();
        result = result * 59 + ($key == null ? 43 : $key.hashCode());
        Object $policy = this.getPolicy();
        result = result * 59 + ($policy == null ? 43 : $policy.hashCode());
        Object $xAmzAlgorithm = this.getXAmzAlgorithm();
        result = result * 59 + ($xAmzAlgorithm == null ? 43 : $xAmzAlgorithm.hashCode());
        Object $xAmzCredential = this.getXAmzCredential();
        result = result * 59 + ($xAmzCredential == null ? 43 : $xAmzCredential.hashCode());
        Object $xAmzDate = this.getXAmzDate();
        result = result * 59 + ($xAmzDate == null ? 43 : $xAmzDate.hashCode());
        Object $xAmzSignature = this.getXAmzSignature();
        result = result * 59 + ($xAmzSignature == null ? 43 : $xAmzSignature.hashCode());
        Object $xAmzSecurityToken = this.getXAmzSecurityToken();
        result = result * 59 + ($xAmzSecurityToken == null ? 43 : $xAmzSecurityToken.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getKey();
        return "FieldsBean(key=" + var10000 + ", policy=" + this.getPolicy() + ", xAmzAlgorithm=" + this.getXAmzAlgorithm() + ", xAmzCredential=" + this.getXAmzCredential() + ", xAmzDate=" + this.getXAmzDate() + ", xAmzSignature=" + this.getXAmzSignature() + ", xAmzSecurityToken=" + this.getXAmzSecurityToken() + ")";
    }
}

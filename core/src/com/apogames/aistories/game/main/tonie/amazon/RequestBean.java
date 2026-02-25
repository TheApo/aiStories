package com.apogames.aistories.game.main.tonie.amazon;

public class RequestBean {
    private String url;
    private FieldsBean fields;

    public RequestBean() {
    }

    public String getUrl() {
        return this.url;
    }

    public FieldsBean getFields() {
        return this.fields;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFields(FieldsBean fields) {
        this.fields = fields;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RequestBean)) {
            return false;
        } else {
            RequestBean other = (RequestBean)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$url = this.getUrl();
                Object other$url = other.getUrl();
                if (this$url == null) {
                    if (other$url != null) {
                        return false;
                    }
                } else if (!this$url.equals(other$url)) {
                    return false;
                }

                Object this$fields = this.getFields();
                Object other$fields = other.getFields();
                if (this$fields == null) {
                    if (other$fields != null) {
                        return false;
                    }
                } else if (!this$fields.equals(other$fields)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof RequestBean;
    }

    public int hashCode() {
        int result = 1;
        Object $url = this.getUrl();
        result = result * 59 + ($url == null ? 43 : $url.hashCode());
        Object $fields = this.getFields();
        result = result * 59 + ($fields == null ? 43 : $fields.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getUrl();
        return "RequestBean(url=" + var10000 + ", fields=" + this.getFields() + ")";
    }
}

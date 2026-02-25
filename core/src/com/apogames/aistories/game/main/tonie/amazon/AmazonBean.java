package com.apogames.aistories.game.main.tonie.amazon;

public class AmazonBean {
    private String fileId;
    private RequestBean request;

    public AmazonBean() {
    }

    public String getFileId() {
        return this.fileId;
    }

    public RequestBean getRequest() {
        return this.request;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setRequest(RequestBean request) {
        this.request = request;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof AmazonBean)) {
            return false;
        } else {
            AmazonBean other = (AmazonBean)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$fileId = this.getFileId();
                Object other$fileId = other.getFileId();
                if (this$fileId == null) {
                    if (other$fileId != null) {
                        return false;
                    }
                } else if (!this$fileId.equals(other$fileId)) {
                    return false;
                }

                Object this$request = this.getRequest();
                Object other$request = other.getRequest();
                if (this$request == null) {
                    if (other$request != null) {
                        return false;
                    }
                } else if (!this$request.equals(other$request)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmazonBean;
    }

    public int hashCode() {
        int result = 1;
        Object $fileId = this.getFileId();
        result = result * 59 + ($fileId == null ? 43 : $fileId.hashCode());
        Object $request = this.getRequest();
        result = result * 59 + ($request == null ? 43 : $request.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getFileId();
        return "AmazonBean(fileId=" + var10000 + ", request=" + this.getRequest() + ")";
    }
}

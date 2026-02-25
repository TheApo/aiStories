package com.apogames.aistories.game.main.tonie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JWTToken {
    @JsonProperty("access_token")
    private String accessToken;

    public JWTToken() {
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    @JsonProperty("access_token")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof JWTToken)) {
            return false;
        } else {
            JWTToken other = (JWTToken)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$accessToken = this.getAccessToken();
                Object other$accessToken = other.getAccessToken();
                if (this$accessToken == null) {
                    if (other$accessToken != null) {
                        return false;
                    }
                } else if (!this$accessToken.equals(other$accessToken)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof JWTToken;
    }

    public int hashCode() {
        int result = 1;
        Object $accessToken = this.getAccessToken();
        result = result * 59 + ($accessToken == null ? 43 : $accessToken.hashCode());
        return result;
    }

    public String toString() {
        return "JWTToken(accessToken=" + this.getAccessToken() + ")";
    }
}

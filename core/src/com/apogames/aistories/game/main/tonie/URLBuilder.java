package com.apogames.aistories.game.main.tonie;

import org.apache.commons.lang3.StringUtils;

class URLBuilder {
    URLBuilder() {
    }

    static String getUrl(String URLConstant, Household household) {
        return getUrl(URLConstant, household, (CreativeTonie)null);
    }

    static String getUrl(String URLConstant, CreativeTonie creativeTonie) {
        return getUrl(URLConstant, creativeTonie.getHousehold(), creativeTonie);
    }

    private static String getUrl(String URLConstant, Household household, CreativeTonie creativeTonie) {
        String returnUrl = URLConstant;
        if (household != null) {
            returnUrl = StringUtils.replace(returnUrl, "%h", household.getId());
        }

        if (creativeTonie != null) {
            returnUrl = StringUtils.replace(returnUrl, "%t", creativeTonie.getId());
        }

        return returnUrl;
    }
}

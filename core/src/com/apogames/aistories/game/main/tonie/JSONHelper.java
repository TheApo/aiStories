package com.apogames.aistories.game.main.tonie;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class JSONHelper {
    private static final Logger log = LogManager.getLogger(JSONHelper.class);

    public JSONHelper() {
    }

    public static <T> T createBean(Class<T> clazz, InputStream stream) throws IOException {
        String json = null;

        try {
            json = IOUtils.toString(stream, "UTF-8");
            return createBean(clazz, json);
        } catch (IOException var4) {
            IOException e = var4;
            log.error("JSON: {} ", json, e);
            throw e;
        }
    }

    public static <T> T createBean(Class<T> clazz, String json) throws IOException {
        log.debug("JSON: {} ", json);

        try {
            ObjectMapper mapper = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode jsonNode = (JsonNode)mapper.readValue(json, JsonNode.class);
            return mapper.treeToValue(jsonNode, clazz);
        } catch (IOException var4) {
            IOException e = var4;
            log.error("JSON: {}", json, e);
            throw e;
        }
    }

    public static <T> T createBean(Class<T> clazz, Object obj) throws IOException {
        String jsonString = getJsonString(obj);
        log.debug("JSON: {}", jsonString);
        return createBean(clazz, jsonString);
    }

    public static String getJsonString(Object object) {
        String jsonString = getJson(object).toString();
        log.debug("JSON: {}", jsonString);
        return jsonString;
    }

    private static JsonNode getJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode returnJson = mapper.valueToTree(object);
        return returnJson;
    }
}

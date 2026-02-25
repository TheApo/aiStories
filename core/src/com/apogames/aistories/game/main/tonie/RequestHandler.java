package com.apogames.aistories.game.main.tonie;

import com.apogames.aistories.game.main.tonie.amazon.AmazonBean;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler {
    private HttpHost proxy = null;
    private JWTToken jwtToken;
    private final Header[] headerContentTypeJson = new Header[]{new BasicHeader("Content-Type", "application/json")};

    public RequestHandler() {
    }

    public RequestHandler(String proxySchema, String proxyHost, int proxyPort) {
        this.proxy = new HttpHost(proxyHost, proxyPort, proxySchema);
    }

    public void Login(Login loginBean) throws IOException {
        this.jwtToken = this.executeLoginRequest(loginBean);
    }

    private JWTToken executeLoginRequest(Login loginBean) throws IOException {
        try {
            HttpPost post = new HttpPost("https://login.tonies.com/auth/realms/tonies/protocol/openid-connect/token");
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> params = new ArrayList();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("client_id", "my-tonies"));
            params.add(new BasicNameValuePair("scope", "openid"));
            params.add(new BasicNameValuePair("username", loginBean.getEmail()));
            params.add(new BasicNameValuePair("password", loginBean.getPassword()));


            post.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post);
            return (JWTToken) JSONHelper.createBean(JWTToken.class, response.getEntity().getContent());
        } catch (Throwable var6) {
            throw (IOException)var6;
        }
    }

    public Me getMe() throws IOException {
        return (Me)this.executeGetRequest("https://api.tonie.cloud/v2/me", this.jwtToken, Me.class);
    }

    public Household[] getHouseholds() throws IOException {
        return (Household[])this.executeGetRequest("https://api.tonie.cloud/v2/households", this.jwtToken, Household[].class);
    }

    public CreativeTonie[] getCreativeTonies(Household household) throws IOException {
        CreativeTonie[] creativeTonieBeans = (CreativeTonie[])this.executeGetRequest(URLBuilder.getUrl("https://api.tonie.cloud/v2/households/%h/creativetonies", household), this.jwtToken, CreativeTonie[].class);
        CreativeTonie[] var3 = creativeTonieBeans;
        int var4 = creativeTonieBeans.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            CreativeTonie creativeTonie = var3[var5];
            creativeTonie.setHousehold(household);
            creativeTonie.setRequestHandler(this);
        }

        return creativeTonieBeans;
    }

    public CreativeTonie refreshTonie(CreativeTonie tonie) throws IOException {
        CreativeTonie returnTonie = (CreativeTonie)this.executeGetRequest(URLBuilder.getUrl("https://api.tonie.cloud/v2/households/%h/creativetonies/%t", tonie), this.jwtToken, CreativeTonie.class);
        returnTonie.setHousehold(tonie.getHousehold());
        return returnTonie;
    }

    public void commitTonie(CreativeTonie tonie) throws IOException {
        this.executePatchRequest(URLBuilder.getUrl("https://api.tonie.cloud/v2/households/%h/creativetonies/%t", tonie), this.headerContentTypeJson, new StringEntity(JSONHelper.getJsonString(tonie), "UTF-8"), this.jwtToken, (Class)null);
    }

    public void uploadFile(CreativeTonie tonie, File file, String title) throws IOException {
        HttpEntity emptyBlock = new StringEntity("{headers:{}}", "UTF-8");
        AmazonBean amazonBean = (AmazonBean)this.executePostRequest("https://api.tonie.cloud/v2/file", this.headerContentTypeJson, emptyBlock, this.jwtToken, AmazonBean.class);
        HttpEntity entity = MultipartEntityBuilder.create().setLaxMode().addTextBody("key", amazonBean.getRequest().getFields().getKey()).addTextBody("x-amz-algorithm", amazonBean.getRequest().getFields().getXAmzAlgorithm()).addTextBody("x-amz-credential", amazonBean.getRequest().getFields().getXAmzCredential()).addTextBody("x-amz-date", amazonBean.getRequest().getFields().getXAmzDate()).addTextBody("policy", amazonBean.getRequest().getFields().getPolicy()).addTextBody("x-amz-signature", amazonBean.getRequest().getFields().getXAmzSignature()).addTextBody("x-amz-security-token", amazonBean.getRequest().getFields().getXAmzSecurityToken()).addBinaryBody("file", file, ContentType.DEFAULT_BINARY, amazonBean.getRequest().getFields().getKey()).build();
        this.executePostRequest("https://bxn-toniecloud-prod-upload.s3.amazonaws.com/", (Header[])null, entity, (JWTToken)null, (Class)null);
        int chapterSize = tonie.getChapters().length;
        Chapter[] chapters = new Chapter[chapterSize + 1];
        System.arraycopy(tonie.getChapters(), 0, chapters, 0, chapterSize);
        chapters[chapterSize] = new Chapter();
        chapters[chapterSize].setTitle(title);
        chapters[chapterSize].setFile(amazonBean.getFileId());
        chapters[chapterSize].setId(amazonBean.getRequest().getFields().getKey());
        tonie.setChapters(chapters);
    }

    public void disconnect() throws IOException {
        this.executeDeleteRequest("https://api.tonie.cloud/v2/sessions", this.jwtToken, (Class)null);
    }

    private <T> T executeGetRequest(String URI, JWTToken jwtToken, Class<T> clazz) throws IOException {
        HttpGet method = new HttpGet(URI);
        return this.executeRequest(method, jwtToken, clazz);
    }

    private <T> T executeDeleteRequest(String URI, JWTToken jwtToken, Class<T> clazz) throws IOException {
        HttpDelete method = new HttpDelete(URI);
        return this.executeRequest(method, jwtToken, clazz);
    }

    private <T> T executePostRequest(String URI, Header[] headers, HttpEntity entity, JWTToken jwtToken, Class<T> clazz) throws IOException {
        HttpPost method = new HttpPost(URI);
        if (headers != null) {
            method.setHeaders(headers);
        }

        method.setEntity(entity);
        return this.executeRequest(method, jwtToken, clazz);
    }

    private <T> T executePatchRequest(String URI, Header[] headers, HttpEntity entity, JWTToken jwtToken, Class<T> clazz) throws IOException {
        HttpPatch method = new HttpPatch(URI);
        if (headers != null) {
            method.setHeaders(headers);
        }

        method.setEntity(entity);
        return this.executeRequest(method, jwtToken, clazz);
    }

    private <T> T executeRequest(HttpRequestBase method, JWTToken jwtToken, Class<T> clazz) throws IOException {
        int CONNECTION_TIMEOUT_MS = 5000;
        if (jwtToken != null) {
            method.addHeader("Authorization", "Bearer " + jwtToken.getAccessToken());
        }

        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS).setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(CONNECTION_TIMEOUT_MS).build();
        method.setConfig(requestConfig);
        CloseableHttpResponse response = this.getHttpClient().execute(method);
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        } else {
            InputStream inputStream = entity.getContent();

            T var10;
            label51: {
                T var9;
                try {
                    if (clazz == null) {
                        var10 = null;
                        break label51;
                    }

                    var9 = JSONHelper.createBean(clazz, inputStream);
                } catch (Throwable var12) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var11) {
                            var12.addSuppressed(var11);
                        }
                    }

                    throw var12;
                }

                if (inputStream != null) {
                    inputStream.close();
                }

                return var9;
            }

            if (inputStream != null) {
                inputStream.close();
            }

            return var10;
        }
    }

    private CloseableHttpClient getHttpClient() {
        if (this.proxy == null) {
            return HttpClients.createDefault();
        } else {
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(this.proxy);
            return HttpClients.custom().setRoutePlanner(routePlanner).build();
        }
    }
}

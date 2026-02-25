package com.apogames.aistories.game.main.tonie;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TonieHandler {
    private final RequestHandler requestHandler;

    public TonieHandler() {
        this.requestHandler = new RequestHandler();
    }

    public TonieHandler(String proxySchema, String proxyHost, int proxyPort) {
        this.requestHandler = new RequestHandler(proxySchema, proxyHost, proxyPort);
    }

    public void login(String username, String password) throws IOException {
        Login loginBean = new Login();
        loginBean.setEmail(username);
        loginBean.setPassword(password);
        this.requestHandler.Login(loginBean);
    }

    public List<Household> getHouseholds() throws IOException {
        return Arrays.asList(this.requestHandler.getHouseholds());
    }

    public List<CreativeTonie> getCreativeTonies(Household household) throws IOException {
        return Arrays.asList(this.requestHandler.getCreativeTonies(household));
    }

    public Me getMe() throws IOException {
        return this.requestHandler.getMe();
    }

    public void disconnect() throws IOException {
        this.requestHandler.disconnect();
    }
}


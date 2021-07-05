package com.example.mychat.models;

public class User {


    private String login;
    private String name;
    private String email;
    private String id;
    private String avatarResource;
    private String onlineStatus;
    private String accountStatus;

    public User() {
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public User(String name, String email, String id, String avatarResource, String onlineStatus, String login, String accStatus) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.avatarResource = avatarResource;
        this.onlineStatus = onlineStatus;
        this.login = login;
        this.accountStatus = accStatus;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarMockUpResource() {
        return avatarResource;
    }

    public void setAvatarMockUpResource(String avatarMockUpResource) {
        this.avatarResource = avatarMockUpResource;
    }



}

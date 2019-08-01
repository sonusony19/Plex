package com.example.plex.model;

public class User {

    private String id;
    private String userName;
    private String imageLink;
    private String age;
    private String status;
    private String sex;
    private String lattitude;
    private String longitude;

    public User(String id, String userName, String imageLink, String age, String status, String sex, String lattitude, String longitude) {
        this.id = id;
        this.userName = userName;
        this.imageLink = imageLink;
        this.age = age;
        this.status = status;
        this.sex = sex;
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getLattitude() {
        return lattitude;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

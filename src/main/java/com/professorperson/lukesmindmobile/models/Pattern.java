package com.professorperson.lukesmindmobile.models;

public class Pattern {
    private String image;
    private String name;

    public String getBase64() {
        return image;
    }

    public void setBase64(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.example.myapplication.model;

public class TextModel {
    private String textTitle;
    private String textContent;

    public TextModel(String textTitle, String textContent) {
        this.textTitle = textTitle;
        this.textContent = textContent;
    }

    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}

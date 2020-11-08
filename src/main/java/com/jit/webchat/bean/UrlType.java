package com.jit.webchat.bean;

import lombok.Data;

@Data
public class UrlType {
    private String url;
    private String type;
    private String oldFileName;

    public UrlType() {
    }

    public UrlType(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public UrlType(String url, String type, String oldFileName) {
        this.url = url;
        this.type = type;
        this.oldFileName = oldFileName;
    }
}

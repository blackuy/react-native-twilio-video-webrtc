package com.twiliorn.library.utils;

public class ImageFileReference {
    private final String uri;
    private final int width;
    private final int height;

    public ImageFileReference(String uri, int width, int height) {
        this.uri = uri;
        this.width = width;
        this.height = height;
    }

    public String getUri() {
        return uri;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

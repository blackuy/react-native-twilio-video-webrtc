package com.twiliorn.library;

public interface Callback<T> {
    void invoke(T data);
}

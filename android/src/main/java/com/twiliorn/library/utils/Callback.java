package com.twiliorn.library.utils;

public interface Callback<T> {
    void invoke(T data);
}

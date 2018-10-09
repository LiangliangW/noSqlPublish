package com.wll.nosqlpublish.constant;

import lombok.Getter;

/**
 * Created by WLL on 10/9/18 12:11 PM
 */
@Getter
public enum HttpMethodEnum {
    GET("GET"),
    POST("POST");

    String method;
    HttpMethodEnum(String method) {
        this.method = method;
    }
}

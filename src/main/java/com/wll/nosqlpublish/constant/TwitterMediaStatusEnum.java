package com.wll.nosqlpublish.constant;

import lombok.Getter;

/**
 * Created by WLL on 2018/10/10 9:04 AM
 */
@Getter
public enum TwitterMediaStatusEnum {
    FAILED(-1, "failed"),
    SUCCEEDED(0, "succeeded"),
    IN_PROGRESS(1, "in_progress");

    private Integer code;
    private String msg;
    TwitterMediaStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

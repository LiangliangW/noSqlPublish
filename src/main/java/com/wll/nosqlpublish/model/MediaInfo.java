package com.wll.nosqlpublish.model;

import lombok.Data;

/**
 * Created by WLL on 10/9/18 10:24 AM
 */
@Data
public class MediaInfo {
    private Long mediaId;
    private String mediaIdString;
    private Integer size;
    private Integer expiresAfterSecs;
}

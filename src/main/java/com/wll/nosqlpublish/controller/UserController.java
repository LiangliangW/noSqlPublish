package com.wll.nosqlpublish.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wll.nosqlpublish.service.imp.Oauth2ServiceImp;

@RestController
@RequestMapping(value = "/")
public class UserController {

    @Autowired
    Oauth2ServiceImp oauth2ServiceImp;

    protected final Log logger = LogFactory.getLog(this.getClass());

    @RequestMapping(value = "/publishAll", method = RequestMethod.GET)
    public String publishAll() {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateformat.format(System.currentTimeMillis());
        String message = "一键多发demo，发送时间: " + dateStr;
        String pageRes = oauth2ServiceImp.publishPageWithCharacters(message);
        String groupRes = oauth2ServiceImp.publishGroupWithCharacters(message);
        String twitterRes = oauth2ServiceImp.tweetTest(message);
        return "OK";
    }

    @RequestMapping(value = "/publishAllPhoto", method = RequestMethod.GET)
    public String publishAllPhoto() {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateformat.format(System.currentTimeMillis());
        //上传图片至facebook 主页
        String url = "http://oysf0b7t0.bkt.clouddn.com/focus.jpeg";//焦点图片
        String pageRes = oauth2ServiceImp.publishPageWithPhoto(url);
        logger.info("facebook's page photo: " + pageRes);
        //上传图片至facebook 小组
        String groupRes = oauth2ServiceImp.publishGroupWithPhoto(url);
        logger.info("facebook's group photo: " + groupRes);
        //上传图片至twitter
        String mediaId = oauth2ServiceImp.tweetUploadSingleImage("./src/main/resources/firstImage.jpg");
        String twitterRes = oauth2ServiceImp.tweetTest("焦点图片: " + dateStr, mediaId);
        logger.info("twitter's photo: " + twitterRes);
        return "OK";
    }

    @GetMapping(value = "/loginFacebook")
    public ModelAndView loginWithOauth() {
        //这个重定向，会访问两次，初始就访问一次，手动输入不会
        String appId = oauth2ServiceImp.facebookAppId;
        return new ModelAndView(new RedirectView("https://www.facebook.com/dialog/oauth?client_id="
            + appId
            + "&redirect_uri=https://localhost:8443/code&response_type=code&state=nfeXN4&scope=publish_pages,manage_pages,publish_to_groups"));
    }

    @GetMapping(value = "/code")
    public String getCode(@RequestParam("code") String code) {
        logger.info("time: " + System.currentTimeMillis());
        String accessToken = oauth2ServiceImp.getAccessToken(code);
        logger.info("access_token: " + accessToken);
        return accessToken;
    }

    //主页发布文字帖子
    @RequestMapping(value = "/publishPage1", method = RequestMethod.GET)
    public String publishPage1() {
        String message = "publish FaceBook page";
        String pageRes = oauth2ServiceImp.publishPageWithCharacters(message);
        return pageRes;
    }
    //主页发布图片
    @RequestMapping(value = "/publishPage2", method = RequestMethod.GET)
    public String publishPage2() {
        String url = "http://oyf9q4qzp.bkt.clouddn.com/1513493085.jpeg";
        String pageRes = oauth2ServiceImp.publishPageWithPhoto(url);
        return pageRes;
    }
    //主页发布视频
    @RequestMapping(value = "/publishPage3", method = RequestMethod.GET)
    public String publishPage3() {
        String file_url = "http://oysf0b7t0.bkt.clouddn.com/8a43ca6f2dac0b605cd024c086436b98.mp4";
        String pageRes = oauth2ServiceImp.publishPageWithVideo(file_url);
        return pageRes;
    }

    //在小组中发布文字帖子
    @RequestMapping(value = "/publishGroup1", method = RequestMethod.GET)
    public String publishGroup1() {
        String message = "publish FaceBook page";
        String groupRes = oauth2ServiceImp.publishGroupWithCharacters(message);
        return groupRes;
    }
    //在小组中发布图片
    @RequestMapping(value = "/publishGroup2", method = RequestMethod.GET)
    public String publishGroup2() {
        String url = "http://oyf9q4qzp.bkt.clouddn.com/1514099374.jpeg";
        String groupRes = oauth2ServiceImp.publishGroupWithPhoto(url);
        return groupRes;
    }
    //在小组中发布视频
    @RequestMapping(value = "/publishGroup3", method = RequestMethod.GET)
    public String publishGroup3() {
        String file_url = "http://oysf0b7t0.bkt.clouddn.com/8a43ca6f2dac0b605cd024c086436b98.mp4";
        String groupRes = oauth2ServiceImp.publishGroupWithVideo(file_url);
        return groupRes;
    }


    @RequestMapping(value = "/publishGroup", method = RequestMethod.GET)
    public String publishGroup() {
        String message = "publish FaceBook group";
        String groupRes = oauth2ServiceImp.publishGroupWithCharacters(message);
        return groupRes;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Long test(String message) {
        File file = new File("./src/main/resources/test1.mp4");
        return file.length();
    }

    @RequestMapping(value = "/loginTwitter", method = RequestMethod.GET)
    public String authTwitter() {
        return oauth2ServiceImp.authTwitter();
    }

//    @RequestMapping(value = "/getOauthVerifier", method = RequestMethod.GET)
//    public Boolean getOauthVerifierFromRedirect(@RequestParam("oauth_token") String oauthToken, @RequestParam("oauth_verifier") String oauthVerifier) {
//        if (oauth2ServiceImp.getAccessTokenInTwitter(oauthVerifier) != null) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    @RequestMapping(value = "/getOauthVerifier", method = RequestMethod.GET)
    public Map getOauthVerifierFromRedirect(@RequestParam("oauth_token") String oauthToken, @RequestParam("oauth_verifier") String oauthVerifier) {
        Map accessToken = oauth2ServiceImp.getAccessTokenInTwitter(oauthVerifier);
        return accessToken;
    }

    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    public String getUserInfo() {
        return oauth2ServiceImp.getUserInfo();
    }

    @RequestMapping(value = "/tweet", method = RequestMethod.GET)
    public String tweet() {
        return oauth2ServiceImp.tweetTest(oauth2ServiceImp.getOauthNonce());
    }

    @RequestMapping(value = "/tweetUploadSingleImage", method = RequestMethod.GET)
    public String tweetUploadSingleImage() {
        String mediaId = oauth2ServiceImp.tweetUploadSingleImage("./src/main/resources/firstImage.jpg");
        return mediaId;
    }

    @RequestMapping(value = "/tweetChunkedUploadInit", method = RequestMethod.GET)
    public String tweetChunkedUploadInit() {
        String result = oauth2ServiceImp.tweetChunkedUploadInit("./src/main/resources/test2.mp4", "video/mp4");
        return result;
    }

    @RequestMapping(value = "/tweetChunkedUploadAppend", method = RequestMethod.GET)
    public String tweetChunkedUploadAppend(String mediaId) {
        String result = oauth2ServiceImp.tweetChunkedUploadAppend("./src/main/resources/test2.mp4", mediaId, 0);
        return result;
    }

    @RequestMapping(value = "/tweetChunkedUploadFinalize", method = RequestMethod.GET)
    public Integer tweetChunkedUploadFinalize(String mediaId) {
        Integer result = oauth2ServiceImp.tweetChunkedUploadFinalize(mediaId);
        return result;
    }

    @RequestMapping(value = "/tweetVideo", method = RequestMethod.GET)
    public String tweetVideo(String mediaId) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateformat.format(System.currentTimeMillis());
        return oauth2ServiceImp.tweetTest("一键多发视频demo，发送时间: " + dateStr, mediaId);
    }

    @RequestMapping(value = "/tweetChunkedUpload", method = RequestMethod.GET)
    public String tweetChunkedUpload() {
//        String filePath = "./src/main/resources/douyin1.mp4";
        String filePath = "D:/springboot/3.mp4";
        String mediaType = "video/mp4";
        String res = oauth2ServiceImp.tweetChunkedUpload(filePath, mediaType);
        return res;
    }

    @RequestMapping(value = "/facebookChunkedUpload", method = RequestMethod.GET)
    public String facebookChunkedUpload() {
        String filePath = "D:/springboot/2.mp4";
        String groupId = "1972058782882587";
//        oauth2ServiceImp.facebookAccessToken = "EAAEAfkSI6DkBAE24f6TT4bekbuAaUKVgD5Hp2YpsEWcea4CnH5DdbZCYWC2xfxIv6wohoFnrkbxZAtUfTZAEE1ZBHMtZCCQ7cogczJ8TvhSEQEwP7BVrZAW1Nnu4EZCUVxHe37FintoZCXEZBgX73hHyyJxgxwPpyQsSTlL27989eQeVn47VihSqLDmOh6hNtL0DYEYnWFIKpXHK0EX7iUCDMJtgMeGatZCbbC0Hw4WUVEBwZDZD";
        String userAccessToken = "EAAeuoevhPZC0BAHmvcpySbfy9cPsABpJSbf230wrIa5dtZBNIZCMKhuzsNDkbpq06cvBoEMn4eN9aWxYVfrJmEcd74bsYK5OSnsNKE4YYnAznhjsUFYkhI73ZA6HCDCP1v03p5hMj9UNd11lOxD7DMOzWc0eqYl18pzklUb1r3G6IQkkvDc2GLyCZCpf24ymK2XK0U9q1uCQjMNdGn4FEy6sZAByPg70xc96yzkfA1gUWQfXBoE2IfPCpAGZAk77LUZD";

        String result1 = oauth2ServiceImp.facebookChunkedUploadVideoInit(groupId, filePath, userAccessToken);
        logger.info("hinson'log: rseult1: " + result1);
        JSONObject data = JSONObject.parseObject(result1);

        String uploadSessionId = data.getString("upload_session_id");
        long startOffset = Long.parseLong(data.getString("start_offset"));
        long endOffset = Long.parseLong(data.getString("end_offset"));
        String result2 = oauth2ServiceImp.facebookChunkedUploading(groupId, filePath, userAccessToken,
            uploadSessionId, startOffset, endOffset);
        logger.info("hinson'log: result2: " + result2);
        return result1;
    }

    @RequestMapping(value = "/tweetVideoOneClick", method = RequestMethod.GET)
    public String tweetVideoOneClick() {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String text = "一键多发视频demo，发送时间: " + dateformat.format(System.currentTimeMillis());
        String filePath = "./src/main/resources/test2.mp4";
        String mediaType = "video/mp4";
        return oauth2ServiceImp.tweetVideoTmp(text, filePath, mediaType);
    }

    @RequestMapping(value = "/publishVideoOneClick", method = RequestMethod.GET)
    public String publishVideoOneClick() {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String text = "一键多发视频demo，发送时间: " + dateformat.format(System.currentTimeMillis());
        String filePath = "./src/main/resources/test2.mp4";
        String mediaType = "video/mp4";
        String twitterResult = oauth2ServiceImp.tweetVideoTmp(text, filePath, mediaType);

        String fileUrl = "http://oysf0b7t0.bkt.clouddn.com/8a43ca6f2dac0b605cd024c086436b98.mp4";
        String pageRes = oauth2ServiceImp.publishPageWithVideo(fileUrl);
        String facebookGroupResult = oauth2ServiceImp.publishGroupWithVideo(fileUrl);
        // TODO: 2018/10/10 返回状态判断
        return "OK";
    }
}
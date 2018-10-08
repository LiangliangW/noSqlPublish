package com.wll.nosqlpublish.controller;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

import com.wll.nosqlpublish.service.imp.Oauth2ServiceImp;
import com.wll.nosqlpublish.util.UrlEncodeUtil;

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
        String message = "publishAll: " + dateStr;
        String pageRes = oauth2ServiceImp.publishPageWithCharacters(message);
        String groupRes = oauth2ServiceImp.publishGroupWithCharacters(message);
        String twitterRes = oauth2ServiceImp.tweetTest(message);
        return "OK";
    }

    @GetMapping(value = "/oauth")
    public ModelAndView loginWithOauth() {
        //这个重定向，会访问两次，初始就访问一次，手动输入不会
        return new ModelAndView(new RedirectView("https://www.facebook.com/dialog/oauth?"
            + "client_id=549957782114948"
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
        String file_url = "http://oysf0b7t0.bkt.clouddn.com/527c5fcfc1ed4ac568e9284ebf96d342.mp4";
        String pageRes = oauth2ServiceImp.publishPageWithVideo(file_url);
        return pageRes;
    }

    //在小组中发布文字帖子
    @RequestMapping(value = "/publishGroup1", method = RequestMethod.GET)
    public String publishGroup1() {
        String message = "publish FaceBook page";
        String pageRes = oauth2ServiceImp.publishGroupWithCharacters(message);
        return pageRes;
    }
    //在小组中发布图片
    @RequestMapping(value = "/publishGroup2", method = RequestMethod.GET)
    public String publishGroup2() {
        String url = "http://oyf9q4qzp.bkt.clouddn.com/1514099374.jpeg";
        String pageRes = oauth2ServiceImp.publishGroupWithPhoto(url);
        return pageRes;
    }
    //在小组中发布视频
    @RequestMapping(value = "/publishGroup3", method = RequestMethod.GET)
    public String publishGroup3() {
        String file_url = "http://oysf0b7t0.bkt.clouddn.com/527c5fcfc1ed4ac568e9284ebf96d342.mp4";
        String pageRes = oauth2ServiceImp.publishGroupWithVideo(file_url);
        return pageRes;
    }


    @RequestMapping(value = "/publishGroup", method = RequestMethod.GET)
    public String publishGroup() {
        String message = "publish FaceBook group";
        String groupRes = oauth2ServiceImp.publishGroupWithCharacters(message);
        return groupRes;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() throws UnsupportedEncodingException {
        return UrlEncodeUtil.encode("My first twitter by coding");
    }

    @RequestMapping(value = "/authTwitter", method = RequestMethod.GET)
    public String authTwitter() throws UnsupportedEncodingException, SignatureException {
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
    public String getOauthVerifierFromRedirect(@RequestParam("oauth_token") String oauthToken, @RequestParam("oauth_verifier") String oauthVerifier) {
        Map accessToken = oauth2ServiceImp.getAccessTokenInTwitter(oauthVerifier);
        return "";
    }

    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    public String getUserInfo() {
        return oauth2ServiceImp.getUserInfo();
    }

    @RequestMapping(value = "/tweet", method = RequestMethod.GET)
    public String tweet() {
        return oauth2ServiceImp.tweetTest("测试一起发" + System.currentTimeMillis());
    }
}
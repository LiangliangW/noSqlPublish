package com.wll.nosqlpublish.controller;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
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

import com.wll.nosqlpublish.service.imp.Oauth2ServiceImp;

@RestController
@RequestMapping(value = "/")
public class UserController {

    @Autowired
    Oauth2ServiceImp oauth2ServiceImp;

    protected final Log logger = LogFactory.getLog(this.getClass());

    @GetMapping(value = "/oauth")
    public ModelAndView loginWithOauth() {
        //这个重定向，会访问两次，初始就访问一次，手动输入不会
        return new ModelAndView(new RedirectView("https://www.facebook.com/dialog/oauth?client_id=469354166884422&redirect_uri=http://localhost:8080/code&response_type=code&state=nfeXN4&scope=publish_pages,manage_pages,publish_to_groups"));
    }

    @GetMapping(value = "/code")
    public String getCode(@RequestParam("code") String code) {
        logger.info("time: " + System.currentTimeMillis());
        String accessToken = oauth2ServiceImp.getAccessToken(code);
        logger.info("access_token: " + accessToken);
//        String pageId = "239040333455132";
//        String res = oauth2Service.publishPage(accessToken, pageId);
        String groupId = "109289593293363";
        String res = oauth2ServiceImp.publishGroup(accessToken, groupId);
        return res;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        return oauth2ServiceImp.authTwitter();
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
        return oauth2ServiceImp.tweetTest("Myfirsttwitterbycoding");
    }

    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    public String getUserInfo() {
        return oauth2ServiceImp.getUserInfo();
    }

    @RequestMapping(value = "/tweet", method = RequestMethod.GET)
    public String tweet() {
        return oauth2ServiceImp.tweetTest("My first twitter by coding");
    }
}
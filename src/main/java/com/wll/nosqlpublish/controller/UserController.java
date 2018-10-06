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
        String httpMethod = "POST";
        String baseUrl = "https://api.twitter.com/oauth/request_token";

        Map<String, String> bodyParams = new HashMap();
        String oauthCallBack = "http://127.0.0.1:8080/getOauthVerifier";
        bodyParams.put("oauth_callback", oauthCallBack);

        //因为 oauth_callback 没有放进 AuthString ，所以此方法不成功。若以后有类似需求，重写 getAuthString 方法
        String authString = oauth2ServiceImp.getAuthString(httpMethod, baseUrl, bodyParams, null);
        Map<String, String> oauthTokenMap = oauth2ServiceImp.getOauthToken(authString);
        String result = oauth2ServiceImp.getOauthVerifierToRedirect(oauthTokenMap.get("oauthToken"));
        return result;
    }

    @RequestMapping(value = "/authTwitter", method = RequestMethod.GET)
    public String authTwitter() throws UnsupportedEncodingException, SignatureException {
        String httpMethod = "POST";
        String baseUrl = "https://api.twitter.com/oauth/request_token";
        String oauthCallBack = "http://127.0.0.1:8080/getOauthVerifier";
        String oauthConsumerKey = "fsbFHibUYg7eOWEwCwCFTFpM9";
        String oauthSignatureMethod = "HMAC-SHA1";
        String oauthTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String oauthVersion = "1.0";
        String consumerSecret = "RICjgnsk1tOIXki5w2jTw6txf0NYdVYbgzYJd5MioW8fFuZBw9";

        Map<String, String> params = new HashMap();
        params.put("oauth_callback", oauthCallBack);
        params.put("oauth_consumer_key", oauthConsumerKey);
        params.put("oauth_nonce", oauth2ServiceImp.getOauthNonce());
        params.put("oauth_signature_method", oauthSignatureMethod);
        params.put("oauth_timestamp", oauthTimestamp);
        params.put("oauth_version", oauthVersion);

        String signature = oauth2ServiceImp.getSignature(httpMethod, baseUrl, params, consumerSecret, "");
        params.put("oauth_signature", signature);
        String authString = oauth2ServiceImp.getAuthStringByParams(params);
        Map<String, String> oauthTokenMap = oauth2ServiceImp.getOauthToken(authString);
        String result = oauth2ServiceImp.getOauthVerifierToRedirect(oauthTokenMap.get("oauthToken"));
        return result;

    }

    @RequestMapping(value = "getOauthVerifier", method = RequestMethod.GET)
    public String getOauthVerifierFromRedirect(@RequestParam("oauth_token") String oauthToken, @RequestParam("oauth_verifier") String oauthVerifier) {
        Map accessTokenMap = oauth2ServiceImp.getAccessTokenInTwitter(oauthVerifier);
        return oauth2ServiceImp.getUserInfo();
    }

}
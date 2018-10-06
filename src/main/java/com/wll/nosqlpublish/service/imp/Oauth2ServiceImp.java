package com.wll.nosqlpublish.service.imp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wll.nosqlpublish.util.HmacSha1Util;
import com.wll.nosqlpublish.util.HttpUtil;

@Service
public class Oauth2ServiceImp {

    private String oauthToken;
    private String oauthTokenSecret;

    protected final Log logger = LogFactory.getLog(this.getClass());

    public String getAccessToken(String code){
        String appId = "469354166884422";
        String clientSecret = "0d7f92e87dc2322c9364fa302d436be5";
        String redirectUri = "http://localhost:8080/code";
        String accessTokenUrl = "https://graph.facebook.com/v3.1/oauth/access_token?"
            + "client_id=" + appId
            + "&redirect_uri=" + redirectUri
            + "&client_secret=" + clientSecret
            + "&code=" + code;
        String res =  HttpUtil.get(accessTokenUrl);
        JSONObject jsonObject = JSONObject.parseObject(res);
        return jsonObject.getString("access_token");
    }

    public String publishPage(String accessToken, String pageId) {
        //获取主页的accessToken
        String getPageAccessToken = "https://graph.facebook.com/v3.1/me/accounts";
        JSONObject json = HttpUtil.getJsonObject(getPageAccessToken + "?access_token="+accessToken);
        logger.info("json: " + json);
        JSONArray jsonArray = (JSONArray)json.get("data");
        JSONObject page1 = null;
        for(int i=0; i < jsonArray.size(); i++) {
            JSONObject tmp = jsonArray.getJSONObject(i);
            if (tmp.getString("id").equals(pageId))
                page1 = tmp;
        }
        if(page1 == null){
            logger.error("pageId： " + pageId + "不存在");
            return "pageId： " + pageId + "不存在";
        }

        String pageAccessToken = page1.getString("access_token");
        String pargeId = page1.getString("id");


        //调用以主页身份发帖api
        String message = "This is a message from focus" + System.currentTimeMillis();
        logger.info("message: " + message);
        Map<String, String> pageParams = new HashMap<>();
        String publishPageUrl = "https://graph.facebook.com/v3.1/" + pargeId + "/feed";
        pageParams.put("message", message);
        pageParams.put("access_token", pageAccessToken);
        String res  = HttpUtil.post(publishPageUrl, pageParams);
        logger.info(res);
        return res;
    }

    public String publishGroup(String accessToken, String groupId){
        String message = "为什么一次发两条";
        Map<String, String> groupParams = new HashMap<>();
        String publishGroupUrl = "https://graph.facebook.com/v3.1/" + groupId + "/feed";
        groupParams.put("message", message);
        groupParams.put("access_token", accessToken);
        String res  = HttpUtil.post(publishGroupUrl, groupParams);
        return res;
    }



    /**
     * The value was generated by base64 encoding 32 bytes of random data, and stripping out all non-word characters,
     * but any approach which produces a relatively random alphanumeric string should be OK here.
     * @return
     */
    public String getOauthNonce() {
        String result = RandomStringUtils.randomAlphanumeric(32);
        logger.info("WLL's log: OauthNonce: " + result);
        return result;
    }

    public String getParameterString(Map params) {
        Map<String, String> sortedParams = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return  o1.compareTo(o2);
            }
        });

        Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            try {
                sortedParams.put(URLEncoder.encode(entry.getKey(), "utf-8"), URLEncoder.encode(entry.getValue(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> sortedEntries = sortedParams.entrySet().iterator();
        while (sortedEntries.hasNext()) {
            Map.Entry<String, String> sortedEntry = sortedEntries.next();
            stringBuilder.append(sortedEntry.getKey() + '=' + sortedEntry.getValue() + '&');
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        String result = stringBuilder.toString();
        logger.info("WLL's log: ParameterString: " + result);
        return result;
    }

    public String getBaseString(String httpMethod, String baseUrl, String params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(httpMethod + '&');
        try {
            stringBuilder.append(URLEncoder.encode(baseUrl, "utf-8") + '&');
            stringBuilder.append(URLEncoder.encode(params, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String result = stringBuilder.toString();
        logger.info("WLL's log: SignatureBaseString: " + result);
        return result;
    }

    public String getSigningKey(String consumerSecret, String oAuthTokenSecret) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(URLEncoder.encode(consumerSecret, "utf-8") + '&');
            if (this.oauthTokenSecret != null) {
                stringBuilder.append(URLEncoder.encode(oAuthTokenSecret, "utf-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String result = stringBuilder.toString();
        logger.info("WLL's log: SigningKey: " + result);
        return result;
    }

    public String calculateSignatue(String baseString, String signingKey) throws SignatureException {
        String result = HmacSha1Util.calculateRFC2104HMAC(baseString, signingKey);
        logger.info("WLL's log: Signatue: " + result);
        return result;
    }

    public String getSignature(String httpMethod, String baseUrl, Map params, String consumerSecret, String oauthTokenSecret) {
        String signature = null;
        try {
            String signatureParamString = getParameterString(params);
            String baseString = getBaseString(httpMethod, baseUrl, signatureParamString);
            String signingKey = getSigningKey(consumerSecret, oauthTokenSecret);
            signature = calculateSignatue(baseString, signingKey);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public String getAuthStringByParams(Map params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OAuth ");

        Iterator<Map.Entry<String, String>> entryIterator = params.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            try {
                stringBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8") + "=\"" + URLEncoder.encode(entry.getValue(), "utf-8") + "\", ");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //删掉最后的逗号和空格
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        String result = stringBuilder.toString();
        logger.info("WLL's log: AuthString: " + result);
        return result;
    }

    public String getAuthString(String httpMethod, String baseUrl, Map<String, String> bodyParams, Map<String, String> headerParams) {
        String oauthConsumerKey = "fsbFHibUYg7eOWEwCwCFTFpM9";
        String oauthSignatureMethod = "HMAC-SHA1";
        String oauthNonce = getOauthNonce();
        String oauthTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String oauthVersion = "1.0";
        String consumerSecret = "RICjgnsk1tOIXki5w2jTw6txf0NYdVYbgzYJd5MioW8fFuZBw9";

        Map<String, String> authParams = new HashMap<>();
        authParams.put("oauth_consumer_key", oauthConsumerKey);
        authParams.put("oauth_nonce", oauthNonce);
        authParams.put("oauth_signature_method", oauthSignatureMethod);
        authParams.put("oauth_timestamp", oauthTimestamp);
        authParams.put("oauth_version", oauthVersion);
        if (this.oauthToken != null) {
            authParams.put("oauth_token", this.oauthToken);
        }

        Map<String, String> signatureParams = new HashMap<>();
        signatureParams.putAll(authParams);
        if (bodyParams != null) {
            signatureParams.putAll(bodyParams);
        }
        if (headerParams != null) {
            signatureParams.putAll(headerParams);
        }

        String signature = getSignature(httpMethod, baseUrl, signatureParams, consumerSecret, this.oauthTokenSecret);

        authParams.put("oauth_signature", signature);
        String authString = getAuthStringByParams(authParams);

        return authString;
    }

    public Map<String, String> getOauthToken(String authString) {
        String requestTokenUrl = "https://api.twitter.com/oauth/request_token";
        Map<String, String> header = new HashMap<String, String>();
        header.put("Authorization", authString);
        String httpResult = HttpUtil.post(requestTokenUrl, null, header, "UTF-8");
        logger.info("WLL's log: OauthToken: " + httpResult);

        String[] authResults = httpResult.split("&");
        if (authResults[0].substring(0, authResults[0].indexOf('=')).equals("oauth_token")){
            String oauthToken = authResults[0].substring(authResults[0].indexOf('=') + 1);
            String oauthTokenSecret = authResults[1].substring(authResults[1].indexOf('=') + 1);
            String oauthCallbackConfirmed = authResults[2].substring(authResults[2].indexOf('=') + 1);

            Map<String, String> authResultMap = new HashMap<String, String>();
            authResultMap.put("oauthToken", oauthToken);
            authResultMap.put("oauthTokenSecret", oauthTokenSecret);
            authResultMap.put("oauthCallbackConfirmed", oauthCallbackConfirmed);

            this.oauthToken = oauthToken;
            this.oauthTokenSecret = oauthTokenSecret;

            return authResultMap;
        } else {
            return null;
        }
    }

    public String getOauthVerifierToRedirect(String oauthToken) {
        String requestUrl = "https://api.twitter.com/oauth/authenticate?oauth_token=" + oauthToken;
        String result = HttpUtil.get(requestUrl);
        return result;
    }

    public String authTwitter() {
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
        params.put("oauth_nonce", getOauthNonce());
        params.put("oauth_signature_method", oauthSignatureMethod);
        params.put("oauth_timestamp", oauthTimestamp);
        params.put("oauth_version", oauthVersion);

        String signature = getSignature(httpMethod, baseUrl, params, consumerSecret, "");
        params.put("oauth_signature", signature);
        String authString = getAuthStringByParams(params);
        Map<String, String> oauthTokenMap = getOauthToken(authString);
        String result = getOauthVerifierToRedirect(oauthTokenMap.get("oauthToken"));
        return result;
    }

    //因为 oauth_callback 没有放进 AuthString ，所以此方法不成功。若以后有类似需求，重写 getAuthString 方法
    public String authTwitterPlus() {
        String httpMethod = "POST";
        String baseUrl = "https://api.twitter.com/oauth/request_token";

        Map<String, String> bodyParams = new HashMap();
        String oauthCallBack = "http://127.0.0.1:8080/getOauthVerifier";
        bodyParams.put("oauth_callback", oauthCallBack);

        String authString = getAuthString(httpMethod, baseUrl, bodyParams, null);
        Map<String, String> oauthTokenMap = getOauthToken(authString);
        String result = getOauthVerifierToRedirect(oauthTokenMap.get("oauthToken"));
        return result;
    }

    public Map<String, String> getAccessTokenInTwitter(String oauthVerifier) {
        logger.info("WLL's log: oauthVerifier: " + oauthVerifier);

        String httpMethod = "POST";
        String baseUrl = "https://api.twitter.com/oauth/access_token";
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("oauth_verifier", oauthVerifier);
        String authString = getAuthString(httpMethod, baseUrl, bodyParams, null);

        Map<String, String> header = new HashMap<String, String>();
        header.put("Authorization", authString);
        Map<String, String> body = new HashMap<String, String>();
        body.put("oauth_verifier", oauthVerifier);
        String httpResult = HttpUtil.post(baseUrl, body, header, "UTF-8");
        logger.info("WLL's log: accessToken: " + httpResult);

        String[] accessTokenResults = httpResult.split("&");
        if (accessTokenResults[0].substring(0, accessTokenResults[0].indexOf('=')).equals("oauth_token")){
            String oauthToken = accessTokenResults[0].substring(accessTokenResults[0].indexOf('=') + 1);
            String oauthTokenSecret = accessTokenResults[1].substring(accessTokenResults[1].indexOf('=') + 1);

            Map<String, String> authResultMap = new HashMap<String, String>();
            authResultMap.put("oauthToken", oauthToken);
            authResultMap.put("oauthTokenSecret", oauthTokenSecret);

            this.oauthToken = oauthToken;
            this.oauthTokenSecret = oauthTokenSecret;

            return authResultMap;
        } else {
            return null;
        }
    }

    public String getUserInfo() {
        String httpMethod = "GET";
        String baseUrl = "https://api.twitter.com/1.1/account/verify_credentials.json";
        String authString = getAuthString(httpMethod, baseUrl, null, null);

        Map<String, String> header = new HashMap<String, String>();
        header.put("Authorization", authString);
        String httpResult = HttpUtil.get(baseUrl, header, "utf-8");

        return httpResult;
    }

//    public String tweetTest(String test) {
//        String httpMethod = "POST";
//        String baseUrl = "https://api.twitter.com/1.1/statuses/update.json";
//
//    }
}

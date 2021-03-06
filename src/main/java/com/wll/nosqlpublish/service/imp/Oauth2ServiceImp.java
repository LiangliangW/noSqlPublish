package com.wll.nosqlpublish.service.imp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wll.nosqlpublish.constant.HttpMethodEnum;
import com.wll.nosqlpublish.util.HmacSha1Util;
import com.wll.nosqlpublish.util.HttpUtil;
import com.wll.nosqlpublish.util.UrlEncodeUtil;

@Service
public class Oauth2ServiceImp {


    @Value("${access_token.twitter.oauthToken}")
    public String oauthToken;
    @Value("${access_token.twitter.oauthTokenSecret}")
    private String oauthTokenSecret;

    @Value("${access_token.facebook}")
    public String facebookAccessToken;

    @Value("${facebook.appId}")
    public String facebookAppId;

    @Value("${facebook.appSecret}")
    public String facebookAppSecret;


    protected final Log logger = LogFactory.getLog(this.getClass());

    public String loginFacebook() {
        String loginUrl = "https://www.facebook.com/dialog/oauth?client_id=469354166884422&redirect_uri=https://localhost:8080/code&response_type=code&state=nfeXN4&scope=publish_pages,manage_pages,publish_to_groups";
        return HttpUtil.get(loginUrl);
    }

    public String getAccessToken(String code){

        //Hinson's facebook app id and secret
//        String appId = "469354166884422";
//        String clientSecret = "0d7f92e87dc2322c9364fa302d436be5";


        String appId = this.facebookAppId;
        String clientSecret = this.facebookAppSecret;
        String redirectUri = "https://localhost:8443/code";
        String accessTokenUrl = "https://graph.facebook.com/v3.1/oauth/access_token?"
            + "client_id=" + appId
            + "&redirect_uri=" + redirectUri
            + "&client_secret=" + clientSecret
            + "&code=" + code;
        String res =  HttpUtil.get(accessTokenUrl);
        logger.info(res);
        JSONObject jsonObject = JSONObject.parseObject(res);
        String accessToken = jsonObject.getString("access_token");
        this.facebookAccessToken = accessToken;
        logger.info("WLL's log: After Facebook oauthToken: " + this.oauthToken);
        return accessToken;
    }

    /**
     * 根据用户的accessToken获取主页的accessToken
     * @param pageId
     * @return
     */
    public String getPageAccessToken(String pageId) {

        String getPageAccessToken = "https://graph.facebook.com/v3.1/me/accounts";
        JSONObject json = HttpUtil.getJsonObject(getPageAccessToken + "?access_token="+ this.facebookAccessToken);
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
        return pageAccessToken;
    }

    /**
     * 以主页身份发帖
     * @param pageParams
     * @return
     */
    public String publishPage(Map<String, String> pageParams, String targetUrl) {
        String pageId = "239040333455132";
        //根据accessToken获取pageAccessToken
        String pageAccessToken = getPageAccessToken(pageId);
        //调用以主页身份发帖api
        pageParams.put("access_token", pageAccessToken);
        logger.info("pageParams: " + pageParams);
        String publishPageUrl = "https://graph.facebook.com/v3.1/" + pageId + targetUrl;
        String res  = HttpUtil.post(publishPageUrl, pageParams);
        logger.info(res);
        return res;
    }

    /**
     * 以主页身份发布文字帖子
     * @param message
     * @return
     */
    public String publishPageWithCharacters(String message) {
        Map<String, String> pageParams = new HashMap<>();
        pageParams.put("message", message);
        return publishPage(pageParams, "/feed");
    }

    /**
     * 以主页身份发布图片帖子
     * @param url
     * @return
     */
    public String publishPageWithPhoto(String url) {
        Map<String, String> pageParams = new HashMap<>();
        pageParams.put("url", url);
        return publishPage(pageParams, "/photos");
    }

    /**
     * 以主页身份发布视频帖子
     * @param file_url
     * @return
     */
    public String publishPageWithVideo(String file_url) {
        Map<String, String> pageParams = new HashMap<>();
        pageParams.put("file_url", file_url);
        pageParams.put("name", "focus server upload video");
        pageParams.put("description", "This video is uploaded from foucs server");
        return publishPage(pageParams, "/videos");
    }

    /**
     * 向小组发帖
     * @param groupParams, targetUrl
     * @return
     */
    private String publishGroup(Map<String, String> groupParams, String targetUrl){
        String groupId = "109289593293363";
//        String groupId = "473186779854545";

        String publishGroupUrl = "https://graph.facebook.com/v3.1/" + groupId + targetUrl;
        groupParams.put("access_token", this.facebookAccessToken);
        String res  = HttpUtil.post(publishGroupUrl, groupParams);
        return res;
    }

    /**
     * 在小组中发布文字帖子
     * @param message
     * @return
     */
    public String publishGroupWithCharacters(String message) {
        Map<String, String> pageParams = new HashMap<>();
        pageParams.put("message", message);
        return publishGroup(pageParams, "/feed");
    }

    /**
     * 在小组中发布图片帖子
     * @param url
     * @return
     */
    public String publishGroupWithPhoto(String url) {
        Map<String, String> pageParams = new HashMap<>();
        pageParams.put("url", url);
        return publishGroup(pageParams, "/photos");
    }

    /**
     * 在小组中发布视频帖子
     * @param file_url
     * @return
     */
    public String publishGroupWithVideo(String file_url) {
        Map<String, String> pageParams = new HashMap<>();
        pageParams.put("file_url", file_url);
        pageParams.put("name", "focus server upload video");
        pageParams.put("description", "This video is uploaded from foucs server");
        return publishGroup(pageParams, "/videos");
    }

    /**
     * 分片上传facebook视频
     * @param pageOrGroupId
     * @param filePath
     * @param access_token
     * @return
     */
    public String facebookChunkedUploadVideoInit(String pageOrGroupId, String filePath, String access_token) {
        String postUrl = "https://graph.facebook.com/" + pageOrGroupId + "/videos";
        Long fileSize = new File(filePath).length();
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("access_token", access_token);
        bodyParams.put("upload_phase", "start");
        bodyParams.put("file_size", fileSize.toString());
        return HttpUtil.post(postUrl, bodyParams);
    }

    public String facebookChunkedUploadVideoFinish(String pageOrGroupId, String uploadSessionId, String access_token) {
        String postUrl = "https://graph.facebook.com/" + pageOrGroupId + "/videos";
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("access_token", access_token);
        bodyParams.put("upload_phase", "finish");
        bodyParams.put("upload_session_id", uploadSessionId);
        return HttpUtil.post(postUrl, bodyParams);
    }

    public String facebookChunkedUpload(String pageOrGroupId, String filePath, String accessToken) {
        String baseUrl = "https://graph.facebook.com/" + pageOrGroupId + "/videos";
        Long fileSize = new File(filePath).length();
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("access_token", accessToken);
        bodyParams.put("upload_phase", "start");
        bodyParams.put("file_size", fileSize.toString());
        String initRes = HttpUtil.post(baseUrl, bodyParams);
        JSONObject initResJson = JSONObject.parseObject(initRes);
        String videoId = initResJson.getString("video_id");
        String uploadSessionId = initResJson.getString("upload_session_id");
        long startOffset = initResJson.getLong("start_offset");
        long endOffset = initResJson.getLong("end_offset");

        BufferedInputStream bufferedInputStream = null;

        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }

        int chunkIndex = 0;
        while (startOffset < endOffset) {
            //上传完毕后，请求返回的startOffset = endOffset
            String multipartFileName = "chunk" + chunkIndex+".mp4";
            logger.info("hinson'log: startOffset: " + startOffset);
            logger.info("hinson'log: endOffset: " + endOffset);
            String uploadChunkRes = facebookChunkedUploadChunk(pageOrGroupId, accessToken, uploadSessionId,
                multipartFileName, bufferedInputStream, startOffset, endOffset);
            logger.info("hinson'log: uploadChunkRes: " + uploadChunkRes);
            JSONObject uploadChunkResJson = JSONObject.parseObject(uploadChunkRes);
            JSONObject data = uploadChunkResJson.getJSONObject("data");
            startOffset = data.getLong("start_offset");
            endOffset = data.getLong("end_offset");
        }

        //上传完毕，
        String finishRes = facebookChunkedUploadVideoFinish(pageOrGroupId, uploadSessionId, accessToken);
        JSONObject finishResJson = JSONObject.parseObject(finishRes);
        if (finishResJson.getBoolean("success") == true) {
            return "上传成功";
        }else {
            return "上传失败";
        }
    }

    public String facebookChunkedUploading(String pageOrGroupId, String filePath, String access_token,
        String uploadSessionId, long startOffset, long endOffset) {
        BufferedInputStream bufferedInputStream = null;
        FileInputStream fileInputStream = null;
        String uploadRes = null;
        String chunkName = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            chunkName = "chunk1.mp4";
            uploadRes = facebookChunkedUploadChunk(pageOrGroupId, access_token, uploadSessionId,
                chunkName, bufferedInputStream, startOffset, endOffset);
            return uploadRes;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "失败";
    }
    public String facebookChunkedUploadChunk(String pageOrGroupId, String access_token,
        String uploadSessionId, String ChunkName,
        BufferedInputStream bufferedInputStream, long startOffset, long endOffset) {
        String postUrl = "https://graph.facebook.com/" + pageOrGroupId + "/videos";
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("access_token", access_token);
        bodyParams.put("upload_phase", "transfer");
        bodyParams.put("start_offset", String.valueOf(startOffset));
        bodyParams.put("upload_session_id", uploadSessionId);
        return HttpUtil.postChunkInputStream(postUrl, bodyParams, null, bufferedInputStream,
            startOffset, endOffset, "video_file_chunk", ChunkName,
            "UTF-8", 40000, 40000);
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
            sortedParams.put(UrlEncodeUtil.encode(entry.getKey()), UrlEncodeUtil.encode(entry.getValue()));

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
        stringBuilder.append(UrlEncodeUtil.encode(httpMethod) + '&');
        stringBuilder.append(UrlEncodeUtil.encode(baseUrl) + '&');
        stringBuilder.append(UrlEncodeUtil.encode(params));

        String result = stringBuilder.toString();
        logger.info("WLL's log: SignatureBaseString: " + result);
        return result;
    }

    public String getSigningKey(String consumerSecret, String oAuthTokenSecret) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UrlEncodeUtil.encode(consumerSecret) + '&');
        if (this.oauthTokenSecret != null) {
            stringBuilder.append(UrlEncodeUtil.encode(oAuthTokenSecret));
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

    /**
     * https://developer.twitter.com/en/docs/basics/authentication/guides/creating-a-signature
     * @param httpMethod
     * @param baseUrl
     * @param params
     * @param consumerSecret
     * @param oauthTokenSecret
     * @return
     */
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
            stringBuilder.append(UrlEncodeUtil.encode(entry.getKey()) + "=\"" + UrlEncodeUtil.encode(entry.getValue()) + "\", ");
        }
        //删掉最后的逗号和空格
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        String result = stringBuilder.toString();
        logger.info("WLL's log: AuthString: " + result);
        return result;
    }

    /**
     * https://developer.twitter.com/en/docs/basics/authentication/guides/authorizing-a-request
     * @param httpMethod
     * @param baseUrl
     * @param bodyParams
     * @param headerParams
     * @return
     */
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
            logger.info("hinson's oauthToken: " + this.oauthToken);
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

    /**
     * https://developer.twitter.com/en/docs/basics/authentication/api-reference/request_token
     * @param authString
     * @return
     */
    public Map<String, String> getOauthToken(String authString) {
        String requestTokenUrl = "https://api.twitter.com/oauth/request_token";
        Map<String, String> header = new HashMap<String, String>();
        header.put("Authorization", authString);
        String httpResult = HttpUtil.post(requestTokenUrl, null, header);
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
            logger.info("hinson'log: " + this.oauthToken);
            logger.info("hinson'log: " + this.oauthTokenSecret);

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
        String oauthCallBack = "https://127.0.0.1:8443/getOauthVerifier";
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
        String oauthCallBack = "https://127.0.0.1:8443/getOauthVerifier";
        bodyParams.put("oauth_callback", oauthCallBack);

        String authString = getAuthString(httpMethod, baseUrl, bodyParams, null);
        Map<String, String> oauthTokenMap = getOauthToken(authString);
        String result = getOauthVerifierToRedirect(oauthTokenMap.get("oauthToken"));
        return result;
    }

    /**
     * https://developer.twitter.com/en/docs/basics/authentication/api-reference/access_token
     * @param oauthVerifier
     * @return
     */
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
        String httpResult = HttpUtil.post(baseUrl, body, header);
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

        logger.info("WLL's log: UserInfo: " + httpResult);
        return httpResult;
    }

    public String tweetTest(String text) {
        return tweetTest(text, null);
    }

    /**
     * https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/post-statuses-update#post-statuses-update
     * @param text
     * @param mediaIds
     * @return
     */
    public String tweetTest(String text, String mediaIds) {
        String httpMethod = "POST";
        String baseUrl = "https://api.twitter.com/1.1/statuses/update.json";
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("status", text);
        if (!StringUtils.isBlank(mediaIds)) {
            bodyParams.put("media_ids", mediaIds);
        }
        String authString = getAuthString(httpMethod, baseUrl, bodyParams, null);

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", authString);
        //由于 HTTP 请求 body 中的 "status" 参数必须是经过URL编码的，但是生成signature时不能是提前编码过的，所以覆盖此参数
        bodyParams.put("status", UrlEncodeUtil.encode(text));
        if (!StringUtils.isBlank(mediaIds)) {
            bodyParams.put("media_ids", mediaIds);
        }
        String httpResult = HttpUtil.post(baseUrl, bodyParams, header);

        logger.info("WLL's log: TweetResponse: " + httpResult);
        return httpResult;
    }

    /**
     * https://developer.twitter.com/en/docs/media/upload-media/api-reference/post-media-upload
     * @param filePath
     * @return
     */
    public String tweetUploadSingleImage(String filePath) {
         String httpMethod = "POST";
         String baseUrl = "https://upload.twitter.com/1.1/media/upload.json";
         //注意，media相关的，signature 只包含 oauth_* 的参数
         String authString = getAuthString(httpMethod, baseUrl, null, null);

         Map<String, String> header = new HashMap<>();
         header.put("Authorization", authString);

         Map<String, String> files = new HashMap<>();
         files.put("media", filePath);

         String httpResult = HttpUtil.post(baseUrl, null, header, files, "utf-8");
         logger.info("WLL's log: TwitterSingleImage: " + httpResult);

         JSONObject imageResult = JSON.parseObject(httpResult);
         String mediaId = imageResult.getString("media_id_string");
         return mediaId;
    }

    /**
     * https://developer.twitter.com/en/docs/media/upload-media/api-reference/post-media-upload-init
     * @param filePath
     * @param mediaType
     * @return
     */
    public String tweetChunkedUploadInit(String filePath, String mediaType) {
        String httpMethod = "POST";
        String baseUrl = "https://upload.twitter.com/1.1/media/upload.json";

        Map<String, String> bodyParams = new HashMap<>();
        Long fileSize = new File(filePath).length();
        bodyParams.put("command", "INIT");
        bodyParams.put("media_type", mediaType);
        bodyParams.put("total_bytes", String.valueOf(fileSize));

        Map<String, String> header = new HashMap<>();
        String authString = getAuthString(httpMethod, baseUrl, bodyParams, null);
        header.put("Authorization", authString);

        bodyParams.put("media_type", UrlEncodeUtil.encode(mediaType));
        String httpResult = HttpUtil.post(baseUrl, bodyParams, header);
        logger.info("WLL's log: TweetChunkedUploadInit: " + httpResult);

        JSONObject initResult = JSON.parseObject(httpResult);
        String mediaId = initResult.getString("media_id_string");

        return mediaId;
    }

    /**
     * https://developer.twitter.com/en/docs/media/upload-media/api-reference/post-media-upload-append
     * @param filePath
     * @param mediaId
     * @param segmentIndex
     * @return
     */
    public String tweetChunkedUploadAppend(String filePath, String mediaId, Integer segmentIndex) {
        String httpMethod = HttpMethodEnum.POST.getMethod();
        String baseUrl = "https://upload.twitter.com/1.1/media/upload.json";

        Map<String, String> files = new HashMap<>();
        files.put("media", filePath);

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("command", "APPEND");
        bodyParams.put("media_id", mediaId);
        bodyParams.put("segment_index", String.valueOf(segmentIndex));

        Map<String, String> header = new HashMap<>();
        String authString = getAuthString(httpMethod, baseUrl, null, null);
        header.put("Authorization", authString);

        String httpResult = HttpUtil.post(baseUrl, bodyParams, header, files, "utf-8");
        logger.info("WLL's log: TwitterChunkedUploadAppend: " + httpResult);

        return httpResult;
    }

    /**
     * https://developer.twitter.com/en/docs/media/upload-media/api-reference/post-media-upload-finalize
     * Twitter media 完成上传
     * @param mediaId
     * @return -1：错误； 0：OK； others：checkAfterSecs
     */
    public Integer tweetChunkedUploadFinalize(String mediaId) {
        String httpMethod = "POST";
        String baseUrl = "https://upload.twitter.com/1.1/media/upload.json";

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("command", "FINALIZE");
        bodyParams.put("media_id", mediaId);

        Map<String, String> header = new HashMap<>();
        String authString = getAuthString(httpMethod, baseUrl, bodyParams, null);
        header.put("Authorization", authString);

        String httpResult = HttpUtil.post(baseUrl, bodyParams, header);
        logger.info("WLL's log: tweetChunkedUploadFinalize: " + httpResult);

        JSONObject finalizeResult = JSON.parseObject(httpResult);
        JSONObject processingInfo = finalizeResult.getJSONObject("processing_info");
        if (processingInfo != null) {
            String state = processingInfo.getString("state");
            if (state.equals("failed")) {
                String errorMsg = processingInfo.getJSONObject("error").getString("message");
                logger.error("WLL's log: ERROR : tweetChunkedUploadFinalize: " + httpResult);
                return -1;
            } else {
                Integer checkAfterSecs = processingInfo.getInteger("check_after_secs");
                return checkAfterSecs;
            }
        } else {
            return 0;
        }
    }

    /**
     * https://developer.twitter.com/en/docs/media/upload-media/api-reference/get-media-upload-status
     * Twitter media 检测上传状态
     * @param mediaId
     * @return -1：错误； 0：OK； others：checkAfterSecs
     */
    public Integer tweetChunkedUploadStatus(String mediaId) {
        String httpMethod = HttpMethodEnum.GET.getMethod();
        String baseUrl = "https://upload.twitter.com/1.1/media/upload.json?command=STATUS&media_id=" + mediaId;

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("command", "FINALIZE");
        bodyParams.put("media_id", mediaId);

        Map<String, String> header = new HashMap<>();
        String baseUrlAuth = "https://upload.twitter.com/1.1/media/upload.json";
        String authString = getAuthString(httpMethod, baseUrlAuth, bodyParams, null);
        header.put("Authorization", authString);

        String httpResult = HttpUtil.get(baseUrl);
        logger.info("WLL's log: tweetChunkedUploadFinalize: " + httpResult);

        JSONObject statusResult = JSON.parseObject(httpResult);
        JSONObject processingInfo = statusResult.getJSONObject("processing_info");
        if (processingInfo != null) {
            String state = processingInfo.getString("state");
            if (state.equals("failed")) {
                String errorMsg = processingInfo.getJSONObject("error").getString("message");
                logger.error("WLL's log: ERROR : tweetChunkedUploadFinalize: " + httpResult);
                return -1;
            } else if (state.equals("in_progress")){
                Integer checkAfterSecs = processingInfo.getInteger("check_after_secs");
                return checkAfterSecs;
            } else if (state.equals("succeeded")) {
                return 0;
            }
        }
        return -1;
    }

    public String tweetVideoTmp(String text, String filePath, String mediaType) {
        String mediaId = tweetChunkedUploadInit(filePath, mediaType);
        String appendResult = tweetChunkedUploadAppend(filePath, mediaId, 0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Integer finalizeResult = tweetChunkedUploadFinalize(mediaId);
        if (finalizeResult == 0) {
            return tweetTest(text, mediaId);
        } else {
            return "fail";
        }
    }

    public String tweetChunkedUpload(String filePath, String mediaType) {
        //初始化
        String initRes = tweetChunkedUploadInit(filePath, mediaType);
        JSONObject initResJson = JSONObject.parseObject(initRes);
        logger.info("hinson'log: initResJson: " + initResJson);
        String mediaId = initResJson.getString("media_id");
        logger.info("hinson'log: mediaId: " + mediaId);
        int chunkIndex = 0;//从0开始
        BufferedInputStream bufferedInputStream = null;
        long fileSize = new File(filePath).length();
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
        long startOffSet = 0;
//        long endOffSet = 1048576;//1Mb
        long endOffSet = 1048576;
        logger.info("hisnon's log: fileSize = " + fileSize);
        while(startOffSet < fileSize) {
            logger.info("hinson'log: uploadChunkRes: chunkIndex = " + chunkIndex);
            logger.info("hinson'log: uploadChunkRes: startOffSet = " + startOffSet +" endOffset= " + endOffSet);
            String uploadChunkRes = tweetChunkedUploadChunks(mediaId, chunkIndex,
                bufferedInputStream, startOffSet, endOffSet);
            logger.info("hinson'log: uploadChunkRes: " + uploadChunkRes);
            chunkIndex++;
            startOffSet = endOffSet + 1;
            endOffSet = startOffSet + 1048576; //每次1Mb
            if(endOffSet > fileSize) {
                endOffSet = fileSize - 1;
            }
        }

        //下面这步应该要等待一段时间后再访问，因为twitter合成视频需要时间
        //这里视频不大，就直接访问了
        int mediaStatus = tweetChunkedUploadFinalize(String.valueOf(mediaId));
        if (mediaStatus == 0) {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = dateformat.format(System.currentTimeMillis());
            tweetTest("发布自focus后台，时间：" + dateStr, mediaId);
        }
        return "mediaStatus: " + mediaStatus;
    }

    public String tweetChunkedUploadChunks(String mediaId, int segmentIndex,
        BufferedInputStream bufferedInputStream, long startOffset, long endOffset) {

        String httpMethod = "POST";
        String baseUrl = "https://upload.twitter.com/1.1/media/upload.json";

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("command", "APPEND");
        bodyParams.put("media_id", mediaId);
        bodyParams.put("segment_index", String.valueOf(segmentIndex));

        Map<String, String> header = new HashMap<>();
        String authString = getAuthString(httpMethod, baseUrl, null, null);
        header.put("Authorization", authString);


        String multipartFileParam = "media";
        String multipartFileName = "media" + segmentIndex;

        return HttpUtil.postChunkInputStream(baseUrl, bodyParams, header, bufferedInputStream,
            startOffset, endOffset, multipartFileParam,multipartFileName,
            "UTF-8", 40000, 40000);
    }
}

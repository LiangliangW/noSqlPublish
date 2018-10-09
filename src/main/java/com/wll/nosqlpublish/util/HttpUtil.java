package com.wll.nosqlpublish.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static int CONNECT_TIME_OUT = 40000;

    private static int READ_TIME_OUT = 40000;

    private static byte[] BUFFER = new byte[1024];

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static JSONObject getJsonObject(String url) {
        String result = get(url);
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return JSONObject.parseObject(result);
    }

    public static String get(String url) {
        return get(url, null, DEFAULT_CHARSET);
    }

    public static String get(String url, String charset) {
        return get(url, null, charset);
    }

    public static String get(String url, Map<String, String> header, String charset) {
        return get(url, header, charset, CONNECT_TIME_OUT, READ_TIME_OUT);
    }

    public static String get(String url, Map<String, String> header,  String charset,
                             int connectTimeout, int readTimeout) {
        String result = "";
        try {
//            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 1080);
//            Proxy proxy = new Proxy(Type.SOCKS, address);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int responseCode = connection.getResponseCode();
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                InputStream is = connection.getInputStream();
                int readCount;
                while ((readCount = is.read(BUFFER)) > 0) {
                    out.write(BUFFER, 0, readCount);
                }
                is.close();
            } else {
                logger.warn("{} http response code is {}", url, responseCode);
            }
            connection.disconnect();
            result = out.toString();
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
        }
        return result;
    }

    public static JSONObject postJsonObject(String url, Map<String, String> params) {
        String result = post(url, params);
        if (result == null) {
            return null;
        }
        return JSONObject.parseObject(result);
    }

    public static String post(String url, Map<String, String> params) {
        return post(url, params, DEFAULT_CHARSET);
    }

    public static String post(String url, Map<String, String> params, String charset) {
        return post(url, params, null, charset);
    }

    public static String post(String url, Map<String, String> params, Map<String, String> header) {
        return post(url, params, header, null, DEFAULT_CHARSET);
    }

    public static String post(String url, Map<String, String> params, Map<String, String> header, String charset) {
        return post(url, params, header, null, charset);
    }

    public static String post(String url, Map<String, String> params, Map<String, String> header, Map<String, String> files,
                              String charset) {
        return post(url, params, header, files, charset, CONNECT_TIME_OUT, READ_TIME_OUT);
    }


    public static String post(String url, Map<String, String> params, Map<String, String> header, Map<String, String> files,
                              String charset, int connectTimeout, int readTimeout) {
        String result = "";
        OutputStream out = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (files == null || files.size() == 0) {
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                out = new DataOutputStream(connection.getOutputStream());
                if (params != null && params.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        stringBuilder.append(entry.getKey());
                        stringBuilder.append("=");
                        stringBuilder.append(entry.getValue());
                        stringBuilder.append("&");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    logger.info("WLL's log: HttpParams(application/x-www-form-urlencoded): url: " + url + "   params: " + stringBuilder.toString());
                    out.write(stringBuilder.toString().getBytes(charset));
                }
            } else {
                String boundary = "=====" + String.valueOf(new Date().getTime()) + "=====";
                connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
                out = new DataOutputStream(connection.getOutputStream());
                if (params != null && params.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        stringBuilder.append("--" + boundary + "\r\n");
                        stringBuilder.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                        stringBuilder.append(entry.getValue() + "\r\n");
                    }
                    logger.info("WLL's log: HttpParams(multipart/form-data): url: " + url + "   params: " + stringBuilder.toString());
                    out.write(stringBuilder.toString().getBytes(charset));
                }

                for (Map.Entry<String, String> entry : files.entrySet()) {
                    String fileName = entry.getKey();
                    String filePath = entry.getValue();
                    if (StringUtils.isBlank(fileName) || StringUtils.isBlank(filePath)) {
                        continue;
                    }
                    File file = new File(filePath);
                    if (!file.exists()) {
                        continue;
                    }
                    out.write(("--" + boundary + "\r\n").getBytes(charset));
                    out.write(("Content-Disposition: form-data; name=\"" + fileName + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(charset));
                    out.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes(charset));

                    DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
                    int readCount;
                    while ((readCount = inputStream.read(BUFFER)) > 0) {
                        out.write(BUFFER, 0, readCount);
                    }
                    inputStream.close();
                    out.write("\r\n".getBytes(charset));
                }

                out.write(("--" + boundary + "--\r\n").getBytes(charset));
            }
            out.flush();
            out.close();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            if (connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                InputStream is = connection.getInputStream();
                int readCount;
                while ((readCount = is.read(BUFFER)) > 0) {
                    bout.write(BUFFER, 0, readCount);
                }
                is.close();
            }else {
                InputStream is = connection.getErrorStream();
                int readCount;
                while ((readCount = is.read(BUFFER)) > 0) {
                    bout.write(BUFFER, 0, readCount);
                }
                is.close();
            }
            connection.disconnect();
            result = bout.toString();
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
        }
        return result;
    }
}

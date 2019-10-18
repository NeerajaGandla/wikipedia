package com.neeraja.wikipedia.utils;

import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.UnknownHostException;

public class HttpRequest {
    public static final String CONTENT_TYPE = "application/json";
    public static Object getInputStreamFromUrl(String url, Class classOfT,
                                               Context context) throws CustomException {
        Utils.logD("URL : " + url);
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpclient = new DefaultHttpClient();
            httpGet.addHeader("Content-Type", CONTENT_TYPE);
            httpGet.addHeader("Accept",CONTENT_TYPE);

            Utils.logD("Log 2");
            HttpResponse response = httpclient.execute(httpGet);

            Utils.logD("Log 3");
            Utils.logD("Log 3");

            return Utils.parseResp(response.getEntity().getContent(),classOfT);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Globals.lastErrMsg = Constants.SERVER_NOT_REACHABLE;
            throw new CustomException("", Constants.PROB_WITH_SERVER);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.logD(e.toString());
            Globals.lastErrMsg = Constants.DEVICE_CONNECTIVITY;
            throw new CustomException(Constants.DEVICE_CONNECTIVITY, "");
        }
    }
    public static String convertStreamToString(HttpResponse response) {
        String responseBody = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                responseBody = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseBody;
    }

}

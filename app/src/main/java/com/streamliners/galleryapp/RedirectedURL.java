package com.streamliners.galleryapp;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RedirectedURL extends AsyncTask<String, Void, String> {
    private OnCompleteListener listener;

    /**
     * Fetch the redirected URL
     */
    public RedirectedURL fetchRedirectedURL(OnCompleteListener listener1){
        listener=listener1;
        return this;
    }
    //Async methods

    /**
     * After getting response use OnPostExecute method used
     */
    @Override
    protected void onPostExecute(String s) {
        listener.onFetched(s);
    }

    /**
     * When we want to send the request doInBackground method used
     */

    @Override
    protected String doInBackground(String... strings) {
        return getRedirectUrl(strings[0]);
    }

    private String getRedirectUrl(String url){
        URL uTemp = null;
        String redUrl;
        HttpURLConnection connection = null;

        try{
            uTemp = new URL(url);
        } catch (MalformedURLException exp){
            exp.printStackTrace();
        }

        try{
            connection = (HttpURLConnection) uTemp.openConnection();
        } catch (IOException e){
            e.printStackTrace();
        }

        try{
            connection.getResponseCode();
        } catch (IOException e){
            e.printStackTrace();
        }

        redUrl = connection.getURL().toString();
        connection.disconnect();

        return redUrl;

    }
    // For CallBack

    interface OnCompleteListener{
        void onFetched(String redirectedUrl);
    }
}

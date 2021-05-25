package com.streamliners.galleryapp;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class RedirectURLHelper extends AsyncTask<String, Void, String> {

    private OnCompleteListener listener;


    /**
     * @param listener OnComplete handler
     */
    public RedirectURLHelper(OnCompleteListener listener) {
        this.listener = listener;
    }


    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection httpURLConnection=null;
        try {
            URL imageUrl = new URL(strings[0]);
            httpURLConnection = (HttpURLConnection) imageUrl.openConnection();
            httpURLConnection.getResponseCode();

            httpURLConnection.disconnect();
//            listener.fetchRedirectUrl(httpURLConnection.getURL().toString());
        } catch (Exception e) {
            Log.d("Abhi", "doInBackground: " );
            return null;

        }

        return httpURLConnection.getURL().toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s==null){
            listener.OnFail();
            return;
        }
        listener.fetchRedirectUrl(s);
    }

    interface OnCompleteListener {
        void fetchRedirectUrl(String s);

        void OnFail();
    }
}

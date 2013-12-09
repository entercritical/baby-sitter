package com.wednesday.bippobippo.network;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.wednesday.bippobippo.Constants;

public class NetworkUtils {
    private NetworkUtils() {
    }

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static HttpClient getHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, Constants.HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, Constants.HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, Constants.HTTP_REQUEST_TIMEOUT_MS);
        return httpClient;
    }
    

}

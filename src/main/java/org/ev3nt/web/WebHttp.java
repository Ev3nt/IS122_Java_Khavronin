package org.ev3nt.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WebHttp {
    static public String request(String requestUrl, String requestMethod) {
        String response = "";

        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A").next();
            }
        } catch (IOException ignored) {}

        return response;
    }

    static public String request(String requestUrl) {
        return request(requestUrl, "GET");
    }
}

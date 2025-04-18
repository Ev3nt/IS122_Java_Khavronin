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

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
            }
        } catch (IOException e) {
//           throw new RuntimeException(e);
        }

        return response;
    }

    static public String request(String requestUrl) {
        return request(requestUrl, "GET");
    }
}

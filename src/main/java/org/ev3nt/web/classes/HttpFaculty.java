package org.ev3nt.web.classes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class HttpFaculty {
    static public String getFaculties() {
        String response = "";

        try {
            URL url = new URL( "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
            }
        } catch (IOException e) {
//           throw new RuntimeException(e);
        }

        return response;
    }

    static public String getGroups(int facultyNumber)  {
        String response = "";

        try {
            URL url = new URL( "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash/listgroup&faculty=" + facultyNumber);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
            }
        } catch (IOException e) {
//           throw new RuntimeException(e);
        }

        return response;
    }
}
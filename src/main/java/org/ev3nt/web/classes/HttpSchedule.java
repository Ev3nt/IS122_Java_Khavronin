package org.ev3nt.web.classes;

import org.ev3nt.web.interfaces.WebSchedule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class HttpSchedule implements WebSchedule {
    @Override
    public String getSchedule(String group, Integer semester, Integer year)  {
        String response = "";

        try {
            URL url = new URL("https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash/group&group=" + URLEncoder.encode(group) + "&semester=" + semester + "&year=" + year + "&format=json");
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
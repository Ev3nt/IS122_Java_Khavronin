package org.ev3nt.web.classes;

import org.ev3nt.web.interfaces.WebSchedule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class HttpTeacher implements WebSchedule {
    @Override
    public String getSchedule(String teacherFIO, Integer semester, Integer year)  {
        String response = "";

        try {
            String encodedTeacherFIO = URLEncoder.encode(teacherFIO, StandardCharsets.UTF_8.toString());

            URL url = new URL( "https://www.mivlgu.ru/out-inf/scala/findteacher.php?semester=" + semester + "&year=" + year + "&fio=" + encodedTeacherFIO);
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
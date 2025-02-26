package org.ev3nt.web.classes;

import org.ev3nt.web.interfaces.WebSchedule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpSchedule implements WebSchedule {
    @Override
    public String getSchedule(String group, Integer semester, Integer year)  {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash/group&group=" + group + "&semester=" + semester + "&year=" + year + "&format=json"))
                .build();

        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
//            throw new RuntimeException(e);
        }

        return response != null ? response.body() : "";
    }
}
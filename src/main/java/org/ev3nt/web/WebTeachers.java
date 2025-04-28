package org.ev3nt.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.ev3nt.files.CacheManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WebTeachers {
    static Map<Integer, String> fetchTeachers(int semester, int year)
            throws UnsupportedEncodingException {

        Map<Integer, String> teachers = new HashMap<>();

        for (char letter = 'А'; letter <= 'Я'; letter++) {
            String encodedLetter = URLEncoder.encode(String.valueOf(letter), StandardCharsets.UTF_8.toString());
            String url = "https://www.mivlgu.ru/out-inf/scala/findteacher.php?"
                    + "semester=" + semester
                    + "&year=" + year
                    + "&fio=" + encodedLetter + "&format=json";

            String xml = WebHttp.request(url);

            Document doc = Jsoup.parse(xml);
            Elements teacherElements = doc.select("div.sch-teacher a");

            for (Element element : teacherElements) {
                Integer teacherId = Integer.parseInt(element.attr("data-teacher-id"));
                String name = element.attr("data-teacher-name");

                if (!teachers.containsKey(teacherId)) {
                    teachers.put(teacherId, name);
                }
            }
        }

        return teachers;
    }

    static Map<Integer, String> getCachedTeachers()
            throws JsonProcessingException {

        String data = CacheManager.getCachedDataAsString(cacheName);

        return mapper.readValue(data, new TypeReference<Map<Integer, String>>() {});
    }

    static void cacheTeachers(Map<Integer, String> teachers)
            throws JsonProcessingException {

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String data = mapper.writeValueAsString(teachers);
        CacheManager.saveDataAsCache(cacheName, data);
    }

    static public Map<Integer, String> getTeachers(int semester, int year) {
        Map<Integer, String> teachers = new HashMap<>();

        try {
            teachers = getCachedTeachers();
        } catch (JsonProcessingException ignored) {}

        try {
            if (teachers.isEmpty()) {
                teachers = fetchTeachers(semester, year);

                if (!teachers.isEmpty()) {
                    cacheTeachers(teachers);
                }
            }
        } catch (JsonProcessingException | UnsupportedEncodingException ignored) {}

        return teachers;
    }

    static ObjectMapper mapper = new ObjectMapper();
    static String cacheName = "teachers";
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebGroups {
    static private Map<String, List<String>> fetchGroups() {
        String url = "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash";
        String xml = WebHttp.request(url);
        Map<String, List<String>> groups = new HashMap<>();

        if (xml.isEmpty()) {
            return groups;
        }

        Document doc = Jsoup.parse(xml);
        Element select = doc.selectFirst("select#searchschedule-faculty");

        if (select == null) {
            throw new RuntimeException("Не удалось получить список факультетов!");
        }

        Elements options = select.select("option");
        for (Element option : options) {
            String value = option.attr("value");
            if (value.isEmpty()) {
                continue;
            }

            String faculty = option.text();
            int id = Integer.parseInt(value);

            if (groups.containsKey(faculty)) {
                continue;
            }

            List<String> groupsList = new ArrayList<>();

            url = "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash/listgroup&faculty=" + id;
            xml = WebHttp.request(url);

            doc = Jsoup.parse(xml);

            Elements checkboxDivs = doc.select("div.custom-control.custom-checkbox");
            for (Element div : checkboxDivs) {
                Element label = div.selectFirst("label.custom-control-label");
                if (label == null) {
                    continue;
                }

                String groupName = label.text();

                groupsList.add(groupName);
            }

            groups.put(faculty, groupsList);
        }

        return groups;
    }

    static private Map<String, List<String>> getCachedGroups() throws JsonProcessingException {
        String data = CacheManager.getCachedDataAsString(cacheName);

        return mapper.readValue(data, new TypeReference<Map<String, List<String>>>() {});
    }

    static private void cacheGroups(Map<String, List<String>> groups) throws JsonProcessingException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String data = mapper.writeValueAsString(groups);
        CacheManager.saveDataAsCache(cacheName, data);
    }

    static public Map<String, List<String>> getGroups() {
        Map<String, List<String>> groups = new HashMap<>();

        try {
            groups = getCachedGroups();
            if (groups.isEmpty()) {
                groups = fetchGroups();

                if (!groups.isEmpty()) {
                    cacheGroups(groups);
                }
            }
        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
        }

        return groups;
    }

    static ObjectMapper mapper = new ObjectMapper();
    static String cacheName = "groups";
}

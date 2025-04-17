package org.ev3nt.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpGroups {
    static public Map<String, List<String>> getGroups() {
        String url = "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash";
        String xml = WebHttp.request(url);

        if (xml == null) {
            return null;
        }

        Map<String, List<String>> groups = new HashMap<>();

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
}

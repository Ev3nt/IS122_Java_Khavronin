package org.ev3nt.modes.classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ev3nt.classes.CacheManager;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpFaculty;
import org.ev3nt.web.classes.dto.TeacherDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class AudienceSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание аудитории";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        List<String> groups = getGroups();

        for (String group : groups) {
            System.out.println(group);
        }

        contentPanel.add(new JLabel("Тестовое поле"));
    }

    private List<String> fetchGroups() {
        List<Integer> faculties = new ArrayList<>();
        List<String> groups = new ArrayList<>();

        try {
            {
                String xml = HttpFaculty.getFaculties();

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

                    faculties.add(Integer.valueOf(value));
                }
            }

            for (int faculty : faculties) {
                String xml = HttpFaculty.getGroups(faculty);

                Document doc = Jsoup.parse(xml);

                Elements checkboxDivs = doc.select("div.custom-control.custom-checkbox");

                for (Element div : checkboxDivs) {
//                    Element input = div.selectFirst("input.custom-control-input");
//                    if (input == null) {
//                        continue;
//                    }
//
//                    String value = input.attr("value");

                    Element label = div.selectFirst("label.custom-control-label");
                    if (label == null) {
                        continue;
                    }

                    String groupName = label.text();
                    groups.add(groupName);
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return groups;
    }

    private List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String cacheFileName = "groups"; // formatter.format(new Date());
        Path cachePath = Paths.get(cacheFileName);
        String cachedData = CacheManager.getCachedDataAsString(cachePath);

        if (cachedData.isEmpty()) {
            groups = fetchGroups();

            try {
                cachedData = mapper.writeValueAsString(groups);

                CacheManager.saveDataAsCache(cachePath, cachedData);
            } catch (JsonProcessingException ignored) {

            }
        } else {
            try {
                groups = mapper.readValue(cachedData, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException ignored) {

            }
        }

        return groups;
    }
}

package org.ev3nt.modes.classes;

import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpFaculty;
import org.ev3nt.web.classes.dto.TeacherDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.util.ArrayList;

public class AudienceSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание аудитории";
    }

    @Override
    public void showContent(JPanel contentPanel) {
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
                    Element input = div.selectFirst("input.custom-control-input");
                    if (input == null) {
                        continue;
                    }

                    String value = input.attr("value");

                    Element label = div.selectFirst("label.custom-control-label");
                    if (label == null) {
                        continue;
                    }

                    String labelText = label.text();

                    System.out.println("Value: " + value + " | Label: " + labelText);
                }
            }

            contentPanel.add(new JLabel("Тестовое поле"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<Integer> faculties = new ArrayList<>();
}

package org.ev3nt.modes;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.files.FavouriteManager;
import org.ev3nt.files.ResourceLoader;
import org.ev3nt.files.ZipCustomCopy;
import org.ev3nt.gui.Window;
import org.ev3nt.web.WebGroups;
import org.ev3nt.web.WebSchedule;
import org.ev3nt.web.dto.LessonDTO;
import org.ev3nt.web.dto.ScheduleDTO;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class GroupSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Группа";
    }

    @Override
    public JPanel getPanel(Window parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel groupPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton favourite = new JButton("+");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Факультет"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(facultyComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Группа"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(groupComboBox, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        groupPanel.add(favourite, gbc);

        groupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, groupPanel.getPreferredSize().height));

        JPanel favouritePanel = new JPanel();
        favouritePanel.setLayout(new BoxLayout(favouritePanel, BoxLayout.Y_AXIS));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel("Избранные группы"));

        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerPanel.getPreferredSize().height));

        DefaultListModel<String> model = new DefaultListModel<>();
        favouriteList = new JList<>(model);
        favouriteList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(favouriteList);

        favouritePanel.add(headerPanel);
        favouritePanel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

//        JButton singleSchedule = new JButton("Индивидуальное расписание");
//        JButton multiSchedule = new JButton("Общее расписание");
        JButton createSchedule = new JButton("Создать расписание");

//        buttonPanel.add(singleSchedule);
//        buttonPanel.add(multiSchedule);
        buttonPanel.add(createSchedule);

        panel.add(groupPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(favouritePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);

        facultyComboBox.addActionListener(e -> {
            groupComboBox.removeAllItems();

            List<String> groups = ((FacultyItem) Objects.requireNonNull(facultyComboBox.getSelectedItem())).groups;

            for (String group : groups) {
                groupComboBox.addItem(group);
            }
        });

        favourite.addActionListener(e -> {
            String group = (String)groupComboBox.getSelectedItem();

            if (group != null) {
                DefaultListModel<String> favouriteGroups = (DefaultListModel<String>)favouriteList.getModel();

                if (!favouriteGroups.contains(group)) {
                    favouriteGroups.addElement(group);

                    FavouriteManager.saveFavourites(favouriteKey, favouriteGroups);

                    favouriteList.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Группа не выбрана!",
                        "Не удалось добавить группу",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        createSchedule.addActionListener(e -> {
            List<String> selectedGroups = favouriteList.getSelectedValuesList();

            if (selectedGroups.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ни одна группа не выбрана!",
                        "Не удалось создать расписание",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            try {
                process(selectedGroups, parent.getSemester(), parent.getYear());

                JOptionPane.showMessageDialog(
                        null,
                        "Расписание создано!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException | TemplateException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "Не удалось создать расписание",
                        JOptionPane.ERROR_MESSAGE
                );
//                throw new RuntimeException(ex);
            }
//            ScheduleDTO group = WebSchedule.getGroupSchedule("ИСз-124", parent.getSemester(), parent.getYear());
//            System.out.println(group.getDisciplines());
//
//            Map<String, List<String>> groups = WebGroups.getGroups();
//
//            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
//                for (String groupName : entry.getValue()) {
//                    group = WebSchedule.getGroupSchedule(groupName, parent.getSemester(), parent.getYear());
//                }
//            }
//
//            group = WebSchedule.getTeacherSchedule(146, parent.getSemester(), parent.getYear());
//            System.out.println(group.getDisciplines());
        });

        InitFields();

        return panel;
    }

    private void InitFields() {
        Map<String, List<String>> groups = WebGroups.getGroups();
        List<String> favouriteGroups = FavouriteManager.loadFavourites(favouriteKey);

        for (String faculty : groups.keySet()) {
            facultyComboBox.addItem(new FacultyItem(faculty.substring(0, 1).toUpperCase() + faculty.substring(1), groups.get(faculty)));
        }

        DefaultListModel<String> model = (DefaultListModel<String>)favouriteList.getModel();

        if (!favouriteGroups.isEmpty()) {
            for (String group : favouriteGroups) {
                model.addElement(group);
            }

            favouriteList.repaint();
        }
    }

    static class FacultyItem {
        FacultyItem(String label, List<String> groups) {
            this.label = label;
            this.groups = groups;
        }

        @Override
        public String toString() {
            return label;
        }

        String label;
        List<String> groups;
    }

    private void appendIfNotNull(StringBuilder builder, String value) {
        if (value != null) {
            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(value);
        }
    }

    String preparePlainText(LessonDTO lesson) {
        StringBuilder text = new StringBuilder();

        appendIfNotNull(text, lesson.getDiscipline());
        appendIfNotNull(text, lesson.getType());
        appendIfNotNull(text, lesson.getNumber_week() != null ? lesson.getNumber_week().replace("/", "-") : null);
        appendIfNotNull(text, lesson.getUnder_group());
        appendIfNotNull(text, lesson.getName());
        appendIfNotNull(text, lesson.getAud());

        if (lesson.getType_week() != null && !"all".equals(lesson.getType_week())) {
            text.append(" (").append("even".equals(lesson.getType_week()) ? "чётная" : "нечётная").append(")");
        }

        return text.toString();
    }

    void process(List<String> groups, int semester, int year) throws IOException, TemplateException {
        ResourceLoader.extract(templatesPath.resolve("document.xml").toString());
        ResourceLoader.extract(templatesPath.resolve("Template.docx").toString());

        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(templatesPath.toAbsolutePath().toFile());

        Template template = cfg.getTemplate("document.xml");

        Map<String, Object> root = new HashMap<>();
        List<Map<String, Object>> groupList = new ArrayList<>();
        for (String groupName : groups) {
            ScheduleDTO schedule = WebSchedule.getGroupSchedule(groupName, semester, year);

            if (!schedule.getStatus().equals("ok")) {
                throw new IOException(schedule.getMessage() + "\nГруппа: " + groupName);
            }

            Map<Integer, Map<Integer, List<LessonDTO>>> disciplines = schedule.getDisciplines();
            schedule.getDisciplines().values().stream()
                    .flatMap(semesterMap -> semesterMap.values().stream())
                    .flatMap(List::stream)
                    .forEach(lesson -> lesson.setPlainText(
                            preparePlainText(lesson)
                    ));

            List<String> daysOfWeek = Arrays.asList("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС");

            Map<String, Object> group = new HashMap<>();
            group.put("title", schedule.getGroup().getName());
            group.put("first_lesson_number", schedule.getMinLessonNumber());
            group.put("last_lesson_number", schedule.getMaxLessonNumber());
            group.put("schedule", disciplines);
            group.put("row_names", daysOfWeek);

            groupList.add(group);
        }

        root.put("schedules", groupList);

        StringWriter writer = new StringWriter();
        template.process(root, writer);

        try {
            Files.createDirectory(Paths.get("schedules"));
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

        ZipCustomCopy zip = new ZipCustomCopy(
                "schedules/" + String.join(", ", groups) + ".docx",
                templatesPath.resolve("Template.docx").toString());

        zip.add("word/document.xml", writer.toString());

        zip.close();
    }

    JComboBox<FacultyItem> facultyComboBox = new JComboBox<>();
    JComboBox<String> groupComboBox = new JComboBox<>();
    JList<String> favouriteList;

    String favouriteKey = "Groups";

    Path templatesPath = Paths.get("templates");
}

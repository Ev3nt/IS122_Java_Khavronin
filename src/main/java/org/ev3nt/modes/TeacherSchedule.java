package org.ev3nt.modes;

import com.fasterxml.jackson.annotation.JsonProperty;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.files.FavouriteManager;
import org.ev3nt.files.ResourceLoader;
import org.ev3nt.files.ZipCustomCopy;
import org.ev3nt.gui.ModalMessage;
import org.ev3nt.gui.Window;
import org.ev3nt.utils.StringUtils;
import org.ev3nt.web.WebSchedule;
import org.ev3nt.web.WebTeachers;
import org.ev3nt.web.dto.LessonDTO;
import org.ev3nt.web.dto.ScheduleDTO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.ev3nt.utils.StringUtils.appendIfNotNull;

public class TeacherSchedule implements ScheduleMode{
    public TeacherSchedule() {
        updateTeacherList();
    }

    void updateTeacherList() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int semester = month > 8 ? 1 : 2;
        int year = now.getYear() - (semester - 1);

        teachers = WebTeachers.getTeachers(semester, year);
    }

    @Override
    public void setParent(Window parent) {
        this.parent = parent;
    }

    @Override
    public String getName() {
        return "Преподаватель";
    }

    @Override
    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel groupPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton favourite = new JButton("+");
        Dimension buttonSize = new Dimension(favourite.getPreferredSize().width, favourite.getPreferredSize().height);
        favourite.setPreferredSize(buttonSize);

        Dimension fieldSize = new Dimension(teacherName.getPreferredSize().width, buttonSize.height);
        teacherName.setPreferredSize(fieldSize);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Имя преподавателя"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(teacherName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Преподаватель"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(teacherComboBox, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        groupPanel.add(favourite, gbc);

        groupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, groupPanel.getPreferredSize().height));

        JPanel favouritePanel = new JPanel();
        favouritePanel.setLayout(new BoxLayout(favouritePanel, BoxLayout.Y_AXIS));

        JButton unFavourite = new JButton("–"); // "–" or "-"
        JPanel unFavouriteButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        unFavourite.setPreferredSize(buttonSize);
        unFavouriteButtonPanel.add(unFavourite);

        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.add(new JLabel("Избранные преродаватели"), BorderLayout.WEST);
        headerPanel.add(unFavouriteButtonPanel, BorderLayout.EAST);

        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerPanel.getPreferredSize().height));

        DefaultListModel<TeacherItem> model = new DefaultListModel<>();
        favouriteList = new JList<>(model);
        favouriteList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(favouriteList);

        favouritePanel.add(headerPanel);
        favouritePanel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

        JButton createSchedule = new JButton("Создать расписание");
        JButton flatSchedule = new JButton("Создать общее расписание");

        buttonPanel.add(createSchedule);
        buttonPanel.add(flatSchedule);

        panel.add(groupPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(favouritePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);

        teacherName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            void onChanged() {
                teacherComboBox.removeAllItems();
                String name = teacherName.getText().toLowerCase();

                teachers.entrySet().stream()
                        .filter(entry -> entry.getValue().toLowerCase().contains(name))
                        .map(entry -> new TeacherItem(entry.getValue(), entry.getKey()))
                        .forEach(teacherComboBox::addItem);
            }
        });

        favourite.addActionListener(e -> {
            TeacherItem teacher = (TeacherItem) teacherComboBox.getSelectedItem();

            if (teacher != null) {
                DefaultListModel<TeacherItem> favouriteTeachers = (DefaultListModel<TeacherItem>)favouriteList.getModel();

                if (!favouriteTeachers.contains(teacher)) {
                    favouriteTeachers.addElement(teacher);

                    FavouriteManager.saveFavourites(favouriteKey, favouriteTeachers);

                    favouriteList.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Преподаватель не выбран!\nПожалуйста, выберите преподавателя в выпадающем списке.",
                        "Не удалось добавить преподавателя",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        unFavourite.addActionListener(e -> {
            List<TeacherItem> teachers = favouriteList.getSelectedValuesList();

            if (!teachers.isEmpty()) {
                DefaultListModel<TeacherItem> favouriteGroups = (DefaultListModel<TeacherItem>)favouriteList.getModel();

                for (TeacherItem teacher : teachers) {
                    favouriteGroups.removeElement(teacher);
                }

                FavouriteManager.saveFavourites(favouriteKey, favouriteGroups);

                favouriteList.repaint();
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Не выбрано ни одного преподавателя для удаления!\n" +
                                "Пожалуйста, выделите нужных преподавателей в списке.",
                        "Не удалось удалить преподавателя",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        createSchedule.addActionListener(e -> createSchedule(false));

        flatSchedule.addActionListener(e -> createSchedule(true));

        InitFields();

        return panel;
    }

    void InitFields() {
        List<TeacherItem> favouriteTeachers = FavouriteManager.loadFavourites(favouriteKey, TeacherItem.class);

        DefaultListModel<TeacherItem> model = (DefaultListModel<TeacherItem>)favouriteList.getModel();

        if (!favouriteTeachers.isEmpty()) {
            for (TeacherItem teacher : favouriteTeachers) {
                model.addElement(teacher);
            }

            favouriteList.repaint();
        }

        parent.addUpdateDataCallback(e -> ModalMessage.wait(parent.getWindow(), "Обновление данных", "Обновление списка преподавателей...", this::updateTeacherList));
    }

    static class TeacherItem {
        public TeacherItem(String name, Integer teacherId) {
            this.name = name;
            this.teacherId = teacherId;
        }

        @SuppressWarnings("unused")
        public TeacherItem() {}

        @Override
        public String toString() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setTeacherId(Integer teacherId) {
            this.teacherId = teacherId;
        }

        public Integer getTeacherId() {
            return teacherId;
        }

        @JsonProperty
        String name;

        @JsonProperty
        Integer teacherId;
    }

    void createSchedule(boolean isFlat) {
        ModalMessage.wait(parent.getWindow(), "Создание расписания", "Расписание создаётся...", () -> {
            List<TeacherItem> selectedTeachers = favouriteList.getSelectedValuesList();

            if (selectedTeachers.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ни один преподаватель не выбран!\n" +
                                "Пожалуйста, выделите нужных преподавателей в списке.",
                        "Не удалось создать расписание",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            try {
                process(selectedTeachers, parent.getSemester(), parent.getYear(), parent.getScheduleFormat(), isFlat);

                JOptionPane.showMessageDialog(
                        null,
                        "Расписание создано!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException | TemplateException | RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "Не удалось создать расписание",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    String preparePlainText(LessonDTO lesson) {
        StringBuilder text = new StringBuilder();

        appendIfNotNull(text, lesson.getDiscipline());
        appendIfNotNull(text, lesson.getType());
        appendIfNotNull(text, lesson.getNumber_week() != null ? lesson.getNumber_week().replace("/", "-") : null);
        appendIfNotNull(text, lesson.getUnder_group());
        appendIfNotNull(text, lesson.getAud());
        appendIfNotNull(text, lesson.getGroup_name());

        if (lesson.getType_week() != null && !"all".equals(lesson.getType_week())) {
            text.append(" (").append("even".equals(lesson.getType_week()) ? "чётная" : "нечётная").append(")");
        }

        return text.toString();
    }

    void createSchedule(Template template, List<TeacherItem> teachers, int semester, int year, boolean isFlat)
            throws IOException, TemplateException {

        List<String> rowNames = Arrays.asList("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС");

        Map<String, Object> root = new HashMap<>();
        List<Map<String, Object>> teacherList = new ArrayList<>();
        List<Integer> days = new ArrayList<>();
        Map<Integer, List<Integer>> lessonsCounts = new HashMap<>();
        for (TeacherItem teacher : teachers) {
            Integer teacherId = teacher.getTeacherId();
            String teacherName = teacher.getName();
            ScheduleDTO schedule = WebSchedule.getTeacherSchedule(teacherId, semester, year);

            if (!schedule.getStatus().equals("ok")) {
                throw new IOException(schedule.getMessage() + "\nПреподаватель: " + teacherName);
            }

            Map<Integer, Map<Integer, List<LessonDTO>>> disciplines = schedule.getDisciplines();

            disciplines.entrySet().removeIf(dayEntry -> {
                dayEntry.getValue().entrySet().removeIf(lessonsEntry -> {
                    lessonsEntry.getValue().removeIf(lesson -> lesson.getGroup_name().contains("з-"));

                    return lessonsEntry.getValue().isEmpty();
                });

                return dayEntry.getValue().isEmpty();
            });

            schedule.prepareDisciplines(this::preparePlainText);

            days.addAll(new ArrayList<>(disciplines.keySet()));
            disciplines.forEach((k, v) -> lessonsCounts
                    .computeIfAbsent(k, e -> new ArrayList<>())
                    .addAll(v.keySet()));

            List<Integer> pairNumbers = schedule.getPairNumbers();

            Map<String, Object> teacherMap = new HashMap<>();
            teacherMap.put("title", teacherName);
            teacherMap.put("columns", pairNumbers);
            teacherMap.put("schedule", disciplines);
            teacherMap.put("row_names", rowNames);

            teacherList.add(teacherMap);
        }

        days = days.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        lessonsCounts.replaceAll((k, v) -> v.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

        root.put("days", days);
        root.put("lessonsCounts", lessonsCounts);
        root.put("rowNames", rowNames);
        root.put("schedules", teacherList);

        StringWriter writer = new StringWriter();
        template.process(root, writer);

        try {
            Files.createDirectory(Paths.get("schedules"));
        } catch (IOException ignored) { }

        List<String> teacherNames = teachers.stream().map(TeacherItem::getName).collect(Collectors.toList());
        if (!isFlat) {
            ZipCustomCopy zip = new ZipCustomCopy(
                    StringUtils.getFileNameByList("schedules", teacherNames, ".docx"),
                    templatesPath.resolve("Template.docx").toString());

            zip.add("word/document.xml", writer.toString());

            zip.close();
        } else {
            FileOutputStream file = new FileOutputStream(StringUtils.getFileNameByList("schedules", teacherNames, ".html"));
            file.write(writer.toString().getBytes());
            file.close();
        }
    }

    void process(List<TeacherItem> teachers, int semester, int year, boolean combined, boolean isFlat)
            throws IOException, TemplateException, RuntimeException {

        if (!isFlat) {
            ResourceLoader.extract(templatesPath.resolve("document.xml").toString());
            ResourceLoader.extract(templatesPath.resolve("Template.docx").toString());
        } else {
            ResourceLoader.extract(templatesPath.resolve("template.html").toString());
        }

        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(templatesPath.toAbsolutePath().toFile());

        Template template = !isFlat ? cfg.getTemplate("document.xml") : cfg.getTemplate("template.html");

        if (!isFlat) {
            if (combined) {
                createSchedule(template, teachers, semester, year, false);
            } else {
                teachers.forEach(teacher -> {
                    try {
                        createSchedule(template, Collections.singletonList(teacher), semester, year, false);
                    } catch (IOException | TemplateException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } else {
            createSchedule(template, teachers, semester, year, true);
        }
    }

    JTextField teacherName = new JTextField();
    JComboBox<TeacherItem> teacherComboBox = new JComboBox<>();
    JList<TeacherItem> favouriteList;
    Map<Integer, String> teachers;

    String favouriteKey = "Teachers";

    Path templatesPath = Paths.get("templates");

    Window parent;
}

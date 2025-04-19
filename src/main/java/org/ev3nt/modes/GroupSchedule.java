package org.ev3nt.modes;

import org.ev3nt.files.FavouriteManager;
import org.ev3nt.gui.Window;
import org.ev3nt.web.WebGroups;
import org.ev3nt.web.WebSchedule;
import org.ev3nt.web.dto.ScheduleDTO;

import javax.swing.*;
import java.awt.*;
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

        JButton singleSchedule = new JButton("Индивидуальное расписание");
        JButton multiSchedule = new JButton("Общее расписание");

        buttonPanel.add(singleSchedule);
        buttonPanel.add(multiSchedule);

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

        singleSchedule.addActionListener(e -> {
            ScheduleDTO group = WebSchedule.getGroupSchedule("ИСз-124", parent.getSemester(), parent.getYear());
            System.out.println(group.getDisciplines());

            group = WebSchedule.getTeacherSchedule(146, parent.getSemester(), parent.getYear());
            System.out.println(group.getDisciplines());
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

    JComboBox<FacultyItem> facultyComboBox = new JComboBox<>();
    JComboBox<String> groupComboBox = new JComboBox<>();
    JList<String> favouriteList;

    String favouriteKey = "Groups";
}

package org.ev3nt.modes;

import javax.swing.*;
import java.awt.*;

public class GroupSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Группа";
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

        facultyComboBox = new JComboBox<>();
        groupComboBox = new JComboBox<>();
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

        String[] groups = new String[13];
        groups[0] = "ИС-122";
        groups[1] = "ИС-123";
        groups[2] = "ИС-124";
        groups[3] = "ИС-125";
        groups[4] = "ИС-126";
        groups[5] = "ИС-127";
        groups[6] = "ИС-128";
        groups[7] = "ИС-129";
        groups[8] = "ИС-130";
        groups[9] = "ИС-131";
        groups[10] = "ИС-132";
        groups[11] = "ИС-133";
        groups[12] = "ИС-134";

        JList<String> favouriteList = new JList<>(groups);
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

        return panel;
    }

    JComboBox<String> facultyComboBox;
    JComboBox<String> groupComboBox;
}

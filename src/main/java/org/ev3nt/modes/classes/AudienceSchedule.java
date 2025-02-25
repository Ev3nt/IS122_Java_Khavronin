package org.ev3nt.modes.classes;

import org.ev3nt.gui.interfaces.ComboBoxItem;

import javax.swing.*;

public class AudienceSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание аудитории";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        contentPanel.add(new JLabel("Тестовое поле"));
    }
}

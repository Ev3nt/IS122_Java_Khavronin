package org.ev3nt.modes;

import javax.swing.*;

public class TeacherSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Преподаватель";
    }

    @Override
    public JPanel getPanel() {
        JPanel panel = new JPanel();

        return panel;
    }
}

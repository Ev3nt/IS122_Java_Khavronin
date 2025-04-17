package org.ev3nt.modes;

import org.ev3nt.gui.Window;

import javax.swing.*;

public class TeacherSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Преподаватель";
    }

    @Override
    public JPanel getPanel(Window parent) {
        JPanel panel = new JPanel();

        return panel;
    }
}

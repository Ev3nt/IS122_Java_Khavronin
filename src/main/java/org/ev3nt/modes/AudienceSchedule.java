package org.ev3nt.modes;

import javax.swing.*;

public class AudienceSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Аудитория";
    }

    @Override
    public JPanel getPanel() {
        JPanel panel = new JPanel();

        return panel;
    }
}

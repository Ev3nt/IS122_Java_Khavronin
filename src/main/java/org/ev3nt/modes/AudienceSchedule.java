package org.ev3nt.modes;

import org.ev3nt.gui.Window;

import javax.swing.*;

public class AudienceSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Аудитория";
    }

    @Override
    public JPanel getPanel(Window parent) {
        JPanel panel = new JPanel();

        return panel;
    }
}

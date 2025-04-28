package org.ev3nt.modes;

import org.ev3nt.gui.Window;

import javax.swing.*;

public interface ScheduleMode {
    void setParent(Window parent);
    String getName();
    JPanel getPanel();
}

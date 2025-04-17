package org.ev3nt.modes;

import org.ev3nt.gui.Window;

import javax.swing.*;

public interface ScheduleMode {
    String getName();
    JPanel getPanel(Window parent);
}

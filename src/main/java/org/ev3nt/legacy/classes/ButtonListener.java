package org.ev3nt.legacy.classes;

import org.ev3nt.legacy.interfaces.Execute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonListener implements ActionListener {

    public ButtonListener(Execute callback) {
        this.callback = callback;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.callback.execute();
    }

    private final Execute callback;
}

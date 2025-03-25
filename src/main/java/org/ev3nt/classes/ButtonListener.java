package org.ev3nt.classes;

import org.ev3nt.interfaces.Execute;

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

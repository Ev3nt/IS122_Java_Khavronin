package org.ev3nt.gui;

import javax.swing.*;
import java.awt.*;

public class TwoPanelsWindow extends JFrame {
    public TwoPanelsWindow(double coefficient) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = coefficient;
        gbc.weighty = 1;
        panel.add(leftPanel, gbc);

        panel.add(Box.createHorizontalStrut(5));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 1;
        panel.add(new JSeparator(SwingConstants.VERTICAL), gbc);

        panel.add(Box.createHorizontalStrut(5));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1 - coefficient;
        gbc.weighty = 1;
        panel.add(rightPanel, gbc);

        this.add(panel);
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public JPanel getRightPanel() {
        return rightPanel;
    }

    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();
}

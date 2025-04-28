package org.ev3nt.gui;

import javax.swing.*;
import java.awt.*;

public class UpdatingMessage {
    public static void wait(JFrame parent, String text, Runnable func) {
        JDialog dialog = new JDialog(parent, "Обновление данных", true);

        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        JLabel label = new JLabel("<html><div style='width: 200px; text-align: center;'>" + text + "</div></html>");
        label.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.CENTER);
        dialog.add(panel);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(250, 100));
        dialog.setLocationRelativeTo(parent);

        Thread worker = new Thread(() -> {
            func.run();

            SwingUtilities.invokeLater(dialog::dispose);
        });

        worker.start();
        dialog.setVisible(true);
    }
}

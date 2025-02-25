package org.ev3nt.gui.classes;

import org.ev3nt.gui.interfaces.ComboBoxItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.function.Supplier;

public class Menu {
    public Menu(String title, int width, int height) {
        menu = new JFrame();
        menu.setTitle(title);
        menu.setSize(width, height);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menu.setLocationRelativeTo(null);

        comboBox = new JComboBox<>();
        comboBox.addActionListener(new ComboBoxListener());
        itemHashMap = new HashMap<>();

        contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout());

        menu.setLayout(new BorderLayout());
        menu.add(comboBox, BorderLayout.NORTH);
        menu.add(contentPanel, BorderLayout.CENTER);
    }

    public <T extends ComboBoxItem> void add(Supplier<T> itemFactory) {
        T item = itemFactory.get();
        itemHashMap.put(item.getName(), item);
        comboBox.addItem(item.getName());
    }

    public void run() {
        menu.setVisible(true);
    }

    private class ComboBoxListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedItem = (String) comboBox.getSelectedItem();
            updateContent(selectedItem);
        }
    }

    private void updateContent(String itemName) {
        contentPanel.removeAll();

        itemHashMap.get(itemName).showContent(contentPanel);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private final JFrame menu;
    private final JComboBox<String> comboBox;
    private final JPanel contentPanel;

    private final HashMap<String, ComboBoxItem> itemHashMap;
}

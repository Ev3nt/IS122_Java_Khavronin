package gui.classes;

import gui.interfaces.ComboBoxItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class Menu {
    public Menu(String title, int width, int height) {
        menu = new JFrame();
        menu.setTitle(title);
        menu.setSize(width, height);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menu.setLocationRelativeTo(null);

        comboBox = new JComboBox<>();
        comboBox.addActionListener((ActionListener) new ComboBoxListener());
        itemHashMap = new HashMap<>();

        contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout());

        menu.setLayout(new BorderLayout());
        menu.add(comboBox, BorderLayout.NORTH);
        menu.add(contentPanel, BorderLayout.CENTER);
    }

    public void add(ComboBoxItem item) {
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

    private JFrame menu;
    private JComboBox<String> comboBox;
    private JPanel contentPanel;

    private HashMap<String, ComboBoxItem> itemHashMap;
}

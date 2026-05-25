package com.rrt.chat.client;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingClient("localhost", 4004));
    }
}
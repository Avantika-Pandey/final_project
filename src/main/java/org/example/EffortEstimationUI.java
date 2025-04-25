package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EffortEstimationUI extends JFrame {

    private JTextArea outputArea;

    public EffortEstimationUI() {
        setTitle("Effort Estimation Tool");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create buttons
        JButton runCocomoButton = new JButton("Run COCOMO");
        JButton runPsoButton = new JButton("Run PSO");
        JButton runRegressionButton = new JButton("Run Regression");

        // Create output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runCocomoButton);
        buttonPanel.add(runPsoButton);
        buttonPanel.add(runRegressionButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        runCocomoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputArea.append("Running COCOMO model...\n");
                // Call your Main.java COCOMO part here (I'll show you how next)
            }
        });

        runPsoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputArea.append("Running PSO Optimization...\n");
                // Call your Main.java PSO part here
            }
        });

        runRegressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputArea.append("Running Regression model...\n");
                // Call your Main.java Regression part here
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EffortEstimationUI ui = new EffortEstimationUI();
            ui.setVisible(true);
        });
    }
}

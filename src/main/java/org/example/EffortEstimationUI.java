package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class EffortEstimationUI extends JFrame {

    private JTextField klocInput;
    private JLabel cocomoEffortLabel, cocomoMMRELabel;
    private JLabel regressionEffortLabel, regressionMMRELabel;
    private JLabel psoEffortLabel, psoMMRELabel;

    public EffortEstimationUI() {
        setTitle("Effort Estimation Tool");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create buttons
        JButton runCocomoButton = new JButton("Run COCOMO");
        JButton runPsoButton = new JButton("Run PSO");
        JButton runRegressionButton = new JButton("Run Regression");
        JButton compareMMREButton = new JButton("Compare MMREs");
        JButton clearOutputButton = new JButton("Clear Output");


        // KLOC input
        JPanel inputPanel = new JPanel();
        klocInput = new JTextField(10);
        JButton predictButton = new JButton("Predict Effort");
        inputPanel.add(new JLabel("Enter KLOC:"));
        inputPanel.add(klocInput);
        inputPanel.add(predictButton);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runCocomoButton);
        buttonPanel.add(runPsoButton);
        buttonPanel.add(runRegressionButton);
        buttonPanel.add(compareMMREButton);
        buttonPanel.add(clearOutputButton);

        // Result boxes
        JPanel resultPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resultPanel.add(createResultBox("COCOMO Estimate"));
        resultPanel.add(createResultBox("Regression Estimate"));
        resultPanel.add(createResultBox("PSO Estimate"));

        add(buttonPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Button actions
        runCocomoButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                String result = estimator.runCocomoDefault();
                JOptionPane.showMessageDialog(this, result, "COCOMO Output", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        runPsoButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                String result = estimator.runPso();
                JOptionPane.showMessageDialog(this, result, "PSO Output", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        runRegressionButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                String result = estimator.runRegression();
                JOptionPane.showMessageDialog(this, result, "Regression Output", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        compareMMREButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                estimator.runCocomoDefault();
                estimator.runPso();
                estimator.runRegression();
                showMMREChart(estimator.getMmreCocomo(), estimator.getMmrePso(), estimator.getMmreRegression());
            } catch (Exception ex) {
                showError("Chart Error: " + ex.getMessage());
            }
        });

        predictButton.addActionListener(e -> {
            try {
                double kloc = Double.parseDouble(klocInput.getText());
                EffortEstimator estimator = new EffortEstimator();

                double cocomoEffort = estimator.predictEffortCocomo(kloc);
                double regressionEffort = estimator.predictEffortRegression(kloc);
                double psoEffort = estimator.predictEffortPso(kloc);

                double actual = kloc * 2.5; // or retrieve from dataset if available
                double cocomoMMRE = Math.abs((actual - cocomoEffort) / actual);
                double regressionMMRE = Math.abs((actual - regressionEffort) / actual);
                double psoMMRE = Math.abs((actual - psoEffort) / actual);

                cocomoEffortLabel.setText("Effort: " + String.format("%.2f", cocomoEffort) + " PM");
                cocomoMMRELabel.setText("MMRE: " + String.format("%.3f", cocomoMMRE));

                regressionEffortLabel.setText("Effort: " + String.format("%.2f", regressionEffort) + " PM");
                regressionMMRELabel.setText("MMRE: " + String.format("%.3f", regressionMMRE));

                psoEffortLabel.setText("Effort: " + String.format("%.2f", psoEffort) + " PM");
                psoMMRELabel.setText("MMRE: " + String.format("%.3f", psoMMRE));

            } catch (Exception ex) {
                showError("Invalid input: " + ex.getMessage());
            }
        });

        clearOutputButton.addActionListener(e -> {
            cocomoEffortLabel.setText("Effort: ");
            cocomoMMRELabel.setText("MMRE: ");
            regressionEffortLabel.setText("Effort: ");
            regressionMMRELabel.setText("MMRE: ");
            psoEffortLabel.setText("Effort: ");
            psoMMRELabel.setText("MMRE: ");
        });
    }

    private JPanel createResultBox(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        panel.setBackground(new Color(230, 240, 255));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel effortLabel = new JLabel("Effort: ");
        JLabel mmreLabel = new JLabel("MMRE: ");
        effortLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mmreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (title.contains("COCOMO")) {
            cocomoEffortLabel = effortLabel;
            cocomoMMRELabel = mmreLabel;
        } else if (title.contains("Regression")) {
            regressionEffortLabel = effortLabel;
            regressionMMRELabel = mmreLabel;
        } else if (title.contains("PSO")) {
            psoEffortLabel = effortLabel;
            psoMMRELabel = mmreLabel;
        }

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(effortLabel);
        panel.add(mmreLabel);

        return panel;
    }

    private void showMMREChart(double cocomo, double pso, double regression) {
        double cocomoPercent = Double.parseDouble(String.format("%.2f", cocomo * 100));
        double psoPercent = Double.parseDouble(String.format("%.2f", pso * 100));
        double regressionPercent = Double.parseDouble(String.format("%.2f", regression * 100));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(cocomoPercent, "MMRE", "COCOMO");
        dataset.addValue(psoPercent, "MMRE", "PSO");
        dataset.addValue(regressionPercent, "MMRE", "Regression");

        JFreeChart chart = ChartFactory.createBarChart(
                "MMRE Comparison", "Model", "MMRE (%)",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        JTextArea mmreText = new JTextArea(
                String.format("COCOMO MMRE: %.2f%%\nPSO MMRE: %.2f%%\nRegression MMRE: %.2f%%",
                        cocomoPercent, psoPercent, regressionPercent)
        );
        mmreText.setEditable(false);
        mmreText.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(chartPanel, BorderLayout.CENTER);
        combinedPanel.add(mmreText, BorderLayout.SOUTH);

        JFrame chartFrame = new JFrame("MMRE Chart");
        chartFrame.setContentPane(combinedPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setVisible(true);

        try {
            java.io.File outputFile = new java.io.File("mmre_chart.png");
            org.jfree.chart.ChartUtils.saveChartAsPNG(outputFile, chart, 500, 300);
        } catch (Exception e) {
            System.err.println("❌ Failed to save chart: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EffortEstimationUI().setVisible(true));
    }
}

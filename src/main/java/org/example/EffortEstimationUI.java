package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class EffortEstimationUI extends JFrame {

    private JTextArea outputArea;

    public EffortEstimationUI() {
        setTitle("Effort Estimation Tool");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create buttons
        JButton runCocomoButton = new JButton("Run COCOMO");
        JButton runPsoButton = new JButton("Run PSO");
        JButton runRegressionButton = new JButton("Run Regression");
        JButton compareMMREButton = new JButton("Compare MMREs");
        JButton clearOutputButton = new JButton("Clear Output");

        // Create output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Custom KLOC input
        JPanel klocPanel = new JPanel();
        JTextField klocInput = new JTextField(10);
        JButton predictButton = new JButton("Predict Effort");
        klocPanel.add(new JLabel("Enter KLOC:"));
        klocPanel.add(klocInput);
        klocPanel.add(predictButton);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runCocomoButton);
        buttonPanel.add(runPsoButton);
        buttonPanel.add(runRegressionButton);
        buttonPanel.add(compareMMREButton);
        buttonPanel.add(clearOutputButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(klocPanel, BorderLayout.SOUTH);

        // Button actions
        runCocomoButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                String result = estimator.runCocomoDefault();
                outputArea.append("🔵 " + result + "\n\n");
            } catch (Exception ex) {
                outputArea.append("❌ Error: " + ex.getMessage() + "\n");
            }
        });

        runPsoButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                String result = estimator.runPso();
                outputArea.append("🟢 " + result + "\n\n");
            } catch (Exception ex) {
                outputArea.append("❌ Error: " + ex.getMessage() + "\n");
            }
        });

        runRegressionButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();
                String result = estimator.runRegression();
                outputArea.append("🟣 " + result + "\n\n");
            } catch (Exception ex) {
                outputArea.append("❌ Error: " + ex.getMessage() + "\n");
            }
        });

        compareMMREButton.addActionListener(e -> {
            try {
                EffortEstimator estimator = new EffortEstimator();

                // FIRST: Run all models to calculate the MMREs
                estimator.runCocomoDefault();
                estimator.runPso();
                estimator.runRegression();

                // THEN: show the chart
                showMMREChart(estimator.getMmreCocomo(), estimator.getMmrePso(), estimator.getMmreRegression());
            } catch (Exception ex) {
                outputArea.append("❌ Chart Error: " + ex.getMessage() + "\n");
            }
        });


        predictButton.addActionListener(e -> {
            try {
                double kloc = Double.parseDouble(klocInput.getText());
                EffortEstimator estimator = new EffortEstimator();
                String prediction = estimator.predictEffortFromKloc(kloc);
                outputArea.append(prediction + "\n\n");
            } catch (Exception ex) {
                outputArea.append("❌ Invalid input: " + ex.getMessage() + "\n");
            }
        });

        clearOutputButton.addActionListener(e -> outputArea.setText(""));
    }

    private void showMMREChart(double cocomo, double pso, double regression) {
        // Format MMREs to percentages with two decimal places
        double cocomoPercent = Double.parseDouble(String.format("%.2f", cocomo * 100));
        double psoPercent = Double.parseDouble(String.format("%.2f", pso * 100));
        double regressionPercent = Double.parseDouble(String.format("%.2f", regression * 100));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(cocomoPercent, "MMRE", "COCOMO");
        dataset.addValue(psoPercent, "MMRE", "PSO");
        dataset.addValue(regressionPercent, "MMRE", "Regression");

        // Chart Creation (Pratistha)
        JFreeChart chart = ChartFactory.createBarChart(
                "MMRE Comparison", "Model", "MMRE (%)",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        // Chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        // MMRE value text display
        JTextArea mmreText = new JTextArea(
                String.format("COCOMO MMRE: %.2f%%\nPSO MMRE: %.2f%%\nRegression MMRE: %.2f%%",
                        cocomoPercent, psoPercent, regressionPercent)
        );
        mmreText.setEditable(false);
        mmreText.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Combine chart and text
        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(chartPanel, BorderLayout.CENTER);
        combinedPanel.add(mmreText, BorderLayout.SOUTH);

        // Frame
        JFrame chartFrame = new JFrame("MMRE Chart");
        chartFrame.setContentPane(combinedPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setVisible(true);

         // Optional: Save chart to PNG file
        try {
            java.io.File outputFile = new java.io.File("mmre_chart.png");
            org.jfree.chart.ChartUtils.saveChartAsPNG(outputFile, chart, 500, 300);
            System.out.println("✅ Chart saved as mmre_chart.png");
        } catch (Exception e) {
            System.err.println("❌ Failed to save chart: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EffortEstimationUI().setVisible(true));
    }
}

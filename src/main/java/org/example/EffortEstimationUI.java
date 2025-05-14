package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;

public class EffortEstimationUI extends JFrame {

    private JTextField klocInput;
    private JLabel cocomoEffortLabel, cocomoMMRELabel;
    private JLabel regressionEffortLabel, regressionMMRELabel;
    private JLabel psoEffortLabel, psoMMRELabel;

    public EffortEstimationUI() {
        setTitle("Effort Estimation Tool");
        setSize(1000, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton runCocomoButton = new JButton("Run COCOMO");
        JButton runPsoButton = new JButton("Run PSO");
        JButton runRegressionButton = new JButton("Run Regression");
        JButton compareMMREButton = new JButton("Compare MMREs");
        JButton clearOutputButton = new JButton("Clear Output");
        JButton exportPdfButton = new JButton("Export to PDF");
        JButton uploadExcelButton = new JButton("Upload Excel File");

        JPanel inputPanel = new JPanel();
        klocInput = new JTextField(10);
        JButton predictButton = new JButton("Predict Effort");
        inputPanel.add(new JLabel("Enter KLOC:"));
        inputPanel.add(klocInput);
        inputPanel.add(predictButton);
        inputPanel.add(uploadExcelButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runCocomoButton);
        buttonPanel.add(runPsoButton);
        buttonPanel.add(runRegressionButton);
        buttonPanel.add(compareMMREButton);
        buttonPanel.add(clearOutputButton);
        buttonPanel.add(exportPdfButton);

        JPanel resultPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resultPanel.add(createResultBox("COCOMO Estimate"));
        resultPanel.add(createResultBox("Regression Estimate"));
        resultPanel.add(createResultBox("PSO Estimate"));

        add(buttonPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        runCocomoButton.addActionListener(e -> runModel("cocomo"));
        runPsoButton.addActionListener(e -> runModel("pso"));
        runRegressionButton.addActionListener(e -> runModel("regression"));

        compareMMREButton.addActionListener(e -> {
            try {
                EffortEstimator est = new EffortEstimator();
                est.runCocomoDefault();
                est.runPso();
                est.runRegression();
                showMMREChart(est.getMmreCocomo(), est.getMmrePso(), est.getMmreRegression());
            } catch (Exception ex) {
                showError("Chart Error: " + ex.getMessage());
            }
        });

        predictButton.addActionListener(e -> predictFromTextInput());
        clearOutputButton.addActionListener(e -> clearOutput());
        exportPdfButton.addActionListener(e -> exportToPDF());
        uploadExcelButton.addActionListener(e -> uploadExcelFile());
    }

    private void exportToPDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("EffortEstimationReport.pdf"));
            document.open();

            document.add(new Paragraph("Effort Estimation Report"));
            document.add(new Paragraph(" ")); // empty line

            document.add(new Paragraph("COCOMO Estimate"));
            document.add(new Paragraph(cocomoEffortLabel.getText()));
            document.add(new Paragraph(cocomoMMRELabel.getText()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Regression Estimate"));
            document.add(new Paragraph(regressionEffortLabel.getText()));
            document.add(new Paragraph(regressionMMRELabel.getText()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("PSO Estimate"));
            document.add(new Paragraph(psoEffortLabel.getText()));
            document.add(new Paragraph(psoMMRELabel.getText()));

            document.close();

            JOptionPane.showMessageDialog(this, "PDF exported successfully as EffortEstimationReport.pdf", "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("Failed to export PDF: " + e.getMessage());
        }
    }

    private void showMMREChart(double mmreCocomo, double mmrePso, double mmreRegression) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(mmreCocomo, "MMRE", "COCOMO");
        dataset.addValue(mmreRegression, "MMRE", "Regression");
        dataset.addValue(mmrePso, "MMRE", "PSO");

        JFreeChart barChart = ChartFactory.createBarChart(
                "MMRE Comparison",
                "Model",
                "MMRE",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, chartPanel, "MMRE Comparison Chart", JOptionPane.INFORMATION_MESSAGE);
    }

    private void runModel(String model) {
        try {
            EffortEstimator estimator = new EffortEstimator();
            String result = switch (model) {
                case "cocomo" -> estimator.runCocomoDefault();
                case "pso" -> estimator.runPso();
                case "regression" -> estimator.runRegression();
                default -> "Unknown model.";
            };
            JOptionPane.showMessageDialog(this, result, model.toUpperCase() + " Output", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void showError(String s) {
        JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void predictFromTextInput() {
        try {
            double kloc = Double.parseDouble(klocInput.getText());
            EffortEstimator estimator = new EffortEstimator();

            double actual = kloc * 2.5;

            double cocomoEffort = estimator.predictEffortCocomo(kloc);
            double regressionEffort = estimator.predictEffortRegression(kloc);
            double psoEffort = estimator.predictEffortPso(kloc);

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
    }

    private void uploadExcelFile() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            try (FileInputStream fis = new FileInputStream(chooser.getSelectedFile());
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                EffortEstimator estimator = new EffortEstimator();
                StringBuilder results = new StringBuilder();

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    Cell cell = row.getCell(0);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        double kloc = cell.getNumericCellValue();
                        double actual = kloc * 2.5;

                        double cEffort = estimator.predictEffortCocomo(kloc);
                        double rEffort = estimator.predictEffortRegression(kloc);
                        double pEffort = estimator.predictEffortPso(kloc);

                        double cMMRE = Math.abs((actual - cEffort) / actual);
                        double rMMRE = Math.abs((actual - rEffort) / actual);
                        double pMMRE = Math.abs((actual - pEffort) / actual);

                        results.append(String.format(
                                "KLOC: %.2f | COCOMO Effort: %.2f PM (MMRE: %.3f), Regression Effort: %.2f PM (MMRE: %.3f), PSO Effort: %.2f PM (MMRE: %.3f)\n\n",
                                kloc, cEffort, cMMRE, rEffort, rMMRE, pEffort, pMMRE
                        ));
                    }
                }

                JTextArea textArea = new JTextArea(results.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(700, 400));

                JOptionPane.showMessageDialog(this, scrollPane, "Excel File Results", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Failed to read Excel: " + ex.getMessage());
            }
        }
    }

    private JPanel createResultBox(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        panel.setBackground(new java.awt.Color(230, 240, 255));

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

    private void clearOutput() {
        cocomoEffortLabel.setText("Effort: ");
        cocomoMMRELabel.setText("MMRE: ");
        regressionEffortLabel.setText("Effort: ");
        regressionMMRELabel.setText("MMRE: ");
        psoEffortLabel.setText("Effort: ");
        psoMMRELabel.setText("MMRE: ");
    }
// Main file
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EffortEstimationUI().setVisible(true));
    }
}

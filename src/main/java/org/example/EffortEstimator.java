package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

public class EffortEstimator {

    private double[] klocs;
    private double[] methods;
    private double[] actualEfforts;

    private double mmreCocomo = 0;
    private double mmrePso = 0;
    private double mmreRegression = 0;

    public double getMmreCocomo() { return mmreCocomo; }
    public double getMmrePso() { return mmrePso; }
    public double getMmreRegression() { return mmreRegression; }

    public EffortEstimator() throws Exception {
        loadExcelData();
    }

    private void loadExcelData() throws Exception {
        InputStream excelStream = EffortEstimator.class.getClassLoader().getResourceAsStream("data.xlsx");
        if (excelStream == null) throw new Exception("Excel file not found!");

        Workbook workbook = new XSSFWorkbook(excelStream);
        Sheet sheet = workbook.getSheetAt(0);
        int n = sheet.getLastRowNum();

        klocs = new double[n];
        methods = new double[n];
        actualEfforts = new double[n];

        for (int i = 1; i <= n; i++) {
            Row row = sheet.getRow(i);
            klocs[i - 1] = row.getCell(0).getNumericCellValue();
            methods[i - 1] = row.getCell(1).getNumericCellValue();
            actualEfforts[i - 1] = row.getCell(2).getNumericCellValue();
        }
        workbook.close();
    }

    public String runCocomoDefault() {
        double[] predicted = new double[klocs.length];
        for (int i = 0; i < klocs.length; i++) {
            double A = 2.94;
            double B = 1.1;
            double EAF = methods[i] / 30.0;
            predicted[i] = A * Math.pow(klocs[i], B) * EAF;
        }
        mmreCocomo = calculateMMRE(actualEfforts, predicted);
        return String.format("COCOMO MMRE: %.4f", mmreCocomo);
    }

    public String runPso() {
        double[][] data = new double[klocs.length][3];
        for (int i = 0; i < klocs.length; i++) {
            data[i][0] = klocs[i];
            data[i][1] = methods[i];
            data[i][2] = actualEfforts[i];
        }

        Particle best = Main.runPSO(data, 30, 100);
        double[] predicted = new double[klocs.length];
        for (int i = 0; i < klocs.length; i++) {
            double eaf = methods[i] / 30.0;
            predicted[i] = best.a * Math.pow(klocs[i], best.b) * eaf;
        }
        mmrePso = calculateMMRE(actualEfforts, predicted);
        return String.format("PSO Optimized A: %.4f, B: %.4f\nPSO MMRE: %.4f", best.a, best.b, mmrePso);
    }

    public String runRegression() {
        int n = klocs.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += klocs[i];
            sumY += actualEfforts[i];
            sumXY += klocs[i] * actualEfforts[i];
            sumX2 += klocs[i] * klocs[i];
        }

        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double c = (sumY - m * sumX) / n;

        double[] predicted = new double[n];
        for (int i = 0; i < n; i++) {
            predicted[i] = m * klocs[i] + c;
        }
        mmreRegression = calculateMMRE(actualEfforts, predicted);
        return String.format("Regression Equation: Effort = %.4f * KLOC + %.4f\nRegression MMRE: %.4f", m, c, mmreRegression);
    }

    public String predictEffortFromKloc(double kloc) {
        double method = 30;
        double eaf = method / 30.0;

        double cocomo = 2.94 * Math.pow(kloc, 1.1) * eaf;

        double[][] data = new double[klocs.length][3];
        for (int i = 0; i < klocs.length; i++) {
            data[i][0] = klocs[i];
            data[i][1] = methods[i];
            data[i][2] = actualEfforts[i];
        }
        Particle best = Main.runPSO(data, 30, 100);
        double pso = best.a * Math.pow(kloc, best.b) * eaf;

        int n = klocs.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += klocs[i];
            sumY += actualEfforts[i];
            sumXY += klocs[i] * actualEfforts[i];
            sumX2 += klocs[i] * klocs[i];
        }
        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double c = (sumY - m * sumX) / n;
        double regression = m * kloc + c;

        return String.format("Predicted effort for %.2f KLOC:\nCOCOMO: %.2f\nPSO: %.2f\nRegression: %.2f",
                kloc, cocomo, pso, regression);
    }

    private double calculateMMRE(double[] actual, double[] predicted) {
        double sum = 0;
        for (int i = 0; i < actual.length; i++) {
            sum += Math.abs(actual[i] - predicted[i]) / actual[i];
        }
        return sum / actual.length;
    }
}

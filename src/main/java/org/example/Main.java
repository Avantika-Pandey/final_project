package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        try {
            // Load the Excel file from resources
            InputStream excelStream = Main.class.getClassLoader().getResourceAsStream("data.xlsx");

            if (excelStream == null) {
                System.out.println("Excel file not found in resources folder!");
                return;
            }

            Workbook workbook = new XSSFWorkbook(excelStream);
            Sheet sheet = workbook.getSheetAt(0);

            double[] actualEfforts = new double[sheet.getLastRowNum()];
            double[] predictedEfforts = new double[sheet.getLastRowNum()];
            int index = 0;

            // Read data from Excel and predict using default COCOMO model
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip the header

                double kloc = row.getCell(0).getNumericCellValue();
                double method = row.getCell(1).getNumericCellValue();
                double actualEffort = row.getCell(2).getNumericCellValue();

                double A = 2.94;
                double B = 1.1;
                double EAF = method / 30.0;

                double predictedEffort = A * Math.pow(kloc, B) * EAF;

                actualEfforts[index] = actualEffort;
                predictedEfforts[index] = predictedEffort;
                index++;
            }

            double mmre = calculateMMRE(actualEfforts, predictedEfforts);
            System.out.printf("Default COCOMO MMRE: %.4f%n", mmre);

            // Build dataset for PSO
            double[][] dataset = new double[actualEfforts.length][3];
            for (int i = 0; i < actualEfforts.length; i++) {
                Row row = sheet.getRow(i + 1);
                dataset[i][0] = row.getCell(0).getNumericCellValue(); // KLOC
                dataset[i][1] = row.getCell(1).getNumericCellValue(); // Method
                dataset[i][2] = row.getCell(2).getNumericCellValue(); // Actual effort
            }

            // Run PSO
            Particle best = runPSO(dataset, 30, 100);
            System.out.printf("PSO Optimized A: %.4f | PSO Optimized B: %.4f%n", best.a, best.b);

            double[] optimizedPredicted = new double[dataset.length];
            for (int i = 0; i < dataset.length; i++) {
                double kloc = dataset[i][0];
                double method = dataset[i][1];
                double eaf = method / 30.0;
                optimizedPredicted[i] = best.a * Math.pow(kloc, best.b) * eaf;
            }

            double optimizedMMRE = calculateMMRE(actualEfforts, optimizedPredicted);
            System.out.printf("Optimized MMRE (after PSO): %.4f%n", optimizedMMRE);

            // Run Linear Regression
            double[] klocs = new double[dataset.length];
            for (int i = 0; i < dataset.length; i++) {
                klocs[i] = dataset[i][0];
            }

            double[] regressionPredicted = runLinearRegression(klocs, actualEfforts);
            double regressionMMRE = calculateMMRE(actualEfforts, regressionPredicted);
            System.out.printf("Regression MMRE: %.4f%n", regressionMMRE);

            // Final Summary
            System.out.println("\n--- Final Comparison ---");
            System.out.printf("Default COCOMO MMRE: %.4f%n", mmre);
            System.out.printf("PSO Optimized MMRE: %.4f%n", optimizedMMRE);
            System.out.printf("Linear Regression MMRE: %.4f%n", regressionMMRE);

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double calculateMMRE(double[] actual, double[] predicted) {
        double totalMRE = 0;
        for (int i = 0; i < actual.length; i++) {
            totalMRE += Math.abs(actual[i] - predicted[i]) / actual[i];
        }
        return totalMRE / actual.length;
    }

    public static Particle runPSO(double[][] data, int numParticles, int maxIterations) {
        Particle[] swarm = new Particle[numParticles];

        double globalBestA = 0, globalBestB = 0;
        double globalBestScore = Double.MAX_VALUE;

        for (int i = 0; i < numParticles; i++) {
            double a = 1 + Math.random() * 10;
            double b = 0.5 + Math.random();
            swarm[i] = new Particle(a, b);
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            for (Particle p : swarm) {
                double[] actual = new double[data.length];
                double[] predicted = new double[data.length];

                for (int i = 0; i < data.length; i++) {
                    double kloc = data[i][0];
                    double method = data[i][1];
                    double eaf = method / 30.0;
                    actual[i] = data[i][2];
                    predicted[i] = p.a * Math.pow(kloc, p.b) * eaf;
                }

                double score = calculateMMRE(actual, predicted);

                if (score < p.bestScore) {
                    p.bestScore = score;
                    p.bestA = p.a;
                    p.bestB = p.b;
                }

                if (score < globalBestScore) {
                    globalBestScore = score;
                    globalBestA = p.a;
                    globalBestB = p.b;
                }
            }

            for (Particle p : swarm) {
                double w = 0.5;
                double c1 = 1.5;
                double c2 = 1.5;
                double r1 = Math.random();
                double r2 = Math.random();

                p.velocityA = w * p.velocityA + c1 * r1 * (p.bestA - p.a) + c2 * r2 * (globalBestA - p.a);
                p.velocityB = w * p.velocityB + c1 * r1 * (p.bestB - p.b) + c2 * r2 * (globalBestB - p.b);

                p.a += p.velocityA;
                p.b += p.velocityB;
            }
        }

        return new Particle(globalBestA, globalBestB);
    }

    public static double[] runLinearRegression(double[] klocs, double[] actualEfforts) {
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

        System.out.printf("Linear Regression Equation: Effort = %.4f * KLOC + %.4f%n", m, c);

        double[] predicted = new double[n];
        for (int i = 0; i < n; i++) {
            predicted[i] = m * klocs[i] + c;
        }

        return predicted;
    }
}

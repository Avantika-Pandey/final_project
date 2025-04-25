package org.example;

public class Particle {
    public double a, b;              // Current position (solution)
    public double velocityA, velocityB;  // Velocity for each dimension
    public double bestA, bestB;      // Personal best position
    public double bestScore;         // MMRE at personal best

    public Particle(double a, double b) {
        this.a = a;
        this.b = b;
        this.velocityA = 0;
        this.velocityB = 0;
        this.bestA = a;
        this.bestB = b;
        this.bestScore = Double.MAX_VALUE; // Start with worst possible score
    }
}

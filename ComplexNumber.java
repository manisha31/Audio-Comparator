public class ComplexNumber {
    private double real;
    private double imaginary;

    public ComplexNumber(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    // Getter for real part of the complex number
    public double getReal() {
        return this.real;
    }

    // Getter for the imaginary part of the complex number
    public double getImaginary() {
        return this.imaginary;
    }

    // Adding two complex numbers
    public ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber((this.real + other.real),
                (this.imaginary + other.imaginary));
    }

    // Subtracting two complex numbers
    public ComplexNumber sub(ComplexNumber other) {
        return new ComplexNumber((this.real - other.real),
                (this.imaginary - other.imaginary));
    }

    // Multiplication of two complex numbers
    public ComplexNumber product(ComplexNumber other) {
        double real =
                this.real * other.real - this.imaginary * other.imaginary;
        double imaginary =
                this.real * other.imaginary + this.imaginary * other.real;
        return new ComplexNumber(real, imaginary);
    }

    // Multiplication of a complex number with a number
    public ComplexNumber product(double n) {
        return new ComplexNumber(n * this.real, n * this.imaginary);
    }

    // Calculates the magnitude of the complex number
    public double magnitude() {
        return Math.hypot(real, imaginary);
    }

};

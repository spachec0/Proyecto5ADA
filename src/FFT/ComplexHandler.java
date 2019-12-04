package FFT;
//https://introcs.cs.princeton.edu/java/32class/Complex.java.html
import java.util.Objects;

public class ComplexHandler {
    private final double re;
    private final double im;

    ComplexHandler(double real, double imag) {
        re = real;
        im = imag;
    }

    // return a string representation of the invoking Complex object
    @Override
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im <  0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    // return abs/modulus/magnitude
    double abs() {
        return Math.hypot(re, im);
    }

    // return angle/phase/argument, normalized to be between -pi and pi
    public double phase() {
        return Math.atan2(im, re);
    }

    // return a new Complex object whose value is (this + b)
    ComplexHandler plus(ComplexHandler b) {
        ComplexHandler a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new ComplexHandler(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    ComplexHandler minus(ComplexHandler b) {
        ComplexHandler a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new ComplexHandler(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    ComplexHandler times(ComplexHandler b) {
        ComplexHandler a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new ComplexHandler(real, imag);
    }

    // return a new object whose value is (this * alpha)
    ComplexHandler scale(double alpha) {
        return new ComplexHandler(alpha * re, alpha * im);
    }

    // return a new Complex object whose value is the conjugate of this
    ComplexHandler conjugate() {
        return new ComplexHandler(re, -im);
    }

    // return a new Complex object whose value is the reciprocal of this
    private ComplexHandler reciprocal() {
        double scale = re*re + im*im;
        return new ComplexHandler(re / scale, -im / scale);
    }

    // return the real or imaginary part
    public double re() { return re; }
    public double im() { return im; }

    // return a / b
    private ComplexHandler divides(ComplexHandler b) {
        ComplexHandler a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public ComplexHandler exp() {
        return new ComplexHandler(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    // return a new Complex object whose value is the complex sine of this
    private ComplexHandler sin() {
        return new ComplexHandler(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex cosine of this
    private ComplexHandler cos() {
        return new ComplexHandler(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex tangent of this
    public ComplexHandler tan() {
        return sin().divides(cos());
    }
    
    // a static version of plus
    public static ComplexHandler plus(ComplexHandler a, ComplexHandler b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new ComplexHandler(real, imag);
    }

    @Override
    public boolean equals(Object x) {
        if (x == null) return false;
        if (this.getClass() != x.getClass()) return false;
        ComplexHandler that = (ComplexHandler) x;
        return (this.re == that.re) && (this.im == that.im);
    }

    @Override
    public int hashCode() {
        return Objects.hash(re, im);
    }
}

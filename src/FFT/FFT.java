package FFT;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.IntStream;

import FFT.wavHandlers.WavFile;
import FFT.wavHandlers.WavFileException;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class FFT {

    public FFT() {
        buffeOriginalWAV = null;
        realTransformed = null;
        frames = 0;
        rate = 0;
        index = 0;
        minWAV = Double.MAX_VALUE;
        maxWAV = Double.MIN_VALUE;
        max = Double.MIN_VALUE;
        original = null;
        transformed = null;
        inverted = null;
    }

    private WavFile WAV = null;

    private double[] buffeOriginalWAV;
    private double[] realTransformed;
    private double frames;
    private double rate;
    private int index;

    private double minWAV;
    private double maxWAV;
    private double max;

    private ComplexHandler[] original;
    private ComplexHandler[] transformed;
    private ComplexHandler[] inverted;

    public void doFFT(Path path) {
        // Cargando WAV
        try {
            WAV = WavFile.openWavFile(new File(path.toString()));
            frames = WAV.getNumFrames();
            rate = WAV.getSampleRate();
        } catch (IOException | WavFileException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingWorker<Void, Void> swingWorker;
        JFrame frame = new JFrame();
        final JDialog dialog = new JDialog(frame, true);
        //dialog.pack();
        swingWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    getDataFromWAV(WAV);
                    WAV.close();
                } catch (IOException | WavFileException ignored) {
                }
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                frame.dispose();
            }
        };

        frame.setVisible(false);
        swingWorker.execute();
        dialog.setVisible(true);

        double[][] dbC = getDbC((int) frames);
        double[][] rifft = getRIFFT(0, (int) frames);
        double[][] iDbC = getIDbC((int) frames);


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM.dd.HH.mm.ss");
        LocalDateTime now = LocalDateTime.now();

        try{
            String name = "/Users/sebastian/Music/ADA/audioRecuperado" + dtf.format(now);
            int sampleRate = (int) rate;
            double duration = frames / sampleRate;
            long numFrames = (long) (duration * sampleRate);
            WavFile wavFile = WavFile.newWavFile(new File(name + ".wav"), 1, numFrames, 16, sampleRate);
            double[][] buffer = new double[1][(int) numFrames];
            for (int s = 0; s < numFrames; s++) {
                buffer[0][s] = inverted[s].abs();
            }
            wavFile.writeFrames(buffer, (int) numFrames);
            wavFile.close();

            System.out.println("Archivo: " + name + ".wav guardado");
        } catch (IOException | WavFileException exc){
            System.out.println("El archivo no pudo ser guardado");
        }

        // Crea graficas
        final XYChart originalChart = QuickChart.getChart("Audio original", "Tiempo", "f(t)-Amplitud", "wav", dbC[0], dbC[1]);
        final XYChart fftChart = QuickChart.getChart("FFT", "Frecuencia", "Amplitud", "fft", rifft[0], rifft[1]);
        final XYChart recoverChart = QuickChart.getChart("Audio recuperado", "Tiempo", "f(t)-Amplitud", "ifft wav", iDbC[0], iDbC[1]);

        originalChart.getStyler().setYAxisMax(maxWAV);
        originalChart.getStyler().setYAxisMin(minWAV);
        originalChart.getStyler().setSeriesColors(new Color[]{Color.BLACK});

        fftChart.getStyler().setSeriesColors(new Color[]{Color.BLACK});
        fftChart.getStyler().setYAxisMax(max);

        recoverChart.getStyler().setYAxisMax(maxWAV);
        recoverChart.getStyler().setYAxisMin(minWAV);
        recoverChart.getStyler().setSeriesColors(new Color[]{Color.BLACK});

        // GUI
        JFrame mainFrame = new JFrame("Tranformada rapida de Fourier");

        JPanel panel0 = new XChartPanel<>(fftChart);
        JPanel panel2 = new XChartPanel<>(recoverChart);
        JPanel panel3 = new XChartPanel<>(originalChart);

        JPanel panelPrincipal = new JPanel();
        BoxLayout layout1 = new BoxLayout(panelPrincipal, BoxLayout.X_AXIS);
        panelPrincipal.setLayout(layout1);

        panelPrincipal.add(panel0);
        panelPrincipal.add(panel2);
        panelPrincipal.add(panel3);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(new Dimension(1000,790));

        mainFrame.add(panelPrincipal);
        mainFrame.add(panelPrincipal);

        Dimension tamanoPantalla = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((int) ((tamanoPantalla.getWidth() - mainFrame.getWidth()) / 2), (int) ((tamanoPantalla.getHeight() - mainFrame.getHeight()) / 2));
        mainFrame.pack();
        mainFrame.setVisible(true);

    }

    private ComplexHandler[] fastFurierTransform(ComplexHandler[] x) {
        int n = x.length;
        if (n == 1) return new ComplexHandler[]{x[0]};

        // Terminos pares
        int len;
        if (n % 2 != 0) len = n + 1;
        else len = n / 2;
        ComplexHandler[] even = new ComplexHandler[len];

        int k = 0;
        while (k < len) {
            even[k] = x[2 * k];
            k++;
        }
        ComplexHandler[] e = fastFurierTransform(even);

        // Terminos impares
        if (n % 2 != 0) len = n - 1;
        else len = n / 2;
        //len = (n % 2 != 0) ? (n - 1) / 2 : n / 2;
        ComplexHandler[] odd = new ComplexHandler[len];

        k = 0;
        while (k < len) {
            odd[k] = x[2 * k + 1];
            k++;
        }
        ComplexHandler[] o = fastFurierTransform(odd);

        // Se combinan
        if (n % 2 != 0) len = (n - 1) / 2;
        else len = n / 2;
        ComplexHandler[] y = new ComplexHandler[n];
        int bound = n / 2;

        IntStream.range(0, bound).forEachOrdered(i -> {
            double kth = -2 * i * Math.PI / n;
            ComplexHandler wk = new ComplexHandler(Math.cos(kth), Math.sin(kth));
            y[i] = e[i].plus(wk.times(o[i]));
            y[i + n / 2] = e[i].minus(wk.times(o[i]));
        });

        return y;
    }

    private ComplexHandler[] invFastFourierTransform(ComplexHandler[] x) {
        int n = x.length;
        ComplexHandler[] y = new ComplexHandler[n];

        // Toma el conjugado
        int i = 0;
        while (i < n) {
            y[i] = x[i].conjugate();
            i++;
        }

        // Calcula la FFT
        y = fastFurierTransform(y);

        // Toma el conjugado
        i = 0;
        while (i < n) {
            y[i] = y[i].conjugate();
            i++;
        }

        // Divide entre n
        i = 0;
        while (i < n) {
            y[i] = y[i].scale(1.0 / n);
            i++;
        }

        return y;
    }

    private double[][] getDbC(int sizeOfChunk) { //getDataByChunks
        if (buffeOriginalWAV.length < index + sizeOfChunk) {
            sizeOfChunk = buffeOriginalWAV.length - index;
        }
        double[] xData = new double[10];
        int count = 0;
        for (int i1 = 0; i1 < sizeOfChunk; i1++) {
            double v = (i1 + index) / rate;
            if (xData.length == count) xData = Arrays.copyOf(xData, count * 2);
            xData[count++] = v;
        }
        xData = Arrays.copyOfRange(xData, 0, count);
        double[] yData = new double[10];
        int count1 = 0;
        for (int i = 0; i < sizeOfChunk; i++) {
            double v = buffeOriginalWAV[index + i];
            if (yData.length == count1) yData = Arrays.copyOf(yData, count1 * 2);
            yData[count1++] = v;
        }
        yData = Arrays.copyOfRange(yData, 0, count1);
        return new double[][]{xData, yData};
    }

    private double[][] getRIFFT(int init, int end) {
        if (frames < end) {
            end = (int) frames;
        }
        double[] xData = new double[10];
        int count = 0;
        int bound = end - init;
        for (int a = 0; a < bound; a++) {
            double v = a;
            if (xData.length == count) xData = Arrays.copyOf(xData, count * 2);
            xData[count++] = v;
        }
        xData = Arrays.copyOfRange(xData, 0, count);

        double[] yData = new double[10];
        int count1 = 0;
        int bound1 = end - init;
        for (int i = 0; i < bound1; i++) {
            double v = realTransformed[i + init];
            if (yData.length == count1) yData = Arrays.copyOf(yData, count1 * 2);
            yData[count1++] = v;
        }
        yData = Arrays.copyOfRange(yData, 0, count1);

        return new double[][]{xData, yData};
    }

    private void getDataFromWAV(WavFile wavFile) throws IOException, WavFileException {
        int numChannels = wavFile.getNumChannels();
        double numFrames = wavFile.getNumFrames();
        double power = Math.log10(numFrames * numChannels) / Math.log10(2);
        power = (power % 1 != 0) ? Math.ceil(power) : power;
        int size = (int) Math.pow(2, power);
        buffeOriginalWAV = new double[size];
        double[] xData = new double[size];
        original = new ComplexHandler[size];
        transformed = new ComplexHandler[size];
        inverted = new ComplexHandler[size];
        int framesRead;
        do {
            framesRead = wavFile.readFrames(buffeOriginalWAV, (int) numFrames);
            for (int s = 0; s < size; s++) {
                xData[s] = s;
                original[s] = new ComplexHandler(buffeOriginalWAV[s], 0.0);
            }
        } while (framesRead != 0);
        maxWAV = Arrays.stream(buffeOriginalWAV).max().getAsDouble();
        minWAV = Arrays.stream(buffeOriginalWAV).min().getAsDouble();
        transformed = fastFurierTransform(original);
        realTransformed = IntStream.range(0, (int) frames).mapToDouble(i -> transformed[i].abs()).toArray();
        max = Arrays.stream(realTransformed).max().getAsDouble();
        inverted = invFastFourierTransform(transformed);
    }

    private double[][] getIDbC(int sizeOfChunk) { //getInverseDataByChunks
        if (buffeOriginalWAV.length < index + sizeOfChunk) {
            sizeOfChunk = buffeOriginalWAV.length - index;
        }
        double[] xData = new double[10];
        int count = 0;
        for (int i1 = 0; i1 < sizeOfChunk; i1++) {
            double v = (i1 + index) / rate;
            if (xData.length == count) xData = Arrays.copyOf(xData, count * 2);
            xData[count++] = v;
        }
        xData = Arrays.copyOfRange(xData, 0, count);

        double[] yData = new double[10];
        int count1 = 0;
        for (int i = 0; i < sizeOfChunk; i++) {
            double v = inverted[index + i].abs() * Math.signum(buffeOriginalWAV[i]);
            if (yData.length == count1) yData = Arrays.copyOf(yData, count1 * 2);
            yData[count1++] = v;
        }
        yData = Arrays.copyOfRange(yData, 0, count1);
        return new double[][]{xData, yData};
    }



}
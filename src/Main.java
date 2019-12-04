import FFT.FFT;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Main {

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {

        EventQueue.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                showGUI();
            } catch (Exception e) {
                System.out.println("Error inicializando GUI: " + e);
            }
        });
    }

    private static void showGUI() {
        FFT FFT = new doFFT();
        JFrame frameFFT = new JFrame("");
        JPanel panelFFT = new JPanel();

        frameFFT.setLayout(new BoxLayout(frameFFT, BoxLayout.X_AXIS));

        frameFFT.setLocationRelativeTo(null);
        frameFFT.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton botonCorrer = new JButton("Correr");
        JButton botonAbrir = new JButton("Abrir WAV");
        panelFFT.add(botonCorrer);
        panelFFT.add(botonAbrir);

        botonCorrer.setEnabled(false);

        final Path[] path = new Path[1];

        botonAbrir.addActionListener(actionEvent -> {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV FIles", "wav", "wav");
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Escoge un archivo WAV");
            fc.setFileFilter(filter);
            fc.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (fc.showOpenDialog(botonAbrir.getParent()) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fc.getSelectedFile();
                path[0] = Paths.get(selectedFile.getAbsolutePath());
                botonCorrer.setEnabled(true);
            }
        });

        botonCorrer.addActionListener(e1 -> {
            JOptionPane.getRootFrame().dispose();
            FFT.doFFT(path[0]);
        });

        String[] options = {"OK"};
        JOptionPane.showOptionDialog(null,
                panelFFT,
                "Transformada rapida de Fourier",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

    }
}

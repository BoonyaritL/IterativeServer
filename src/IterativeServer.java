import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.function.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Optional;

public class IterativeServer {
    private static final int PORT = 8888;
    private static JFrame frame;
    private static JLabel imageLabel;
    
    public static void main(String[] args) {
        initializeGUI();
        startServer(PORT);
    }
    
    // Pure function: สร้าง GUI
    private static void initializeGUI() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Screen Monitor - Iterative Server");
            imageLabel = new JLabel("Waiting for screen capture...");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            
            frame.add(new JScrollPane(imageLabel));
            frame.setSize(1024, 768);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
    
    // Higher-order function: รับ connection และ process
    private static void startServer(int port) {
        uncheck(() -> createServerSocket(port))
            .ifPresent(IterativeServer::runServerLoop);
    }
    
    private static void acceptConnections(ServerSocket serverSocket) {
        uncheck(() -> acceptClient(serverSocket))
            .ifPresent(client -> {
                System.out.println("Client connected: " + client.getInetAddress());
                handleClient(client);
                acceptConnections(serverSocket); // Tail recursion
            });
    }
    
    // Pure function: จัดการ client
    private static void handleClient(Socket socket) {
        uncheck(() -> socket.getInputStream())
            .map(IterativeServer::readImageFromStream)
            .ifPresent(IterativeServer::displayImage);
    }
    
    // Exception extraction utilities
    private static <T> Optional<T> uncheck(CheckedSupplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception e) {
            handleException().accept(e);
            return Optional.empty();
        }
    }
    
    private static Consumer<Exception> handleException() {
        return exception -> {
            System.err.println("Exception handled: " + exception.getMessage());
            // Control flow can resume here
        };
    }
    
    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
    
    private static BufferedImage readImage(InputStream inputStream) {
        return uncheck(() -> ImageIO.read(inputStream))
            .orElse(null);
    }
    
    // Helper methods for method references
    private static ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        return serverSocket;
    }
    
    private static void runServerLoop(ServerSocket serverSocket) {
        acceptConnections(serverSocket);
    }
    
    private static Socket acceptClient(ServerSocket serverSocket) throws IOException {
        return serverSocket.accept();
    }
    
    private static BufferedImage readImageFromStream(InputStream inputStream) {
        BufferedImage image = readImage(inputStream);
        return image;
    }
    
    private static void displayImage(BufferedImage image) {
        Optional.ofNullable(image)
            .ifPresent(img -> {
                SwingUtilities.invokeLater(() -> {
                    ImageIcon icon = new ImageIcon(scaleImage(img, 1000, 700));
                    imageLabel.setIcon(icon);
                    imageLabel.setText("");
                    frame.repaint();
                });
                System.out.println("Image displayed: " + img.getWidth() + "x" + img.getHeight());
            });
    }
    
    private static Image scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        double scale = Math.min(
            (double) maxWidth / original.getWidth(),
            (double) maxHeight / original.getHeight()
        );
        
        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);
        
        return original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }
}
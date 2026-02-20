import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.function.*;
import javax.imageio.ImageIO;
import java.util.Optional;

public class ScreenCaptureClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int CAPTURE_INTERVAL = 2000; // 2 วินาที
    private static boolean hasCaptured = false; // เพิ่ม flag เพื่อให้ capture ครั้งเดียว

    public static void main(String[] args) {
        System.out.println("Screen Capture Client Started");
        captureAndSendLoop();
    }

    private static void captureAndSendLoop() {
        if (!hasCaptured) { // capture เฉพาะครั้งแรกเท่านั้น
            captureScreen()
                .flatMap(ScreenCaptureClient::sendToServer)
                .ifPresent(System.out::println);
            hasCaptured = true; // ตั้งค่า flag ว่าได้ capture แล้ว
        }

        sleep(CAPTURE_INTERVAL);
        captureAndSendLoop(); // Tail recursion
    }

    // Pure function: จับภาพหน้าจอ (return Optional)
    private static Optional<BufferedImage> captureScreen() {
        return uncheck(ScreenCaptureClient::createRobot)
            .map(ScreenCaptureClient::captureScreenRect)
            .map(ScreenCaptureClient::logScreenCapture);
    }

    // Function with side-effect: ส่งภาพไปยัง server
    private static Optional<String> sendToServer(BufferedImage image) {
        return uncheck(() -> writeImageToServer(image));
    }

    // Pure function: sleep wrapper
    private static void sleep(int millis) {
        uncheck(() -> {
            Thread.sleep(millis);
            return null;
        }).ifPresentOrElse(
            value -> {}, // Success case - continue normal flow
            () -> handleException().accept(new InterruptedException("Sleep interrupted"))
        );
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
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // Control flow can resume here
        };
    }
    
    // Helper methods for method references
    private static Robot createRobot() throws AWTException {
        return new Robot();
    }
    
    private static Rectangle getScreenRect() {
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    private static BufferedImage captureScreenRect(Robot robot) {
        Rectangle screenRect = getScreenRect();
        return robot.createScreenCapture(screenRect);
    }
    
    private static BufferedImage logScreenCapture(BufferedImage screenshot) {
        System.out.println("Screen captured: " + screenshot.getWidth() + "x" + screenshot.getHeight());
        return screenshot;
    }
    
    private static Socket createSocket() throws IOException {
        return new Socket(SERVER_HOST, SERVER_PORT);
    }
    
    private static String writeImageToServer(BufferedImage image) throws IOException {
        try (Socket socket = createSocket();
             OutputStream outputStream = socket.getOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            outputStream.flush();
            return "Image sent successfully";
        }
    }
    
    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
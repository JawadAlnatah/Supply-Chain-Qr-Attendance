package com.team.supplychain.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating QR codes using ZXing library
 * Converts QR code data strings into scannable JavaFX Images
 */
public class QRCodeService {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * Generate QR code as JavaFX Image from string data
     * @param data The data to encode (employee QR code string from database)
     * @param width Width of the QR code image in pixels
     * @param height Height of the QR code image in pixels
     * @return JavaFX Image or null if generation fails
     */
    public static Image generateQRCodeImage(String data, int width, int height) {
        if (data == null || data.isEmpty()) {
            System.err.println("QR Code data cannot be null or empty");
            return null;
        }

        try {
            // Configure QR code parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // Minimal margin for cleaner look

            // Generate QR code BitMatrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
            );

            // Convert BitMatrix to BufferedImage
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Convert BufferedImage to JavaFX Image
            return convertToFxImage(bufferedImage);

        } catch (WriterException e) {
            System.err.println("Failed to generate QR code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate QR code with default dimensions (300x300)
     * @param data The data to encode
     * @return JavaFX Image or null if generation fails
     */
    public static Image generateQRCodeImage(String data) {
        return generateQRCodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Validate if a QR code string is valid (basic validation)
     * @param qrCode The QR code string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidQRCode(String qrCode) {
        return qrCode != null && !qrCode.trim().isEmpty() && qrCode.length() >= 3;
    }

    /**
     * Convert BufferedImage to JavaFX WritableImage
     * @param bufferedImage The BufferedImage to convert
     * @return JavaFX Image
     */
    private static Image convertToFxImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }

        return writableImage;
    }
}

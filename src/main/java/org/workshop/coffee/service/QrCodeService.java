package org.workshop.coffee.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    private static final int QR_CODE_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generate QR code as byte array (PNG image)
     * 
     * @param text The text to encode in the QR code
     * @return Byte array representing the QR code image
     * @throws WriterException If QR code generation fails
     * @throws IOException If image writing fails
     */
    public byte[] generateQrCodeImage(String text) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, IMAGE_FORMAT, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Generate QR code data string for an order
     * 
     * @param orderId Order ID
     * @param customerName Customer name
     * @param orderDate Order date
     * @param totalAmount Total order amount
     * @return Formatted string for QR code
     */
    public String generateOrderQrCodeData(Long orderId, String customerName, String orderDate, Double totalAmount) {
        StringBuilder qrData = new StringBuilder();
        qrData.append("ORDER INFO\n");
        qrData.append("==========\n");
        qrData.append("Order ID: ").append(orderId).append("\n");
        qrData.append("Customer: ").append(customerName).append("\n");
        qrData.append("Date: ").append(orderDate).append("\n");
        qrData.append("Total: $").append(String.format("%.2f", totalAmount)).append("\n");
        qrData.append("\nCoffeeShop Order");
        return qrData.toString();
    }

    /**
     * Generate QR code as byte array for an order
     * 
     * @param orderId Order ID
     * @param customerName Customer name
     * @param orderDate Order date
     * @param totalAmount Total order amount
     * @return Byte array representing the QR code image
     * @throws WriterException If QR code generation fails
     * @throws IOException If image writing fails
     */
    public byte[] generateOrderQrCode(Long orderId, String customerName, String orderDate, Double totalAmount) 
            throws WriterException, IOException {
        String qrData = generateOrderQrCodeData(orderId, customerName, orderDate, totalAmount);
        return generateQrCodeImage(qrData);
    }
}


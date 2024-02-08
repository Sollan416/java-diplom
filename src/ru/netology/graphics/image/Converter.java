package ru.netology.graphics.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

public class Converter implements TextGraphicsConverter {

    TextColorSchema schema = new Schema();
    private int maxWidth = -1;
    private int maxHeight = -1;
    private double maxRatio = -1;

    @Override
    public String convert(String url) throws IOException, BadImageSizeException {

        BufferedImage img = ImageIO.read(new URL(url));

        double width = img.getWidth();
        double height = img.getHeight();
        double ratio;

        ratio = (Math.max(width, height)) / (Math.min(width, height));

        if (maxRatio != -1 && ratio > maxRatio) {
            throw new BadImageSizeException(ratio, maxRatio);
        }

        ratio = width / height;
        int newWidth = (int) width;
        int newHeight = (int) height;

        if (maxWidth > 0 && maxHeight > 0) {

            double widthPercent = maxWidth / width;
            double heightPercent = maxHeight / height;

            if (widthPercent < heightPercent) {
                newWidth = Math.min(maxWidth, (int) width);
                newHeight = calcProportion('w', ratio, Math.min(maxWidth, (int) width));

            } else {
                newHeight = Math.min(maxHeight, (int) width);
                newWidth = calcProportion('h', ratio, Math.min(maxHeight, (int) height));
            }

        } else if (maxWidth > 0 && maxHeight == -1) {
            newWidth = Math.min(maxWidth, (int) width);
            newHeight = calcProportion('w', ratio, Math.min(maxWidth, (int) width));

        } else if (maxHeight > 0 && maxWidth == -1) {
            newHeight = Math.min(maxHeight, (int) width);
            newWidth = calcProportion('h', ratio,Math.min(maxHeight, (int) height));
        }

        Image scaledImage = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
        BufferedImage bwImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = bwImg.createGraphics();
        graphics.drawImage(scaledImage, 0, 0, null);

        WritableRaster bwRaster = bwImg.getRaster();

        char[][] pixels = new char[newHeight][newWidth];
        int[] depthOfWhite = new int[3];

        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                int color = bwRaster.getPixel(j, i, depthOfWhite)[0];
                char c = schema.convert(color);
                pixels[i][j] = c;
            }
        }
        StringBuilder textImage = new StringBuilder();
        for (int i = 0; i < newHeight; i++) {
            if (i > 0) textImage.append("\n");
            for (int j = 0; j < newWidth; j++) {
                textImage.append(pixels[i][j]);
                textImage.append(pixels[i][j]);
            }
        }
        return textImage.toString();
    }

    @Override
    public void setMaxWidth(int width) {
        maxWidth = width;
    }

    @Override
    public void setMaxHeight(int height) {
        maxHeight = height;
    }

    @Override
    public void setMaxRatio(double maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public void setTextColorSchema(TextColorSchema schema) {
        this.schema = schema;
    }

    public int calcProportion(char widthOrHeight, double ratio, int lengthOfSide) {
        if (widthOrHeight == 'w') {
            return (int) Math.round(lengthOfSide / ratio);
        }
        if (widthOrHeight == 'h') {
            return (int) Math.round(lengthOfSide * ratio);
        }
        return 0;
    }
}
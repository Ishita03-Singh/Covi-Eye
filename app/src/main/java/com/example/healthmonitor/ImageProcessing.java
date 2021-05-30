package com.example.healthmonitor;

public abstract class ImageProcessing {
    private static int decode420sptoRedBlueGreenSum(byte[] yuv420sp, int width, int height, int type) {
        if (yuv420sp == null) return 0;

        final int frameSize = width * height;

        int sum = 0;
        int sumr = 0;
        int sumg = 0;
        int sumb = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                sumr += red;
                sumg += green;
                sumb += blue;
            }
        }
        switch (type) {
            case (1):
                sum = sumr;
                break;
            case (2):
                sum = sumb;
                break;
            case (3):
                sum = sumg;
                break;
        }
        return sum;
    }


    static double decodetoRedBlueGreenAvg(byte[] yuv420sp, int width, int height, int type) {
        if (yuv420sp == null) return 0;
        final int frameSize = width * height;

        int sum = decode420sptoRedBlueGreenSum(yuv420sp, width, height, type);

        return (sum / frameSize);
    }
}

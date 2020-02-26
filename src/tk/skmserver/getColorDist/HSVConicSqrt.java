package tk.skmserver.getColorDist;

import tk.skmserver.ColorDist;

import java.awt.*;

public class HSVConicSqrt implements ColorDist {
    @Override
    public double getDist(int i, int i1) {
        int r1 = (i >> 16) & 0xFF;
        int r2 = (i1>> 16) & 0xFF;
        int g1 = (i >> 8) & 0xFF;
        int g2 = (i1>> 8) & 0xFF;
        int b1 = i & 0xFF;
        int b2 = i1& 0xFF;
        float[] hsv1 = new float[3];
        float[] hsv2 = new float[3];
        Color.RGBtoHSB(r1, g1, b1, hsv1);
        Color.RGBtoHSB(r2, g2, b2, hsv2);
        float h1 = hsv1[0], s1 = hsv1[1], v1 = hsv1[2],
                h2 = hsv2[0], s2 = hsv2[1], v2 = hsv2[2];
        double d1 = (s1 * Math.cos(2 * Math.PI * h1 / 255.0d) * v1 / 255.0d) + (s2 * Math.cos(2 * Math.PI * h2 / 255.0d) * v2 / 255.0d);
        double d2 = (s1 * Math.sin(2 * Math.PI * h1 / 255.0d) * v1 / 255.0d) + (s2 * Math.sin(2 * Math.PI * h2 / 255.0d) * v2 / 255.0d);
        double d3 = v1 - v2;
        return Math.sqrt((d1 * d1) + (d2 * d2) + (d3 * d3));
    }
}

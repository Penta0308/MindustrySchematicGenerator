package tk.skmserver.getColorDist;

import tk.skmserver.ColorDist;

import java.awt.*;

public class HSVConicSqrt implements ColorDist {
    @Override
    public double getDist(int i, int i1) {
        int r1 = (i >>> 16) & 0x000000FF;
        int r2 = (i1>>> 16) & 0x000000FF;
        int g1 = (i >>> 8) & 0x000000FF;
        int g2 = (i1>>> 8) & 0x000000FF;
        int bl1 = i & 0x000000FF;
        int bl2 = i1& 0x000000FF;
        float[] hsb1 = new float[3];
        float[] hsb2 = new float[3];
        Color.RGBtoHSB(r1, g1, bl1, hsb1);
        Color.RGBtoHSB(r2, g2, bl2, hsb2);
        float h1 = hsb1[0], s1 = hsb1[1], b1 = hsb1[2],
                h2 = hsb2[0], s2 = hsb2[1], b2 = hsb2[2];
        double d1 = (s1 * Math.cos(2 * Math.PI * h1 / 255.0d) * b1 / 255.0d) + (s2 * Math.cos(2 * Math.PI * h2 / 255.0d) * b2 / 255.0d);
        double d2 = (s1 * Math.sin(2 * Math.PI * h1 / 255.0d) * b1 / 255.0d) + (s2 * Math.sin(2 * Math.PI * h2 / 255.0d) * b2 / 255.0d);
        double d3 = b1 - b2;
        return Math.sqrt((d1 * d1) + (d2 * d2) + (d3 * d3));
    }
}

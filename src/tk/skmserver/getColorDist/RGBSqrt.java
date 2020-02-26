package tk.skmserver.getColorDist;

import tk.skmserver.ColorDist;

public class RGBSqrt implements ColorDist {
    @Override
    public double getDist(int i, int i1) {
        int r1 = (i >> 16) & 0xFF;
        int r2 = (i1>> 16) & 0xFF;
        int g1 = (i >> 8) & 0xFF;
        int g2 = (i1>> 8) & 0xFF;
        int b1 = i & 0xFF;
        int b2 = i1& 0xFF;
        return Math.sqrt((r1-r2)^2 + (g1-g2)^2 + (b1-b2)^2);
    }
}

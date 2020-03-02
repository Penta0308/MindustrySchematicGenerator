package tk.skmserver.getColorDist;

import tk.skmserver.CIELab;
import tk.skmserver.ColorDist;

public class CIELabSqrt implements ColorDist {
    @Override
    public double getDist(int i, int i1) {
        CIELab lab = CIELab.getInstance();
        float[] rgb = new float[3];
        rgb[0] = ((i & 0x00FF0000) >> 16) / 256.0f;
        rgb[1] = ((i & 0x0000FF00) >> 8)  / 256.0f;
        rgb[2] =  (i & 0x000000FF)        / 256.0f;
        float[] l1 = lab.fromRGB(rgb);
        rgb[0] = ((i1& 0x00FF0000) >> 16) / 256.0f;
        rgb[1] = ((i1& 0x0000FF00) >> 8)  / 256.0f;
        rgb[2] =  (i1& 0x000000FF)        / 256.0f;
        float[] l2 = lab.fromRGB(rgb);
        return Math.sqrt(Math.pow(l1[0] - l2[0], 2) + Math.pow(l1[1]-l2[1], 2) + Math.pow(l1[2] - l2[2], 2));
    }
}

package tk.skmserver;

import tk.skmserver.getColorDist.HSVConicSqrt;
import tk.skmserver.getColorDist.RGBSqrt;

import java.util.HashMap;
import java.util.Map;

public class GetColorDist {
    public static Map<String, ColorDist> m;
    public GetColorDist() {
        m = new HashMap<>();
        m.put("RGB", new RGBSqrt());
        m.put("HSVConic", new HSVConicSqrt());
    }
    public ColorDist getColorDist(String s) {
        return m.get(s);
    }
}

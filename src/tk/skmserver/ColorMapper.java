package tk.skmserver;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ColorMapper {
    public static Map<Integer, Integer> colorspace;
    private String type;
    GetColorDist gcd;

/*
#d99d73 copper
#8c7fa9 lead
#ebeef5 metaglass
#b2c6d2 graphite
#f7cba4 sand
#272727 coal
#8da1e3 titanium
#f9a3c7 thorium
#777777 scrap
#53565c silicon
#cbd97f plastanium
#f4ba6e phase-fabric
#f3e979 surge-alloy
#7457ce spore-pod
#ff795e blast-compound
#ffaa5f pyratite
 */

    public ColorMapper(String type) {
        this.type = type;
        colorspace = new HashMap<>();
        /*colorspace.put(0xd99d73, "copper");
        colorspace.put(0x8c7fa9, "lead");
        colorspace.put(0xebeef5, "metaglass");
        colorspace.put(0xb2c6d2, "graphite");
        colorspace.put(0xf7cba4, "sand");
        colorspace.put(0x272727, "coal");
        colorspace.put(0x8da1e3, "titanium");
        colorspace.put(0xf9a3c7, "thorium");
        colorspace.put(0x777777, "scrap");
        colorspace.put(0x53565c, "silicon");
        colorspace.put(0xcbd97f, "plastaium");
        colorspace.put(0xf4ba6e, "phase-fabric");
        colorspace.put(0xf3e979, "surge-alloy");
        colorspace.put(0x7457ce, "spore-pod");
        colorspace.put(0xff795e, "blast-compound");
        colorspace.put(0xffaa5f, "pyratite");
        */
        colorspace.put(0xd99d73, 0);
        colorspace.put(0x8c7fa9, 1);
        colorspace.put(0xebeef5, 2);
        colorspace.put(0xb2c6d2, 3);
        colorspace.put(0xf7cba4, 4);
        colorspace.put(0x272727, 5);
        colorspace.put(0x8da1e3, 6);
        colorspace.put(0xf9a3c7, 7);
        colorspace.put(0x777777, 8);
        colorspace.put(0x53565c, 9);
        colorspace.put(0xcbd97f, 10);
        colorspace.put(0xf4ba6e, 11);
        colorspace.put(0xf3e979, 12);
        colorspace.put(0x7457ce, 13);
        colorspace.put(0xff795e, 14);
        colorspace.put(0xffaa5f, 15);

        gcd = new GetColorDist();
    }


    public Integer ColorMapper(Color c) {
        int i = (c.getRed() << 16) & 0x00ff0000 | (c.getGreen() << 8) & 0x0000ff00 | c.getBlue() & 0x000000ff;
        AtomicReference<Integer> nowLessy = new AtomicReference<>(0);
        AtomicReference<Double> nowValue = new AtomicReference<>(Double.MAX_VALUE);
        colorspace.forEach((k, v) -> {
            double e = getDist(k, i);
            if( e < nowValue.get()) {
                nowValue.set(e);
                nowLessy.set(v);
            }
        });
        return nowLessy.get();
    }

    public double getDist(int i, int i1) {
        ColorDist cdf = gcd.getColorDist(type);
        return cdf.getDist(i, i1);
    }
}

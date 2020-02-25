package tk.skmserver;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class FilterMapper {
    public static Map<Integer, Integer> colorspace;

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

    public FilterMapper() {
        colorspace = new HashMap<>();
        colorspace.put(0, 0xd99d73);
        colorspace.put(1, 0x8c7fa9);
        colorspace.put(2, 0xebeef5);
        colorspace.put(3, 0xb2c6d2);
        colorspace.put(4, 0xf7cba4);
        colorspace.put(5, 0x272727);
        colorspace.put(6, 0x8da1e3);
        colorspace.put(7, 0xf9a3c7);
        colorspace.put(8, 0x777777);
        colorspace.put(9, 0x53565c);
        colorspace.put(10, 0xcbd97f);
        colorspace.put(11, 0xf4ba6e);
        colorspace.put(12, 0xf3e979);
        colorspace.put(13, 0x7457ce);
        colorspace.put(14, 0xff795e);
        colorspace.put(15, 0xffaa5f);
    }


    public Integer FilterMapper(int f) {
        return colorspace.get(f);
    }
}

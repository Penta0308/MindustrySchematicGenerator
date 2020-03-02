package tk.skmserver.mindustry;

import arc.struct.Array;
import arc.struct.StringMap;

public class TSchematic {
    public short width;
    public short height;
    public StringMap tags;
    public Array<TTile> tiles;

    public TSchematic(Array<TTile> tiles, StringMap tags, short width, short height) {
        this.tiles = tiles;
        this.tags = tags;
        this.width = width;
        this.height = height;
    }
}

package tk.skmserver.mindustry;

public class TTile {

    public TBlock block;
    public int config;
    public byte rotation = 0;
    public short x, y;

    public TTile(TBlock block, int x, int y, int config, byte rotation) {
        this.block = block;
        this.x = (short)x;
        this.y = (short)y;
        this.config = config;
        this.rotation = rotation;
    }
}
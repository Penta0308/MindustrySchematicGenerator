package tk.skmserver;

import arc.files.Fi;
import arc.struct.*;
import arc.util.io.Streams;
import arc.util.serialization.Base64Coder;
import mindustry.world.Pos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Main {


    public static void main(String[] args) throws IOException { //ImagePath Resolution(x y) OutputPath
        if (args.length == 6 && args[0].equals("generate")) {
            File original_image = new File(args[1]);
            BufferedImage buffer_original_image = null;

            try {
                buffer_original_image = ImageIO.read(original_image);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            int width = Integer.parseInt(args[2]);
            int height = Integer.parseInt(args[3]);

            BufferedImage buffer_post_image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphic = buffer_post_image.createGraphics();
            graphic.drawImage(buffer_original_image, 0, 0, width, height, null);

            ColorMapper colorMapper = new ColorMapper();

            Array<TTile> tiles = new Array<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    tiles.add(new TTile(null, x, y, colorMapper.ColorMapper(new Color(buffer_post_image.getRGB(x, height - y - 1))), (byte) 0));
                }
            }


            StringMap tags = new StringMap();
            //tags.put("name", args[4]);
            Schematic sch = new Schematic(tiles, tags, (short) width, (short) height);

            if (args[5].equals("STDIO")) {
                Streams.OptimizedByteArrayOutputStream out = new Streams.OptimizedByteArrayOutputStream(1024);
                out.reset();
                write(sch, out);
                System.out.print(new String(Base64Coder.encode(out.getBuffer(), out.size())));
            } else {
                write(sch, new Fi(args[5]).write(false, 1024));
            }


        } else if (args.length == 3 && args[0].equals("extract")) {
            File fi = new File(args[1]);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fi);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fis.skip(5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InflaterInputStream is = new InflaterInputStream(fis);

            if (args[2].equals("STDIO")) {
                Streams.OptimizedByteArrayOutputStream out = new Streams.OptimizedByteArrayOutputStream(1024);
                out.reset();
                while(fis.available() != 0) {
                    out.write(fis.read());
                }

                System.out.print(new String(Base64Coder.encode(out.getBuffer(), out.size())));
            } else {
                try {
                    Files.copy(is, Paths.get(args[2]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            System.out.println("Usage : generate <imagepath> <X-resolution> <Y-resolution> <name> <outputpath>");
            System.out.println("Usage : extract <schpath> <outputpath>");
            System.out.println("set <outputpath> \"STDIO\" to get output by base64");
        }
    }

    public static class TBlock {
        public String name;
        public TBlock(String name) {
            this.name = name;
        }
    }

    public static class TTile {

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

    public static class Schematic {
        public short width;
        public short height;
        public StringMap tags;
        public Array<TTile> tiles;

        public Schematic(Array<TTile> tiles, StringMap tags, short width, short height) {
            this.tiles = tiles;
            this.tags = tags;
            this.width = width;
            this.height = height;
        }
    }

    private static final byte[] header = {'m', 's', 'c', 'h'};
    private static final byte version = 0;

    public static void write(Schematic schematic, OutputStream output) throws IOException {
        output.write(header);
        output.write(version);

        try (DataOutputStream stream = new DataOutputStream(new DeflaterOutputStream(output))) {

            stream.writeShort(schematic.width);
            stream.writeShort(schematic.height);

            stream.writeByte(0);
            //stream.writeByte(schematic.tags.size);
            //for (ObjectMap.Entry<String, String> e : schematic.tags.entries()) {
            //    stream.writeUTF(e.key);
            //    stream.writeUTF(e.value);
            //}

            //OrderedSet<TBlock> blocks = new OrderedSet<>();
            //schematic.tiles.each(t -> blocks.add(t.block));

            //create dictionary
            //stream.writeByte(blocks.size);
            //for (int i = 0; i < blocks.size; i++) {
            //    stream.writeUTF(blocks.orderedItems().get(i).name);
            //}
            stream.writeByte(1);
            stream.writeUTF("sorter");

            stream.writeInt(schematic.tiles.size);
            //write each tile
            for (TTile tile : schematic.tiles) {
                //stream.writeByte(blocks.orderedItems().indexOf(tile.block));
                stream.writeByte(0);
                stream.writeInt(Pos.get(tile.x, tile.y));
                stream.writeInt(tile.config);
                stream.writeByte(tile.rotation);
            }
        }
    }
}

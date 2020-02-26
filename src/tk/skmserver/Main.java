package tk.skmserver;

import arc.files.Fi;
import arc.struct.*;
import arc.util.ArcAnnotate;
import arc.util.io.Streams;
import arc.util.serialization.Base64Coder;
import mindustry.world.Pos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

            ColorMapper colorMapper = new ColorMapper("HSVConic");

            Array<TTile> tiles = new Array<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color c = new Color(buffer_post_image.getRGB(x, height - y - 1));
                    if(!c.equals(Color.WHITE)) tiles.add(new TTile(new TBlock("sorter"), x, y, colorMapper.ColorMapper(c), (byte) 0));
                }
            }


            StringMap tags = new StringMap();
            tags.put("name", args[4]);
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

        } else if (args.length == 3 && args[0].equals("image")) {
            Schematic s = read(Fi.get(args[1]));
            BufferedImage buffer_pre_image = new BufferedImage(s.width, s.height, BufferedImage.TYPE_3BYTE_BGR);
            FilterMapper filterMapper = new FilterMapper();

            for (int y = 0; y < s.height; y++)
                for (int x = 0; x < s.width; x++)
                    buffer_pre_image.setRGB(x, y, Color.WHITE.getRGB());

            s.tiles.forEach((t) -> {
                if (t.block.name.equals("sorter"))
                    buffer_pre_image.setRGB(t.x, s.height - t.y - 1, filterMapper.FilterMapper(t.config));
                else buffer_pre_image.setRGB(t.x, s.height - t.y - 1, Color.WHITE.getRGB());
            });
            File out = new File(args[2]);
            ImageIO.write(buffer_pre_image, "png", out);

        } else if (args.length == 5 && args[0].equals("map")) {
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

            BufferedImage buffer_pre_image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

            ColorMapper colorMapper = new ColorMapper("HSVConic");

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color c = new Color(buffer_post_image.getRGB(x, y));
                    buffer_pre_image.setRGB(x, y, colorMapper.ColorMapper(c));
                }
            }
            ImageIO.write(buffer_pre_image, "png", new File(args[4]));
        } else {

            System.out.println("Usage : generate <imagepath> <X-resolution> <Y-resolution> <name> <outputpath>");
            System.out.println("Usage : extract <schpath> <outputpath>");
            System.out.println("Set <outputpath> \"STDIO\" to get output by base64");
            System.out.println("Usage : image <schpath> <outputpath>");
            System.out.println("Generates PNG Image From Schematic");
            System.out.println("Usage : map <inputpath> <X-resolution> <Y-resolution> <outputpath>");
            System.out.println("Generates Mapped PNG Image from PNG Image");
            System.out.println("#FFFFFF means Air");
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

    public static Schematic read(Fi file) throws IOException {
        return read(new DataInputStream(file.read(1024)));
    }

    private @ArcAnnotate.Nullable
    Schematic loadFile(Fi file){
        if(!file.extension().equals("msch")) return null;

        try {
            return read(file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Schematic read(InputStream input) throws IOException {
        for (byte b : header) {
            if (input.read() != b) {
                throw new IOException("Not a schematic file (missing header).");
            }
        }

        int ver;
        if ((ver = input.read()) != version) {
            throw new IOException("Unknown version: " + ver);
        }

        try (DataInputStream stream = new DataInputStream(new InflaterInputStream(input))) {
            short width = stream.readShort(), height = stream.readShort();

            StringMap map = new StringMap();
            byte tags = stream.readByte();
            for (int i = 0; i < tags; i++) {
                stream.readUTF(); stream.readUTF();
            }

            byte sortercode = 0;

            switch(stream.readByte()) {
                case 1:
                    if(stream.readUTF().equals("sorter")) sortercode = 0;
                    break;
                case 2:
                    if(stream.readUTF().equals("sorter")) {
                        sortercode = 0;
                    } else {
                        sortercode = 1;
                    }
                    stream.readUTF();
                    break;
                default:
            }

            int total = stream.readInt();
            Array<TTile> tiles = new Array<>(total);
            for (int i = 0; i < total; i++) {
                TBlock block;
                if(stream.readByte() == sortercode) block = new TBlock("sorter"); else block = new TBlock("air");
                int position = stream.readInt();
                int config = stream.readInt();
                byte rotation = stream.readByte();
                tiles.add(new TTile(block, Pos.x(position), Pos.y(position), config, rotation));
            }

            return new Schematic(tiles, map, width, height);
        }
    }

    public static void write(Schematic schematic, OutputStream output) throws IOException {
        output.write(header);
        output.write(version);

        try (DataOutputStream stream = new DataOutputStream(new DeflaterOutputStream(output))) {

            stream.writeShort(schematic.width);
            stream.writeShort(schematic.height);

            //stream.writeByte(0);
            stream.writeByte(schematic.tags.size);
            for (ObjectMap.Entry<String, String> e : schematic.tags.entries()) {
                stream.writeUTF(e.key);
                stream.writeUTF(e.value);
            }

            //OrderedSet<TBlock> blocks = new OrderedSet<>();
            //schematic.tiles.each(t -> blocks.add(t.block));

            //create dictionary
            //stream.writeByte(blocks.size);
            //for (int i = 0; i < blocks.size; i++) {
            //    stream.writeUTF(blocks.orderedItems().get(i).name);
            //}
            AtomicBoolean enableair = new AtomicBoolean(false);
            schematic.tiles.forEach((t) -> {
                if(!t.block.name.equals("sorter")) enableair.set(true);
            });
            if(enableair.get()) {
                stream.writeByte(2);
                stream.writeUTF("sorter");
                stream.writeUTF("air");
            } else {
                stream.writeByte(1);
                stream.writeUTF("sorter");
            }

            stream.writeInt(schematic.tiles.size);
            //write each tile
            for (TTile tile : schematic.tiles) {
                //stream.writeByte(blocks.orderedItems().indexOf(tile.block));
                if(tile.block.name.equals("sorter")) {
                    stream.writeByte(0);
                    stream.writeInt(Pos.get(tile.x, tile.y));
                    stream.writeInt(tile.config);
                    stream.writeByte(tile.rotation);
                } else {
                    stream.writeByte(1);
                    stream.writeInt(Pos.get(tile.x, tile.y));
                    stream.writeInt(0);
                    stream.writeByte(0);
                }
            }
        }
    }
}

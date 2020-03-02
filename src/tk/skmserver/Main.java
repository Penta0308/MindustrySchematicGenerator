package tk.skmserver;

import arc.files.Fi;
import arc.struct.*;
import arc.util.io.Streams;
import arc.util.serialization.Base64Coder;
import mindustry.world.Pos;
import tk.skmserver.mindustry.TBlock;
import tk.skmserver.mindustry.TSchematic;
import tk.skmserver.mindustry.TTile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Main {


    public static void main(String[] args) throws IOException {
        if (args.length == 4 && args[0].equals("generate")) {
            File original_image = new File(args[1]);
            BufferedImage buffer_original_image = ImageIO.read(original_image);
            int width = buffer_original_image.getWidth();
            int height = buffer_original_image.getHeight();

            ColorMapper colorMapper = new ColorMapper("CIELab");

            int[][] buffer_post_image = doMap(buffer_original_image, colorMapper);

            Array<TTile> tiles = new Array<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int c = buffer_post_image[x][height - y - 1];
                    if(c != -1) {
                        tiles.add(new TTile(new TBlock("sorter"), x, y, c, (byte) 0));
                    }
                }
            }

            StringMap tags = new StringMap();
            tags.put("name", args[2]);
            TSchematic sch = new TSchematic(tiles, tags, (short) width, (short) height);

            if (args[3].equals("STDIO")) {
                Streams.OptimizedByteArrayOutputStream out = new Streams.OptimizedByteArrayOutputStream(1024);
                out.reset();
                write(sch, out);
                System.out.print(new String(Base64Coder.encode(out.getBuffer(), out.size())));
            } else {
                write(sch, new Fi(args[3]).write(false, 1024));
            }


        }
        else if (args.length == 3 && args[0].equals("extract")) {
            File fi = new File(args[1]);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fi);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(fis == null) System.exit(1);

            if(fis.skip(5) == 0) System.exit(1);

            InflaterInputStream is = new InflaterInputStream(fis);

            if (args[2].equals("STDIO")) {
                Streams.OptimizedByteArrayOutputStream out = new Streams.OptimizedByteArrayOutputStream(1024);
                out.reset();
                while(fis.available() != 0) out.write(fis.read());

                System.out.print(new String(Base64Coder.encode(out.getBuffer(), out.size())));
            } else {
                Files.copy(is, Paths.get(args[2]));
            }

        }
        else if (args.length == 3 && args[0].equals("image")) {
            TSchematic s = loadFile(Fi.get(args[1]));
            if (s == null) System.exit(1);
            BufferedImage buffer_pre_image = new BufferedImage(s.width, s.height, BufferedImage.TYPE_INT_ARGB);
            FilterMapper filterMapper = new FilterMapper();

            for (int y = 0; y < s.height; y++)
                for (int x = 0; x < s.width; x++)
                    buffer_pre_image.setRGB(x, y, 0x00FFFFFF); //Clear(Invisible) White

            s.tiles.forEach((t) -> {
                if (t.block.name.equals("sorter"))
                    buffer_pre_image.setRGB(t.x, s.height - t.y - 1, filterMapper.filterMapper(t.config) | 0xFF000000);
                else buffer_pre_image.setRGB(t.x, s.height - t.y - 1, 0x00FFFFFF);
            });

            ImageIO.write(buffer_pre_image, "png", new File(args[2]));

        }
        else if (args.length == 3 && args[0].equals("map")) {
            BufferedImage origimg = ImageIO.read(new File(args[1]));

            int width, height;
            width = origimg.getWidth();
            height = origimg.getHeight();

            BufferedImage buffer_pre_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            FilterMapper filterMapper = new FilterMapper();

            int[][] filters = doMap(origimg, new ColorMapper("CIELab"));

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int filter = filters[x][y];
                    buffer_pre_image.setRGB(x, y, ( (filter != -1) ? filterMapper.filterMapper(filter) | 0xFF000000 : 0x00FFFFFF ) );
                }
            }
            ImageIO.write(buffer_pre_image, "png", new File(args[2]));
        }
        else {
            System.out.println("Usage : generate <imagepath> <name> <outputpath>");
            System.out.println("Usage : extract <schpath> <outputpath>");
            System.out.println("Set <outputpath> \"STDIO\" to get output by base64");
            System.out.println("Usage : image <schpath> <outputpath>");
            System.out.println("Generates PNG Image From Schematic");
            System.out.println("Usage : map <inputpath> <outputpath>");
            System.out.println("Generates Mapped PNG Image from PNG Image");
        }
    }

    private static int[][] doMap (BufferedImage origimg, ColorMapper colorMapper) {
        int width = origimg.getWidth();
        int height = origimg.getHeight();

        int[][] out = new int[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = origimg.getRGB(x, y);
                out[x][y] = ((c >>> 24) > 127) ? colorMapper.colorMapper(c & 0x00FFFFFF) : -1;
                //System.out.println(Integer.toHexString(c));
            }
        }
        return out;
    }

    private static final byte[] header = {'m', 's', 'c', 'h'};
    private static final byte version = 0;

    private static TSchematic read(Fi file) throws IOException {
        return read(new DataInputStream(file.read(1024)));
    }

    private static TSchematic loadFile(Fi file){
        if(!file.extension().equals("msch")) return null;

        try {
            return read(file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static TSchematic read(InputStream input) throws IOException {
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

            if (stream.readByte() == 2) {
                if (!stream.readUTF().equals("sorter")) sortercode = 1;
                stream.readUTF();
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

            return new TSchematic(tiles, map, width, height);
        }
    }

    private static void write(TSchematic schematic, OutputStream output) throws IOException {
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

import javax.sound.midi.SysexMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        args = new String[] {"resources/input.txt"};

        if (args.length == 0) {
            throw new IllegalArgumentException("You need to provide the name of the input file in the console");
        }

        List<String> input = readInput(args[0]);

        assert input != null;
        Iterator<String> inputIterator = input.listIterator();
        int cubeSize = Integer.parseInt(inputIterator.next().split(" ")[0].trim());
        int numBlocks = Integer.parseInt(inputIterator.next().trim());

        Block[] blocks = new Block[numBlocks];

        int index = 0;
        while (index < numBlocks) {
            if (!inputIterator.hasNext()) throw new IllegalStateException("numBlocks in file doesn't equal actual number of blocks");
            String[] dims = inputIterator.next().split(" ");
            int[] intArray = new int[dims.length];
            for (int i = 0; i < dims.length; i++) {
                intArray[i] = Integer.parseInt(dims[i]);
            }
            blocks[index] = new Block(intArray[0], intArray[1], intArray[2], (byte)(index+1));
            index++;
        }

        BlockSaver saver = new BlockSaver(cubeSize, numBlocks, blocks);

        Arrays.sort(blocks, Block.volumeComparator); // macht den Algorithmus schneller

        if (computeTotalVolume(blocks) != (cubeSize*cubeSize*cubeSize)-1) {
            System.out.println("Keine Lösung gefunden");
            return;
        }

        long start = System.currentTimeMillis();
        System.out.println(runAlgorithm(blocks, 0, cubeSize, new CubeState(cubeSize), saver));
        System.out.println("It took " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
    }

    private static String runAlgorithm(Block[] blocks, int index, int cubeSize, CubeState cubeState, BlockSaver saver) {
        if (index == blocks.length) return cubeState.toString();

        Block block = blocks[index];

        List<int[]> coords = new LinkedList<>();

        int combinations = block.getX() == block.getY() && block.getY() == block.getZ() ? 1 : 1 << 3; // 8 Möglichkeiten für Rotationen
        Block[] rotatedCubes = new Block[combinations];
        for (int i = 0; i < combinations; i++) {
            boolean rotX = (i & 1) != 0; // Bit 1
            boolean rotY = (i & 2) != 0; // Bit 2
            boolean rotZ = (i & 4) != 0; // Bit 3

            Block rotated = cubeState.rotateBlock(block, rotX, rotY, rotZ);
            rotatedCubes[i] = rotated;
        }

        for (int x = 0; x < cubeSize; x++) {
            for (int y = 0; y < cubeSize; y++) {
                for (int z = 0; z < cubeSize; z++) {
                    if (x==cubeSize/2 && y==cubeSize/2 && z==cubeSize/2) continue;
                    if (saver.wasPreviouslyPlaced(block, x, y, z)) continue;
                    for (int i = 0; i < combinations; i++) {
                        Block rotated = rotatedCubes[i];
                        if (cubeState.canPlaceBlock(rotated, x, y, z)) {
                            cubeState.addBlock(rotated, x, y, z);
                            saver.placeBlock(block, x, y, z);
                            coords.add(new int[]{x,y,z});
                            String result = runAlgorithm(blocks, index + 1, cubeSize, cubeState, saver);
                            if (!result.contains("Keine")) return result;
                            cubeState.removeBlock(rotated, x, y, z);
                        }
                    }
                }
            }
        }
        for (int[] coord: coords) {
            saver.removeBlock(block, coord[0], coord[1], coord[2]);
        }
        return "Keine Lösung gefunden";
    }

    private static int computeTotalVolume(Block[] blocks) {
        int totalV = 0;
        for (Block block: blocks) {
            totalV+=block.getVolume();
        }
        return totalV;
    }

    private static List<String> readInput(String inputFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            List<String> content = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                content.add(line); // Add newline character
            }

            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
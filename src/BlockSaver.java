
public class BlockSaver {
    private final byte[][][][] saved;
    private final Block[] blocks;

    public BlockSaver(int cubeSize, int numberOfShapes, Block[] blocks) {
        this.saved = new byte[cubeSize][cubeSize][cubeSize][numberOfShapes];
        this.blocks = blocks;
    }

    public boolean wasPreviouslyPlaced(Block block, int x, int y, int z) {
        byte[] idsPlaced = saved[x][y][z];
        for (byte id: idsPlaced) {
            if (id == 0) continue;
            if (blocks[id-1].equals(block)) {
                return true;
            }
        }
        return false;
    }

    public void placeBlock(Block block, int x, int y, int z) {
        saved[x][y][z][block.getId()-1] = block.getId();
    }

    public void removeBlock(Block block, int x, int y, int z) {
        saved[x][y][z][block.getId()-1] = 0;
    }
}

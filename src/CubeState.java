
public class CubeState {
    private final int cubeSize;
    private final byte[][][] occupied;

    public CubeState(int cubeSize) {
        this.cubeSize = cubeSize;
        occupied = new byte[cubeSize][cubeSize][cubeSize];
        int middle = cubeSize/2;
        occupied[middle][middle][middle] = -1; // Goldenen Würfel einfügen
    }

    public boolean canPlaceBlock(Block block, int posX, int posY, int posZ) {
        if (block.getX()+posX>cubeSize || block.getY()+posY>cubeSize || block.getZ()+posZ>cubeSize) return false;
        for (int x = posX; x < posX+block.getX(); x++) {
            for (int y = posY; y < posY+block.getY(); y++) {
                for (int z = posZ; z < posZ+block.getZ(); z++) {
                    if (occupied[x][y][z] != 0) return false;
                }
            }
        }
        return true;
    }

    public void addBlock(Block block, int x, int y, int z) {
        setBlock(block, x, y, z, block.getId());
    }

    public Block rotateBlock(Block block, boolean rotX, boolean rotY, boolean rotZ) {
        int newX, newY, newZ;

        if (rotX) {
            newX = block.getX(); newY = block.getZ(); newZ = block.getY();
        } else {
            newX = block.getX(); newY = block.getY(); newZ = block.getZ();
        }

        if (rotY) {
            int t = newX;
            newX = newZ;
            newZ = t;
        }
        if (rotZ) {
            int t = newX;
            newX = newY;
            newY = t;
        }

        return new Block(newX, newY, newZ, block.getId());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < cubeSize; y++) {
            builder.append("Ebene ").append(y + 1).append("\n");
            for (int z = 0; z < cubeSize; z++) {
                for (int x = 0; x < cubeSize; x++) {
                    if (occupied[x][y][z]==-1) builder.append("G ");
                    else builder.append(occupied[x][y][z]).append(" ");
                }
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    public void removeBlock(Block block, int x, int y, int z) {
        setBlock(block, x, y, z, (byte)0);
    }

    public void setBlock(Block block, int x, int y, int z, byte value) {
        for (int i = x; i < x+block.getX(); i++) {
            for (int j = y; j < y+block.getY(); j++) {
                for (int k = z; k < z+block.getZ(); k++) {
                    occupied[i][j][k] = value;
                }
            }
        }
    }
}

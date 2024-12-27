import java.sql.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Block {
    private final int x, y, z;
    private final byte id;

    public static Comparator<Block> volumeComparator = (block1, block2) -> {
        // Compare by volume in descending order
        return Integer.compare(block2.getVolume(), block1.getVolume());
    };

    public Block(int x, int y, int z, byte id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public byte getId() {
        return id;
    }

    public int getVolume() {
        return x*y*z;
    }

    @Override
    public String toString() {
        return "Block: " + x + " " + y + " " + z + " id:" + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Block other) {
            List<Integer> mySize = new ArrayList<>(List.of(x, y, z));
            List<Integer> otherSize = new ArrayList<>(List.of(other.x, other.y, other.z));
            mySize.sort(Integer::compareTo);
            otherSize.sort(Integer::compareTo);

            return mySize.equals(otherSize);
        }
        return false;
    }
}

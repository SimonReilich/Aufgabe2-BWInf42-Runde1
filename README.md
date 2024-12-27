# Aufgabe 2 - Die goldene Mitte

Dokumentation und Programmcode von Tymoteusz Wilk und Simon Reilich

## Lösungsidee

Der grundlegende Gedanke hinter unserer Lösung ist das Backtracking. Das heißt es wird rekursiv ein Baum mit allen möglichen Kombinationen und so Schritt für Schritt eine Lösung aufgebaut. Hier der Ablauf: Zuerst muss eine Position und Rotation für den ersten Würfel / Quader ausgewählt werden. Sobald eine Position gefunden wurde wird das Objekt platziert und derselbe Prozess wird für alle weiteren Formen durchgeführt. Falls am Ende keine Lösung gefunden wird im Rekursions-Stack auf die vorherige Form zurückgegriffen, die noch nicht all ihre möglichen Positionen eingenommen hat, und diese dann nach dem gleichen Schema auf alle möglichen Positionen und Rotationen verschoben / rotiert. Die Schritte werden nochmal ausgeführt mit dem Unterschied natürlich, dass der Zustand der platzierten Würfel anders ist und so eventuell bei dem Durchlauf eine Lösung gefunden werden kann. Dieser Algorithmus findet garantiert die richtige Antwort da alle möglichen Anordnungen der Formen durchprobiert werden. Nach zahlreichen Versuchen haben wir jedoch noch gemerkt, dass der Algorithmus Optimierungen benötigt. So haben wir unter anderem die Rotationen nicht bei jedem Positionswechsel neu berechnet sondern nur einmal pro Rekursionsaufruf und einen Block-Saver eingebaut, der bei mehrfachem Vorkommen des gleichen Blocks dafür sorgt, dass dieser nicht nochmal auf die gleiche Positionen in einem tieferen Rekursionsaufruf gesetzt wird. Doch sogar mit dieser Optimierung hat der Algorithmus bei bestimmten Rätseln eine sehr suboptimale Laufzeit.

## Umsetzung

Zunächst müssen natürlich die Verschieden zu Verfügung gestellten Blöcke aus einer Datei eingelesen werden. Die Block-Klasse repräsentiert dabei einen Block mit seiner Breite, Höhe und Tiefe und einer einzigartigen ID:

```java
Block[] blocks = new Block[numBlo
int index = 0;
while (index < numBlocks) {
	if (!inputIterator.hasNext()) throw new IllegalStateException("numBlocks in file doesn't equal actual number of blocks");
	String[] dims = inputIterator.next().split(" "); // Input am Leerzeichen splitten
  int[] intArray = new int[dims.length];
  for (int i = 0; i < dims.length; i++) {
	  intArray[i] = Integer.parseInt(dims[i]);
  }
  blocks[index] = new Block(intArray[0], intArray[1], intArray[2], (byte)(index+1)); // Neuer Block erstellt
  index++;
}
```

Hier wird für jeden Eintrag in der Datei der eingelesene String in die einzelnen Koordinaten aufgeteilt, welche anschließend verarbeitet und in einem neuen Block-Objekt gespeichert werden.

Das eigentliche Herzstück des Algorithmus ist die `runAlgorithm` Methode.

```java
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
```

Sie läuft rekursiv durch das gesamte Array an Blöcken durch, speichert sich das aktuelle Element mit dem `index`Zeiger und gibt bei einer gefundenen Lösung die einzelnen Ebenen, wie in dem Beispielrätsel auch, als String zurück. Der erste Schritt ist das Überprüfen des aktuellen Index. Falls dieser über die Grenzen des Arrays hinausgegangen ist, heißt das, dass jeder Block im Array erfolgreich einen Platz im großen Würfel bekommen hat und gibt, wie bereits beschrieben, den Zustand des Würfels als String zurück (`toString` Methode von `CubeState`). `CubeState` speichert dabei nach jedem Rekursionsaufruf den Zustand des äußeren Würfels.  Der nächste Schritt ist das Auswählen des aktuellen Blocks und die Berechnung all dessen möglichen Rotationen. Dies wird durch eine Methode im `CubeState` bewerkstelligt, die die X- Y- und Z-Rotation übergeben bekommt und anhand dieser den rotierten Block zurückgibt und ihn in einem speziellen `rotatedCubes` Array speichert.  Die `for` Schleife durchläuft alle 8 möglichen elementaren Rotationen, indem sie aus dem Indexcounter die Bits ausliest und jeweils ein Bit als Signal verwendet ob um eine Axis gedreht werden soll. Anschließend wird mit einer 3-fachen `for`-Schleife jede mögliche Position, außer die goldene Mitte, im äußeren Würfel durchprobiert. Bevor die einzelnen Rotationen des Cubes noch durchgelaufen werden, wird mit des`BlockSaver` Objekts nachgeschaut ob in dem aktuellen Rekursionsast der Block an dieser Position schon einmal platziert wurde. Falls ja wird direkt zur nächsten Position gesprungen. Angenommen der Block wird dort nochmal platziert, würde der Algorithmus unnötig einen bereits erreichten Zustand erneut komplett durchtesten. Dies ist vor allem hilfreich, wenn in dem Input-Datensatz mehrere Kopien desselben Blocks zur Verfügung gestellt werden. Falls die beiden Tests nicht den aktuellen Durchlauf schon abgebrochen haben, werden noch zusätzlich alle möglichen Rotationen des Cubes ausprobiert. Bei jeder Rotation wird mithilfe des `CubeState` nun überprüft, ob der Block in dem aktuellen Setup  platziert werden kann. Die Methode `canPlaceBlock` gibt `false` zurück, wenn die angefragte Positionierung des Objekts entweder über die Grenzen des äußeren Würfels hinausgeht oder einen anderen bereits aufgestellten Block überschneidet. Wurde alle Bedingungen überprüft, können der `CubeState` und `BlockSaver` beide aktualisiert werden, wobei auch die Koordinaten des gesetzten Blocks gespeichert werden, um beim Verlassen des aktuellen Rekursionsasts den `BlockSaver` wieder freizugeben. Mit dem neuen Zustand werden mit dem rekursiven Aufruf alle Unterzustände überprüft und das Ergebnis auf die aufrufende Methode zurückgegeben. Konnte in den Unterzuständen keine Lösung gefunden werden, muss in der Methode die sequentiell nächste Position in Betracht gezogen werden. Konnte jedoch eine Lösung gefunden werden, wird die Rekursion abgebrochen und das Ergebnis im Stack nach oben propagiert. Falls sogar nach dem Durchprobieren aller Positionen des derzeitigen Blocks keine Lösung gefunden wurde, wird dies mit dem `return "Keine Lösung gefunden";` signalisiert. 

Hier zur Vollständigkeit die wichtigsten Abschnitte der Datenstruktur `CubeState`:

```java
private final byte[][][] occupied;
...
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
...
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
```

Der Grundgedanke der Datenstruktur ist das Abspeichern jeder Koordinate im äußeren Würfel mit einem `byte`, der signalisiert ob exakt diese Position durch einen Block, dessen ID im `byte` kodiert ist (mit der `addBlock`-Methode) schon besetzt wurde. Dieser Gedanke wird mit dem 3-dimensionalen `occupied` Array implementiert.

Die Methoden `addBlock` und `removeBlock` infolge dessen einfach nur triviale Set-Operationen auf dem Array. 

Die interessanten Methoden sind `canPlaceBlock` und `rotateBlock` :

Bei `canPlaceBlock` wird ausgehend von der angefragten Position anhand der Breite, Höhe und Tiefe des übergebenen Objekts, festgestellt, ob ein beliebiger Punkt im gewollten Bereich in der`CubeState`-Datenstruktur schon besetzt wurde. Realisiert wird das mit einer 3-fachen `for`-Schleife, die alle Koordinaten, die im Block enthalten sind, überprüft. Falls `occupied` an der gewünschten Position nicht null, bedeutet das, dass sich bereits ein anderes Objekt an dieser Position befindet und die Methode kann mit dem Rückgabewert `false` abgebrochen werden.

Die `rotateBlock` Methode rotiert den gegebenen Block in die X-, Y- und Z-Richtung. Erreicht wird dies durch einen simplen Tausch der Breite, Höhe bzw. Tiefe des Würfels oder Rechtecks. Beispielsweise muss, wenn das Objekt in X-Richtung rotiert werden soll, einfach nur die Höhe und Tiefe, also Größe des Würfels in Y- und Z-Richtung vertauscht werden.

Sehr ähnlich wird dazu die `BlockSaver` Klasse implementiert, mit dem einzigen Unterschied, dass anstatt einem 3-dimensionalen `byte` Array ein 4-Dimensionales verwendet wird. Es müssen nämlich nicht nur der aktuell platzierte Block, sondern alle in der Rekursion davor gesetzten Blöcke gespeichert werden. 

```java
private final byte[][][][] saved;
```

Ganz analog kann hier durch triviale Set- und Get-Operationen der Zustand des `BlockSaver` manipuliert werden.

## Beispiele

Beim Beispielsrätsel gibt der Algorithmus folgende Lösung zurück:

```java
Ebene 1
1 3 2 
1 3 2 
1 3 2 
Ebene 2
1 4 2 
1 G 2 
1 5 2 
Ebene 3
1 4 2 
1 6 2 
1 5 2
```

G steht für den Goldenen Würfel und die Zahlen repräsentieren die Block-IDs

Rätsel 1:

```java
Ebene 1
5 8 8 
5 3 3 
6 6 2 
Ebene 2
5 8 8 
5 G 4 
6 6 2 
Ebene 3
1 1 1 
7 7 4 
7 7 2
```

Rätsel 2:

```java
Ebene 1
1 1 1 
5 3 6 
2 2 2 
Ebene 2
1 1 1 
5 G 6 
2 2 2 
Ebene 3
1 1 1 
5 4 6 
2 2 2
```

Rätsel 3:

```java
Keine Lösung gefunden
```

Rätsel 4 & 5 führen zu einem Timeout.

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/**
 * A map for the game.
 *
 * @author campberobe1
 */
public class GameMap {
	// The name of the map
	private String mapName;
	// The amount of elements in the map
	private int elementCount;
	// The audio file corresponding to this map
	private String audio;
	// The queue of elements in the map
	private Queue<Element> elements;

	/**
	 * Constructor; creates a new instance of GameMap.
	 * @param s A scanner on a map's save file.
	 */
	public GameMap(Scanner s){
		// First, read the header elements
		mapName = s.nextLine();
		elementCount = s.nextInt();
		audio = s.next();

		// Then, initialise the queue
		elements = new LinkedList<Element>();

		// And scroll through the scanner, finding all the values
		for(int i = 0; i < elementCount; i++){
			int elementType = s.nextInt();
			// Make a circle
			if(elementType == 1){
				elements.offer(new Circle(s.nextInt(), s.nextInt(), s.nextInt()));
			}
		}

		// Close the scanner!
		s.close();
	}

	/**
	 * Returns the name of the map.
	 */
	public String getName(){
		return mapName;
	}

	/**
	 * Returns and deletes the next element of the map
	 * @return The next element in the map.
	 */
	public Element poll(){
		if(elements.isEmpty()) return null;
		return elements.poll();
	}

	/**
	 * Returns but does NOT DELETE next element in the map
	 * @return The next element in the map.
	 */
	public Element peek(){
		if(elements.isEmpty()) return null;
		return elements.peek();
	}
}

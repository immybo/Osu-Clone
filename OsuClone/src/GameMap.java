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
	// A queue of the types of these elements (circle, slider, spinner),(1,2,3)
	private Queue<Integer> elementType;
	// Queues of the time which these elements occur and their x/y positions
	private Queue<Integer> elementTime;
	private Queue<Integer> elementX;
	private Queue<Integer> elementY;

	/**
	 * Constructor; creates a new instance of GameMap.
	 * @param s A scanner on a map's save file.
	 */
	public GameMap(Scanner s){
		// First, read the header elements
		mapName = s.nextLine();
		elementCount = s.nextInt();
		audio = s.next();

		// Then, initialise the queues
		elementType = new LinkedList<Integer>();
		elementTime = new LinkedList<Integer>();
		elementX = new LinkedList<Integer>();
		elementY = new LinkedList<Integer>();

		// And scroll through the scanner, finding all the values
		for(int i = 0; i < elementCount; i++){
			if(s.hasNextInt()) elementType.add(s.nextInt());
			if(s.hasNextInt()) elementTime.add(s.nextInt());
			if(s.hasNextInt()) elementX.add(s.nextInt());
			if(s.hasNextInt()) elementY.add(s.nextInt());
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
	 * Returns and deletes the next elements of the map
	 * @return An array of {element type, element time, element x, element y}, where element type is 1, 2 or 3 for circle, slider or spinner. Note that element time is in ms.
	 */
	public int[] next(){
		int[] returnValues = new int[4];
		returnValues[0] = elementType.poll();
		returnValues[1] = elementTime.poll();
		returnValues[2] = elementX.poll();
		returnValues[3] = elementY.poll();

		return returnValues;
	}

	/**
	 * Returns but does NOT DELETE the time of the next element in the map
	 */
	public int nextTime(){
		if(elementTime.isEmpty()){ return -1; }
		return elementTime.peek();
	}
}

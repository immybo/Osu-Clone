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
	private Queue<Double> elementTime;
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
		elementTime = new LinkedList<Double>();
		elementX = new LinkedList<Integer>();
		elementY = new LinkedList<Integer>();

		// And scroll through the scanner, finding all the values
		for(int i = 0; i < elementCount; i++){
			if(s.hasNextInt()) elementType.add(s.nextInt());
			if(s.hasNextDouble()) elementTime.add(s.nextDouble());
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
	 * @return A list of {element type, element time, element x, element y}, all doubles, where element type is 1, 2 or 3 for circle, slider or spinner
	 */
	public List<Double> next(){
		List<Double> returnList = new ArrayList<Double>();
		returnList.add((double)elementType.poll());
		returnList.add(elementTime.poll());
		returnList.add((double)elementX.poll());
		returnList.add((double)elementY.poll());

		return returnList;
	}
}

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * A map for the game.
 *
 * @author campberobe1
 */
public class GameMap {
	// There are 10 levels of the attributes, these map the 1-10 level to the actual value
	// Overall difficulty is in ms, and gives an array for getting 100-50-miss
	private static final int[][] OVERALL_DIFFICULTY_VALUES = {
		{1000, 1500, 2000},
		{800, 1200, 1600},
		{600, 900, 1200},
		{400, 600, 800},
		{300, 450, 600},
		{200, 300, 400},
		{140, 210, 280},
		{90, 135, 180},
		{50, 75, 100},
		{30, 45, 60}
	};
	// Approach rate is in ms
	private static final int[] APPROACH_RATE_VALUES = { 6000, 4500, 3500, 2500, 1800, 1300, 1100, 900, 700, 500 };
	// Circle size is in pixels
	private static final int[] CIRCLE_SIZE_VALUES = { 170, 155, 140, 125, 110, 95, 80, 65, 50, 35 };

	// The name of the map
	private String mapName;
	// The amount of elements in the map
	private int elementCount;
	// The audio file corresponding to this map
	private String audio;
	// The queue of elements in the map
	private Queue<Element> elements;

	// Overall difficulty governs the time accuracy required to hit elements (minimum to get 100, 50, miss)
	private int[] overallDifficulty;
	// Approach rate governs how long elements appear on screen before they need to be clicked
	private int approachRate;
	// Circle size is self-explanatory
	private int circleSize;

	/**
	 * Constructor; creates a new instance of GameMap.
	 * @param s A scanner on a map's save file.
	 */
	public GameMap(Scanner s){
		// First, read the header elements
		mapName = s.nextLine();
		elementCount = s.nextInt();
		audio = s.next();

		overallDifficulty = OVERALL_DIFFICULTY_VALUES[s.nextInt()];
		approachRate = APPROACH_RATE_VALUES[s.nextInt()];
		circleSize = CIRCLE_SIZE_VALUES[s.nextInt()];

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

	/**
	 * Returns the overall difficulty values for this map
	 */
	public int[] getOD(){
		return overallDifficulty;
	}

	/**
	 * Returns the approach rate value for this map
	 */
	public int getAR(){
		return approachRate;
	}

	/**
	 * Returns the circle size value for this map
	 */
	public int getCS(){
		return circleSize;
	}
}

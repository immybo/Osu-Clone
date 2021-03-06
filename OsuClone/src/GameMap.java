import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 * Defines all values (e.g. accuracy) for and
 * elements within a MyOsu map. Created with a
 * scanner on a map file.
 *
 * @author campberobe1
 */
public class GameMap {
	/* OLD VALUES
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
	// Health is as a ratio to 1; 1 is average
	private static final double[] HEALTH_VALUES = { 4, 3, 2, 1, 0.8, 0.65, 0.5, 0.4, 0.3, 0.2 };
	*/

	// The name of the map
	private String mapName;
	// The audio file corresponding to this map
	private String audio;
	// A list of all elements in this map
	private List<Element> elementList;
	// A queue of elements in this map. This is created only when
	// resetQueue() is called, and does not affect the list of
	// elements in the map.
	private Queue<Element> elements;
	
	// The time (ms) when the map's audio should start
	private int audioStartTime;

	// Overall difficulty governs the time accuracy required to hit elements (minimum to get 100, 50, miss)
	private int odValue;
	// Approach rate governs how long elements appear on screen before they need to be clicked
	private int arValue;
	// Circle size is self-explanatory
	private int csValue;
	// Governs the rate at which health decays and how much the player is penalised for missing
	private int healthValue;
	
	// The background image that should be displayed for this map
	private BufferedImage backgroundImage;
	
	// When the initial break for the map ends
	private int initialBreakEndTime;

	/**
	 * Constructor; creates a new instance of GameMap.
	 * @param s A scanner on a map's save file.
	 * @param name The name of this map.
	 */
	public GameMap(String name, Scanner s){
		// First, read the header elements
		mapName = name;
		audio = "maps/" + mapName + "/audio.mp3";
		audioStartTime = s.nextInt();
		
		// Initialise the background
		try{ backgroundImage = ImageIO.read(new File("maps/" + mapName + "/background.jpg")); }
		// It's fine if there's no background image
		catch(IOException e){ backgroundImage = null; }

		odValue = s.nextInt();
		arValue = s.nextInt();
		csValue = s.nextInt();
		healthValue = s.nextInt();
		initialBreakEndTime = s.nextInt();

		// Then, initialise the data structures
		elementList = new ArrayList<Element>();

		// And scroll through the scanner, finding all the values
		while(s.hasNextLine()){
			int elementType = s.nextInt();
			// Make a circle
			if(elementType == 1){
				// A circle has 3 parameters: start time, and x and y position
				elementList.add(new Circle(s.nextInt(), s.nextInt(), s.nextInt()));
			}
			// Make a slider
			if(elementType == 2){
				// A slider has 6 parameters: start time, end time, length, angle, and x and y position (all integers)
				elementList.add(new Slider(s.nextInt(), s.nextInt(), s.nextInt(), s.nextInt(), s.nextInt(), s.nextInt()));
			}
			// Add a break
			if(elementType == 3){
				// A break has 2 parameters: start time and end time
				elementList.add(new Break(s.nextInt(), s.nextInt()));
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
	 * Returns and deletes the next element of the initialised map queue
	 * @return The next element in the map.
	 */
	public Element poll(){
		if(elements.isEmpty()) return null;
		return elements.poll();
	}

	/**
	 * Returns but does NOT DELETE next element in the initialised map queue
	 * @return The next element in the map.
	 */
	public Element peek(){
		if(elements.isEmpty()) return null;
		return elements.peek();
	}
	
	/**
	 * Recreates the 'map queue'; the queue of all elements in the map,
	 * from the list of elements
	 */
	public void resetQueue(){
		elements = new LinkedList<Element>();
		for(Element e : elementList){
			elements.offer(e);
		}
	}

	/**
	 * Returns the overall difficulty values for this map
	 */
	public int[] getOD(){
		return getOD(odValue);
	}
	/**
	 * Returns the overall difficulty values for this map, given that Hard Rock applies.
	 */
	public int[] getODHR(){
		return getOD(odValue*Options.HRODMod);
	}

	/**
	 * Returns the approach rate value for this map
	 */
	public int getAR(){
		return getAR(arValue);
	}
	/**
	 * Returns the approach rate value for this map, given that Hard Rock applies.
	 */
	public int getARHR(){
		return getAR(arValue*Options.HRARMod);
	}

	/**
	 * Returns the circle size value for this map
	 */
	public int getCS(){
		return getCS(csValue);
	}
	/**
	 * Returns the circle size value for this map, given that Hard Rock applies.
	 */
	public int getCSHR(){
		return getCS(csValue*Options.HRCSMod);
	}
	
	/**
	 * Returns the health value for this map
	 */
	public double getHealth(){
		return getHealth(healthValue);
	}
	/**
	 * Returns the health value for this map, given that Hard Rock applies.
	 */
	public double getHealthHR(){
		return getHealth(healthValue*Options.HRHealthMod);
	}

	/**
	 * Returns the audio file name for this map
	 */
	public String getAudio(){
		return audio;
	}

	/**
	 * Returns the time at which the audio file should start for this map
	 */
	public int getAudioStartTime(){
		return audioStartTime;
	}
	
	/**
	 * Returns the BufferedImage of the background of this map
	 * Returns null if this map has no background image
	 */
	public BufferedImage getBackground(){
		return backgroundImage;
	}
	
	/**
	 * Returns the interpolated AR time in ms, for a given value.
	 */
	public int getAR(double ar){
		return (int)(6000*Math.pow(1.28, -ar));
	}
	/**
	 * Returns the interpolated CS in pixels, for a given value.
	 */
	public int getCS(double cs){
		return (int)(200*Math.pow(1.13, -cs));
	}
	/**
	 * Returns the interpolated OD values in ms, for a given value.
	 */
	public int[] getOD(double od){
		double missTime = 1200*Math.pow(1.27, -od);
		return new int[]{(int)missTime, (int)(missTime*0.75), (int)(missTime*0.5)}; 
	}
	/**
	 * Returns the interpolated health values, for a given value.
	 */
	public double getHealth(double health){
		return 5*Math.pow(1.4, -health);
	}
	
	/**
	 * Returns the map time when the initial break end
	 */
	public int getInitialBreakEnd(){
		return initialBreakEndTime;
	}
}

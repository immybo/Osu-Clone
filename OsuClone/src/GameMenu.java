import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The selection menu of the game.
 * @author campberobe1
 */
public class GameMenu {
	/** PARAMETERS **/
	private final static int MAIN_WINDOW_DEFAULT_WIDTH = 500;
	private final static int MAIN_WINDOW_DEFAULT_HEIGHT = 500;

	private final static int MAIN_WINDOW_INITIAL_X = 300;
	private final static int MAIN_WINDOW_INITIAL_Y = 300;

	private final static boolean MAIN_WINDOW_RESIZABLE = false;

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JButton optionButton;
	private JButton modButton;
	private ArrayList<JButton> mapButton;

	// Handler for mouse events in the menu
	private GameMenuMouseHandler mouseHandler;

	/**
	 * Begins the game by instantiating GameMenu
	 */
	public static void main(String[] args){
		new GameMenu();
	}

	/**
	 * Constructor: creates a new instance of GameMenu;
	 * Should be called at startup.
	 */
	public GameMenu(){
		initialiseMenu();
		initialiseMouse();
	}

	/**
	 * Creates the interface for the game's selection menu
	 */
	private void initialiseMenu(){
		// Create the outer JFrame for the selection menu
		menuOuterFrame = new JFrame();
		menuOuterFrame.setSize(MAIN_WINDOW_DEFAULT_WIDTH, MAIN_WINDOW_DEFAULT_HEIGHT);
		menuOuterFrame.setLocation(MAIN_WINDOW_INITIAL_X, MAIN_WINDOW_INITIAL_Y);
		menuOuterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Using a GridBagLayout to allow for multiple, differently sized cells in columns and rows.
		menuOuterFrame.setLayout(new GridBagLayout());

		// Create the various buttons

		menuOuterFrame.setVisible(true);
	}

	/**
	 * Finds the list of available maps from a file
	 * @param filename The file to read from. Throws an exception if it can't read from this file.
	 * @return An array of maps found from the file. Gives an empty array if it couldn't read from the file.
	 */
	private GameMap[] getMaps(String filename){
		// A temporary list of maps to return
		ArrayList<GameMap> returnMaps = new ArrayList<GameMap>();
		// Attempt to create a scanner
		try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));
			// Add to returnMaps a new map with the given toString
			// TODO proper map reading/writing
			returnMaps.add(new GameMap(s.next()));

			s.close();
		}
		// Throw an exception if it can't read from the file
		catch(IOException e){
			System.err.println("Could not read from map file '"+filename+"'. Is this file not present? Details: " + e);
		}

		// Convert the list of maps into an array and return it
		GameMap[] finalReturnMaps = new GameMap[returnMaps.size()];
		returnMaps.toArray(finalReturnMaps);
		return finalReturnMaps;
	}

	/**
	 * Initialises the mouse listener for the menu
	 */
	private void initialiseMouse(){
		// Add a mouse listener to the outer frame
		mouseHandler = new GameMenuMouseHandler();
		menuOuterFrame.addMouseListener(mouseHandler);
	}

	/**
	 * Handles mouse actions for the menu
	 */
	private void doMouse(String action, double x, double y){

	}
}

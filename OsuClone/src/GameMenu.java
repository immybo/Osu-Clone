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

	private final static int OPTION_WINDOW_DEFAULT_WIDTH = 500;
	private final static int OPTION_WINDOW_DEFAULT_HEIGHT = 500;

	private final static int OPTION_WINDOW_INITIAL_X = 400;
	private final static int OPTION_WINDOW_INITIAL_Y = 400;

	private final static boolean OPTION_WINDOW_RESIZABLE = false;

	private final static String MAP_FILE = "maps.txt";
	private final static String OPTION_FILE = "options.txt";

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JPanel leftPanel;
	private JPanel rightPanel;

	private JFrame optionFrame;
	private JTextArea optionTextArea;

	private JButton optionButton;
	private JButton modButton;
	private java.util.List<JButton> mapButton = new ArrayList<JButton>();

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
		initialiseButtons();
	}

	/**
	 * Creates the interface for the game's selection menu
	 */
	private void initialiseMenu(){
		// Create the outer JFrame for the selection menu
		menuOuterFrame = new JFrame();
		menuOuterFrame.setSize(MAIN_WINDOW_DEFAULT_WIDTH, MAIN_WINDOW_DEFAULT_HEIGHT);
		menuOuterFrame.setLocation(MAIN_WINDOW_INITIAL_X, MAIN_WINDOW_INITIAL_Y);
		menuOuterFrame.setResizable(MAIN_WINDOW_RESIZABLE);
		menuOuterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menuOuterFrame.setTitle("Osu Clone Menu");
		// Using a GridLayout for the outer frame
		menuOuterFrame.setLayout(new GridLayout());

		// Create two JPanels: one for regular buttons and one for maps
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		// And add them to the outer frame
		menuOuterFrame.add(leftPanel);
		menuOuterFrame.add(rightPanel);

		// Create the various buttons
		optionButton = new JButton("Options");
		leftPanel.add(optionButton);

		modButton = new JButton("Mods");
		leftPanel.add(modButton);

		// And fill the map buttons from getMaps
		GameMap[] maps = getMaps(MAP_FILE);
		for(GameMap map : maps){
			JButton btn = new JButton(map.getName());
			rightPanel.add(btn);
			mapButton.add(btn);
		}

		menuOuterFrame.setVisible(true);
	}

	/**
	 * Initialises the button listeners for the menu
	 */
	private void initialiseButtons(){
		// Add an action listener to each of the buttons
		ActionListener l = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				doButtons(e);
			}
		};

		optionButton.addActionListener(l);
		modButton.addActionListener(l);

		//for(JButton btn : mapButton){
		//	btn.addActionListener(l);
		//}
	}

	/**
	 * Handles button actions for the menu
	 */
	private void doButtons(ActionEvent e){
		Object source = e.getSource();

		if(source.equals(optionButton)){
			initialiseOptionFrame();
		}
		else if(source.equals(modButton)){
			System.out.println("MOD");
		}
	}

	/**
	 * Opens a new frame which allows specification of options
	 */
	private void initialiseOptionFrame(){
		if(optionFrame != null) return;

		optionFrame = new JFrame();
		optionFrame.setSize(OPTION_WINDOW_DEFAULT_WIDTH, OPTION_WINDOW_DEFAULT_HEIGHT);
		optionFrame.setLocation(OPTION_WINDOW_INITIAL_X, OPTION_WINDOW_INITIAL_Y);
		optionFrame.setResizable(OPTION_WINDOW_RESIZABLE);
		// Don't close the entire program when closing the option window!
		optionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Grab the map of options from the file
		Map<String, Integer> optionMap = getOptions(OPTION_FILE);

		// Create the text area for displaying the options
		optionTextArea = new JTextArea();
		optionTextArea.setEditable(false);
		// TODO editing options after displaying them

		// Iterate through the map and add all of the options as strings
		for(Map.Entry<String, Integer> entry : optionMap.entrySet()){
			optionTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
		}

		optionFrame.add(optionTextArea);
		optionFrame.setVisible(true);
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
		/**try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));
			// Add to returnMaps a new map with the given toString
			// TODO proper map reading/writing
			while(s.hasNext()){
				returnMaps.add(new GameMap(s.next()));
			}
			s.close();
		}
		// Throw an exception if it can't read from the file
		catch(IOException e){
			System.err.println("Could not read from map file '"+filename+"'. Is this file not present? Details: " + e);
		}*/

		returnMaps.add(new GameMap(""));

		// Convert the list of maps into an array and return it
		GameMap[] finalReturnMaps = new GameMap[returnMaps.size()];
		returnMaps.toArray(finalReturnMaps);
		return finalReturnMaps;
	}

	/**
	 * Finds and reads all options from a file
	 * @param filename The file to read from. Throws an exception if it can't read from this file.
	 * @return A map of options found from the file (String -> Integer). 0/1 is used for boolean options.
	 */
	private Map<String, Integer> getOptions(String filename){
		// The map to return; specifically a HashMap
		Map<String, Integer> options = new HashMap<String, Integer>();

		// Attempt to create a scanner
		try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));

			// While the scanner still has entries,
			while(s.hasNext()){
				// Take the next string as the option descriptor
				String descriptor = s.next();
				// Ignore the =
				s.next();
				// And check to see whether the option is boolean or numerical
				int value;
				if(s.hasNextInt()){
					value = s.nextInt();
				}
				else{
					// If it's boolean, convert it to 1 if "TRUE" and 0 otherwise
					String boolValue = s.next();
					if(boolValue.equals("TRUE")){
						value = 1;
					}
					else{
						value = 0;
					}
				}

				options.put(descriptor, value);
			}

			s.close();
		}
		// Throw an exception if it can't read from the file
		catch(IOException e){
			System.err.println("Could not read from options file '"+filename+"'. Is this file not present? Details: " + e);
		}

		return options;
	}
}

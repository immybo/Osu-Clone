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
	private static int MAIN_WINDOW_DEFAULT_WIDTH;
	private static int MAIN_WINDOW_DEFAULT_HEIGHT;

	private static int MAIN_WINDOW_INITIAL_X;
	private static int MAIN_WINDOW_INITIAL_Y;

	private static boolean MAIN_WINDOW_RESIZABLE;

	private static int OPTION_WINDOW_DEFAULT_WIDTH;
	private static int OPTION_WINDOW_DEFAULT_HEIGHT;

	private static int OPTION_WINDOW_INITIAL_X;
	private static int OPTION_WINDOW_INITIAL_Y;

	private static boolean OPTION_WINDOW_RESIZABLE;

	private static String MAP_FILE = "maps.txt";
	private static String OPTION_FILE = "options.txt";

	/** MODS ACTIVE **/
	private boolean isHidden = false;

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JPanel leftPanel;
	private JPanel rightPanel;

	private JFrame optionFrame;
	private JTextArea optionTextArea;

	private JButton optionButton;

	private JCheckBox hiddenCheckbox;

	private java.util.List<JButton> mapButton = new ArrayList<JButton>();

	// A list of all maps available
	private java.util.List<GameMap> maps;

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
		setOptionParams();
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

		hiddenCheckbox = new JCheckBox("Hidden");
		leftPanel.add(hiddenCheckbox);

		// And fill the map buttons from getMaps
		maps = getMaps(MAP_FILE);
		for(GameMap map : maps){
			JButton btn = new JButton(map.getName());
			rightPanel.add(btn);
			mapButton.add(btn);
		}

		menuOuterFrame.setVisible(true);
	}

	/**
	 * Initialises the button and checkbox listeners for the menu
	 */
	private void initialiseButtons(){
		// Add an action listener to each of the buttons
		ActionListener l = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				doButtons(e);
			}
		};

		optionButton.addActionListener(l);

		for(JButton btn : mapButton){
		  btn.addActionListener(l);
		}

		// And add one to each of the checkboxes
		ItemListener c = new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				doCheckbox(e);
			}
		};

		hiddenCheckbox.addItemListener(c);
	}

	/**
	 * Handles button actions for the menu
	 */
	private void doButtons(ActionEvent e){
		Object source = e.getSource();

		if(source.equals(optionButton)){
			initialiseOptionFrame();
		}
	}

	/**
	 * Handles checkbox actions for the menu
	 */
	private void doCheckbox(ItemEvent e){
		Object source = e.getSource();
		boolean state = e.getStateChange()==1 ? true : false;

		if(source.equals(hiddenCheckbox)){
			isHidden = state;
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
	 * Sets all parameters based on the option file
	 */
	private void setOptionParams(){
		Map<String, Integer> options = getOptions(OPTION_FILE);
		MAIN_WINDOW_DEFAULT_WIDTH = options.get("MAIN_WINDOW_DEFAULT_WIDTH");
		MAIN_WINDOW_DEFAULT_HEIGHT = options.get("MAIN_WINDOW_DEFAULT_HEIGHT");
		MAIN_WINDOW_INITIAL_X = options.get("MAIN_WINDOW_INITIAL_X");
		MAIN_WINDOW_INITIAL_Y = options.get("MAIN_WINDOW_INITIAL_Y");
		MAIN_WINDOW_RESIZABLE = options.get("MAIN_WINDOW_RESIZABLE")==1 ? true : false;

		OPTION_WINDOW_DEFAULT_WIDTH = options.get("OPTION_WINDOW_DEFAULT_WIDTH");
		OPTION_WINDOW_DEFAULT_HEIGHT = options.get("OPTION_WINDOW_DEFAULT_HEIGHT");
		OPTION_WINDOW_INITIAL_X = options.get("OPTION_WINDOW_INITIAL_X");
		OPTION_WINDOW_INITIAL_Y = options.get("OPTION_WINDOW_INITIAL_Y");
		OPTION_WINDOW_RESIZABLE = options.get("OPTION_WINDOW_RESIZABLE")==1 ? true : false;
	}

	/**
	 * Finds the list of available maps from a file
	 * @param filename The file to read from. Throws an exception if it can't read from this file.
	 * @return A List of maps found from the file. Gives an empty List if it couldn't read from the file.
	 */
	private java.util.List<GameMap> getMaps(String filename){
		// A list of maps to return
		ArrayList<GameMap> returnMaps = new ArrayList<GameMap>();
		// A list of map filenames to scan in
		ArrayList<String> mapNames = new ArrayList<String>();
		// Attempt to create a scanner to find map names and add them to the list of map names
		try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));
			// Add to mapNames each name in the file
			while(s.hasNext()){
				mapNames.add(s.next());
			}
			s.close();
		}
		// Throw an exception if it can't read from the file
		catch(IOException e){
			System.err.println("Could not read from map names file ("+filename+"). Details: " + e);
		}

		// Scan through all of these files and make a new map with the scanner, then add it to the List to return
		for(String fname : mapNames){
			try{
				Scanner s = new Scanner(new File("maps/" + fname));
				GameMap newMap = new GameMap(s);
				returnMaps.add(newMap);
			}
			catch(IOException e){
				System.err.println("Could not read from map file '"+fname+"'. Is this file not present? Details: " + e);
			}
		}

		return returnMaps;
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

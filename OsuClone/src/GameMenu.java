import javax.imageio.ImageIO;
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

	public static int GAME_WINDOW_DEFAULT_HEIGHT;
	public static int GAME_WINDOW_DEFAULT_WIDTH;
	public static int GAME_WINDOW_INITIAL_X;
	public static int GAME_WINDOW_INITIAL_Y;
	public static boolean GAME_WINDOW_RESIZABLE;

	public static int GAME_TICK_TIME = 20;
	public static int GAME_CIRCLE_SIZE = 100;

	public static char GAME_KEY_1;
	public static char GAME_KEY_2;

	private static String MAP_FILE = "maps.txt";
	private static String OPTION_FILE = "options.txt";
	private static String DEFAULT_BG = "defaultbg.jpg";

	/** MODS ACTIVE **/
	private boolean isHidden = false;

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JPanel backgroundPanel;
	private JPanel leftPanel;
	private JPanel rightPanel;

	private JFrame optionFrame;
	private JTextArea optionTextArea;

	private JButton optionButton;

	private JCheckBox hiddenCheckbox;

	private java.util.List<JButton> mapButton = new ArrayList<JButton>();
	private Map<JButton, String> mapList = new HashMap<JButton, String>();

	private Game currentGame = null;
	
	// The image to draw as the background
	private Image bgImage = null;

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
		menuOuterFrame.setTitle("MyOsu! Menu");
		// Using a GridLayout for the outer frame
		menuOuterFrame.setLayout(new GridLayout());

		// First, create a panel for the song/menu background
		initBackground();
		
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
		hiddenCheckbox.setOpaque(false);
		leftPanel.add(hiddenCheckbox);

		// And fill the map buttons from getMaps
		java.util.List<String> mapNames = getMaps(MAP_FILE);
		for(String name : mapNames){
			GameMap currentMap = getMap(name);
			JButton btn = new JButton(currentMap.getName());
			mapList.put(btn, currentMap.getName());
			rightPanel.add(btn);
			mapButton.add(btn);
		}

		menuOuterFrame.setVisible(true);
	}
	
	/**
	 * Initialises the background of the menu frame; uses a custom JFrame which paints an image
	 */
	private void initBackground(){
		// Finds the default background to set at the start
		try{
			bgImage = ImageIO.read(new File(DEFAULT_BG));
		}
		catch(IOException e){
			System.out.println("Could not read from image file.");
		}
		
		// Private inner JPanel-extending class to define drawing the background image
		class MenuBackground extends JPanel {
			public void paintComponent(Graphics g){
				if(bgImage != null){
					g.drawImage(bgImage,0,0,menuOuterFrame.getWidth(),menuOuterFrame.getHeight(),this);
				}
			}
		}
		
		// Add a new MenuBackground
		menuOuterFrame.setContentPane(new MenuBackground());
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

		if(mapButton.contains(source)){
			if(currentGame != null) currentGame.terminate();
			GameMap newGameMap = getMap(mapList.get(source));
			currentGame = new Game(newGameMap);
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
		Map<String, Object> optionMap = getOptions(OPTION_FILE);

		// Create the text area for displaying the options
		optionTextArea = new JTextArea();
		optionTextArea.setEditable(false);
		// TODO editing options after displaying them

		// Iterate through the map and add all of the options as strings
		for(Map.Entry<String, Object> entry : optionMap.entrySet()){
			optionTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
		}

		optionFrame.add(optionTextArea);
		optionFrame.setVisible(true);
	}

	/**
	 * Sets all parameters based on the option file
	 */
	private void setOptionParams(){
		Map<String, Object> options = getOptions(OPTION_FILE);
		MAIN_WINDOW_DEFAULT_WIDTH = (int)options.get("MAIN_WINDOW_DEFAULT_WIDTH");
		MAIN_WINDOW_DEFAULT_HEIGHT = (int)options.get("MAIN_WINDOW_DEFAULT_HEIGHT");
		MAIN_WINDOW_INITIAL_X = (int)options.get("MAIN_WINDOW_INITIAL_X");
		MAIN_WINDOW_INITIAL_Y = (int)options.get("MAIN_WINDOW_INITIAL_Y");
		MAIN_WINDOW_RESIZABLE = (boolean)options.get("MAIN_WINDOW_RESIZABLE");

		OPTION_WINDOW_DEFAULT_WIDTH = (int)options.get("OPTION_WINDOW_DEFAULT_WIDTH");
		OPTION_WINDOW_DEFAULT_HEIGHT = (int)options.get("OPTION_WINDOW_DEFAULT_HEIGHT");
		OPTION_WINDOW_INITIAL_X = (int)options.get("OPTION_WINDOW_INITIAL_X");
		OPTION_WINDOW_INITIAL_Y = (int)options.get("OPTION_WINDOW_INITIAL_Y");
		OPTION_WINDOW_RESIZABLE = (boolean)options.get("OPTION_WINDOW_RESIZABLE");

		GAME_WINDOW_DEFAULT_WIDTH = (int)options.get("GAME_WINDOW_DEFAULT_WIDTH");
		GAME_WINDOW_DEFAULT_HEIGHT = (int)options.get("GAME_WINDOW_DEFAULT_HEIGHT");
		GAME_WINDOW_INITIAL_X = (int)options.get("GAME_WINDOW_INITIAL_X");
		GAME_WINDOW_INITIAL_Y = (int)options.get("GAME_WINDOW_INITIAL_Y");
		GAME_WINDOW_RESIZABLE = (boolean)options.get("GAME_WINDOW_RESIZABLE");

		GAME_KEY_1 = options.get("GAME_KEY_1").toString().charAt(0);
		GAME_KEY_2 = options.get("GAME_KEY_2").toString().charAt(0);
	}

	/**
	 * Finds the list of available maps from a file
	 * @param filename The file to read from. Throws an exception if it can't read from this file.
	 * @return A List of maps found from the file. Gives an empty List if it couldn't read from the file.
	 */
	private java.util.List<String> getMaps(String filename){
		// A list of map filenames
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
		return mapNames;
	}

	private GameMap getMap(String filename){
		// Scan through the file and attempt to make a map from it, then return it.
		// Return null after an IOException
		try{
			Scanner s = new Scanner(new File("maps/" + filename));
			GameMap newMap = new GameMap(s);
			return newMap;
		}
		catch(IOException e){
			System.err.println("Could not read from map file '"+filename+"'. Is this file not present? Details: " + e);
		}
		return null;
	}

	/**
	 * Finds and reads all options from a file
	 * @param filename The file to read from. Throws an exception if it can't read from this file.
	 * @return A map of options found from the file (String -> Object). 0/1 is used for boolean options.
	 */
	private Map<String, Object> getOptions(String filename){

		// The map to return; specifically a HashMap
		Map<String, Object> options = new HashMap<String, Object>();

		// Attempt to create a scanner
		try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));

			// While the scanner still has entries,
			while(s.hasNext()){
				// Take the next string as the option descriptor
				String descriptor = s.next();
				// And check to see whether the option is boolean or numerical or a string
				Object value;
				if(s.hasNextInt()){
					value = s.nextInt();
				}
				else if(s.hasNextBoolean()){
					// If it's boolean, put it as a boolean
					value = s.nextBoolean();
				}
				// If it's a string, put it as a string
				else{
					value = s.next();
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

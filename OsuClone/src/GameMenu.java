import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.embed.swing.JFXPanel;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The selection menu of the game.
 * @author campberobe1
 */
public class GameMenu {
	/** MODS ACTIVE **/
	private boolean isHidden = false;

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JPanel backgroundPanel;
	private JPanel leftPanel;
	private JPanel rightPanel;

	private OptionMenu optionFrame;

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
		Options.init();
		initialiseMenu();
		initialiseButtons();
		new JFXPanel();	// To initialise the class - this sometimes freezes it for a few seconds,
		// to which I haven't been able to find a solution.
	}

	/**
	 * Creates the interface for the game's selection menu
	 */
	private void initialiseMenu(){
		// Create the outer JFrame for the selection menu
		menuOuterFrame = new JFrame();

		// Set the window to fullscreen or not, depending on which it is
		if(Options.MENU_FULLSCREEN){
			menuOuterFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			menuOuterFrame.setUndecorated(true);
		}
		else{
			menuOuterFrame.setSize(Options.MAIN_WINDOW_DEFAULT_WIDTH, Options.MAIN_WINDOW_DEFAULT_HEIGHT);
			menuOuterFrame.setLocation(Options.MAIN_WINDOW_INITIAL_X, Options.MAIN_WINDOW_INITIAL_Y);
			menuOuterFrame.setResizable(Options.MAIN_WINDOW_RESIZABLE);
		}

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
		java.util.List<String> mapNames = getMaps(Options.MAP_FILE);
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
			bgImage = ImageIO.read(new File(Options.DEFAULT_BG));
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
		backgroundPanel = new MenuBackground();
		menuOuterFrame.setContentPane(backgroundPanel);
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
			openOptionFrame();
		}

		if(mapButton.contains(source)){
			buildGame(getMap(mapList.get(source)));
		}
	}
	
	/**
	 * Closes any current game instance and
	 * creates a new one from the specified
	 * GameMap
	 */
	private void buildGame(GameMap map){
		if(currentGame != null) currentGame.terminate();
		currentGame = new Game(map);
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
	private void openOptionFrame(){
		optionFrame = new OptionMenu();
		optionFrame.init();
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


}

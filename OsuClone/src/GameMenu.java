import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
	private Map<String, Boolean> mods;
	private boolean modWindowOpen = false;

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JPanel backgroundPanel;
	private JPanel mainPanel;
	private JPanel mapPanel;
	private JPanel modPanel;
	private JLabel titleLabel;
	
	private JCheckBox hiddenCheckbox;
	private JCheckBox hardRockCheckbox;

	private OptionMenu optionFrame;

	private JButton optionButton;
	private JButton modButton;
	private JButton exitButton;

	private java.util.List<JButton> mapButton = new ArrayList<JButton>();
	private Map<JButton, GameMap> mapList = new HashMap<JButton, GameMap>();
	private JButton selectedButton = null;
	
	private Game currentGame = null;

	// The image to draw as the background
	private Image bgImage = null;
	
	// The name of the font to use as titles
	private String titleFontName = "Elephant";
	// Whether or not the preferred font exists
	private boolean fontExists;

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
		new JFXPanel();	// To initialise the class - this sometimes freezes it for a few seconds,
		// to which I haven't been able to find a solution.
		Options.init();
		initialiseMenu();
		initialiseButtons();
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

		// Initialise the background
		backgroundPanel = new MenuBackground();
		menuOuterFrame.setContentPane(backgroundPanel);
		
		menuOuterFrame.setLayout(new BorderLayout(0,0));
		
		checkFont();
		
		// Create the title
		initTitle();
		
		// Create two JPanels: one for regular buttons and one for maps
		initMapPanel();
		initMainPanel();

		// Select a random map to start
		selectRandom();
		
		// Initialise the mods panel at the bottom (not displayed unless the mods button is clicked)
		initModPanel();

		menuOuterFrame.setVisible(true);
	}
	
	/**
	 * Checks whether or not the preferred font exists, 
	 * and sets the boolean accordingly.
	 */
	private void checkFont(){
		// Check to see if we have the preferred font (otherwise don't use that font)
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(String s : fonts)
			if(s.equals(titleFontName)){
				fontExists = true;
				return;
			}
		fontExists = false;
	}
	
	/**
	 * Initialises the mod panel of the menu frame
	 */
	private void initModPanel(){
		modPanel = new JPanel(new GridLayout(0, 2, 0, 0) );
		modPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		modPanel.setBackground(Color.WHITE);
		
		hiddenCheckbox = addMod(modPanel, "Hidden");
		hardRockCheckbox = addMod(modPanel, "Hard Rock");
	}
	
	/**
	 * Used for initModPanel; adds a mod label and checkbox
	 * to the specified component with the specified label,
	 * and returns the checkbox.
	 */
	private JCheckBox addMod(JComponent component, String name){
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JCheckBox checkbox = new JCheckBox();
		checkbox.setOpaque(false);
		checkbox.setHorizontalAlignment(SwingConstants.LEFT);
		
		component.add(label);
		component.add(checkbox);
		
		return checkbox;
	}
	
	/**
	 * Initialises the title panel of the menu frame
	 */
	private void initTitle(){
		titleLabel = new JLabel("MyOsu! Menu", JLabel.CENTER);
		
		titleLabel.setPreferredSize(new Dimension(menuOuterFrame.getWidth(),70));
		titleLabel.setBackground(Color.WHITE);
		titleLabel.setOpaque(true);
		if(fontExists)
			titleLabel.setFont(new Font(titleFontName, Font.PLAIN, 45));
		else
			titleLabel.setFont(new Font(titleLabel.getFont().getFontName(), Font.PLAIN, 45));
		
		menuOuterFrame.add(titleLabel, BorderLayout.NORTH);
	}
	
	/**
	 * Initialises the map panel of the menu frame
	 */
	private void initMapPanel(){
		mapPanel = new JPanel();
		mapPanel.setPreferredSize(new Dimension(menuOuterFrame.getWidth()-5, menuOuterFrame.getHeight()-(int)titleLabel.getPreferredSize().getHeight()-37));
		mapPanel.setLayout(new BorderLayout(0,0));
		mapPanel.setOpaque(false);
		
		// Add the title label to the map panel
		JPanel mapTitlePanel = new JPanel();
		mapTitlePanel.setPreferredSize(new Dimension(menuOuterFrame.getWidth(), 45));
		mapTitlePanel.setBackground(Color.BLACK);
		
		JLabel mapTitle = new JLabel("Available Maps", JLabel.CENTER);
		
		if(fontExists)
			mapTitle.setFont(new Font(titleFontName, Font.PLAIN, 30));
		else
			mapTitle.setFont(new Font(mapTitle.getFont().getFontName(), Font.PLAIN, 30));
		mapTitle.setForeground(Color.WHITE);
		
		mapTitlePanel.add(mapTitle);
		
		mapPanel.add(mapTitlePanel, BorderLayout.NORTH);
		
		// And fill the map buttons from getMaps
		JPanel mapButtonPanel = new JPanel();
		mapButtonPanel.setOpaque(false);
		mapButtonPanel.setLayout(new BoxLayout(mapButtonPanel, BoxLayout.Y_AXIS));
		
		// Make a scrollPane, in case we have more maps than we can display in one screen
		JScrollPane scrollPane = new JScrollPane(mapButtonPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		// scroll pane defaults to having a border?
		scrollPane.setBorder(new EmptyBorder(0,0,0,0));
		
		Set<String> mapNames = getMaps();
		for(String name : mapNames){
			GameMap currentMap = getMap(name);
			JButton btn = addButton(name, Color.WHITE, Color.BLACK, mapButtonPanel);
			btn.setAlignmentX(Component.CENTER_ALIGNMENT);
			mapList.put(btn, currentMap);
			mapButton.add(btn);
		}
		
		mapPanel.add(scrollPane, BorderLayout.CENTER);
		
		menuOuterFrame.add(mapPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Creates and returns a new JButton with the given parameters.
	 * @param name The text to appear on the button.
	 * @param foreground The foreground (text) color to use.
	 * @param background The background color to use.
	 * @param component The component to add the button to.
	 */
	private JButton addButton(String name, Color foreground, Color background, JComponent component){
		JButton button = new JButton(name);
		button.setForeground(foreground);
		button.setBackground(background);
		component.add(button);
		return button;
	}
	
	/**
	 * Initialises the main panel of the menu frame
	 */
	private void initMainPanel(){
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(false);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setOpaque(false);
		
		// Create the various buttons
		optionButton = addButton("Options", Color.WHITE, Color.BLACK, buttonPanel);
		modButton = addButton("Mods", Color.WHITE, Color.BLACK, buttonPanel);
		exitButton = addButton("Exit", Color.WHITE, Color.BLACK, buttonPanel);
		
		// set the buttons to each take a third of the width (but maintain the default height)
		optionButton.setPreferredSize(new Dimension((int)menuOuterFrame.getPreferredSize().getWidth()/3, (int)optionButton.getPreferredSize().getHeight()));
		modButton.setPreferredSize(new Dimension((int)menuOuterFrame.getPreferredSize().getWidth()/3, (int)optionButton.getPreferredSize().getHeight()));
		exitButton.setPreferredSize(new Dimension((int)menuOuterFrame.getPreferredSize().getWidth()/3, (int)optionButton.getPreferredSize().getHeight()));
		
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		menuOuterFrame.add(mainPanel, BorderLayout.SOUTH);
	}

	/**
	 * Selects a random map.
	 */
	private void selectRandom(){
		java.util.List<JButton> buttonsList = new ArrayList<JButton>(mapList.keySet());
		int randomIndex = (int)(Math.random()*buttonsList.size());
		
		JButton randomButton = buttonsList.get(randomIndex);
		
		selectButton(randomButton);
	}

	// Defines drawing the background image
	private class MenuBackground extends JPanel {
		public void paintComponent(Graphics g){
			if(bgImage != null){
				// Check if the frame width or the image width is greater
				if(menuOuterFrame.getWidth() > bgImage.getWidth(this)){
					// If the frame is bigger, we want to stretch the image such that its width fills the whole width of the frame
					double scaleRatio = menuOuterFrame.getWidth()/bgImage.getWidth(this);
					// height of the blank space at the top and bottom of the frame
					double blankSpace = (menuOuterFrame.getHeight() - bgImage.getHeight(this)*scaleRatio)/2;
					if(blankSpace < 0) blankSpace = 0;
					
					g.drawImage(bgImage, 0, (int)blankSpace, menuOuterFrame.getWidth(), menuOuterFrame.getHeight()-(int)blankSpace, 0, 0, bgImage.getWidth(this), bgImage.getHeight(this), this);
				}
					
					
				// If the image is bigger, only use a part of the image
				else{
					// We want to get the middle of the background in the middle of the area
					// So, figure out the middle of the x and y of the picture's area
					int midPictureX = bgImage.getWidth(this)/2;
					int midPictureY = bgImage.getHeight(this)/2;
					// And the middle positions of the frame
					int midFrameX = menuOuterFrame.getWidth()/2;
					int midFrameY = menuOuterFrame.getHeight()/2;
				
					g.drawImage(bgImage,0,0,menuOuterFrame.getWidth(),menuOuterFrame.getHeight(),midPictureX-midFrameX,midPictureY-midFrameY,midPictureX+midFrameX,midPictureY+midFrameY,this);
				}
			}
		}
	}
	
	/**
	 * Sets the background of the menu frame to be the background
	 * of the given GameMap.
	 * Sets the background to the default background as defined in
	 * Options, if the given GameMap's background is null.
	 */
	private void setBackground(GameMap map){
		if(map == null) return;
		if(map.getBackground() != null)
			bgImage = map.getBackground();
		// Set to the default background if that map doesn't have a background
		else{
			try{
				bgImage = ImageIO.read(new File(Options.DEFAULT_BG));
			}
			catch(IOException e){
				System.out.println("Could not read from default background image file; " + e);
			}
		}
		backgroundPanel.repaint();
	}
	
	/**
	 * Plays the audio clip corresponding to a given map.
	 * Starts at the map's start position
	 */
	private void playAudio(GameMap map){
		if(map == null) return;
		if(map.getAudio() == null) return;
		
		AudioPlayer.playLongAudio(map.getAudio(), map.getAudioStartTime(), 0.3);
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
		exitButton.addActionListener(l);

		for(JButton btn : mapButton){
		  btn.addActionListener(l);
		}

	}

	/**
	 * Handles button actions for the menu
	 */
	private void doButtons(ActionEvent e){
		Object source = e.getSource();

		if(source.equals(optionButton)){
			openOptionFrame();
		}
		else if(source.equals(exitButton)){
			System.exit(0);
		}
		else if(source.equals(modButton)){
			toggleModWindow();
		}

		// If one of the map buttons is pressed,
		else if(mapButton.contains(source)){
			// If the button was already selected, we want to start the game with that map
			if(selectedButton == source)
				buildGame(mapList.get(source));
			// Otherwise, we want to select that button
			else
				selectButton((JButton)source);
		}
	}
	
	/**
	 * Selects the given map button, changing the background and the button colors
	 */
	private void selectButton(JButton button){
		// Change the colours of the previous button back (if there is one)
		if(selectedButton != null){
			selectedButton.setBackground(Color.BLACK);
			selectedButton.setForeground(Color.WHITE);
		}
		
		selectedButton = button;
		setBackground(mapList.get(button));
		playAudio(mapList.get(button));
		
		// Change the colours of the new button
		selectedButton.setBackground(Color.WHITE);
		selectedButton.setForeground(Color.BLACK);
	}
	
	/**
	 * Closes any current game instance and
	 * creates a new one from the specified
	 * GameMap
	 */
	private void buildGame(GameMap map){
		// Evaluate mods active
		Map<String, Boolean> modsActive = new HashMap<String, Boolean>();
		modsActive.put("hidden", hiddenCheckbox.isSelected());
		modsActive.put("hardrock", hardRockCheckbox.isSelected());
		
		if(currentGame != null) currentGame.terminate();
		currentGame = new Game(map, modsActive);
	}

	/**
	 * Opens a new frame which allows specification of options
	 */
	private void openOptionFrame(){
		optionFrame = new OptionMenu();
		optionFrame.init();
	}

	/**
	 * Finds the list of available maps from the folder specified in options.
	 * Maps' files should be contained within a folder; e.g. a path for a map could be "/maps/Scarlet Rose (Hard)", with
	 * that folder containing "background.jpg", "audio.mp3" and "map".
	 * @return A set of map names found in the folder. Gives an empty set if it couldn't read from the folder.
	 */
	private Set<String> getMaps(){
		// A set of map filesnames.
		Set<String> mapNames = new HashSet<String>();
		
		File dir = new File(Options.MAP_FOLDER);
		File[] fileList = dir.listFiles();
		for(File f : fileList)
			mapNames.add(f.getName());
		
		return mapNames;
		
		/* Old code to get the names from a file
		// A list of map filenames
		ArrayList<String> mapNames = new ArrayList<String>();
		// Attempt to create a scanner to find map names and add them to the list of map names
		try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));
			// Add to mapNames each name in the file
			while(s.hasNextLine()){
				mapNames.add(s.nextLine());
			}
			s.close();
		}
		// Throw an exception if it can't read from the file
		catch(IOException e){
			System.err.println("Could not read from map names file ("+filename+"). Details: " + e);
		}
		return mapNames;
		*/
	}

	/**
	 * Constructs a new map from the file with the given filename.
	 * @return The newly constructed map.
	 */
	private GameMap getMap(String filename){
		// Scan through the file and attempt to make a map from it, then return it.
		// Return null after an IOException
		try{
			Scanner s = new Scanner(new File("maps/" + filename + "/map"));
			GameMap newMap = new GameMap(filename, s);
			return newMap;
		}
		catch(IOException e){
			System.err.println("Could not read from map file '"+filename+"'. Is this file not present? Details: " + e);
		}
		return null;
	}

	/**
	 * Toggles whether or not the mod area is open.
	 */
	private void toggleModWindow(){
		if(modWindowOpen){
			mainPanel.remove(modPanel);
			menuOuterFrame.revalidate();
		}
		else{
			mainPanel.add(modPanel,BorderLayout.NORTH);
			menuOuterFrame.revalidate();
		}
		modWindowOpen = !modWindowOpen;
	}
}

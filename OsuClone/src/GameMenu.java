import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
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
	private boolean isHidden = false;

	/** JCOMPONENTS **/
	private JFrame menuOuterFrame;
	private JPanel backgroundPanel;
	private JPanel mainPanel;
	private JPanel mapPanel;
	private JLabel titleLabel;

	private OptionMenu optionFrame;

	private JButton optionButton;

	private JCheckBox hiddenCheckbox;

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
		menuOuterFrame.setLayout(new BorderLayout(0,0));

		// Initialise the background
		backgroundPanel = new MenuBackground();
		menuOuterFrame.setContentPane(backgroundPanel);
		
		checkFont();
		
		// Create the title
		initTitle();
		
		// Create two JPanels: one for regular buttons and one for maps
		initMapPanel();
		initMainPanel();

		// Select a random map to start
		selectRandom();

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
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		
		java.util.List<String> mapNames = getMaps(Options.MAP_FILE);
		for(String name : mapNames){
			GameMap currentMap = getMap(name);
			JButton btn = new JButton(name);
			btn.setAlignmentX(Component.CENTER_ALIGNMENT);
			btn.setForeground(Color.WHITE);
			btn.setBackground(Color.BLACK);
			
			mapList.put(btn, currentMap);
			mapButtonPanel.add(btn);
			mapButton.add(btn);
		}
		
		mapPanel.add(scrollPane, BorderLayout.CENTER);
		
		menuOuterFrame.add(mapPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Initialises the main panel of the menu frame
	 */
	private void initMainPanel(){
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setOpaque(false);
		
		// Create the various buttons
		optionButton = new JButton("Options");
		optionButton.setForeground(Color.WHITE);
		optionButton.setBackground(Color.BLACK);
		mainPanel.add(optionButton);

		hiddenCheckbox = new JCheckBox("Hidden");
		hiddenCheckbox.setOpaque(false);
		//mainPanel.add(hiddenCheckbox);
		
		menuOuterFrame.add(mainPanel, BorderLayout.SOUTH);
	}

	/**
	 * Selects a random map.
	 */
	private void selectRandom(){
		java.util.List<JButton> buttonsList = new ArrayList<JButton>(mapList.keySet());
		int randomIndex = (int)(Math.random()*buttonsList.size());
		
		JButton randomButton = buttonsList.get(randomIndex);
		GameMap randomMap = mapList.get(randomButton);
		
		selectedButton = randomButton;
		setBackground(randomMap);
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

		// If one of the map buttons is pressed,
		else if(mapButton.contains(source)){
			// If the button was already selected, we want to start the game with that map
			if(selectedButton == source)
				buildGame(mapList.get(source));
			// Otherwise, we want to select that button and set the background to the background of that map
			else{
				selectedButton = (JButton)source;
				setBackground(mapList.get(mapButton));
			}
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

	/**
	 * Constructs a new map from the file with the given filename.
	 * @return The newly constructed map.
	 */
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

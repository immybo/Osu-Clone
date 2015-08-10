import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

import javafx.scene.media.*;
import javafx.util.Duration;
import javafx.embed.swing.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

/**
 * A map which is being played
 * @author campberobe1
 *
 */
public class Game {
	// The map that this instance of Game is using
	private GameMap map;

	// Swing components
	private JFrame mainFrame;
	private GameDraw mainPanel;
	private GameGUI guiPanel;

	// The game timer
	private Timer timer;

	// The sound player
	private MediaPlayer audioPlayer;

	// The queue of elements to be displayed
	private Queue<Element> elements = new LinkedList<Element>();
	// The list of all elements which are currently being displayed
	private List<Element> currentElements = new ArrayList<Element>();

	// The time between an element appearing and it needing to be clicked
	private int approachTime;
	// The size, in pixels, of all elements
	private int circleSize;
	// The current time in the map
	private int currentMapTime;
	// The system time at which the map started
	private long mapStartTime;
	// The time at which the audio for the map should start
	private int audioStartTime;

	// To have a unique id for every element within a certain map
	private int currentElementId = 0;

	// The score and current health of the player
	private int score = 0;
	private int health = 100;

	// The time offsets which give the player scores of: 100, 50 and MISS, respectively
	private int[] timeOffsets;
	private int[] scores = { 300, 100, 50, 0 };
	private int[] healthChange = { 10, -5, -10, -20 };

	// The current position of the mouse
	private int mouseX = -1;
	private int mouseY = -1;

	// Whether the mouse (or key) is currently being held down or not
	private boolean mouseDown = false;

	// The slider which is currently being held down; null if none is being held down
	private Slider activeSlider = null;

	/**
	 * Constructor; instantiates Game.
	 * @param map The GameMap which this instance will play.
	 */
	public Game(GameMap map){
		this.map = map;

		currentMapTime = 0;
		mapStartTime = System.currentTimeMillis();

		timeOffsets = map.getOD();
		approachTime = map.getAR();
		circleSize = map.getCS();

		audioStartTime = map.getAudioStartTime();

		// Begins playing the audio of the map
		playAudio(map.getAudio());

		createWindow();

		// Set the timer to do the game loop
		class InnerActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e){
				// If it's the timer, do the game loop
				if(e.getSource().equals(timer)){
					doGame();
				}
			}
		}
		timer = new Timer(GameMenu.GAME_TICK_TIME, new InnerActionListener());
		timer.setInitialDelay(0);
		timer.start();
	}

	/**
	 * Creates the interface for the game
	 */
	private void createWindow(){
		// Create the outer frame
		mainFrame = new JFrame("MyOsu! Playing " + map.getName());
		mainFrame.setSize(GameMenu.GAME_WINDOW_DEFAULT_WIDTH, GameMenu.GAME_WINDOW_DEFAULT_HEIGHT);
		mainFrame.setLocation(GameMenu.GAME_WINDOW_INITIAL_X, GameMenu.GAME_WINDOW_INITIAL_Y);
		mainFrame.setResizable(GameMenu.GAME_WINDOW_RESIZABLE);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Calls the terminate method when the window is closed
		mainFrame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				terminate();
			}
		});

		// And the panel to draw on inside it
		mainPanel = new GameDraw();
		// Initialise its attributes
		mainPanel.init(circleSize, approachTime, timeOffsets[2], this);

		mainPanel.setFocusable(true);
		mainPanel.requestFocus();

		// Add the GUI panel to draw on
		guiPanel = new GameGUI();
		guiPanel.setPreferredSize(new Dimension(1000,70));

		mainFrame.add(guiPanel, BorderLayout.NORTH);
		mainFrame.add(mainPanel, BorderLayout.CENTER);

		mainFrame.setVisible(true);

		// Set the mouse listener on the screen
		class InnerMouseListener extends MouseInputAdapter{
			public void mouseClicked(MouseEvent e){
				doMouse(e);
			}
			public void mouseMoved(MouseEvent e){
				doMouse(e);
			}
			public void mouseDragged(MouseEvent e){
				doMouse(e);
			}
			public void mouseReleased(MouseEvent e){
				doMouse(e);
			}
			public void mousePressed(MouseEvent e){
				doMouse(e);
			}
		}
		mainPanel.addMouseListener(new InnerMouseListener());
		mainPanel.addMouseMotionListener(new InnerMouseListener());

		// Set the key listener on the screen
		class InnerKeyListener extends KeyAdapter{
			public void keyPressed(KeyEvent e){
				doKey(e);
			}
			public void keyReleased(KeyEvent e){
				doKey(e);
			}
		}
		mainPanel.addKeyListener(new InnerKeyListener());
	}

	/**
	 * Terminates the game
	 */
	public void terminate(){
		stopAudio();
		mainFrame.dispose();
	}

	/**
	 * Handles key actions
	 */
	public void doKey(KeyEvent e){
		// If a keyevent was passed but it wasn't one of the game keys, do nothing
		if(e.getKeyChar() != GameMenu.GAME_KEY_1 && e.getKeyChar() != GameMenu.GAME_KEY_2) return;

		// If a key was pressed, set the 'mouse' to be down and check for elements it could be down on
		if(e.getID() == KeyEvent.KEY_PRESSED && mouseDown == false){
			mouseDown = true;
			elementCheck(mouseX, mouseY);
		}

		// If it was released, the mouse is no longer down and sliders should be checked
		else if(e.getID() == KeyEvent.KEY_RELEASED && mouseDown == true){
			mouseDown = false;
			if(activeSlider != null){
				endSliderDrag();
			}
		}
	}

	/**
	 * Handles mouse actions
	 */
	private void doMouse(MouseEvent e){
		// Find the new co-ordinates of the mouse
		mouseX = e.getX();
		mouseY = e.getY();

		// If it was pressed,
		if(e.getID() == MouseEvent.MOUSE_PRESSED){
			mouseDown = true;
			// Check current elements to see which one it's on (if any)
			elementCheck(mouseX, mouseY);
		}

		// Otherwise, if it was released, check if it was on a slider
		else if(e.getID() == MouseEvent.MOUSE_RELEASED){
			mouseDown = false;
			if(activeSlider != null){
				endSliderDrag();
			}
		}
	}

	/**
	 * Stops the slider that is currently being dragged from being dragged.
	 * Does not actually fail the slider; merely stops accruing 'points'.
	 */
	private void endSliderDrag(){
		activeSlider = null;
	}

	/**
	 * Checks to see that the mouse is down and on the required position on the active slider,
	 * and increments the active's slider points if it is.
	 */
	private void changeActiveSliderPoints(){
		// Don't do anything if there isn't a slider active!
		if(activeSlider == null) return;

		// The proportion of time between the start and end of the slider
		double timeProportion = (currentMapTime - activeSlider.getTime() + 0.0) / (activeSlider.getEndTime() - activeSlider.getTime() + 0.0);

		// The coordinates that the cursor is required to be within range of
		int reqX = activeSlider.getX() + (int)(Math.cos(activeSlider.getAngle()) * timeProportion * activeSlider.getLength());
		int reqY = activeSlider.getY() + (int)(Math.sin(activeSlider.getAngle()) * timeProportion * activeSlider.getLength());

		// The amount which the cursor is out by
		double offset = Math.sqrt(Math.pow(mouseX-reqX, 2) + Math.pow(mouseY-reqY, 2));
		//System.out.println("offset : " + offset);
		//System.out.println("x: " + mouseX + " y : " + mouseY);

		if(offset < circleSize/2){
			activeSlider.sliderPoints++;
		}
	}

	/**
	 * Processes a click or key press at the specified position
	 */
	private void elementCheck(int x, int y){
		// Scroll through all the circles which are currently on the screen
		Iterator<Element> iter = currentElements.iterator();
		ArrayList<Element> elementsToRemove = new ArrayList<Element>();

		while(iter.hasNext()){
			Element element = iter.next();
			// Circles:
			if(element.getElementType() == 1){
				// If they are very close to the mouse click, remove the circle
				double radius = Math.sqrt(Math.pow(x-element.getX(),2) + Math.pow(y-element.getY(),2));
				if(radius < circleSize/2){
					elementsToRemove.add(element);
					break;
				}
			}
			// Sliders:
			if(element.getElementType() == 2){
				Slider slider = (Slider)element;
				// Make it the active slider if the mouse is on the follow circle
				int sliderFollowX = (int)(slider.getX() + slider.followCirclePos * Math.cos(slider.getAngle()));
				int sliderFollowY = (int)(slider.getY() + slider.followCirclePos * Math.sin(slider.getAngle()));
				double radius = Math.sqrt(Math.pow(x-sliderFollowX,2) + Math.pow(y-sliderFollowY,2));
				if(radius < circleSize/2){
					activeSlider = (Slider)element;
					break;
				}
			}
		}

		Element e = null;
		// Find the element that has spent the longest time on the screen
		for(Element element : elementsToRemove){
			if(e == null || element.getTime() < e.getTime()){
				e = element;
			}
		}

		// And remove it
		if(e != null) removeElement(e, true);
	}

	/**
	 * Removes an element checking the time offset and giving the appropriate score/hp modification
	 * @param element The element to remove
	 * @param wasClicked If the element was clicked to remove or not (if not, it must have timed out)
	 */
	private void removeElement(Element element, boolean wasClicked){
		if(element == null) { return; }

		int classification = 0;

		// Dequeue the element
		mainPanel.dequeueElement(element);

		if(element.getElementType() == 1){
			// Figure out the time offset for circles
			int supposedTime = element.getTime();
			long timeOffset = Math.abs(System.currentTimeMillis() - mapStartTime - supposedTime);

			if(wasClicked){
				if(timeOffset < timeOffsets[0]) classification = 0;
				else if(timeOffset < timeOffsets[1]) classification = 1;
				else if(timeOffset < timeOffsets[2]) classification = 2;
				else classification = 3;

			}
			else{
				// If it was never clicked, then it was a miss
				classification = 3;
			}
		}

		if(element.getElementType() == 2){
			Slider slider = (Slider)element;
			// Figure out the amount of points accrued for sliders
			int totalSliderTime = slider.getEndTime() - slider.getTime();
			// If the slider isn't being dragged at the end, remove some points
			if(activeSlider != null && !slider.equals(activeSlider)) slider.sliderPoints *= 0.8;

			if(slider.sliderPoints > (totalSliderTime+0.0)/20 * 0.9) classification = 0; // 90% of time held to get 300
			else if(slider.sliderPoints > (totalSliderTime+0.0)/20 * 0.7) classification = 1; // 70% to get 100
			else if(slider.sliderPoints > (totalSliderTime+0.0)/20 * 0.5) classification = 2; // 50% to get 50
			else classification = 3;
		}

		// Change score and health
		processElementRemoval(classification);

		// And finally, remove the element from currentelements
		currentElements.remove(element);
	}

	/**
	 * Increments the score and health with the given score
	 * @param scoreId 3, 2, 1, 0 for 300, 100, 50 or miss
	 */
	private void processElementRemoval(int scoreId){
		score += scores[scoreId];
		health += healthChange[scoreId];

		if(health > 100) health = 100;
		if(health < 0) health = 0;
	}

	/**
	 * Handles the game loop
	 */
	private void doGame(){
		// Evaluate the new time
		currentMapTime = (int)(System.currentTimeMillis()-mapStartTime);

		// Update the active slider
		if(mouseDown) changeActiveSliderPoints();

		// Check if any elements should be removed
		checkDisposal();
		// Check if the next circle in the map should be shown
		evaluateNext();
		// Draw all circles left in the queue
		drawQueue();

		// Update the gui attributes
		setGuiAttributes();

		// Render
		mainPanel.repaint();
		guiPanel.repaint();
	}

	/**
	 * Updates the score and health for the gui
	 */
	private void setGuiAttributes(){
		guiPanel.setScore(score);
		guiPanel.setHealth(health);
	}

	/**
	 * Checks if any elements should be removed
	 * And removes them if they should be
	 */
	private void checkDisposal(){
		// Grab the queue of elements that should be disposed
		Queue<Element> disposalQueue = mainPanel.getDisposalQueue();
		// And remove them all
		while(!disposalQueue.isEmpty()){
			removeElement(disposalQueue.poll(), false);
		}
	}

	/**
	 * Checks to see if the next element in the map's queue should be displayed.
	 * Adds it to the queue if it should be.
	 */
	private void evaluateNext(){
		// TODO end the map if the map is finished. Currently: return
		if(map.peek() == null) return;

		int nextTime = map.peek().getTime();
		if(nextTime == -1) return;

		// If it's within the time range where it should appear
		if(nextTime - currentMapTime < approachTime){
			// Then add the element to the queue of elements to display (and remove it from the map)
			elements.add(map.poll());
		}
	}

	/**
	 * Draws all elements in the queue
	 */
	private void drawQueue(){
		for(Element e : elements){
			e.setId(currentElementId);
			mainPanel.queueElement(e);
			currentElements.add(e);
			currentElementId++;
		}
		elements.clear();
	}

	/**
	 * Returns the current time in the map
	 */
	public int getMapTime(){
		return currentMapTime;
	}

	/**
	 * Plays the audio that is mapped to the current map;
	 * Does nothing if the audio file name was "null".
	 */
	private void playAudio(String audioFilename){
		if(audioFilename == null) return;
		JFXPanel panel = new JFXPanel();
		File file = new File(audioFilename);
		Media media = new Media(file.toURI().toString());
		audioPlayer = new MediaPlayer(media);
		audioPlayer.setStartTime(new Duration(audioStartTime));
		audioPlayer.play();
	}

	/**
	 * Stops the audio from being played.
	 */
	private void stopAudio(){
		audioPlayer.stop();
	}

	/**
	 * Returns the current time in ms relative to the map start.
	 */
	public int getCurrentMapTime(){
		return currentMapTime;
	}
}

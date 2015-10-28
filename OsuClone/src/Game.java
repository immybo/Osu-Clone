import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.List;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

/**
 * A map which is being played
 * @author campberobe1
 */
public class Game {
	// The minimum combo that is required to break to play the combo break sound
	private static final int MIN_COMBO_FOR_BREAK = 10;
	// The base loss of health per tick; modified by the map's health
	private static final double HEALTH_LOSS = 0.1;
	
	// Volumes for sound clips; in relative dB (I think)
	private static final float HIT_SOUND_VOLUME = -20f;
	private static final float COMBO_BREAK_VOLUME = 0.7f;
	
	// The map that is being played
	private GameMap map;

	// Swing components
	private JFrame mainFrame;
	private GameDraw mainPanel;
	private GamePauseMenu pauseMenu;
	private GameEndScreen endScreen;
	private JFrame endFrame;

	// The game timer
	private Timer timer;

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
	private double health = 0;
	
	// The current combo
	private int combo = 0;
	
	// The element that the player is up to on the map
	private int currentElement = 0;
	
	// The accuracy of the player so far (0-100%)
	private double accuracy = 0;
	
	// The number of each score which the player has gotten (300/100/50/MISS)
	private int[] scoreCounts = { 0, 0, 0, 0 };

	// The time offsets which give the player scores of: 100, 50 and MISS, respectively
	// Set at the start based off the map's health value, with these as the defaults
	// Changing these will change all health losses in the same ratio for all maps
	private int[] timeOffsets;
	private int[] scores = { 300, 100, 50, 0 };
	private int[] healthChange = { 10, 0, -7, -14 };
	private double healthLoss;

	// The current position of the mouse
	private int mouseX = -1;
	private int mouseY = -1;
	
	private InnerMouseListener mouseListener;
	private InnerKeyListener keyListener;

	// Whether the mouse (or key) is currently being held down or not
	private boolean mouseDown = false;

	// The slider which is currently being held down; null if none is being held down
	private Slider activeSlider = null;
	
	// The volume that the music should be played at
	private double volume = 0.5;
	
	// The total amount of time which the map has been paused for
	private double pauseDelay = 0;
	// The start time of the current pause; only reliable when the game is paused
	private long pauseStartTime;
	
	// The amount of time skipped from the initial pause
	private int skippedTime = 0;
	
	// Whether or not the audio has initialised, since it has a short delay
	public boolean audioInitialised = false;
	
	// Mods which are active for this game
	private Map<String, Boolean> modsActive;
	
	// Whether or not a 'break' in the map is active, wherein no health will be lost
	private boolean breakActive = false;
	// Only used when breakActive
	private int breakEndTime = -1;
	
	// When the break, before play starts, ends
	private int initialBreakEnd;

	/**
	 * Constructor; instantiates Game.
	 * @param map The GameMap which this instance will play.
	 * @param modsActive Which mods are active for this game.
	 */
	public Game(GameMap map, Map<String, Boolean> modsActive){
		// Set all the initial attributes from the map
		
		this.map = map;
		map.resetQueue();
		this.modsActive = modsActive;

		currentMapTime = 0;
		initialBreakEnd = map.getInitialBreakEnd();

		if(modsActive.get("hardrock")){
			timeOffsets = map.getODHR();
			approachTime = map.getARHR();
			circleSize = map.getCSHR();
			healthChange[3] /= map.getHealthHR();
			healthChange[2] /= map.getHealthHR();
			healthChange[1] /= map.getHealthHR();
			healthChange[0] *= map.getHealthHR();
			healthLoss = HEALTH_LOSS/map.getHealthHR();
		}
		else{
			timeOffsets = map.getOD();
			approachTime = map.getAR();
			circleSize = map.getCS();
			healthChange[3] /= map.getHealth();
			healthChange[2] /= map.getHealth();
			healthChange[1] /= map.getHealth();
			// Negative ones (1,2,3) increase loss as health gets higher level (lower)
			// Positive one (hit) decrease gain as health gets higher level
			healthChange[0] *= map.getHealth();
			healthLoss = HEALTH_LOSS/map.getHealth();
		}
		

		audioStartTime = map.getAudioStartTime();
		
		// Begins playing the audio of the map
		AudioPlayer.playLongAudio(map.getAudio(), audioStartTime, volume, this);
		
		// I know that this is poor code, but I'm not really sure what else to use.
		while(!audioInitialised){ try {Thread.sleep(10);} catch(Exception e){}}
		
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
		timer = new Timer(Options.GAME_TICK_TIME, new InnerActionListener());
		timer.setInitialDelay(0);
		timer.start();
		
		mapStartTime = System.currentTimeMillis();
	}
	
	/**
	 * A mouse listener for the game; simply
	 * calls doMouse(mouseevent) whenever 
	 * something is done with the mouse.
	 */
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
	
	/**
	 * A key listener for the game; simply
	 * calls doKey(keyevent) whenever something
	 * relevant is done with keys.
	 */
	class InnerKeyListener extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			doKey(e);
		}
		public void keyReleased(KeyEvent e){
			doKey(e);
		}
	}

	/**
	 * Creates the interface for the game
	 */
	private void createWindow(){
		// Create the outer frame
		mainFrame = new JFrame("MyOsu! Playing " + map.getName());
		
		// Code for windowed
		//mainFrame.setSize(Options.GAME_WINDOW_DEFAULT_WIDTH, Options.GAME_WINDOW_DEFAULT_HEIGHT);
		//mainFrame.setLocation(Options.GAME_WINDOW_INITIAL_X, Options.GAME_WINDOW_INITIAL_Y);
		//mainFrame.setResizable(Options.GAME_WINDOW_RESIZABLE);
		
		// Code for fullscreen
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainFrame.setUndecorated(true);
		
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

		mainFrame.add(mainPanel, BorderLayout.CENTER);

		mainFrame.setVisible(true);

		// Set the mouse listener on the screen
		mainPanel.addMouseListener(mouseListener = new InnerMouseListener());
		mainPanel.addMouseMotionListener(mouseListener);

		keyListener = new InnerKeyListener();
		mainPanel.addKeyListener(keyListener);
	}

	/**
	 * Terminates the game
	 */
	public void terminate(){
		AudioPlayer.stopLongAudio(map.getAudio());
		AudioPlayer.terminate();
		timer.stop();
		mainFrame.dispose();
	}

	/**
	 * Handles key actions
	 */
	public void doKey(KeyEvent e){
		// If it was escape, open the pause menu
		if(e.getKeyChar() == KeyEvent.VK_ESCAPE){
			doPause();
		}
		
		// If a keyevent was passed but it wasn't one of the game keys or the escape key, return,
		if(e.getKeyChar() != Options.GAME_KEY_1 && e.getKeyChar() != Options.GAME_KEY_2) return;

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
	 * Opens the pause menu and pauses the game
	 */
	private void doPause(){
		// Swap the graphics around
		pauseMenu = new GamePauseMenu();
		pauseMenu.init(this);
		pauseMenu.setVisible(true);
		
		mainFrame.remove(mainPanel);
		mainFrame.add(pauseMenu, BorderLayout.CENTER);
		
		mainFrame.revalidate();
		pauseMenu.repaint();
		
		// Stop the timer, remove the listeners and pause the audio
		pauseStartTime = System.currentTimeMillis();
		timer.stop();
		mainPanel.removeMouseListener(mouseListener);
		mainPanel.removeMouseMotionListener(mouseListener);
		mainPanel.removeKeyListener(keyListener);
		AudioPlayer.pauseLongAudio();
	}
	
	/**
	 * Resumes the game after pausing
	 */
	public void doUnpause(){
		// Swap the graphics around
		mainFrame.remove(pauseMenu);
		pauseMenu = null;
		mainFrame.add(mainPanel, BorderLayout.CENTER);
		mainFrame.revalidate();
		
		// Resume the audio, listeners and timer
		AudioPlayer.resumeLongAudio();
		mainPanel.addMouseListener(mouseListener);
		mainPanel.addMouseMotionListener(mouseListener);
		mainPanel.addKeyListener(keyListener);
		mainPanel.requestFocus();
		
		// Change the total time spent paused so that we know where we're up to in the map
		pauseDelay += System.currentTimeMillis() - pauseStartTime;
		timer.start();
	}
	
	/**
	 * Restarts the map
	 */
	public void restart(){
		terminate();
		new Game(map, modsActive);
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
			
			// Check to see if it's over the skip button if applicable
			if(currentMapTime < initialBreakEnd){
				int[] buttonPosition = mainPanel.getSkipButtonPosition();
				if(mouseX > buttonPosition[0] && mouseX < buttonPosition[1]
				 &&mouseY > buttonPosition[2] && mouseY < buttonPosition[3]){
					mainPanel.removeSkipButton();
					skipIntro();
					health = 100;
				}
			}
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
	 * Removes an element checking the time offset and giving the appropriate score/HP/accuracy modification
	 * @param element The element to remove
	 * @param wasClicked If the element was clicked to remove or not (if not, it must have timed out)
	 */
	private void removeElement(Element element, boolean wasClicked){
		if(element == null) { return; }
		
		currentElement++;

		int classification = 0;

		// Dequeue the element
		mainPanel.dequeueElement(element);

		if(element.getElementType() == 1){
			// Figure out the time offset for circles
			int supposedTime = element.getTime();
			long timeOffset = Math.abs(currentMapTime - supposedTime);

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

		// Figure out how many points to give for a slider
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
		
		// Play the combo break sound if necessary, and break the combo if they miss
		if(classification == 3){
			if(combo > MIN_COMBO_FOR_BREAK)
				AudioPlayer.playClip(Options.SKIN_COMBO_BREAK_SOUND, COMBO_BREAK_VOLUME);
			
			combo = 0;
		}
		// Otherwise increment the current combo and play the hit sound
		else{
			AudioPlayer.playClip(Options.SKIN_CIRCLE_HIT_SOUND, HIT_SOUND_VOLUME);
			combo++;
		}
			
		// Change score, health and accuracy
		processElementRemoval(classification);

		// And finally, remove the element from currentelements
		currentElements.remove(element);
		
		// If the map has no more elements and there are none on the screen, then it is finished!
		if(map.peek() == null && currentElements.isEmpty())
			finishMap();
	}

	/**
	 * Increments the score, accuracy and health with the given score
	 * @param scoreId 3, 2, 1, 0 for 300, 100, 50 or miss
	 */
	private void processElementRemoval(int scoreId){
		// The formula for the score involves the combo and isn't linear;
		// I'm not sure exactly what it is in the real game but this should
		// be a reasonable approximation.
		score += scores[scoreId]*(Math.pow(combo*0.1,2) + 1);
		
		health += healthChange[scoreId];

		if(health > 100) health = 100;
		if(health < 0) health = 0;
		
		// The accuracy needs to be a percent of total hits, and it's just
		// simpler to recalculate it every time.
		scoreCounts[scoreId]++;
		accuracy = (double)(scoreCounts[0]*100 + scoreCounts[1]*100/3 + scoreCounts[2] * 100/6) / currentElement;
	}

	/**
	 * Handles the game loop
	 */
	private void doGame(){
		// Evaluate the new time
		currentMapTime = (int)(System.currentTimeMillis()-pauseDelay-mapStartTime+skippedTime);

		// Update the active slider
		if(mouseDown) changeActiveSliderPoints();
		
		// Split into 2 parts: Initial break (before play starts) and play time
		if(currentMapTime < initialBreakEnd)
			processInitialBreak();
		else{
			// Check if any elements should be removed
			checkDisposal();
			// Check if the next circle in the map should be shown
			evaluateNext();
			// Draw all circles left in the queue
			drawQueue();

		
			// Reduce the remaining health
			reduceHealth();

			// Check if we're in a break, and then check if we should be out of it if so
			if(breakActive)
				if(breakEndTime < currentMapTime)
					breakActive = false;
		}
		
		// Update the gui attributes
		setGuiAttributes();

		// Render
		mainPanel.repaint();
	}
	
	/**
	 * If we're in the initial break, increases the health by the appropriate amount
	 */
	private void processInitialBreak(){
		health += 100*(double)Options.GAME_TICK_TIME/initialBreakEnd;
		// If it's the last tick of the initial break, remove the skip button
		if(currentMapTime + Options.GAME_TICK_TIME > initialBreakEnd){
			mainPanel.removeSkipButton();
		}
	}
	
	/**
	 * Skips past the initial break to the start of gameplay (almost)
	 */
	private void skipIntro(){
		skippedTime = initialBreakEnd - 1000 - currentMapTime;
	}
	
	/**
	 * Reduces the remaining health by the appropriate amount
	 * IFF there isn't currently a break.
	 */
	private void reduceHealth(){
		if(!breakActive) health -= healthLoss;
	}

	/**
	 * Updates the score, accuracy and health for the gui
	 */
	private void setGuiAttributes(){
		mainPanel.setScore(score);
		mainPanel.setHealth(health);
		mainPanel.setAccuracy(accuracy);
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
		// There might still be elements on the screen even if there aren't any in the map
		if(map.peek() == null)
			return;

		int nextTime = map.peek().getTime();
		if(nextTime == -1) return;
		
		// If it's within the time range where it should appear

		// If it's a break, we want to process it as such (without the approach time)
		if(map.peek().getElementType() == 3){
			if(nextTime < currentMapTime){
				breakActive = true;
				breakEndTime = ((Break)map.peek()).getEndTime();
				map.poll();
			}
		}
		// Otherwise add the element to the queue of elements to display (and remove it from the map)
		else if(nextTime - currentMapTime < approachTime)
			elements.add(map.poll());
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
	 * Returns the current time in ms relative to the map start.
	 */
	public int getCurrentMapTime(){
		return currentMapTime;
	}
	
	/**
	 * Ends the currently playing map;
	 * Brings up the end of map screen
	 */
	private void finishMap(){
		// Stop the game
		AudioPlayer.stopLongAudio(map.getAudio());
		AudioPlayer.terminate();
		timer.stop();
		
		// Remove the listeners
		mainPanel.removeMouseListener(mouseListener);
		mainPanel.removeMouseMotionListener(mouseListener);
		mainPanel.removeKeyListener(keyListener);
		
		// Switch the game panel out for the end of game panel
		endScreen = new GameEndScreen();
		endScreen.setPreferredSize(new Dimension(Options.END_SCREEN_WIDTH, Options.END_SCREEN_HEIGHT));
		endScreen.init(score, accuracy, scoreCounts, map, this);
		endScreen.setVisible(true);
		
		// Get rid of this frame and make a new, non-fullscreen one
		mainFrame.dispose();
		
		endFrame = new JFrame();
		endFrame.setSize(Options.END_SCREEN_WIDTH, Options.END_SCREEN_HEIGHT);
		endFrame.setLocation(Options.END_WINDOW_INITIAL_X, Options.END_WINDOW_INITIAL_Y);
		endFrame.setResizable(Options.END_WINDOW_RESIZABLE);
		endFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		endFrame.setTitle("MyOsu! Map End Screen");
		endFrame.add(endScreen);
		endFrame.setVisible(true);
	}
	
	/**
	 * Disposes of the end screen frame.
	 */
	public void terminateEndScreen(){
		endFrame.dispose();
	}
}

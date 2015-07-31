import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

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

	// The queue of elements to be displayed
	private Queue<Element> elements = new LinkedList<Element>();
	// The list of all elements which are currently being displayed
	private List<Element> currentElements = new ArrayList<Element>();

	// The time between an element appearing and it needing to be clicked
	private int approachTime = 500;
	// The size, in pixels, of all elements
	private int circleSize = 100;
	// The current time in the map
	private int currentMapTime;
	// The system time at which the map started
	private long mapStartTime;

	// To have a unique id for every circle within a certain map
	private int currentCircleId = 0;

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

	/**
	 * Constructor; instantiates Game.
	 * @param map The GameMap which this instance will play.
	 */
	public Game(GameMap map){
		this.map = map;

		currentMapTime = 0;
		mapStartTime = System.currentTimeMillis();

		timeOffsets = new int[3];
		timeOffsets[0] = 1000; timeOffsets[1] = 2500; timeOffsets[2] = 5000;

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
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// And the panel to draw on inside it
		mainPanel = new GameDraw();
		mainPanel.setPreferredSize(new Dimension(1000,930));
		// Initialise its attributes
		mainPanel.init(circleSize, approachTime);

		mainPanel.setFocusable(true);
		mainPanel.requestFocus();

		// Add the GUI panel to draw on
		guiPanel = new GameGUI();
		guiPanel.setPreferredSize(new Dimension(1000,70));

		mainFrame.add(guiPanel, BorderLayout.NORTH);
		mainFrame.add(mainPanel, BorderLayout.SOUTH);

		mainFrame.setVisible(true);

		// Set the mouse listener on the screen
		class InnerMouseListener extends MouseInputAdapter{
			public void mouseClicked(MouseEvent e){
				doMouse(e);
			}
			public void mouseMoved(MouseEvent e){
				mouseX = e.getX();
				mouseY = e.getY();
			}
		}
		mainPanel.addMouseMotionListener(new InnerMouseListener());

		// Set the key listener on the screen
		class InnerKeyListener extends KeyAdapter{
			public void keyPressed(KeyEvent e){
				doKey(e);
			}
		}
		mainPanel.addKeyListener(new InnerKeyListener());
	}

	/**
	 * Terminates the game
	 */
	public void terminate(){
		mainFrame.dispose();
	}

	/**
	 * Handles key actions
	 */
	public void doKey(KeyEvent e){
		// If it's one of the game keys, check to see if the current mouse position is on an element
		if(e.getKeyChar() == GameMenu.GAME_KEY_1 || e.getKeyChar() == GameMenu.GAME_KEY_2){
			elementCheck(mouseX, mouseY);
		}
	}

	/**
	 * Handles mouse actions
	 */
	private void doMouse(MouseEvent e){
		// Find the co-ordinates of the mouse click
		int x = e.getX();
		int y = e.getY();

		// And check current elements to see which one it's on (if any)
		elementCheck(x,y);
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
			// If they are very close to the mouse click, remove the circle
			double radius = Math.sqrt(Math.pow(x-element.getX(),2) + Math.pow(y-element.getY(),2));
				if(radius < circleSize/2){
					elementsToRemove.add(element);
					break;
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
		if(e != null) removeElement(e, (int)(System.currentTimeMillis()-mapStartTime), true);
	}

	/**
	 * Removes an element checking the time offset and giving the appropriate score/hp modification
	 * @param element The element to remove
	 * @param wasClicked If the element was clicked to remove or not (if not, it must have timed out)
	 */
	private void removeElement(Element element, int time, boolean wasClicked){
		if(element == null) { return; }

		// Dequeue the element
		mainPanel.dequeueElement(element);

		// Figure out the time offset from when the element was supposed to be clicked
		int supposedTime = element.getTime();
		int timeOffset = Math.abs(time - supposedTime);

		int classification = 0;
		if(wasClicked){
			if(timeOffset > timeOffsets[0]) classification = 1;
			if(timeOffset > timeOffsets[1]) classification = 2;
			if(timeOffset > timeOffsets[2]) classification = 3;
		}
		else{
			// If it was never clicked, then it was a miss
			classification = 3;
		}

		score += scores[classification];
		health += healthChange[classification];

		if(health > 100) health = 100;
		if(health < 0) health = 0;

		// And finally, remove the element from currentelements
		currentElements.remove(element);
	}

	/**
	 * Handles the game loop
	 */
	private void doGame(){
		// Evaluate the new time
		currentMapTime = (int)(System.currentTimeMillis()-mapStartTime);

		// Check if any circles should be removed
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
		// Grab the queue of circles that should be disposed
		Queue<Element> disposalQueue = mainPanel.getDisposalQueue();
		// And remove them all
		while(!disposalQueue.isEmpty()){
			removeElement(disposalQueue.poll(), (int)(System.currentTimeMillis()-mapStartTime), false);
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
			e.setId(currentCircleId);
			mainPanel.queueElement(e);
			currentElements.add(e);
			currentCircleId++;
		}
		elements.clear();
	}
}

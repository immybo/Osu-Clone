import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

import javax.swing.*;

/**
 * A map which is being played
 * @author campberobe1
 *
 */
public class Game implements ActionListener{
	// The map that this instance of Game is using
	private GameMap map;

	// Swing components
	private JFrame mainFrame;
	private GameDraw mainPanel;

	// The game timer
	private Timer timer;

	// The queue of elements to be displayed
	// {element type, element time, element x, element y}
	private Queue<int[]> elements = new LinkedList<int[]>();
	// The list of all elements which are currently being displayed
	// {element type, element time, element x, element y}
	private List<int[]> currentElements = new ArrayList<int[]>();

	// The time between an element appearing and it needing to be clicked
	private int approachTime = 500;
	// The size, in pixels, of all elements
	private int circleSize = 100;
	// The current time in the map
	private int currentMapTime;
	// The system time at which the map started
	private long mapStartTime;

	// To have a unique id for every circle
	private int currentCircleId = 0;

	// The score and current health of the player
	private int score = 0;
	private int health = 100;

	// The time offsets which give the player scores of: 100, 50 and MISS, respectively
	private int[] timeOffsets;
	private int[] scores = { 300, 100, 50, 0 };
	private int[] healthChange = { 10, -10, -20, -50 };

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
		timer = new Timer(GameMenu.GAME_TICK_TIME, this);
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
		// Initialise its attributes
		mainPanel.init(circleSize, approachTime);
		mainFrame.add(mainPanel);

		mainFrame.setVisible(true);

		// Set the mouse listener on the screen
		class InnerMouseListener extends MouseAdapter{
			public void mouseClicked(MouseEvent e){
				doMouse(e);
			}
		}
		mainPanel.addMouseListener(new InnerMouseListener());


	}

	/**
	 * Terminates the game
	 */
	public void terminate(){
		mainFrame.dispose();
	}

	public void actionPerformed(ActionEvent e){
		// If it's the timer, do the game loop
		if(e.getSource().equals(timer)){
			doGame();
		}
	}

	/**
	 * Handles mouse actions
	 */
	private void doMouse(MouseEvent e){
		// Find the co-ordinates of the mouse click
		int x = e.getX();
		int y = e.getY();

		// Scroll through all the circles which are currently on the screen
		Iterator iter = currentElements.iterator();
		List<int[]> elementsToRemove = new ArrayList<int[]>();
		while(iter.hasNext()){
			int[] element = (int[])iter.next();
			// If they are very close to the mouse click, remove the circle
			double radius = Math.sqrt(Math.pow(x-element[2],2) + Math.pow(y-element[3],2));
			if(radius < circleSize/2){
				elementsToRemove.add(element);
				break;
			}
		}

		for(int[] element : elementsToRemove){
			removeCircle(element, (int)(System.currentTimeMillis()-mapStartTime));
		}
	}

	/**
	 * Removes a circle checking the time offset and giving the appropriate score/hp modification
	 */
	private void removeCircle(int[] element, int time){
		// Dequeue the circle
		mainPanel.dequeueCircle(element[2], element[3], element[4]);

		// Figure out the time offset from when the circle was supposed to be clicked
		int supposedTime = element[1];
		int timeOffset = Math.abs(time - supposedTime);

		int classification = 0;
		if(timeOffset > timeOffsets[0]) classification = 1;
		if(timeOffset > timeOffsets[1]) classification = 2;
		if(timeOffset > timeOffsets[2]) classification = 3;

		score += scores[classification];
		health += healthChange[classification];
		System.out.println(scores[classification]);

		// And finally, remove the circle from currentelements
		currentElements.remove(element);
	}

	/**
	 * Handles the game loop
	 */
	private void doGame(){
		// Evaluate the new time
		currentMapTime = (int)(System.currentTimeMillis()-mapStartTime);

		evaluateNext();
		drawQueue();

		// Render
		mainPanel.repaint();
	}

	/**
	 * Checks to see if the next element in the map's queue should be displayed.
	 * Adds it to the queue if it should be.
	 */
	private void evaluateNext(){
		double nextTime = map.nextTime();
		if(nextTime == -1) return;

		// If it's within the time range where it should appear
		if(nextTime - currentMapTime < approachTime){
			// Then add the element to the queue of elements to display
			elements.add(map.next());
		}
	}

	/**
	 * Draws all elements in the queue
	 */
	private void drawQueue(){
		for(int[] e : elements){
			mainPanel.queueCircle(e[2], e[3], currentCircleId);
			int[] newCircle = { e[0], e[1], e[2], e[3], currentCircleId };
			currentElements.add(newCircle);
			currentCircleId++;
		}
		elements.clear();
	}
}

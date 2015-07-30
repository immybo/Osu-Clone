import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Handles drawing for a game of MyOsu!
 * @author campberobe1
 *
 */
public class GameDraw extends JPanel{
	// The time it takes for an approach circle to reach the size of a circle
	private int approachRate;
	// The size of all circles
	private int circleSize;
	// The initial size of all approach circles
	private int approachSize;

	// The list of circles to be drawn
	private java.util.List<int[]> circles = new ArrayList<int[]>();

	// The list of approach circles to be drawn
	private java.util.List<double[]> approachCircles = new ArrayList<double[]>();

	// The colors to draw the circles in
	Color borderColor = Color.BLACK;
	Color fillColor = Color.RED;
	Color approachColor = Color.BLUE;

	private long previousTime;

	/**
	 * Initialises circle size and approach rate
	 */
	public void init(int circleSize, int approachRate){
		this.circleSize = circleSize;
		this.approachRate = approachRate;
		approachSize = this.circleSize*2;
		previousTime = System.currentTimeMillis();
	}

	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		// Clear current graphics
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,this.getWidth(),this.getHeight());

		for(int[] next : circles){
			g2d.setColor(fillColor);
			g2d.fillOval(next[0]-circleSize/2, next[1]-circleSize/2, circleSize, circleSize);
			g2d.setColor(borderColor);
			g2d.drawOval(next[0]-circleSize/2, next[1]-circleSize/2, circleSize, circleSize);
		}

		// Figure out how much each approach circle needs to have its size reduced by
		long currentTime = System.currentTimeMillis();
		long dT = currentTime - previousTime;

		// Size reduction amount
		double circleLoss = (double)(dT * (approachSize-circleSize))/approachRate;

		for(double[] next : approachCircles){
			next[3] -= circleLoss;

			g2d.setColor(approachColor);
			int newX = (int)(next[0] - next[3]/2);
			int newY = (int)(next[1] - next[3]/2);
			g2d.drawOval(newX, newY, (int)next[3], (int)next[3]);
		}

		previousTime = currentTime;
	}

	/**
	 * Queues a circle to be drawn and leaves it on the screen until dequeue is called on it.
	 * @param xPos The x position of the center of the circle
	 * @param yPos The y position of the center of the circle
	 * @param id The unique ID of the circle, to know which one to remove
	 */
	public void queueCircle(int xPos, int yPos, int id){
		int[] newCircle = { xPos, yPos, id };
		circles.add(newCircle);
		// Add the corresponding approach circle
		queueApproachCircle(xPos, yPos, id);
	}

	/**
	 * Removes a circle from the screen along with its corresponding approach circle
	 */
	public void dequeueCircle(int xPos, int yPos, int id){
		Iterator iter = circles.iterator();
		while(iter.hasNext()){
			int[] next = (int[])iter.next();
			if(next[2] == id){
				iter.remove();
			}
		}

		iter = approachCircles.iterator();
		while(iter.hasNext()){
			double[] next = (double[])iter.next();
			if(next[2] == id){
				iter.remove();
			}
		}
	}

	/**
	 * Queues an approach circle to be drawn with the given approach rate,
	 * which continues to become smaller until it reaches a regular circle size
	 * (then disappears)
	 * @param xPos The x position of the center of the approach circle
	 * @param yPos The y position of the center of the approach circle
	 * @param id The unique ID of the circle, to know which one to remove
	 */
	private void queueApproachCircle(int xPos, int yPos, int id){
		double[] newApproach = { xPos, yPos, id, approachSize };
		approachCircles.add(newApproach);
	}
}

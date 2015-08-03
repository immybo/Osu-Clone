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
	// The accuracy (in ms) required before the circle disappears
	private int accuracy;

	// The list of circles to be drawn
	private java.util.List<Circle> circles = new ArrayList<Circle>();

	// The queue of elements that should be disposed of because they have timed out
	private Queue<Element> disposalElements = new LinkedList<Element>();

	// The colors to draw the circles in
	Color borderColor = Color.BLACK;
	Color fillColor = Color.RED;
	Color approachColor = Color.BLUE;

	private long previousTime;

	/**
	 * Initialises circle size and approach rate
	 */
	public void init(int circleSize, int approachRate, int overallDifficulty){
		this.circleSize = circleSize;
		this.approachRate = approachRate;
		this.accuracy = overallDifficulty;
		approachSize = this.circleSize*2;
		previousTime = System.currentTimeMillis();
	}

	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		// Clear current graphics
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,this.getWidth(),this.getHeight());

		// Draw every circle and its approach circle

		// Figure out how much each approach circle needs to have its size reduced by
		long currentTime = System.currentTimeMillis();
		long dT = currentTime - previousTime;
		// Size reduction amount
		double circleLoss = (double)(dT * (approachSize-circleSize))/approachRate;
		previousTime = currentTime;

		for(Circle next : circles){

			// Decrease the size of every approach circle
			next.approachCircleSize -= circleLoss;

			// Check if the circle is supposed to disappear
			// (Which we can tell from the approach circle size compared to the normal circle size, the accuracy required, and the approach rate
			if(next.approachCircleSize < circleSize - accuracy*(approachSize-circleSize)/approachRate){
				// If it is supposed to disappear, add it to the disposal queue
				disposalElements.offer(next);
				continue;
			}

			// If the approach circle would be smaller than the circle, we don't actually want to draw it, but we do want to start fading the circle out
			if(next.approachCircleSize > circleSize){
				// Draw the approach circle if not
				g2d.setColor(approachColor);
				double approachX = next.getX()-circleSize/2-(next.approachCircleSize-circleSize)/2;
				double approachY = next.getY()-circleSize/2-(next.approachCircleSize-circleSize)/2;
				g2d.drawOval((int)approachX, (int)approachY, (int)next.approachCircleSize, (int)next.approachCircleSize);
			}

			// Circle fadeout
			int fade = 0;
			if(next.approachCircleSize < circleSize){
				fade = (int)((circleSize - next.approachCircleSize)*(255/circleSize));
			}
			// Draw every circle
			g2d.setColor(new Color(fillColor.getRed(), fillColor.getBlue(), fillColor.getGreen(), 255-fade));
			g2d.fillOval(next.getX()-circleSize/2, next.getY()-circleSize/2, circleSize, circleSize);
			g2d.setColor(borderColor);
			g2d.drawOval(next.getX()-circleSize/2, next.getY()-circleSize/2, circleSize, circleSize);
		}

	}

	/**
	 * Adds an element to the queue of elements to be repeatedly drawn.
	 * See also: dequeueElement
	 * @param element The element to be added.
	 */
	public void queueElement(Element element){
		switch(element.getElementType()){
			case 1:
				queueCircle((Circle)element);
				break;
		}
	}
	/**
	 * Adds a circle to the queue of circles to be repeatedly drawn.
	 */
	private void queueCircle(Circle circle){
		// Queue the circle
		circles.add(circle);
		// Set the initial approach circle size
		circle.approachCircleSize = approachSize;
	}

	/**
	 * Removes an element from the queue of elements to be repeatedly drawn.
	 * Also removes any corresponding components, e.g. approach circles.
	 * @param element The element to be removed.
	 */
	public void dequeueElement(Element element){
		switch(element.getElementType()){
			case 1:
				dequeueCircle((Circle)element);
				break;
		}
	}
	/**
	 * Removes a circle from the screen
	 */
	private void dequeueCircle(Circle circle){
		// Iterate through the queue and remove it if it's the same circle
		Iterator<Circle> iter = circles.iterator();
		while(iter.hasNext()){
			Circle c = iter.next();
			if(c.equals(circle)){
				iter.remove();
			}
		}
	}

	/**
	 * Gets the queue of elements that should be
	 * removed, as they have timed out.
	 * Also clears this queue.
	 * @return The queue of elements
	 */
	public Queue<Element> getDisposalQueue(){
		// The queue to return
		Queue<Element> returnQueue = new LinkedList<Element>();
		// Clone the disposal elements queue, emptying it as well
		while(!disposalElements.isEmpty()){
			returnQueue.add(disposalElements.poll());
		}
		return returnQueue;
	}
}

import javax.swing.*;

import java.awt.*;
import java.util.*;

/**
 * Handles drawing for a game of MyOsu!
 * @author campberobe1
 *
 */
public class GameDraw extends JPanel{
	// The instance of game that this is drawing for
	private Game game;
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
	// The list of sliders to be drawn
	private java.util.List<Slider> sliders = new ArrayList<Slider>();

	// The queue of elements that should be disposed of because they have timed out
	private Queue<Element> disposalElements = new LinkedList<Element>();

	// The colors to draw the circles in
	Color borderColor = Color.BLACK;
	Color fillColor = Color.RED;
	Color approachColor = Color.BLUE;
	Color sliderColor = Color.GREEN;
	Color sliderEndColor = Color.CYAN;
	Color followCircleColor = Color.GRAY;

	private long previousTime;

	/**
	 * Initialises map attributes and provides the instance of game
	 */
	public void init(int circleSize, int approachRate, int overallDifficulty, Game game){
		this.circleSize = circleSize;
		this.approachRate = approachRate;
		this.accuracy = overallDifficulty;
		this.game = game;
		approachSize = this.circleSize*2;
		previousTime = System.currentTimeMillis();
	}

	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		// Clear current graphics
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,this.getWidth(),this.getHeight());

		// Figure out how much time has elapsed since last time
		long currentTime = System.currentTimeMillis();
		long dT = currentTime - previousTime;

		// Draw every circle and its approach circle

		// Size reduction amount
		double circleLoss = (double)(dT * (approachSize-circleSize))/approachRate;
		previousTime = currentTime;

		// Reduce each approach circle by the size and then draw the circle
		for(Circle next : circles){
			// Decrease the size of every approach circle
			next.approachCircleSize -= circleLoss;
			// Draw the circle
			drawCircleElement(g2d, next);
		}

		// Next, draw all sliders
		for(Slider next : sliders){
			drawSliderElement(g2d, next, dT);
		}

		previousTime = currentTime;

	}

	/**
	 * Draws a circle along with its approach circle
	 */
	private void drawCircleElement(Graphics2D g2d, Circle circle){
		// Check if the circle is supposed to disappear
		// (Which we can tell from the approach circle size compared to the normal circle size, the accuracy required, and the approach rate
		if(circle.approachCircleSize < circleSize - accuracy*(approachSize-circleSize)/approachRate){
			// If it is supposed to disappear, add it to the disposal queue and don't draw it
			disposalElements.offer(circle);
			return;
		}

		// If the approach circle would be smaller than the circle, we don't actually want to draw it, but we do want to start fading the circle out
		if(circle.approachCircleSize > circleSize){
			// Draw the approach circle if not
			g2d.setColor(approachColor);
			double approachX = circle.getX()-circleSize/2-(circle.approachCircleSize-circleSize)/2;
			double approachY = circle.getY()-circleSize/2-(circle.approachCircleSize-circleSize)/2;
			g2d.drawOval((int)approachX, (int)approachY, (int)circle.approachCircleSize, (int)circle.approachCircleSize);
		}

		// Calculate fadeout of the circle and the new color including alpha
		int fade = 0;
		if(circle.approachCircleSize < circleSize){
			fade = (int)(255 * ((double)(game.getMapTime()-circle.getTime())/accuracy));
			if(fade < 0){
				fade = 0;
			}
			else if(fade > 255){
				fade = 255;
			}
		}
		Color colorWithFade = new Color(fillColor.getRed(), fillColor.getBlue(), fillColor.getGreen(), 255-fade);

		// Actually draw the circle
		drawFilledCircle(g2d, circle.getX(), circle.getY(), colorWithFade, borderColor);
	}

	/**
	 * Draws a slider, including its end points and follow circle
	 * @param dT The change in time since the last time of drawing.
	 */
	private void drawSliderElement(Graphics2D g2d, Slider slider, long dT){
		// Check if the slider is supposed to disappear; if the follow circle has reached the end
		if(slider.followCirclePos >= slider.getLength()){
			disposalElements.offer(slider);
			return;
		}

		// Convert the angle to radians for trigonometric calculations
		double angleRads = slider.getAngle() / 180 * Math.PI;

		g2d.setColor(sliderColor);
		// Draw a diagram to understand these points
		// Depends on the angle of the slider, so we can't just use drawRectangle
		int x1 = (int)(slider.getX() + Math.sin(angleRads)*(circleSize/2));
		int x2 = (int)(slider.getX() - Math.sin(angleRads)*(circleSize/2));
		int x3 = (int)(slider.getX() + Math.cos(angleRads)*(slider.getLength()) - Math.sin(angleRads)*(circleSize/2));
		int x4 = (int)(slider.getX() + Math.cos(angleRads)*(slider.getLength()));

		int y1 = (int)(slider.getY() + Math.cos(angleRads)*(circleSize/2));
		int y2 = (int)(slider.getY() - Math.cos(angleRads)*(circleSize/2));
		int y3 = (int)(slider.getY() + Math.sin(angleRads)*(slider.getLength()) - Math.cos(angleRads)*(circleSize/2));
		int y4 = (int)(slider.getY() + Math.sin(angleRads)*(slider.getLength()) + Math.cos(angleRads)*(circleSize/2));

		int[] xPos = {x1, x2, x3, x4};
		int[] yPos = {y1, y2, y3, y4};

		// Draw a rectangle for the slider body
		g2d.fillPolygon(xPos, yPos, 4);

		// And two circles for either end of the slider
		drawFilledCircle(g2d, slider.getX(), slider.getY(), sliderEndColor, Color.BLACK);
		drawFilledCircle(g2d, slider.getX() + (int)(Math.cos(angleRads)*slider.getLength()), slider.getY() + (int)(Math.sin(angleRads)*slider.getLength()), sliderEndColor, Color.BLACK);

		// Check if the slider has started yet;
		// If so, draw a follow circle
		if(game.getCurrentMapTime() > slider.getTime()){
			// Increment the follow circle position
			slider.followCirclePos += (slider.getLength()+0.0)/(slider.getEndTime()-slider.getTime())*dT;
			// Draw the follow circle
			int followX = (int)(slider.getX() + Math.cos(angleRads)*slider.followCirclePos);
			int followY = (int)(slider.getY() + Math.sin(angleRads)*slider.followCirclePos);

			drawFilledCircle(g2d, followX, followY, followCircleColor, Color.BLACK);
		}
		// If not, draw an approach circle
		else{
			double approachCircleSize = circleSize + ((approachSize-circleSize)*((slider.getTime()-game.getCurrentMapTime()+0.0)/approachRate));
			g2d.setColor(approachColor);
			double approachX = slider.getX()-circleSize/2-(approachCircleSize-circleSize)/2;
			double approachY = slider.getY()-circleSize/2-(approachCircleSize-circleSize)/2;
			g2d.drawOval((int)approachX, (int)approachY, (int)approachCircleSize, (int)approachCircleSize);
		}
	}

	/**
	 * Draws a filled circle at the specific center points with a border
	 */
	private void drawFilledCircle(Graphics2D g2d, int x, int y, Color circleFillColor, Color circleBorderColor){
		g2d.setColor(circleFillColor);
		g2d.fillOval(x-circleSize/2, y-circleSize/2, circleSize, circleSize);
		g2d.setColor(circleBorderColor);
		g2d.drawOval(x-circleSize/2, y-circleSize/2, circleSize, circleSize);
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
			case 2:
				queueSlider((Slider)element);
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
	 * Adds a slider to the queue of sliders to be repeatedly drawn.
	 */
	private void queueSlider(Slider slider){
		sliders.add(slider);
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
			case 2:
				dequeueSlider((Slider)element);
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
	 * Removes a slider from the screen
	 */
	private void dequeueSlider(Slider slider){
		Iterator<Slider> iter = sliders.iterator();
		while(iter.hasNext()){
			Slider s = iter.next();
			if(s.equals(slider)){
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

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles drawing for a game of MyOsu!
 * @author campberobe1
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
	
	// The current score and health of the player;
	// These are accurate only to the last call of setScore/setHealth
	private int score = 0;
	private double health = 100;
	// The accuracy which the player currently has; different to accuracy above
	private double currentAccuracy = 0;

	// The list of circles to be drawn
	private java.util.List<Circle> circles = new ArrayList<Circle>();
	// The list of sliders to be drawn
	private java.util.List<Slider> sliders = new ArrayList<Slider>();

	// The queue of elements that should be disposed of because they have timed out
	private Queue<Element> disposalElements = new LinkedList<Element>();
	
	// Images for the various elements that need to be drawn
	private BufferedImage approachCircleImage;
	private BufferedImage circleImage;
	private BufferedImage outerCircleImage;
	private BufferedImage circleBorderImage;
	private BufferedImage sliderFollowCircleImage;
	
	private BufferedImage[] numberImage;
	private BufferedImage dotImage;
	private BufferedImage percentImage;
	
	private BufferedImage healthBarImage3;
	private BufferedImage healthBarImage2;
	private BufferedImage healthBarImage1;

	// The colors to draw the circles in
	private Color borderColor = Color.BLACK;
	private Color fillColor = Color.RED;
	private Color approachColor = Color.BLUE;
	private Color sliderColor = new Color(240, 240, 240, 100);
	private Color sliderEndColor = Color.CYAN;
    private Color followCircleColor = Color.GRAY; 
	private Color sliderLineColor = Color.WHITE;

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
		initImages();
	}
	
	/**
	 * Initialises the various images
	 */
	private void initImages(){
		try{
			approachCircleImage = ImageIO.read(new File(Options.SKIN_APPROACH_CIRCLE));
			circleImage = ImageIO.read(new File(Options.SKIN_HIT_CIRCLE));
			outerCircleImage = ImageIO.read(new File(Options.SKIN_HIT_CIRCLE_OUTER));
			circleBorderImage = ImageIO.read(new File(Options.SKIN_HIT_CIRCLE_BORDER));
			
			sliderFollowCircleImage = ImageIO.read(new File(Options.SKIN_SLIDER_FOLLOW_CIRCLE));
			
			dotImage = ImageIO.read(new File(Options.SKIN_DOT));
			percentImage = ImageIO.read(new File(Options.SKIN_PERCENT));
			
			healthBarImage3 = ImageIO.read(new File(Options.SKIN_HEALTHBAR_3));
			healthBarImage2 = ImageIO.read(new File(Options.SKIN_HEALTHBAR_2));
			healthBarImage1 = ImageIO.read(new File(Options.SKIN_HEALTHBAR_1));
			
			numberImage = new BufferedImage[10];
			for(int i = 0; i < 10; i++){
				numberImage[i] = ImageIO.read(new File(Options.SKIN_NUMBER_BASE + i + Options.SKIN_NUMBER_END));
			}
			
		}
		catch(IOException e){
			System.err.println("Could not read from skin image files! " + e);
		}
	}

	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		// Clear current graphics
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,this.getWidth(),this.getHeight());

		// Figure out how much time has elapsed since last time
		long currentTime = game.getCurrentMapTime();
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

		drawGUI(g2d);
		
		previousTime = currentTime;
	}

	public void drawGUI(Graphics2D g2d){
		// Draw the score
		String strScore = Integer.toString(score);
		// Start at the right
		int scoreYOffset = 15;
		int scoreXOffset = 15;
		int currentX = this.getWidth()-scoreXOffset;
		for(int i = strScore.length()-1; i >= 0; i--){
			// Well, it turns out that getting an integer value for one place in an integer
			// isn't that simple. First, we get the appropriate character from the string,
			// then get the toString of that, and finally parse that string as an int...
			BufferedImage img = numberImage[Integer.parseInt(((Character)(strScore.charAt(i))).toString())];
			g2d.drawImage(img, currentX-img.getWidth(), scoreYOffset, currentX, scoreYOffset+img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), this);
			currentX -= img.getWidth()+5;
		}
		
		// Draw the health
		{
			BufferedImage img;
			if(health >= 70) img = healthBarImage3;
			else if(health >= 40) img = healthBarImage2;
			else img = healthBarImage1;
		
			g2d.drawImage(img, 0, 0, (int)(health*12), img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), this);
		}
		
		// Draw the accuracy
		String strAcc = String.format("%.2f",currentAccuracy);
		int accYOffset = 80;
		int accXOffset = 15;
		currentX = this.getWidth()-accXOffset;
		
		// Draw a percent sign at the end
		g2d.drawImage(percentImage, currentX-percentImage.getWidth()/2, accYOffset, currentX, accYOffset+percentImage.getHeight()/2, 0, 0, percentImage.getWidth(), percentImage.getHeight(), this);
		currentX -= percentImage.getWidth()/2 + 5;
		for(int i = strAcc.length()-1; i >= 0; i--){
			BufferedImage img;
			// If it's a dot, obviously we can't parse it as an int
			if(strAcc.charAt(i) == '.') img = dotImage;
			else img = numberImage[Integer.parseInt(((Character)(strAcc.charAt(i))).toString())];
			
			g2d.drawImage(img, currentX-img.getWidth()/2, accYOffset, currentX, accYOffset+img.getHeight()/2, 0, 0, img.getWidth(), img.getHeight(), this);
			currentX -= img.getWidth()/2 + 5;
		}
	}

	/**
	 * Sets the score to draw
	 */
	public void setScore(int score){
		this.score = score;
	}

	/**
	 * Sets the health to draw
	 */
	public void setHealth(double health){
		this.health = health;
	}
	
	/**
	 * Sets the accuracy to draw
	 */
	public void setAccuracy(double currentAccuracy){
		this.currentAccuracy = currentAccuracy;
	}

	/**
	 * Draws a circle element along with its approach circle
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
			double approachX = circle.getX()-circleSize/2-(circle.approachCircleSize-circleSize)/2;
			double approachY = circle.getY()-circleSize/2-(circle.approachCircleSize-circleSize)/2;
			g2d.drawImage(approachCircleImage, (int)approachX, (int)approachY, (int)(approachX+circle.approachCircleSize), (int)(approachY+circle.approachCircleSize), 0, 0, approachCircleImage.getWidth(this), approachCircleImage.getHeight(this), this);
		}
		// Set a fadeout for the circle if need be
		else{
			// The time past when the circle was supposed to be clicked
			double pastTime = (1-circle.approachCircleSize/circleSize) * approachRate;
			// A float amount for the fadeout of the circle
			// This is 1 if the time past when the circle should've been clicked is the maximum accuracy threshold
			float fadeout = (float)(pastTime / accuracy);
			
			// Just in case of lag, etc., which might mean that the time for removing the circle
			// has already passed and the fadeout value might try to be over 1.
			if(fadeout > 1) fadeout = 1;
			
			g2d.setComposite(AlphaComposite.SrcOver.derive(1f - fadeout));
		}

		// Actually draw the circle
		drawCircleImage(g2d, circle.getX(), circle.getY());
		
		g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
	}
	
	/**
	 * Draws an actual circle (with border image, outer image and actual image)
	 * @param g2d The graphics to draw the circle on.
	 * @param x The center x position of the circle to draw.
	 * @param y The center y position of the circle to draw.
	 */
	private void drawCircleImage(Graphics2D g2d, int x, int y){
		g2d.drawImage(circleBorderImage, x-circleSize/2, y-circleSize/2, x+circleSize/2, y+circleSize/2, 0, 0, circleBorderImage.getWidth(), circleBorderImage.getHeight(), this); 
		g2d.drawImage(outerCircleImage, x-circleSize, y-circleSize, x+circleSize, y+circleSize, 0, 0, outerCircleImage.getWidth(), outerCircleImage.getHeight(), this); 
		g2d.drawImage(circleImage, x-circleSize/2, y-circleSize/2, x+circleSize/2, y+circleSize/2, 0, 0, circleImage.getWidth(), circleImage.getHeight(), this);
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

		int strokeSize = 4;
		// Draw a diagram to understand these points
		// Depends on the angle of the slider, so we can't just use drawRectangle
		int x1 = (int)(slider.getX() - Math.sin(slider.getAngle())*(circleSize/2 - strokeSize));
		int x2 = (int)(slider.getX() + Math.cos(slider.getAngle())*(slider.getLength()) - Math.sin(slider.getAngle())*(circleSize/2 - strokeSize));
		int x3 = (int)(slider.getX() + Math.sin(slider.getAngle())*(circleSize/2 - strokeSize));
		int x4 = (int)(slider.getX() + Math.cos(slider.getAngle())*(slider.getLength()) + Math.sin(slider.getAngle())*(circleSize/2 - strokeSize));

		int y1 = (int)(slider.getY() + Math.cos(slider.getAngle())*(circleSize/2 - strokeSize));
		int y2 = (int)(slider.getY() + Math.sin(slider.getAngle())*(slider.getLength()) + Math.cos(slider.getAngle())*(circleSize/2 - strokeSize));
		int y3 = (int)(slider.getY() - Math.cos(slider.getAngle())*(circleSize/2 - strokeSize));
		int y4 = (int)(slider.getY() + Math.sin(slider.getAngle())*(slider.getLength()) - Math.cos(slider.getAngle())*(circleSize/2 - strokeSize));

		// Draw two parallel lines for the slider body
		g2d.setColor(sliderLineColor);
		g2d.setStroke(new BasicStroke(strokeSize));
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x3, y3, x4, y4);

		// And two circles for either end of the slider
		double angle = slider.getAngle();
		int endCircleX = slider.getX() + (int)(slider.getLength()*Math.cos(angle));
		int endCircleY = slider.getY() + (int)(slider.getLength()*Math.sin(angle));
		
		drawCircleImage(g2d, slider.getX(), slider.getY());
		drawCircleImage(g2d, endCircleX, endCircleY);

		// Check if the slider has started yet;
		// If so, draw a follow circle
		if(game.getCurrentMapTime() > slider.getTime()){
			// Increment the follow circle position
			slider.followCirclePos += (slider.getLength()+0.0)/(slider.getEndTime()-slider.getTime())*dT;
			
			int followX = (int)(slider.getX() + Math.cos(slider.getAngle())*slider.followCirclePos);
			int followY = (int)(slider.getY() + Math.sin(slider.getAngle())*slider.followCirclePos);
			
			// Draw the follow circle
			g2d.drawImage(sliderFollowCircleImage, followX-circleSize/2, followY-circleSize/2, followX+circleSize/2, followY+circleSize/2, 0, 0, sliderFollowCircleImage.getWidth(), sliderFollowCircleImage.getHeight(), this);
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

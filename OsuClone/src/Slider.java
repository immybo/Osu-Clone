/**
 * A slider in a game of MyOsu; implements element.
 *
 * Sliders are a fairly common element. Similarly to circles, the
 * player must click the start point of the slider at around the
 * correct time. However, they then extend this by requiring the
 * player to drag towards the end of the slider, at the speed
 * specified by a circle moving along the slider.
 *
 * @author campberobe1
 *
 */
public class Slider implements Element {
	// The time at which the start of the slider should be clicked
	private int startTime;
	// The time at which the slider should reach the end
	private int endTime;
	// The length, in pixels, of the slider
	private int length;
	// The angle which the slider faces. This is
	// - 0 degrees for the end of the slider being directly to the right of its start
	// - 90 degrees for the end of the slider being directly down from its start, etc.
	private double angle;

	// The x and y positions of the start of the slider
	private int x;
	private int y;

	private int id;

	// The amount of 'points' accrued on the slider;
	// the amount of these points by the time the slider
	// ends determines the score gained from the slider.
	public int sliderPoints = 0;

	// The current position of the slider follow circle
	// relative to the slider. Should be between 0 and
	// the slider's length.
	public double followCirclePos = 0;

	/**
	 * Constructor; instantiates Slider.
	 * @param startTime The time at which the slider should be clicked.
	 * @param endTime The time at which the slider should reach the end.
	 * @param length The length, in pixels, of the slider.
	 * @param angle The angle, relative to left->right and clockwise, at which this slider should be.
	 * @param x The x position of the start of the slider.
	 * @param y The y position of the start of the slider.
	 */
	public Slider(int startTime, int endTime, int length, int angle, int x, int y){
		this.startTime = startTime;
		this.endTime = endTime;
		this.length = length;
		this.angle = angle;
		this.x = x;
		this.y = y;
	}

	@Override
	public int getElementType() {
		// Sliders will always have an element type of 2
		return 2;
	}

	@Override
	public int getTime() {
		// The time at which the start of the slider is supposed to be clicked
		return startTime;
	}

	/**
	 * Returns the time when the mouse should have been dragged to the end of the slider
	 */
	public int getEndTime(){
		return endTime;
	}

	/**
	 * Returns the angle of the slider in radians
	 */
	public double getAngle(){
		return angle/180*Math.PI;
	}

	/**
	 * Returns the length of the slider
	 */
	public int getLength(){
		return length;
	}

	@Override
	/**
	 * Returns the x position of the start of the slider
	 */
	public int getX() {
		return x;
	}

	@Override
	/**
	 * Returns the y position of the start of the slider
	 */
	public int getY() {
		return y;
	}

	@Override
	/**
	 * Returns the (hopefully) unique element ID of the slider
	 */
	public int getId() {
		return id;
	}

	@Override
	/**
	 * Sets the ID of the slider to the given parameter
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	/**
	 * Finds if this element is equal to another element.
	 * It is equal if and only if the element types and the given IDs are the same.
	 */
	public boolean equals(Element e) {
		if(e.getElementType() == 2 && e.getId() == this.id){
			return true;
		}
		return false;
	}

}

/**
 * A circle in a game of MyOsu; implements Element.
 *
 * Circles usually make up the majority of elements in a map, and can
 * be considered the most 'basic' element. The player must click them
 * at the appropriate time, which is indicated by an 'approach circle';
 * a larger circle which decreases in size until it is the same size as
 * the main circle when the player should click it.
 *
 * @author campberobe1
 */
public class Circle implements Element {
	private int startTime;
	private int x;
	private int y;
	private int id;

	// The size of the approach circle; not used within this class
	// But should be managed outside of it
	public double approachCircleSize = 0;

	/**
	 * Constructor; instantiates circle.
	 * @param startTime The time when this circle _should_ be pressed;
	 * @param x The center x position of this circle.
	 * @param y The center y position of this circle.
	 */
	public Circle(int startTime, int x, int y){
		this.startTime = startTime;
		this.x = x;
		this.y = y;
	}

	@Override
	public int getElementType() {
		// The element type is statically 1 for a circle.
		return 1;
	}

	@Override
	public int getTime() {
		return startTime;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public void setId(int id){
		this.id = id;
	}

	@Override
	public boolean equals(Element e){
		if(e.getElementType() == 1 && e.getId() == id) return true;
		return false;
	}
}

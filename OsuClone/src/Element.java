/**
 * Outlines get, set and equals methods for all element types in MyOsu!.
 *
 * There are 3 types of elements:
 * - Circles, on which the player must click at the appropriate time.
 * - Sliders, which the player must click and drag at the appropriate time and speed.
 * - Spinners, when the player must click and drag around the center of the spinner as fast as possible.
 *
 * In general, circles and then sliders are the most common elements, with
 * spinners making up a minority of elements in any given map.
 *
 * @author campberobe1
 */
public interface Element {
	/**
	 * Returns this element's type;
	 * 1 for a circle, 2 for a slider and 3 for a spinner
	 */
	public int getElementType();

	/**
	 * Returns this element's start time; the time at which it should be pressed first
	 */
	public int getTime();

	/**
	 * Returns this element's initial x position
	 */
	public int getX();

	/**
	 * Returns this element's initial y position
	 */
	public int getY();

	/**
	 * Returns the ID of this element
	 * No guarantees are made that IDs are unique,
	 * but they should be made unique.
	 */
	public int getId();

	/**
	 * Sets the ID of this element
	 * As the IDs of elements are not defined
	 * upon construction.
	 */
	public void setId(int id);

	/**
	 * Checks to see if this element is equal to another element
	 * Two elements are equal if
	 * - They are of the same type, and
	 * - They have the same ID
	 */
	public boolean equals(Element e);
}

/**
 * A break element in a game of MyOsu;
 * between the start and end times of a break, no health will be lost over time.
 * This is used because, often, there are times in the music where it doesn't
 * make sense to map anything, and they should be used to give the user a short
 * pause to rest. This is especially used after an intense moment.
 * 
 * @author Robert Campbell
 */
public class Break implements Element {
	private int startTime;
	private int endTime;
	private int id;
	
	/**
	 * A break only needs the start and end times,
	 * between which health will not be lost over time.
	 */
	public Break(int startTime, int endTime){
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public int getElementType() {
		return 3;
	}

	@Override
	public int getTime() {
		return startTime;
	}
	
	public int getEndTime(){
		return endTime;
	}

	@Override
	/**
	 * Doesn't apply for Break
	 */
	public int getX() {
		throw new UnsupportedOperationException();
	}

	@Override
	/**
	 * Doesn't apply for Break
	 */
	public int getY() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Element e) {
		if(e.getElementType() == getElementType() && 
			e.getTime() == getTime() &&
			((Break)e).getEndTime() == getEndTime()){
			
			return true;
		}
		return false;
	}

}

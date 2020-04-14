package gameboys.chewylokumlegend;

/**
 * @author Gameboys
 *
 */
public class TimedLevel extends Level {
	
	/**
	 * How much time allowed for the player, in seconds
	 */
	private int timeLimit;

	/**
	 * @param levelNum
	 * @param targetScore 
	 * @param timeLimit
	 * @param numSpecialSwaps 
	 */
	public TimedLevel(int levelNum, int targetScore, int timeLimit, int numSpecialSwaps) {
		super(levelNum,targetScore,numSpecialSwaps);
		setResourceAmount(timeLimit);
	}

	@Override
	public String getResourceName() {
		return "Time";
	}

	@Override
	public int getResourceAmount() {
		return timeLimit;
	}

	@Override
	public void setResourceAmount(int resourceAmount) {
		timeLimit = resourceAmount;
	}

}

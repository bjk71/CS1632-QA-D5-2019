import gov.nasa.jpf.annotation.FilterField;

import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Bean: Each bean is assigned a skill level from 0-9 on creation according to
 * a normal distribution with average SKILL_AVERAGE and standard deviation
 * SKILL_STDEV. A skill level of 9 means it always makes the "right" choices
 * (pun intended) when the machine is operating in skill mode ("skill" passed on
 * command line). That means the bean will always go right when a peg is
 * encountered, resulting it falling into slot 9. A skill evel of 0 means that
 * the bean will always go left, resulting it falling into slot 0. For the
 * in-between skill levels, the bean will first go right then left. For example,
 * for a skill level of 7, the bean will go right 7 times then go left twice.
 * 
 * <p>Skill levels are irrelevant when the machine operates in luck mode. In that
 * case, the bean will have a 50/50 chance of going right or left, regardless of
 * skill level.
 */

public class Bean {

	private static final double SKILL_AVERAGE = 4.5;	// MainPanel.SLOT_COUNT * 0.5;
	private static final double SKILL_STDEV = 1.5;		// Math.sqrt(SLOT_COUNT * 0.5 * (1 - 0.5));

	@FilterField private boolean isLuck;
	@FilterField private Random rand;
	@FilterField long skill;

	private int xpos;
	private int ypos;
	
	/**
	 * Constructor - creates a bean in either luck mode or skill mode.
	 * 
	 * @param isLuck	whether the bean is in luck mode
	 * @param rand      the random number generator
	 */
	Bean(boolean isLuck, Random rand) {
		this(isLuck, rand, -1, 0, 0);
	}

	/**
	 * Constructor allowing injection of all dependencies/state
	 * @param isLuck    whether the bean is in luck mode
	 * @param rand      the random number generator
	 * @param skill		the skill of the bean, or a negative number to autogenerate skill
	 * @param xpos		the starting x position of the bean, negative #s corrected to 0
	 * @param ypos		the starting y position of the bean, negative #s corrected to 0
	 */
	Bean(boolean isLuck, Random rand, long skill, int xpos, int ypos) {
		this.isLuck = isLuck;
		this.rand = rand;
		if (skill < 0) {
			this.skill = Math.round(rand.nextGaussian() * SKILL_STDEV + SKILL_AVERAGE);
		} else {
			this.skill = skill;
		}
		if (xpos < 0) {
			this.xpos = 0;
		} else {
			this.xpos = xpos;
		}
		if (ypos < 0) {
			this.ypos = 0;
		} else {
			this.ypos = ypos;
		}
		this.resetPosition();
	}

	/**
	 * @return the current x position of the bean
	 */
	public int getXpos() {
		return xpos;
	}

	/**
	 *
	 * @return the current y position of the bean
	 */
	public int getYpos() {
		return ypos;
	}

	/**
	 * determines if the Bean should go right or left and moves it to the new position
	 * makes decision based on luck vs skill mode
	 * in luck mode decides randomly
	 * in skill mode goes right until number of moves to the right >= skill
	 * @return true if it goes to the right, false if it goes to the left
	 */
	public boolean goRight() {
		boolean goesRight;
		if (isLuck) {
			goesRight = rand.nextBoolean();
		} else {
			goesRight = xpos < skill;
		}
		// ypos always goes up
		ypos += 1;
		// xpos either goes up if going right, stays the same if going left
		if (goesRight) {
			xpos += 1;
		}

		return goesRight;
	}

	/**
	 * resets the bean to the initial position (0,0)
	 */
	public void resetPosition() {
		xpos = 0;
		ypos = 0;
	}
}

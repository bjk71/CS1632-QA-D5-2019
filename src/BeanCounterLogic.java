import gov.nasa.jpf.annotation.FilterField;
import gov.nasa.jpf.vm.Verify;

import java.util.*;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>BeanCounterLogic: The bean counter, also known as a quincunx or the Galton
 * box, is a device for statistics experiments named after English scientist Sir
 * Francis Galton. It consists of an upright board with evenly spaced nails (or
 * pegs) in a triangular form. Each bean takes a random path and falls into a
 * slot.
 *
 * <p>Beans are dropped from the opening of the board. Every time a bean hits a
 * nail, it has a 50% chance of falling to the left or to the right. The piles
 * of beans are accumulated in the slots at the bottom of the board.
 * 
 * <p>This class implements the core logic of the machine. The MainPanel uses the
 * state inside BeanCounterLogic to display on the screen.
 * 
 * <p>Note that BeanCounterLogic uses a logical coordinate system to store the
 * positions of in-flight beans.For example, for a 4-slot machine: (0, 0) (1, 0)
 *                      (0, 0)
 *               (1, 0)        (1, 1)
 *        (2, 0)        (2, 1)        (2, 2)
 * [Slot0]       [Slot1]       [Slot2]      [Slot3]
 *   ^^ that's wrong! check courseweb for the right one ^^
 */

public class BeanCounterLogic {

	@FilterField private List<Bean> remainingBeans;
	@FilterField private List<Bean> flightBeans;
	@FilterField private List<List<Bean>> slots;
	private int slotCount;

	// No bean in that particular Y coordinate
	public static final int NO_BEAN_IN_YPOS = -1;

	/**
	 * Constructor - creates the bean counter logic object that implements the core
	 * logic. Our bean counter should start with a single bean at the top.
	 * 
	 * @param slotCount the number of slots in the machine
	 */
	BeanCounterLogic(int slotCount) {
		this(slotCount, null, null, null);
	}

	/**
	 * Constructor allowing injection of all dependencies/state
	 * @param slotCount number of slots in the machine, or null
	 * @param remainingBeans initial state for remaining beans, or null
	 * @param flightBeans initial state for beans in flight, or null
	 * @param slots initial state for slots, or null
	 *              null lists will start as empty lists, except individual slots being null, which is not handled
	 */
	BeanCounterLogic(int slotCount, List<Bean> remainingBeans, List<Bean> flightBeans, List<List<Bean>> slots) {
		// initialize global vars
		if (remainingBeans == null) {
			this.remainingBeans = new ArrayList<>();
		} else {
			this.remainingBeans = remainingBeans;
		}
		if (flightBeans == null) {
			this.flightBeans = new ArrayList<>();
		} else {
			this.flightBeans = flightBeans;
		}
		if (slots == null) {
			this.slots = new ArrayList<>();
			// initialize each slot
			for (int i = 0; i < slotCount; i++) {
				this.slots.add(new ArrayList<>());
			}
		} else {
			this.slots = slots;
		}

		this.slotCount = slotCount;
	}

	public List<List<Bean>> getSlots() {
		return this.slots;
	}

	public int inFlightBeanCount() {
		return flightBeans.size();
	}

	/**
	 * Returns the number of beans remaining that are waiting to get inserted.
	 * 
	 * @return number of beans remaining
	 */
	public int getRemainingBeanCount() {
		return remainingBeans.size();
	}

	/**
	 * Returns the x-coordinate for the in-flight bean at the provided y-coordinate.
	 * 
	 * @param yPos the y-coordinate in which to look for the in-flight bean
	 * @return the x-coordinate of the in-flight bean
	 */
	public int getInFlightBeanXPos(int yPos) {
		int x = NO_BEAN_IN_YPOS;
		for (Bean bean : flightBeans) {
			if (bean.getYpos() == yPos) {
				x = bean.getXpos();
			}
		}
		return x;
	}

	/**
	 * Returns the number of beans in the ith slot.
	 * 
	 * @param i index of slot
	 * @return number of beans in slot
	 */
	public int getSlotBeanCount(int i) {
		return slots.get(i).size();
	}
	
	/**
	 * Returns the total nuber of beans in all slots
	 * 
	 * @return number of beans in all slots
	 */
	private int totalSlotBeanCount() {
		int total = 0;
		for (List<Bean> slot : slots) {
			total += slot.size();
		}
		return total;
	}
	
	/**
	 * Returns the total number of beans in all slots after mmultiplying the
	 * slot number by the the amount of beans in the slot to create a weighted
	 * number to represent the slot
	 * 
	 * @return weighted number of beans in slot
	 */
	private double getWeightedSlotBeanCount() {
		double total = 0.0;
		int i = 0;
		for (List<Bean> slot : slots) {
			total += slot.size() * i;
			i += 1;
		}
		return total;
	}
	
	/**
	 * NOTE * I think we can use the totalSlotBeanCount and weightedSlotBeanCount
	 * 		  for this method
	 * Calculates the average slot bean count.
	 * 
	 * @return average of all slot bean counts
	 */
	public double getAverageSlotBeanCount(BeanCounterLogic logic) {
		// TODO: Implement
		int totalSlotBeans = logic.totalSlotBeanCount();
		double weightedSlotBeanCount = logic.getWeightedSlotBeanCount();
				
		if (totalSlotBeans > 0) {
			return weightedSlotBeanCount / totalSlotBeans;
		}
				
		return 0.0;
	}
	
	public double getAverageSlotBeanCount() {
		return getAverageSlotBeanCount(this);
	}

	/**
	 * Removes the lower half of all beans currently in slots, keeping only the
	 * upper half.
	 */
	public void upperHalf() {
		int beanCount = totalSlotBeanCount();
		if (beanCount == 0) {
			// if there are no beans in the slots do nothing
			return;
		}
		int numToRemove = beanCount / 2;
		int currentSlotNumber = 0;

		while (numToRemove > 0) {
			numToRemove = removeFromSlot(numToRemove, currentSlotNumber);
			currentSlotNumber++;
		}
	}

	/**
	 * Removes the upper half of all beans currently in slots, keeping only the
	 * lower half.
	 */
	public void lowerHalf() {
		int beanCount = totalSlotBeanCount();
		if (beanCount == 0) {
			// if there are no beans in the slots do nothing
			return;
		}
		int numToRemove = beanCount / 2;
		int currentSlotNumber = slotCount - 1;

		while (numToRemove > 0) {
			numToRemove = removeFromSlot(numToRemove, currentSlotNumber);
			currentSlotNumber--;
		}
	}

	/**
	 * removes up to numToRemove beans from the slot at index currentSlotNumber
	 * @param numToRemove maximum number of beans to remove
	 * @param currentSlotNumber index of the slot to remove from
	 * @return the new value for numToRemove, aka the original value minus the # removed
	 */
	private int removeFromSlot(int numToRemove, int currentSlotNumber) {
		List<Bean> currentSlot = slots.get(currentSlotNumber);
		// if the number of beans in the current slot is <= than the number left to be
		// removed then remove all in the current slot
		if (currentSlot.size() <= numToRemove) {
			numToRemove -= currentSlot.size();
			currentSlot.clear();
		} else {
			while (numToRemove > 0) {
				currentSlot.remove(0);
				numToRemove--;
			}
		}
		return numToRemove;
	}

	/**
	 * A hard reset. Initializes the machine with the passed beans. The machine
	 * starts with one bean at the top.
	 */
	public void reset(Bean[] beans) {
		// clear all lists of beans
		remainingBeans.clear();
		flightBeans.clear();
		for (List<Bean> slot : slots) {
			slot.clear();
		}

		// add given beans to remainingBeans
		remainingBeans.addAll(Arrays.asList(beans));
		// if there are any beans put one at the top of the machine
		if (remainingBeans.size() > 0) {
			for (Bean bean : remainingBeans) {
				bean.resetPosition();
			}
			Bean first = remainingBeans.remove(0);
			flightBeans.add(first);
		}
	}

	/**
	 * Repeats the experiment by scooping up all beans in the slots and all beans
	 * in-flight and adding them into the pool of remaining beans. As in the
	 * beginning, the machine starts with one bean at the top.
	 */
	public void repeat() {
		List<Bean> beans = new ArrayList<>();
		beans.addAll(remainingBeans);
		beans.addAll(flightBeans);
		for (List<Bean> slot : slots) {
			beans.addAll(slot);
		}
		reset(beans.toArray(new Bean[]{}));
	}

	/**
	 * Advances the machine one step. All the in-flight beans fall down one step to
	 * the next peg. A new bean is inserted into the top of the machine if there are
	 * beans remaining.
	 * 
	 * @return whether there has been any status change. If there is no change, that
	 *         means the machine is finished.
	 */
	public boolean advanceStep() {
		if (remainingBeans.size() > 0 || flightBeans.size() > 0) {
			List<Bean> removeFromFlight = new ArrayList<>();
			for (Bean bean : flightBeans) {
				if (bean.getYpos() >= slotCount - 1) {
					slots.get(bean.getXpos()).add(bean);
					removeFromFlight.add(bean);
				} else {
					bean.goRight();
				}
			}
			flightBeans.removeAll(removeFromFlight);

			// if there are any beans put one at the top of the machine
			if (remainingBeans.size() > 0) {
				Bean first = remainingBeans.remove(0);
				flightBeans.add(first);
			}
			return true;
		} else {
			return false;
		}
	}

	public static void showUsage() {
		System.out.println("Usage: java BeanCounterLogic <number of beans> <luck | skill>");
		System.out.println("Example: java BeanCounterLogic 400 luck");
	}

	/**
	 * Auxiliary main method. Runs the machine in text mode with no bells and
	 * whistles. It simply shows the slot bean count at the end. Also, when the
	 * string "test" is passed to args[0], the program enters test mode. In test
	 * mode, the Java Pathfinder model checking tool checks the logic of the machine
	 * for a small number of beans and slots.
	 * 
	 * @param args args[0] is an integer bean count, args[1] is a string which is
	 *             either luck or skill.
	 */
	public static void main(String[] args) {
		boolean luck;
		int beanCount = 0;
		int slotCount = 0;

		if (args.length == 1 && args[0].equals("test")) {
			beanCount = Verify.getInt(0, 3);
			slotCount = Verify.getInt(1, 5);
			
			
			// Create the internal logic
			BeanCounterLogic logic = new BeanCounterLogic(slotCount);

			// Checks invariant property: before reset logic should have no beans
			assert 0 == (logic.getRemainingBeanCount() + logic.totalSlotBeanCount() 
					+ logic.inFlightBeanCount());

			// Create the beans (in luck mode)
			Bean[] beans = new Bean[beanCount];
			for (int i = 0; i < beanCount; i++) {
				beans[i] = new Bean(true, new Random());
			}
			// Initialize the logic with the beans
			logic.reset(beans);

			while (true) {
				if (!logic.advanceStep()) {
					break;
				}

				// Checks invariant property: all positions of in-flight beans have to be
				// legal positions in the logical coordinate system.
				for (int yPos = 0; yPos < slotCount; yPos++) {
					int xPos = logic.getInFlightBeanXPos(yPos);
					assert xPos == BeanCounterLogic.NO_BEAN_IN_YPOS || (xPos >= 0 && xPos <= yPos);
				}
				// Checks invariant property: no position should ever be higher than slotcount
				for (Bean b : beans) {
					assert b.getXpos() <= b.getYpos() && b.getYpos() < slotCount;
				}

				// Check invariant property: the sum of remaining, in-flight, and in-slot
				// beans always have to be equal to beanCount
				assert beanCount == (logic.getRemainingBeanCount() + logic.totalSlotBeanCount()
						+ logic.inFlightBeanCount());
				
			}
			// Check invariant property: when the machine finishes,
			// 1. There should be no remaining beans.
			// 2. There should be no beans in-flight.
			// 3. The number of in-slot beans should be equal to beanCount.
			assert 0 == logic.getRemainingBeanCount();
			assert 0 == logic.inFlightBeanCount();
			assert beanCount == logic.totalSlotBeanCount();

			return;
		}

		if (args.length != 2) {
			showUsage();
			return;
		}

		try {
			beanCount = Integer.parseInt(args[0]);
		} catch (NumberFormatException ne) {
			showUsage();
			return;
		}
		if (beanCount < 0) {
			showUsage();
			return;
		}

		if (args[1].equals("luck")) {
			luck = true;
		} else if (args[1].equals("skill")) {
			luck = false;
		} else {
			showUsage();
			return;
		}
		
		slotCount = 10;

		// Create the internal logic
		BeanCounterLogic logic = new BeanCounterLogic(slotCount);
		// Create the beans (in luck mode)
		Bean[] beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = new Bean(luck, new Random());
		}
		// Initialize the logic with the beans
		logic.reset(beans);
					
		// Perform the experiment
		while (true) {
			if (!logic.advanceStep()) {
				break;
			}
		}
		// display experimental results
		System.out.println("Slot bean counts:");
		for (int i = 0; i < slotCount; i++) {
			System.out.print(logic.getSlotBeanCount(i) + " ");
		}
		System.out.println("");
	}
}

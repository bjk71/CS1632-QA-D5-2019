import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeanCounterLogicTest {
	// Be sure to mock your beans!

	BeanCounterLogic logic;

	@Mock Bean bean1;
	@Mock Bean bean2;
	@Mock Random rand;

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PrintStream originalOut;

	/**
	 * initializes global vars
	 */
	@Before
	public void setUp() throws UnsupportedEncodingException {
		logic = new BeanCounterLogic(10);

		bean1 = mock(Bean.class);
		bean2 = mock(Bean.class);
		rand = mock(Random.class);

		originalOut = System.out;
		System.setOut(new PrintStream(out, false, Charset.defaultCharset().toString()));
	}

	@After
	public void tearDown() {
		System.setOut(originalOut);
	}

	// Tests for getRemainingBeanCount
	@Test
	public void noBeans() {
		assertEquals(0, logic.getRemainingBeanCount());
	}

	@Test
	public void oneBean() {
		logic.reset(new Bean[] { bean1 });
		assertEquals(0, logic.getRemainingBeanCount());
	}

	@Test
	public void fiveBean() {
		logic.reset(new Bean[] { bean1, bean1, bean1, bean1, bean1 });
		assertEquals(4, logic.getRemainingBeanCount());
	}
	
	// Tests for getInFlightBeanXPos
	@Test
	public void firstBean() {
		logic.reset(new Bean[] { bean1 });
		assertEquals(0, logic.getInFlightBeanXPos(0));
	}

	@Test
	public void noBean() {
		logic.reset(new Bean[] { bean1 });
		assertEquals(BeanCounterLogic.NO_BEAN_IN_YPOS, logic.getInFlightBeanXPos(1));
	}

	@Test
	public void secondBean() {
		List<Bean> flightBeans = new ArrayList<>();
		flightBeans.add(bean1);
		when(bean1.getXpos()).thenReturn(2);
		when(bean1.getYpos()).thenReturn(2);
		logic = new BeanCounterLogic(10, null, flightBeans, null);

		assertEquals(2, logic.getInFlightBeanXPos(2));
	}

	// Tests for inFlightBeanCount
	@Test
	public void oneFlight() {
		List<Bean> flightBeans = new ArrayList<>();
		flightBeans.add(bean1);
		logic = new BeanCounterLogic(10, null, flightBeans, null);

		assertEquals(1, logic.inFlightBeanCount());
	}
	
	// Tests for getSlotBeanCount
	
	@Test
	public void zeroSlot() {
		assertEquals(0, logic.getSlotBeanCount(0));
	}
	
	@Test
	public void oneSlot() {
		//logic.reset(new Bean[] { bean1 };
		logic.getSlots().get(0).add(bean1);
		assertEquals(1, logic.getSlotBeanCount(0));
	}
	
	@Test
	public void twoSlot() {
		//logic.reset(new Bean[] { bean1 });
		logic.getSlots().get(0).add(bean1);
		logic.getSlots().get(0).add(bean2);
		
		assertEquals(2,logic.getSlotBeanCount(0));
	}
	
	
	// Tests for getAverageSlotBeanCount
	@Test
	public void averageSlot() {
		int slotCount = 10;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		
		assertEquals(4.5, logic.getAverageSlotBeanCount(), 0.001);
	}
	
	@Test
	public void averagesNoSlot() {
		int slotCount = 0;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		
		assertEquals(0.0, logic.getAverageSlotBeanCount(), 0.001);
	}
	
	
	// Tests for upperHalf
	@Test
	public void noBeansUpper() {
		int slotCount = 10;
		ArrayList<List<Bean>> slots = new ArrayList<>();
		for (int i = 0; i < slotCount; i++) {
			slots.add(new ArrayList<>());
		}
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.upperHalf();
		// assert that each slot is empty
		for (int i = 0; i < slotCount; i++) {
			assertEquals(0, slots.get(i).size());
		}
	}

	@Test
	public void removesLowerHalf() {
		int slotCount = 10;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.upperHalf();
		// assert that each slot is empty
		for (int i = 0; i < slotCount / 2; i++) {
			assertEquals(0, slots.get(i).size());
		}
	}

	@Test
	public void keepsUpperHalf() {
		int slotCount = 10;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.upperHalf();
		// assert that each slot is empty
		for (int i = slotCount / 2; i < slotCount; i++) {
			assertEquals(1, slots.get(i).size());
		}
	}

	@Test
	public void keepsLastBean() {
		int slotCount = 10;
		// make slots and add one bean
		ArrayList<List<Bean>> slots = new ArrayList<>();
		for (int i = 0; i < slotCount; i++) {
			slots.add(new ArrayList<>());
		}
		slots.get(0).add(bean1);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.upperHalf();
		assertEquals(1, slots.get(0).size());
	}
	
	// Tests for lowerHalf

	@Test
	public void noBeansLower() {
		int slotCount = 10;
		ArrayList<List<Bean>> slots = new ArrayList<>();
		for (int i = 0; i < slotCount; i++) {
			slots.add(new ArrayList<>());
		}
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.lowerHalf();
		// assert that each slot is empty
		for (int i = 0; i < slotCount; i++) {
			assertEquals(0, slots.get(i).size());
		}
	}

	@Test
	public void keepsLowerHalf() {
		int slotCount = 10;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.lowerHalf();
		// assert that each slot is empty
		for (int i = 0; i < slotCount / 2; i++) {
			assertEquals(1, slots.get(i).size());
		}
	}

	@Test
	public void removesUpperHalf() {
		int slotCount = 10;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.lowerHalf();
		// assert that each slot is empty
		for (int i = slotCount / 2; i < slotCount; i++) {
			assertEquals(0, slots.get(i).size());
		}
	}


	private ArrayList<List<Bean>> setupSlots(int slotCount) {
		ArrayList<List<Bean>> slots = new ArrayList<>();
		for (int i = 0; i < slotCount; i++) {
			slots.add(new ArrayList<>());
			slots.get(i).add(bean1);
		}
		return slots;
	}
	
	// Tests for reset
	
	
	// Tests for repeat
	@Test
	public void repeatBeanAtTop() {
		ArrayList<Bean> flightBeans = new ArrayList<>();
		flightBeans.add(bean1);
		logic = new BeanCounterLogic(10, null, flightBeans, null);
		logic.repeat();
		assertEquals(1, flightBeans.size());
	}

	@Test
	public void flightBeansScooped() {
		ArrayList<Bean> remainingBeans = new ArrayList<>();
		ArrayList<Bean> flightBeans = new ArrayList<>();
		flightBeans.add(bean1);
		flightBeans.add(bean2);
		logic = new BeanCounterLogic(10, remainingBeans, flightBeans, null);
		logic.repeat();
		assertEquals(1, remainingBeans.size());
	}

	@Test
	public void repeatScoopsRemaining() {
		ArrayList<Bean> remainingBeans = new ArrayList<>();
		remainingBeans.add(bean1);
		remainingBeans.add(bean2);
		logic = new BeanCounterLogic(10, remainingBeans, null, null);
		logic.repeat();
		assertEquals(1, remainingBeans.size());
	}

	@Test
	public void allSlotsScooped() {
		int slotCount = 10;
		// make slots and add a bean to each one
		ArrayList<List<Bean>> slots = setupSlots(slotCount);
		logic = new BeanCounterLogic(slotCount, null, null, slots);
		logic.repeat();
		// assert that each slot is empty
		for (int i = 0; i < slotCount; i++) {
			assertEquals(0, slots.get(i).size());
		}
	}
	
	// Tests for advanceStep
	@Test
	public void advanceDoesSomething() {
		logic.reset(new Bean[] { bean1, bean2 });
		assertTrue(logic.advanceStep());
	}


	@Test
	public void advanceChecksStatus() {
		logic.reset(new Bean[] {});
		assertFalse(logic.advanceStep());
	}
	
	// Tests for showUsage
	@Test
	public void testShown() throws UnsupportedEncodingException {
		BeanCounterLogic.showUsage();
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Usage: java BeanCounterLogic <number of beans> <luck | skill>"));
	}

	
	
	// Tests for main
	
	@Test
	public void validArgs() throws UnsupportedEncodingException {
		String[] args = new String[] { "500", "luck" };
		
		BeanCounterLogic.main(args);
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Slot bean counts:"));
	}
	
	@Test
	public void otherValidArgs() throws UnsupportedEncodingException {
		String[] args = new String[] { "500", "skill" };
		
		BeanCounterLogic.main(args);
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Slot bean counts:"));
	}
	
	@Test
	public void reverseArgs() throws UnsupportedEncodingException {
		String[] args = new String[] { "luck", "500" };
		
		BeanCounterLogic.main(args);
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Usage: java BeanCounterLogic <number of beans> <luck | skill>"));
	}
	
	@Test
	public void tooManyArgs() throws UnsupportedEncodingException {
		String[] args = new String[] { "500", "luck", "yas" };
		
		BeanCounterLogic.main(args);
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Usage: java BeanCounterLogic <number of beans> <luck | skill>"));
	}
	
	@Test
	public void invalidBeansArgs() throws UnsupportedEncodingException {
		String[] args = new String[] { "-300", "luck" };
		
		BeanCounterLogic.main(args);
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Usage: java BeanCounterLogic <number of beans> <luck | skill>"));
	}
	
	@Test
	public void wrongModeArgs() throws UnsupportedEncodingException {
		String[] args = new String[] { "500", "yo" };
		
		BeanCounterLogic.main(args);
		assertTrue(out.toString(Charset.defaultCharset().toString())
				.contains("Usage: java BeanCounterLogic <number of beans> <luck | skill>"));
	}

	

}

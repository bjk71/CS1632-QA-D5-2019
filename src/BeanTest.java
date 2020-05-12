import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Random;

public class BeanTest {
	// Be sure to mock Random if you don't want randomness during testing!

	private Bean luckBean;
	private Bean skillBean;

	@Mock Random rand;

	/**
	 * initialize global vars
	 */
	@Before
	public void setUp() {
		rand = mock(Random.class);

		luckBean = new Bean(true, rand);
	}

	// test setters/getters

	//TODO test getters after calling goRight()

	// test goRight

	@Test
	public void goesLeftLuck() {
		when(rand.nextBoolean()).thenReturn(false);
		assertFalse(luckBean.goRight());
	}

	@Test
	public void goesRightLuck() {
		when(rand.nextBoolean()).thenReturn(true);
		assertTrue(luckBean.goRight());
	}

	@Test
	public void goesRightSkill() {
		when(rand.nextGaussian()).thenReturn(0.0);
		skillBean = new Bean(false, rand);
		assertTrue(skillBean.goRight());
	}

	@Test
	public void goesLeftSkill() {
		when(rand.nextGaussian()).thenReturn(-4.0);
		skillBean = new Bean(false, rand);
		assertFalse(skillBean.goRight());
	}

	@Test
	public void goesLeftSeventhTime() {
		// gives skill == 6
		when(rand.nextGaussian()).thenReturn(1.0);
		skillBean = new Bean(false, rand);
		// go right six times
		for (int i = 0; i < 6; i++) {
			skillBean.goRight();
		}
		// seventh call should go left
		assertFalse(skillBean.goRight());
	}

	@Test
	public void correctThirdPos() {
		// gives skill == 6
		when(rand.nextGaussian()).thenReturn(1.0);
		skillBean = new Bean(false, rand);
		// go right three times
		skillBean.goRight();
		skillBean.goRight();
		skillBean.goRight();

		assertEquals(3, skillBean.getYpos());
		assertEquals(3, skillBean.getXpos());
	}

	@Test
	public void resetMovement() {
		skillBean = new Bean(false, rand);
		skillBean.goRight();
		skillBean.goRight();
		skillBean.goRight();
		skillBean.resetPosition();
		assertEquals(0, skillBean.getXpos());
		assertEquals(0, skillBean.getYpos());
	}
}
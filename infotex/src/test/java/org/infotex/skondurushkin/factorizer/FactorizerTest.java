package org.infotex.skondurushkin.factorizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infotex.skondurushkin.factorizer.Factor;
import org.infotex.skondurushkin.factorizer.Factorizer;
import org.junit.Test;

import junit.framework.TestCase;

public class FactorizerTest extends TestCase {

	static Map<Long, List<Factor>> expectedResults = new HashMap<>();
	static {
		expectedResults.put(2L, Arrays.asList(new Factor(2,1)));
		expectedResults.put(3L, Arrays.asList(new Factor(3,1)));
		expectedResults.put(5L, Arrays.asList(new Factor(5,1)));
		expectedResults.put(1025L, Arrays.asList(new Factor(5,2), new Factor(41,1)));
		expectedResults.put(123456789000000L, 
				Arrays.asList(
						new Factor(2,6),
						new Factor(3,2),
						new Factor(5,6),
						new Factor(3607,1),
						new Factor(3803,1)));
	}
	
	@Test
	public void testFactorizer() {
		for (Map.Entry<Long, List<Factor>> entry : expectedResults.entrySet()) {
			List<Factor> result = Factorizer.factorize(entry.getKey());
			System.out.println(entry.getKey() + "=" + result.toString());
			
			assertEquals( entry.getValue(), result);
			assertEquals( entry.getKey().longValue(), Factorizer.restore(result));
		}
	}

}

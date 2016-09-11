package org.infotex.skondurushkin.factorizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Simple factorization algorithm implemented as <code>Iterable<></code> with interruptible iterator
 * 
 * @author skondurushkin
 *
 */
public class Factorizer implements Iterable<Factor> {

	// original value
	long value;
	
	// result of the last division by factor
    long current;
    
    // current factor.
    long factor = 1;

    // flag telling that the calculation must stop
    volatile boolean cancel = false;
    
    
    public Factorizer(long value) {
    	this.value = value;
    	if (value < 0)
    		value = -value;
        this.current = value;
    }

    /**
     * Cancels the calculation only if it hasn't finished yet
     * 
     * @return true if the cancellation was accepted
     */
    public boolean cancel() {
        // client want us to stop
    	if (canContinue()) {
    		// and we are still executing
    		return this.cancel = true;
    	}
    	return false;
    }

    /**
     * 
     * @return true if the calculation has been effectively cancelled
     */
    public boolean isCancelled() {
    	return this.cancel;
    }
    /**
     * 
     * @return the original value this org.infotex.skondurushkin.factorizer is applied to.
     */
    public long getValue() {
    	return this.value;
    }

    /**
     * Tells whether the calculation can proceed.
     * 
     * @return boolean result
     */
    boolean canContinue() {
    	return current > factor && !this.cancel;
    }
    /**
     * This is the core part of factorization algorithm. Calculates next factor based on its current value
     * Specific to this implementation is that it can be interrupted at any time from other thread.
     * Returns new instance of <code>class Factor</code> containing the found factor and its power or null
     * if the calculation was cancel by <code>cancel()</code>
     *  
     * @return next factor or null if the calculation was cancel
     */
    private Factor getNextFactor() {
        int power = 0;
        while (this.canContinue()) {
            long rem = current % (++factor); // here is the only place where factor is incremented.
            if (rem == 0) {
                // Factor found. Now get its power. 
            	// NOTE: canContinue is ignored at this stage
                do {
                    ++power;
                    current /= factor;
                } while ((rem = current % factor) == 0);
                return new Factor(factor, power);
            }
        }
        return null;
    }

    /**
     * This algorithm is iterative by its nature so it can naturally be invoked as iterator
     */
    @Override
    public Iterator<Factor> iterator() {
        return new Iterator<Factor>() {

            @Override
            public boolean hasNext() {
                return canContinue();
            }

            @Override
            public Factor next() {
                return getNextFactor();
            }

        };
    }

    // Utility methods (primarily for testing)
    
    /**
     * 
     * @param value long value to factorize
     * @return List of factors
     */
    public static List<Factor> factorize(long value) {
        List<Factor> ret = new ArrayList<>();
    	// Normalize the value first and process values 0 and 1 
        // as special
        if (value < 0) {
        	ret.add(new Factor(-1,1));
        	value = -value;
        } else if (value == 1) {
        	ret.add(new Factor(1,1));
        } else if (value == 0) {
        	ret.add(new Factor(0,1));
        }
        	
        if (value > 1) {
	        Factorizer fzer = new Factorizer(value);
	        fzer.forEach(ret::add);
        }
        return ret;
    }
    
    /**
     * Restores original value from passed collection of factors.
     * @param factors Collection of factors
     * @return long value restore from factors
     */
	public static long restore(Collection<Factor> factors) {
		if (factors == null || factors.isEmpty())
			return 0;
		return factors.stream().mapToLong(Factor::restore).reduce(1, (a,b)-> (a*b));
	}
}

package org.infotex.skondurushkin.factorizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.infotex.skondurushkin.Helpers;

public class FactorizerJob implements Callable<List<Factor>> {
	Factorizer fzr;
	volatile List<Factor> result = null;

	public FactorizerJob(long id) {
		this.fzr = new Factorizer(id);
	}

	public long getId() {
		return fzr.getValue();
	}

	public List<Factor> getResult() {
		return this.result;
	}

	public void cancel() {
		this.fzr.cancel();
	}

	public boolean isDone() {
		return this.result != null;
	}
	
	@Override
	public List<Factor> call() throws Exception {
		List<Factor> ret = new ArrayList<>();
		long value = fzr.getValue();
		if (value < 0) {
			ret.add(new Factor(-1, 1));
			value = -value;
		} else if (value == 1) {
			ret.add(new Factor(1, 1));
		} else if (value == 0) {
			ret.add(new Factor(0, 1));
		}
		
		if (value > 1) {
			fzr.forEach(ret::add);
		}
		return ret;
	}
	
	public FactorizerJob go() {
		if (result == null) {
			synchronized(fzr) {
				if (result == null) {
					try {
						result = call();
					} catch (Exception e) {
						Helpers.rethrowAsRunimeException(e);
					}
				}
			}
		}
		return this;
	}

	static ConcurrentMap<Long, FactorizerJob> jobs = new ConcurrentHashMap<>();
	
	public static boolean exists(long key) {
		return jobs.containsKey(key);
	}
	public static FactorizerJob lookup(long key) {
		return jobs.get(key);
	}
	public static FactorizerJob create(long key) {
		return jobs.computeIfAbsent(key, FactorizerJob::new);
	}

	public static boolean remove(long key) {
		FactorizerJob job = jobs.remove(key);
		if (job != null) {
			job.cancel();
			return true;
		}
		return false;
	}
}

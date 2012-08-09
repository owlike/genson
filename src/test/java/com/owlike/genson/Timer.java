package com.owlike.genson;

public class Timer {
	private long start;
	private long end;
	private long sum;
	private int cnt;
	private boolean paused;

	public Timer start() {
		end = 0;
		sum = 0;
		cnt = 0;
		paused = false;
		start = System.currentTimeMillis();
		return this;
	}

	public Timer stop() {
		end = System.currentTimeMillis();

		return this;
	}

	public Timer pause() {
		paused = true;
		end = System.currentTimeMillis();
		return this;
	}

	public Timer unpause() {
		if (paused) {
			long t = System.currentTimeMillis();
			start = t - (end - start);
			paused = false;
		}
		return this;
	}

	public Timer cumulate() {
		cnt++;
		end = System.currentTimeMillis();
		sum += end - start;
		start = end;

		return this;
	}

	public String printMS() {
		if (cnt > 0)
			return ((double) sum / cnt) + " ms";

		return end - start + " ms";
	}

	public String printS() {
		if (cnt > 0)
			return ((double) sum / (cnt * 1000)) + " ms";

		return ((double) (end - start) / 1000) + " s";
	}
}
package org.jessifin.main;

import java.util.ArrayList;

public class Timer {
	
	private static ArrayList<Timer> timers = new ArrayList<Timer>();
	
	public static void updateAll(int timePassed) {
		for(int t = 0; t < timers.size(); t++) {
			timers.get(t).update(timePassed);
		}
	}
	
	public final int totalMillis;
	public int millisPlayed;
	
	public Timer(int totalMillis) {
		this.totalMillis = totalMillis;
		timers.add(this);
	}
	
	private void update(int millisPassed) {
		millisPlayed += millisPassed;
	}
	
	public boolean poll() {
		if(millisPlayed >= totalMillis) {
			millisPlayed -= totalMillis;
			return true;
		} else {
			return false;
		}
	}
}

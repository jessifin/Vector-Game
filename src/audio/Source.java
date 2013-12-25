package audio;

import javax.vecmath.Vector3f;

public class Source {
	
	public final int id;
	
	public float millisPlayed;
	public Buffer currentBuffer;
	public Vector3f pos = new Vector3f(0,0,0);

	private boolean isPlaying = false;
	private boolean justStopped = false;
	
	public Source(int id) {
		this.id = id;
	}
	
	public void update(int timePassed) {
		millisPlayed+=timePassed;
		Audio.updateSource(this);
		if(millisPlayed/1000f >= currentBuffer.duration && !currentBuffer.looping) {
			isPlaying = false;
			justStopped = true;
			millisPlayed = 0;
			currentBuffer.isActive = false;
		}
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public boolean hasStopped() {
		if(justStopped) {
			justStopped = false;
			return true;
		} else {
			return false;
		}
	}
	
	public void setPlaying() {
		isPlaying = true;
	}
} 

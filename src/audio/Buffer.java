package audio;

public class Buffer {
	
	public final int id;
	public final String name;
	public final float duration;
	
	public float pitch = 1, gain = 1;
	public boolean looping = false;
	public boolean isMusic = false;
	boolean isActive = true;
	
	public Buffer(int id, float duration, String name) {
		this.id = id;
		this.duration = duration;
		this.name = name;
	}
	
	public Buffer(Buffer buffer) {
		this.id = buffer.id;
		this.name = buffer.name;
		this.duration = buffer.duration;
		this.pitch = new Float(buffer.pitch);
		this.gain = new Float(buffer.pitch);
		this.looping = new Boolean(buffer.looping);
		this.isMusic = new Boolean(buffer.isMusic);
	}
	
	public boolean isActive() {
		return isActive;
	}
}

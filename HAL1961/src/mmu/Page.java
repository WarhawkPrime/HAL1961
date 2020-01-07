package mmu;

public class Page {

	private boolean referencedBit;
	private boolean modifiedBit;  // Bei schreibendem Zugriff gesetzt, wenn true muss Page in Speicher zurück geschrieben werden
	private boolean presentBit;  // Bei Zugriff auf eine Page bei der dieses Bit 0 ist entsteht ein Page Fault (PF)
	private int 	frameNumber;
	private short[] segments = null;

	public Page (int frameNumber) {
		setSegments(new short[64]);	//64 short register
		this.referencedBit = false;
		this.modifiedBit = false;
		this.presentBit = false;
		this.frameNumber = frameNumber;  
	}

	public boolean isReferenced() {
		return this.referencedBit;
	}
	
	public  boolean isModified() {
		return this.modifiedBit;
	}
	 
	public boolean isPresent() {
		return this.presentBit;
	}

	public void setPresentBit(boolean b) {
		this.presentBit = b;
	}

	private void setSegments(short [] segments) {
		this.segments = segments;
		this.modifiedBit = true;
		this.referencedBit = true;
	}
	
	// Returns offset-specified data segment
	public short getSegmentByOffset(int offset) {
		if(offset < 0 || offset > 63) {
			return -1;
		}
		else {
			this.referencedBit = true;
			return this.segments[offset];
		}	
	}
	
	// Sets offset-specified data segment
	public boolean setSegmentByOffset(int offset, short data) {
		if(offset < 0 || offset > 63) {
			return false;
		}
		else {
			this.segments[offset] = data;
			this.modifiedBit = true;
			this.referencedBit = true;
			return true;
		}	
	}
	
}

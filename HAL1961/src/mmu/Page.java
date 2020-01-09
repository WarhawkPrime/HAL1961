package mmu;

public class Page {

	private boolean referencedBit;
	private boolean presentBit;  // Bei Zugriff auf eine Page bei der dieses Bit 0 ist entsteht ein Page Fault (PF)
	private float[] segments = null;
	private int pageNumber;

	
	public Page (int frameNumber) {
		setSegments(new float[64]);	// 64 float segments
		this.referencedBit = false;
		this.presentBit = false;
		this.pageNumber = frameNumber;
	}

	public int getPageNumber() {
		return this.pageNumber;
	}
	
	public boolean isReferenced() {
		return this.referencedBit;
	}
	 
	public boolean isPresent() {
		return this.presentBit;
	}

	public void setPresentBit(boolean b) {
		this.presentBit = b;
	}

	private void setSegments(float [] segments) {
		this.segments = segments;
	}
	
	// Returns offset-specified data segment
	public float getSegmentByOffset(short offset) {
		if(offset < 0 || offset > 63) {
			return -1;
		}
		else {
			this.referencedBit = true;
			return this.segments[offset];
		}	
	}
	
	// Sets offset-specified data segment
	public boolean setSegmentByOffset(short offset, float data) {
		if(offset < 0 || offset > 63) {
			return false;
		}
		else {
			this.segments[offset] = data;
			this.referencedBit = true;
			return true;
		}	
	}
	
}

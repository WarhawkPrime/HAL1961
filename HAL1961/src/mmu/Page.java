package mmu;

public class Page {
	
	private float[] segments = null;
	private int pageNumber;

	public Page (int frameNumber) {
		setSegments(new float[64]);	// 64 float segments
		this.pageNumber = frameNumber;
	}

	public int getPageNumber() {
		return this.pageNumber;
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
			return true;
		}	
	}
	
}

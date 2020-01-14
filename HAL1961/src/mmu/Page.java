package mmu;

public class Page {
	
	private float[] segments = null;
	private int pageNumber;

	public Page (int frameNumber) {
		setSegments(new float[1024]);	// 1024 float segments
		for (int i = 0; i < segments.length; i++) {
			segments[i] = 0.0f;
		}
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
		if(offset < 0 || offset > 1023) {
			return -1;
		}
		else {
			return this.segments[offset];
		}	
	}
	
	// Sets offset-specified data segment
	public boolean setSegmentByOffset(short offset, float data) {
		if(offset < 0 || offset > 1023) {
			return false;
		}
		else {
			this.segments[offset] = data;
			return true;
		}	
	}
	
}

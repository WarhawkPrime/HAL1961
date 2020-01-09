package mmu;

public class PageTableEntry {

	
	private boolean referencedBit;
	private boolean presentBit;  // Bei Zugriff auf eine Page bei der dieses Bit 0 ist entsteht ein Page Fault (PF)
	private short index;
	
	
	public PageTableEntry(short index) {
		this.index = index;
		this.referencedBit = false;
		this.presentBit = false;
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
	
	public void setReferencedBit(boolean b) {
		this.referencedBit = b;
	}
	
	public short getIndex() {
		return this.index;
	}
	
	
	
}

package mmu;

public class Page {

	private boolean referencebit;
	private int pageNumber;
	private short [] registers = null;

	public Page (int pageNumber) {
		setRegisters(new short[64]);	//64 short register
		setReferencebit(false);
		setPageNumber(pageNumber);
	}

	public boolean isReferencebit() {
		return referencebit;
	}

	public void setReferencebit(boolean referencebit) {
		this.referencebit = referencebit;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public short [] getRegisters() {
		return registers;
	}

	public void setRegisters(short [] registers) {
		this.registers = registers;
	}
}

package mmu;


//hält die Seiten, reagiert auf pagefaults stack??
public class PageTable {

	
	// Page table gets an virtual address and converts it to an location in the virtual storage
	
	short virtualAddress = 0b0;
	short physicalAddress = 0b0;
	LogFile lf;
	
	// 
	 
	
	
	
	public PageTable() {
		
		this.lf = new LogFile();
	}
	
	private void throwPageFault(int index) {
		
		String temp = "Page Fault! Requested Page " + index + ".";
		this.lf.logInfo(temp);
	}
	
	public short resolveAdress(int virtualAddress) {		//wird im interpreter dort aufgerufen, wo auf adressen zugegriffen wird
		
		
		
		
		// Return segment value
		return 0;
	}
	
	public boolean testForPageFault(short reg1, short reg2, short reg3, short reg4) {				//testet auf pagefaults
		return false;
	}
}

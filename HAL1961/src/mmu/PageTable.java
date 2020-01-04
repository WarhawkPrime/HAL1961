package mmu;

public class PageTable {

	public PageTable() {
		
	}
	
	public short mapAdress(int adressNumber, short reg1, short reg2, short reg3, short reg4) {		//wird im interpreter dort aufgerufen, wo auf adressen zugegriffen wird
		
		return 0;
	}
	
	public boolean testForPageFault(short reg1, short reg2, short reg3, short reg4) {				//testet auf pagefaults
		return false;
	}
}

package mmu;

import java.util.ArrayList;

//hält die Seiten, reagiert auf pagefaults stack??
public class PageTable {

	
	// Page table gets an virtual address and converts it to an location in the virtual storage
	short msb = 0b0;
	short offset = 0b0;
	String virtualAddressString;
	short physicalAddress = 0b0;
	String physicalAddressString;
	LogFile lf;
	private ArrayList<Page> pages;
	private VirtualStorage virtualMemory = null;
	private long counter;
	
	
	public PageTable(VirtualStorage vmem) {
		
		this.lf = new LogFile();
		this.pages = new ArrayList<Page>();
		this.counter = 0;
		
		if(vmem != null)
			this.virtualMemory = vmem;
		else
			this.lf.logInfo("Error! Couldn't set Virtual Memory Instance, given instance is NULL!");
		
	}
	
	private void throwPageFault(int index) {
		this.counter++;
		String temp = "Page Fault No. " + this.counter + "! Requested Page " + index;
		this.lf.logInfo(temp);
	}
	
	public float resolveValueAtAddress(int virtualAddress) {		//wird im interpreter dort aufgerufen, wo auf adressen zugegriffen wird
		virtualAddressString = Integer.toBinaryString(virtualAddress);
		String s = "";
		// Extend string to 16 'bit' length
		for (int i = 0; i < (16 - virtualAddressString.length()); i++) {
			 s = s + "0";
		}
		virtualAddressString = s + virtualAddressString;
		
		// Get highest 6 and lowest 10 bits as page table index and offset
		msb = Short.parseShort(virtualAddressString.substring(0, 6), 2);
		offset = Short.parseShort(virtualAddressString.substring(6, 16), 2);

		Page p = accessPageTableWithIndex(msb);
		
		
		// Return segment value
		return p.getSegmentByOffset(offset);
	}
	
	
	private Page accessPageTableWithIndex(int index) {
		
		Page p;
		 
		if(index >= pages.size()) {
			
			// Page isnt loaded in memory, throw PageFault and load missing Page from VirtualStorage
			throwPageFault(index);
			p = loadMissingPage(index);
			return p;
		}
		else {
			
			p = pages.get(index);
				return p;
			
		}
	}
	
	// Get page from memory and run replacement algorithm to replace it in registers
	private Page loadMissingPage(int index) {
		
		Page p = this.virtualMemory.getPage(index);
		this.pages.add(p);
		return p;
	}
	
	
	
	
	
}

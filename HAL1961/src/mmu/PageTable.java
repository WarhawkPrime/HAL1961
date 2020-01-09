package mmu;

import java.util.ArrayList;


public class PageTable {

	
	// Page table gets an virtual address and converts it to an location in the virtual storage
	short index = 0b0;
	short offset = 0b0;
	String virtualAddressString;
	short physicalAddress = 0b0;
	String physicalAddressString;
	private ArrayList<PageTableEntry> entries;
	private VirtualStorage virtualMemory = null;
	private LogFile log;
	
	
	
	public PageTable(VirtualStorage vmem, LogFile lf) {
		
		this.log = lf;
		this.entries = new ArrayList<PageTableEntry>();
		
		
		if(vmem != null)
			this.virtualMemory = vmem;
		else
			this.log.logInfo("Error! Couldn't set Virtual Memory Instance, given instance is NULL!");
		
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
		index = Short.parseShort(virtualAddressString.substring(0, 6), 2);
		offset = Short.parseShort(virtualAddressString.substring(6, 16), 2);
		
		
		for (PageTableEntry pageTableEntry : entries) {
			if(pageTableEntry.getIndex() == index) {
				
				if(pageTableEntry.isPresent()) {
					// Return segment value
					return getPageValue(index, offset);
				}
				else {
					return 0;
				}
			}
			else {
				entries.add(new PageTableEntry(index));
				return getPageValue(index, offset);
			}
		}
		return 0;
	}
	
	public short getIndexFromAddress(int address) {
		
		String addressString = Integer.toBinaryString(address);
		String s = "";
		// Extend string to 16 'bit' length
		for (int i = 0; i < (16 - addressString.length()); i++) {
			 s = s + "0";
		}
		addressString = s + addressString;
		
		// Get highest 6 and lowest 10 bits as page table index and offset
		return Short.parseShort(addressString.substring(0, 6), 2);
		
	}
	
	
	private float getPageValue(short index, short offset) {
		
		return virtualMemory.getPage(index).getSegmentByOffset(offset);
	}
	
	public PageTableEntry getPageEntryByIndex(short index) {
		return entries.get(index);
	}
	
	
}

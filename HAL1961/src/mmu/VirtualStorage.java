package mmu;

import java.util.ArrayList;

public class VirtualStorage {
	private ArrayList<Page> pages;
	
	public VirtualStorage() {
		
	}
	
	public void createPages(int pcount) {
		for(int i = 0; i < 64; i++) {
			Page page = new Page(i);
			pages.add(page);
		}
		
	}

}

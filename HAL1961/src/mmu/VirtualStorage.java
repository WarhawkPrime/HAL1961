package mmu;

import java.util.ArrayList;

public class VirtualStorage {
	private ArrayList<Page> pages;
	
	
	public VirtualStorage() {
		this.createPages();
	}
	
	private void createPages() {	//erstellt 64 pages mit entsprechender nummer
		
		if(this.pages == null) {
			pages = new ArrayList<Page>();
			for(int i = 0; i < 63; i++) {
				Page page = new Page(i);
				pages.add(page);
			}
		}
	}
	
	public Page getPage(int index) {
		
		if(index < 0 || index > 63) {
			return null;
		}
		else {
			return pages.get(index);
		}
	}

}

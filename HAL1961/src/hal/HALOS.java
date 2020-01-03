package hal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

//Main class for our HAL-OS
public class HALOS extends Thread {

	
	// HalProcessor -> Contains HalInterpreter and I/O Components
	private HalInterpreter hal;
	private ArrayList<EA> eaComponents;
	private int procId;
	private String fileName;
	private int halosType;
	
	
	public HALOS(int procId, String filename) {
		
		this.fileName = filename;
		this.procId = procId;
		this.eaComponents = new ArrayList<>();
	}
	
	public void setHalInterpreter(HalInterpreter hal) {this.hal = hal;}
	public int getProcId() { return this.procId;}
	public String getFileName() {return this.fileName;}
	public int getEaComponentsSize() {return this.eaComponents.size();}
	public int getType() {return this.halosType;}
	public void setType(int type) { this.halosType = type;}
	
	public boolean hasEAComponent(int eaId) {
		
		for (Iterator<EA> iterator = eaComponents.iterator(); iterator.hasNext();) {
			EA ea = iterator.next();
			if(ea.getEA_Number() == eaId)
				return true;
			
		}
		return false;
	}
	
	@Override
	public void run() {
		
		hal = new HalInterpreter(eaComponents);
		Scanner scan = new Scanner(System.in);
		hal.startHalInterpreter(this.fileName);
		scan.close();
	}
	
	// After initializing a HALOS object, its eaComponents list is empty, so we set each EA component
	// here. If the component doesn't exist in the list already, it is created.
	// typeId: 0 is hal-0, 1 is hal-1 to hal-n-2, 2 is hal-n-1
	public void setEAComponentById(int eaNumber, int typeId ) {
	
		eaComponents.add(new EA(eaNumber, this.procId, typeId));
		
	}
	
	//gibt die Liste wieder
	/**
	 * 
	 * @return ArrayList<EA> with all EA components
	 */
	public ArrayList<EA> getEAComponents() {
		return eaComponents;
	}
	
	
	
	/**
	 * @param eaId
	 * @return
	 */
	public EA getEAComponentById(int eaId) {
		
		
		for (Iterator<EA> iterator = eaComponents.iterator(); iterator.hasNext();) {
			EA ea = iterator.next();
			
			if(ea.getEA_Number() == eaId) {
				return ea;
			}
		}
		return null;
	}
}

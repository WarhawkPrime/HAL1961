package hal;

import java.util.Scanner;

public class HalThread extends Thread {
	
	public HalThread(int procesId, String programName) {
		this.programName = programName;
		this.procesId = procesId;
	}
	
	String programName;
	int procesId;
	HALOS halos;
	
	public void run() {
		HalInterpreter halinterpreter = new HalInterpreter(halos.getEAComponents());
		halinterpreter.startHalInterpreter(programName );
	}
	
	public void setProcessor(HALOS proc) {this.halos = proc;}

}

import hal.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;


public class Main {
	
	public static void main(String[] args) throws InterruptedException {
	
		
		ArrayList<HalThread> threads = readFiles(args);
		
		threads.get(0).start();
		threads.get(0).join();
		threads.get(1).start();
		threads.get(1).join();
		threads.get(2).start();
		threads.get(2).join();
		threads.get(3).start();
		threads.get(3).join();
		
		
		//for (HalThread halThread : threads) {
			//halThread.start();
			//halThread.join();
		//}
		
		//test
		//dies ist ein test
		
	} 
	
	private static ArrayList<HalThread> readFiles(String[] args) {
		
		String line;
		boolean isStillProcessorCreation = true; 
		ArrayList<HALOS> processors = new ArrayList<>();
		//ArrayList<EA> eaList = new ArrayList<>();
		
		if(new File(args[0]).exists() == false) {
			throw new RuntimeException("Couldn't find game file " + args[0]);
		}
		
		try {
			BufferedReader bfrd = new BufferedReader(new FileReader(args[0]));
			
			while(bfrd.ready()) {
				
				line = bfrd.readLine();
				System.out.println(line);
				
				if(line.contentEquals("HAL-Prozessoren:")) {
					continue;
				}
				else if(line.contentEquals("HAL-Verbindungen:")) {
					isStillProcessorCreation = false;
					continue;
				}
				else {
					// Get all processors and push them into the ArrayList
					if(isStillProcessorCreation) {
						
						//id des Hal-Prozessors, filename für das hal- Programm
						System.out.println("Hal-Prozessor ID: " + Integer.parseInt(line.substring(0, 1)));
						System.out.println("Halo-Prozessor Filename: " + line.substring(2));
						processors.add(new HALOS(Integer.parseInt(line.substring(0, 1)) , line.substring(2)));
					}
					//ea components und connections
					else {
						
						// Configure the EA connections
						// First Processor ID in current line
						int firstProcId = Integer.parseInt(line.substring(0, 1));
						// First EA component in current line 
						int firstEAId = Integer.parseInt(line.substring(2, 3));
						// Second Processor ID in current line
						int secondProcId = Integer.parseInt(line.substring(6,7));
						// Second EA component in current line
						int secondEAId = Integer.parseInt(line.substring(8,9));
						
						int typeIdProc1 = -1;
						int typeIdProc2 = -1;
						
						// Check which processor is the first or last and set its value
						if(firstProcId == 0) {
							typeIdProc1 = 0;
							processors.get(firstProcId).setType(typeIdProc1);
							processors.get(firstProcId).setEAComponentById(typeIdProc1, typeIdProc1);
						}
						else if(firstProcId == (processors.size() -1)) {
							typeIdProc1 = 2;
							processors.get(firstProcId).setType(typeIdProc1);
						}
						else {
							typeIdProc1 = 1;
							processors.get(firstProcId).setType(typeIdProc1);
						}
						if(secondProcId == 0) {
							typeIdProc2 = 0;
							processors.get(firstProcId).setType(typeIdProc2);
						}
						else if(secondProcId == (processors.size() -1)) {
							typeIdProc2 = 2;
							processors.get(firstProcId).setType(typeIdProc2);
						}
						else {
							typeIdProc2 = 1;
							processors.get(firstProcId).setType(typeIdProc2);
						}
						
						
						
							// Set new buffer element between two corresponding EA components
							// Create a new buffer each iteration
							Buffer buffer = new Buffer();
							
							// Create new EA component
							processors.get(firstProcId).setEAComponentById(firstEAId, typeIdProc1);
							processors.get(secondProcId).setEAComponentById(secondEAId, typeIdProc2);
							
							// Set connection
							processors.get(firstProcId).getEAComponentById(firstEAId).connectToBuffer(buffer);
							processors.get(secondProcId).getEAComponentById(secondEAId).connectToBuffer(buffer);
						
					}
				}
			}
			
			bfrd.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		for (HALOS halos : processors) {
			// Set ea components for each processor
			for(int i = 0; i < halos.getEaComponentsSize(); i++) {
			
			if(!halos.hasEAComponent(i)) {
				halos.setEAComponentById(i, halos.getType());
			}
			}
		}
		
		
		for (Iterator<HALOS> iterator = processors.iterator(); iterator.hasNext();) {
			HALOS halos = iterator.next();
			
			if(checkCodeForCorrectEA(halos.getFileName(), processors, halos.getProcId())) {
			
				System.out.println("Correct Program Code for processor " + halos.getProcId());
			}
			else {
				System.out.println("Incorrect Program Code for processor " + halos.getProcId());
				throw new RuntimeException("Error in program code in file " + halos.getFileName());
			}
		}
		
		ArrayList<HalThread> threadList = new ArrayList<>();
		
		for (HALOS halos : processors) {
			HalThread ht = new HalThread(halos.getProcId(), halos.getFileName());
			ht.setProcessor(halos);
			threadList.add(ht);
		}
		return threadList;
	}
	
	private static boolean checkCodeForCorrectEA(String filename, ArrayList<HALOS> processors, int ProcId) {
		
		String line;
		
		//if(new File(filename).exists() == false) {
			//throw new RuntimeException("Couldn't find game file " + filename);
		//}
		
		try {
			BufferedReader bfrd = new BufferedReader(new FileReader(filename));
			
			// Scan text file till end
			while(bfrd.ready()) {
				
				line = bfrd.readLine();
				// If each IN and OUT number in code matches an existing ea component from the current processors, return true.
				if(line.contains("IN") || line.contains("OUT")) {
					System.out.println("Processor Id: " + ProcId);
					if(!processors.get(ProcId).hasEAComponent(Integer.parseInt(line.substring(line.length()-1)))){
						bfrd.close();
						return  false;
					}
				}
				else {
					continue;
				}
			}
			
			bfrd.close();
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return false;
	}
}

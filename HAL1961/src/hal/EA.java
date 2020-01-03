package hal;

import java.util.Scanner;

public class EA {
	
	private int eaNumber;
	private int halosNumber;
	private int halosType;
	private Buffer buffer;
	
	public EA(int eaNumber, int halosNumber, int halosType) {
		this.eaNumber = eaNumber;
		this.halosNumber = halosNumber;
		this.halosType = halosType;
		buffer = null;
	}
	
	
	//holt sich einen Wert vom hal Interpreter in den Buffer
	public void takeInputFromHalInp(double input) {
		double tempInput = input;
		
		//normaler Fall für den Output
		if(eaNumber == 1) {
			System.out.print(input);
			System.out.print(" ");
		}
		else if(halosType != 2) {
			buffer.put(tempInput);
		}
		else {
			
		}
	}
	
	//sendet input vom buffer (oder system.in) zu den Hal interpreter (halos)
	public double sendInputToHalInp() {

		double input;
		
		if(eaNumber == 0 && halosType == 0) {
			Scanner scanner1ea = new Scanner(System.in);
			input = readFromIO(scanner1ea);
			return input;
		}
		else {
			input = buffer.get();
		}
		return 0;
	}
	
	

	public int getEA_Number() {
		return eaNumber;
	}

	
	public void setEA_Number(int eaNumber) {
		this.eaNumber = eaNumber;
	}
	
	public int getHalosNumber() {
		return halosNumber;
	}

	public void setHalosNumber(int halosNumber) {
		this.halosNumber = halosNumber;
	}


	public void connectToBuffer(Buffer buffer) {
		this.buffer = buffer;
	}
	
	public void disconnectFromBufer() {
		buffer = null;
	}
	
	public Buffer getConnectedBuffer() {
		return buffer;
	}
	
	
	public void sendToBuffer(int input) {
		buffer.put(input);
	}
	
	public double getFromBuffer() {
		return buffer.get();
	}
	
	
	public int getHALOSid(int halosId) {
		return halosId;
	}
	
	
	public double readFromIO(Scanner scanner) {
		
		scanner.useDelimiter(System.lineSeparator());
		System.out.println("float Input to write in Akkumulator : ");
		scanner.hasNext();
		String inputString = scanner.next();
		double s = Float.valueOf(inputString.trim()).floatValue();
		
		return s;
	}
	
	public void writeToIO(float s) {
		System.out.println(s);
	}
	
	
	
}

package mmu;

import hal.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class MMU {

	public double akk;
	public int pc;
	private VirtualStorage vm;
	private PageTable pageTable;
	private short[] regs;
	private float[] programStorage;

	public ArrayList<Commandline> commandlinesInMemory = null;
	ArrayList<String> tempcommandLines = new ArrayList<>();
	boolean debugMode = false;
	Scanner scanner = null;
	private long counter;
	LogFile lf;
	
	public MMU() {
		this.commandlinesInMemory = new ArrayList<Commandline>();
		this.vm = new VirtualStorage();
		this.lf = new LogFile();
		this.pageTable = new PageTable(vm, lf);
		this.counter = 0;
		this.regs = new short[4];
		
		for (int i = 0; i < regs.length; i++) {
			regs[i] = -1;
		}
	}
	
	// Set segment data and handle page faults
	public boolean setSegment(int address, float value) {
		
		short index = pageTable.getIndexFromAddress(address);
		if(pageTable.createPageTableEntry(index))
			lf.logInfo("Created new PageTableEntry with index " + index);

		PageTableEntry p = pageTable.getPageEntryByIndex(index);
		
		
		for (int i = 0; i < regs.length; i++) {
			
			if(regs[i] == index) {
				
				p.setReferencedBit(true);
				return pageTable.setValueAtAddress(address, value);
			}
			else if(regs[i] == -1) {
				
				regs[i] = index;
				p.setPresentBit(true);
				p.setReferencedBit(true);
				return pageTable.setValueAtAddress(address, value);
			}
		}
		
		// Page fault occured if execution continues here
		throwPageFault(index);
		secondChance(index, address);
		return pageTable.setValueAtAddress(address, value);
		
	}
	
	
	// Return segment data and handle page faults 
	public float getSegment(int address) {
		
		short index = pageTable.getIndexFromAddress(address);
		if(pageTable.createPageTableEntry(index))
			lf.logInfo("Created new PageTableEntry with index " + index);

		PageTableEntry p = pageTable.getPageEntryByIndex(index);
		
		
		for (int i = 0; i < regs.length; i++) {
			
			if(regs[i] == index) {
				
				p.setReferencedBit(true);
				return pageTable.resolveValueAtAddress(address);
			}
			else if(regs[i] == -1) {
				
				regs[i] = index;
				p.setPresentBit(true);
				p.setReferencedBit(true);
				return pageTable.resolveValueAtAddress(address);
			}
		}
		
		// Page fault occured if execution continues here
		throwPageFault(index);
		secondChance(index, address);
		//Page Fault has been handled, return value at address
		return pageTable.resolveValueAtAddress(address);
		
	}
	
	private LinkedList<Short> getListFromArray(short[] array){
		
		LinkedList<Short> temp = new LinkedList<>();
		for (int i = 0; i < array.length; i++) {
			temp.add(array[i]);
		}
		return temp;
	}
	
	private short[] getArrayFromList(LinkedList<Short> list) {
		
		short[] temp = new short[4];
		for (int i = 0; i < 4; i++) {
			temp[i] = list.get(i);
		}
		return temp;
	}
	
	
	// Run replacement algorithm to replace it in registers
	private void secondChance(short index, int address) {
		
		PageTableEntry entry, temp;
		LinkedList<Short> list = getListFromArray(regs);
		
		for (int i = 0; i < list.size(); i++) {
			
			entry = pageTable.getPageEntryByIndex(list.get(i));
			
			if(entry.isReferenced()) {
				short s = list.remove(0);
				temp = pageTable.getPageEntryByIndex(s);
				temp.setReferencedBit(false);
				list.addLast(s);
			}
			else {
				short s = list.set(0, index);
				temp = pageTable.getPageEntryByIndex(s);
				temp.setPresentBit(false);
			}
		}
		
		regs = getArrayFromList(list);
		

		
		
	}
	
	private float randomReplacement(short index, int address) {
		
		PageTableEntry entry, temp;
		LinkedList<Short> list = getListFromArray(regs);
		
		int i = ThreadLocalRandom.current().nextInt(0,4);
		
		list.set(i, index);
		
		regs = getArrayFromList(list);
		return pageTable.resolveValueAtAddress(address);
		
	}
	
	private void throwPageFault(int index) {
		this.counter++;
		String temp = "Page Fault No. " + this.counter + "! Requested Page " + index;
		this.lf.logInfo(temp);
	}
	
	//========== Getter ==========
	public boolean getDebugMode() {return debugMode;}
	public double getAkku() {return akk;}
	public int getPc() {return pc;}
	public ArrayList<Commandline> getCommandlinesInMemory() {return commandlinesInMemory;}
	//========== Setter ==========
	public void setAkku(double akkuContent) {this.akk = akkuContent;}
	public void setPc(int pcContent) {this.pc = pcContent;}

	public float[] getProgramStorage() {
		return programStorage;
	}
	public void setProgramStorage(float[] programStorage) {
		this.programStorage = programStorage;
	}




	//==================== Andere Variablen	====================
	/**
	 * 
	 * @param filename
	 */
	public void startMMU(String filename) {

		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter(System.lineSeparator());

		//Aufruf um den Debugger nachzufragen und zu setzten
		turnDebugmodeOn(askForDebugMode(scanner));

		//Datei aus der main einlesen und in Strings speichern
		readFile(filename);

		//commandline objekte erstellt und in Array gepackt
		createCommandLines();
		
		convertCommandlinesToFloatArray();	//gibt das shortarray zur�ck

		//interpretHatProgramm durchgehen, auf Start warten und dann nacheinander die Schritte abarbeiten, debug mode nicht vergessen
		interpretHalProgram(scanner);

		scanner.close();
	}


	//Methode um die Eingabe für den Debug modus abzufragen
	/**
	 * 
	 * @return
	 */
	public String askForDebugMode(Scanner scanner) {
		String turnOnDebugModeEingabe;

		//System.out.println("Debug-Mode [y/n]?");
		//�berarbeitete 
		turnOnDebugModeEingabe = readFromIODebugMode(scanner);

		return turnOnDebugModeEingabe;
	}



	//Methode zum entscheiden ob der Debug modus an oder aus ist, was soll passieren wenn die eingabe falsch ist?
	/**
	 * 
	 * @param turnOnDebugModeEingabe
	 */
	public void turnDebugmodeOn(String turnOnDebugModeEingabe) {

		if(turnOnDebugModeEingabe.equals("y") || turnOnDebugModeEingabe.equals("Y"))  {
			debugMode = true;
			System.out.println("Debug Mode enabled");
		}
		else if(turnOnDebugModeEingabe.equals("n") || turnOnDebugModeEingabe.equals("N")) {
			debugMode = false;
			System.out.println("Debug Mode disabled");
		}
		else {
			debugMode = false;
			System.out.println("Task Failed Successfully, Debug mode disabled" + turnOnDebugModeEingabe);
		}
	}


	//Methode um das Hal Programm zu bekommen (über eine .txt file)
	/**
	 * 
	 * @param filename
	 */
	public void readFile(String filename) {
		//file komplett einmal einlesen, evtl. in Array speichern (container nachschauen!)
		//jede Zeile als einen einzigen String speichern, diesen dann bei Aufruf trenen

		//testet ob die File existiert
		if (new File(filename).exists() == false) {
			throw new RuntimeException("Game file " + filename + " not found");
		}

		//Liest die File Zeile für zeile als String pro Zeile in das Array commandLines ein
		try (BufferedReader bfReader = new BufferedReader(new FileReader(filename))) {
			while(bfReader.ready()) {
				tempcommandLines.add(bfReader.readLine());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	//commandLines von der Zeilennummer befreien und nach Command/Parameter getrennt haben
	/**
	 * 
	 */
	public void createCommandLines() {
		//Für jedes Element aus commandLines
		for(String commandLine: tempcommandLines) {
			//String array mit allen Inhalten aus commandLine, getrennt nach einem einzigen Leerzeichen
			String [] tempArray = commandLine.split(" ");

			//daten umwandeln

			//line number initialisieren, 0 jhat hier keine bedeutung
			int commandLineNumber = 0; 

			//befehl
			String command;

			//parameter
			double commandParameter = 0;

			//line number
			try {
				commandLineNumber = Integer.parseInt(tempArray[0]);
			}
			catch (NumberFormatException nfe) {
				System.out.println("NumberformatException: " + nfe.getMessage());
			}

			//Befehl
			command = tempArray[1];

			//Wenn es �berhaupt einen 3. Wert gibt (wenn es �berhaupt einen Parameter wie r, akk, etc gibt)
			if(tempArray.length == 3) {
				try {
					commandParameter = Float.parseFloat(tempArray[2]);
				}
				catch (NumberFormatException nfe) {
					System.out.println("NumberformatException: " + nfe.getMessage());
				}
			}

			//neues Objekt erstellen
			Commandline newCommandline = new Commandline(commandLineNumber, command, commandParameter);

			//In Programm memory schreiben
			commandlinesInMemory.add(newCommandline);
		}
	}






	//was soll ausgegeben werden wenn der Debug modus an ist?
	/**
	 * 
	 * @param i
	 */
	public void showsDebugMode(int i) {


		if(commandlinesInMemory.get(i).getCommandName().equals("START") || commandlinesInMemory.get(i).getCommandName().equals("STOP")) {
			System.out.println("Befehl: " + commandlinesInMemory.get(i).getCommandName());	//Welcher Befehl?
		}
		else {

			System.out.println("Befehl: " + commandlinesInMemory.get(i).getCommandName() + " " + commandlinesInMemory.get(i).getCommandParameter() );	//Welcher Befehl?

			if(testParaForRegister(commandlinesInMemory.get(i).getCommandName()) == true) { //ist der Parameter ein Register?

				int currentRegister = (int) commandlinesInMemory.get(i).getCommandParameter();	//umwandeln von float zu int
				System.out.println("Register " + commandlinesInMemory.get(i).getCommandParameter() + " : " + getRegisterNumber(currentRegister) );	//registerausgabe mit inhalt
			}
			//Hier eine ähnliche abfrage ob der Akku gebraucht wird!!!
			if(testParaForAkku(commandlinesInMemory.get(i).getCommandName()) == true) {	//ist der Parameter der akku?

				System.out.println("Akku : " + akk); //ausgabe von akku inhalt

			}
		}

	}


	//testet darauf welcher Befehl aufgerufen wird und entscheidet so, ob es sich um ein Register handelt oder um einen Wert/Adresse
	/**
	 * 
	 * @param commandName
	 * @return
	 */
	public boolean testParaForRegister(String commandName) {
		boolean isRegister = false;
		if(commandName.equals("LOAD") || commandName.equals("STORE") ) {
			isRegister = true;
		}
		else {
			isRegister = false;
		}
		return isRegister;
	}



	//Methode um zu testen o der Akku gebraucht wird, ähnlich wie testParaForRegister
	/**
	 * 
	 * @param commandName
	 * @return
	 */
	public boolean testParaForAkku(String commandName) {
		boolean isAkku = false;
		if(commandName.equals("JUMPNEG") || commandName.equals("JUMPPOS") || commandName.equals("JUMPNULL") || commandName.equals("JUMP") ||
				commandName.equals("MUL") || commandName.equals("DIV") || commandName.equals("SUBNUM") || commandName.equals("MULNUM") || commandName.equals("DIVNUM") ||
				commandName.equals("IN") || commandName.equals("OUT") || commandName.equals("LOADNUM") || commandName.equals("ADD") || commandName.equals("SUB") ) {
			isAkku = true;
			return isAkku;
		}
		return isAkku;
	}


	public float[] convertCommandlinesToFloatArray() {

		int arraySize = commandlinesInMemory.size();
		float [] commands = new float[arraySize];



		for(int i = 0; i < commandlinesInMemory.size(); i++) {

			float sc;
			float parac;
			
			double mod = Math.pow(10, 11);

			switch( commandlinesInMemory.get(i).getCommandName() ) {

			case("START"):  
				sc = 00000;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			
			
			commands[i] = (float) ( (sc*mod) + parac); 
			break;
			case("STOP"):
				sc = 00001;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
			break;
			case("OUT"):
				sc = 00010;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("IN"):
				sc = 00011;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("LOAD"):
				sc = 00100;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("LOADNUM"):
				sc = 00101;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("STORE"):
				sc = 00110;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("JUMPNEG"):
				sc = 00111;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("JUMPPOS"):
				sc = 01000;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("JUMPNULL"):
				sc = 01001;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("JUMP"):
				sc = 01010;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("ADD"):
				sc = 01011;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("ADDNUM"):
				sc = 01100;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("SUB"):
				sc = 01101;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("MUL"):
				sc = 01110;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("DIV"):
				sc = 01111;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("SUBNUM"):
				sc = 10000;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("MULNUM"):
				sc = 10001;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("DIVNUM"):
				sc = 10010;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("LOADIND"):
				sc = 10011;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("STOREIND"):
				sc = 10100;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("DUMPREG"):
				sc = 10101;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("DUMPPROG"):
				sc = 10110;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			case("ADDIND"):
				sc = 10111;
			parac = (float) commandlinesInMemory.get(i).getCommandParameter();
			commands[i] = (float) ((sc*mod) + parac); 
				break;
			default:

			}
		}
		
		this.setProgramStorage(commands);
		return commands;
	}
	
	
	//cleansedLineCommands durchgehen, auf Start warten und dann nacheinander die Schritte abarbeiten, debug mode nicht vergessen
	/**
	 * 
	 * @return
	 */
	public boolean interpretHalProgram(Scanner scanner) {

		boolean commandsExecuted = false; //variable die bestimmt ob STOP gefunden wurde, wird dabei zur�ckgegeben 
		
		this.setPc(0);
		
		boolean foundStart = false;
		
		for(int i = 0; i < this.getProgramStorage().length; i++) {
		
			float temp = this.getProgramStorage()[i];
			int comm = calcCommand(temp); 
			float para = calcPara(temp, comm);			
			
			if( comm == 00000 || foundStart ) {
			
				foundStart = true;
				
				if(debugMode == true) {	//ist der debugModus angeschaltet?
					//showsDebugMode(pcCounter);
				}
		
				this.setPc(executeCommand(comm, para, scanner, this.getPc()));
		
				if(this.getPc() == -1) { 	//wenn pc von STOP -1 gesetzt wurde, dann:
					System.out.println("Befehl: STOP");
					return commandsExecuted = true;
				}

				if(debugMode == true) {	//ist der debugModus angeschaltet?
					//showsDebugMode(pc - 1);
				}
			}
			else {
				pc++;
			}
		}
		return false;
	}

	
	
	public short calcCommand(float line) {
		double temp = line;
		short comm = 0;
		double mod = Math.pow(10,-11 );
		//short temp = (short) line;
		
		if(findPointPosition(line) == -1) {
			comm = (short) (temp * mod);
		}
		else {
			int i = findPointPosition(line);
			int modpara = 11 - i;
			mod = Math.pow(10, -modpara );
			comm = (short) (temp * mod);
		}
		
	   return comm;
	}
	
	public float calcPara(float line, int command) {
		float para = 0;
		float tempLine = line;
		double mod = Math.pow(10,11 );

		int comm = (int) (command * mod);
		
		para = tempLine - comm;
	    
	   return para;
	}
	

	public int findPointPosition(float line) {
		String str2 = Float.toString(line);
		
		for(int i = 0; i < str2.length(); i++) {
			if(str2.charAt(i) == '.') {
				return i;
			}
		}
		return -1;
	}
	

	//Fuehrt den uebergebenen Befehl aus
	/**
	 * 
	 * @param commandline
	 * @return
	 */
	public int executeCommand(int command, float para,  Scanner scanner, int pcCounter) {

		int commandlineNumber = pcCounter;
		int commandName = command; 
		float commandPara = para;

		Instruktionssatz instructions = null;

		int registerNumber;
		float registerContent;
		double akkuTemp;

		
		
		/*
		 * START			00000			Startet das Programm
		 * STOP				00001			Stoppt das Programm
		 * OUT		s		00010			Druckt den Inhalt vom Akku über E/A Schnittstelle s aus
		 * IN		s		00011			Liest über E/A Schnittstelle s und schreibt den Wert in den Akku
		 * LOAD		r		00100			Lädt Inhalt von Register r in Akku
		 * LOADNUM	k		00101			Lädt konstante k in Akku
		 * STORE	r		00110			Speichert Inhalt von Akku in Register r
		 * JUMPNEG	a		00111			springt zu Programmspeicheradresse a, wenn Akkumulator negativen Wert hat
		 * JUMPPOS	a		01000			springt zu Programmspeicheradresse a, wenn Akkumulator positiven Wert hat
		 * JUMPNULL	a		01001			springt zu Programmspeicheradresse a, wenn Akkumulator den Wert 0 hat
		 * JUMP		a		01010			springt zu Programmadresse a
		 * ADD		r		01011			addiert den Inhalt des Registers r zum Inhalt des Akkumulator und speichert Ergebnis im Akkumulator (a = a + r)
		 * ADDNUM	k		01100			addiert Konstante k zum Inhalt des Akkumulator und speichert Ergebnis im Akkumulator (a = a + k)
		 * SUB		r		01101			subtrahiert den Inhalt des Registers r vom Inhalt des Akkumula- tors (a = a - r)
		 * MUL		a		01110			
		 * DIV		a		01111
		 * SUBNUM	a		10000
		 * MULNUM	a		10001
		 * DIVNUM	a 		10010
		 * 
		 * LOADIND  r		10011			l�dt den Inhalt der Speicherzelle in den Accumulator, deren Adresse im Register r abgelegt ist
		 * STOREIND r		10100			speichert den Inhalt des Akkus in der Speicherzelle, deren Adresse im Register r steht
		 * DUMPREG			10101			gibt den Inhalt aller Register �ber den Kanal 1 in der Form
		 * DUMPPROG			10110			gibt den Programmspeicher �ber den Kanal 2 aus
		 * ADDIND	r		10111			
		 */



		//Schnittstelle s
		double s = 0;

		switch(commandName) {

		case(00000):  //start

			return pcCounter += 1;

		//break;
		case(00001): //STOP

			return pcCounter = -1;

		case(00010): //OUT s

			s = getAkku();

		System.out.println(s);
		return pcCounter +=1;

		case(00011):	//IN s

			//eaComponentNumber = (int) commandPara;
			//ea = getEAbyNumber(eaComponentNumber, eaComponents );

			//s = ea.sendInputToHalInp();
			//bekommt/holt sich vom ausgew�hltem ea Baustein den Wert. Der ea baustein muss sich diesen wert vorher aus dem Buffer holen

		Scanner scanner1ea = new Scanner(System.in);
		scanner1ea.useDelimiter(System.lineSeparator());
		System.out.println("float Input to write in Akkumulator : ");
		scanner1ea.hasNext();
		String inputString = scanner1ea.next();
		s = Float.valueOf(inputString.trim()).floatValue();

		setAkku(s);

		return pcCounter +=1;

		//break;
		case(00100):	//LOAD r

		registerNumber = (int) commandPara;
		registerContent = getSegment(registerNumber);
		setAkku(registerContent);
		return pcCounter +=1;

		//break;
		case(00101):	//LOADNUM	k

			setAkku(commandPara);
		return pcCounter +=1;

		//break;
		case(00110):	//STORE r

			registerNumber = (int) commandPara;
		float akkuContent = getAkku();
		setSegment(registerNumber, akkuContent); 
		return pcCounter +=1;

		//break;
		case(00111):	//JUMPNEG a

			if(getAkku() < 0) {
				int tempPcPosition = (int) commandPara;
				return pcCounter = tempPcPosition;
			}

		return pcCounter +=1;

		//break;
		case(01000):	//JUMPPOS a

			if(getAkku() > 0) {
				int tempPcPosition = (int) commandPara;
				//int pcPos = 0 - tempPcPosition;
				//setPc(tempPcPosition);
				return pcCounter = tempPcPosition;
			}

		return pcCounter +=1;
		//break;
		case(01001):	//JUMPNULL

			if(getAkku() == 0) {
				int tempPcPosition = (int) commandPara;
				return pcCounter = tempPcPosition;
			}

		return pcCounter +=1;
		//break;
		case(01010):	//JUMP

			int tempPcPosition = (int) commandPara;
		return pcCounter = tempPcPosition;
		//break;
		case(01011):	//ADD r

			//(a = a + r)
			registerNumber = (int) commandPara;

		registerContent = getSegment(registerNumber);


		akkuTemp = getAkku() + registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(01100): 	//ADDNUM k

			//(a = a + k)
			akkuTemp = commandPara + getAkku();
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(01101):	//SUB r

			//(a = a - r)
			registerNumber = (int) commandPara;
		registerContent = getSegment(registerNumber);
		akkuTemp =getAkku() - registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(01110):	//MUL a

			//(a = a * r)
			registerNumber = (int) commandPara;
		registerContent = getSegment(registerNumber);
		akkuTemp =getAkku() * registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(01111):	//DIV a

			//(a = a / r)
			registerNumber = (int) commandPara;
		registerContent = getSegment(registerNumber);
		akkuTemp =getAkku() / registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(10000):	//SUBNUM a

			//(a = a - k)
			akkuTemp = getAkku() - commandPara;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(10001):	//MULNUM a

			//(a = a * k)
			akkuTemp = getAkku() * commandPara;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(10010):	//DIVNUM a

			//(a = a / k)
			akkuTemp = getAkku() - commandPara;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case(10011):	//LOADIND r
			registerNumber = (int) commandPara;
		//l�dt den Inhalt der Speicherzelle in den Accumulator, deren Adresse im Register r abgelegt ist


		return pcCounter +=1;
		//break;
		case(10100):	//STOREIND r
			
			registerNumber = (int) commandPara;

		//speichert den Inhalt des Akkus in der Speicherzelle, deren Adresse im Register r steht

		return pcCounter+=1;
		//break;
		case(10101):	//DUMPREG r
			//gibt den Ingalt aller Register �ber den Kanal 1 in der Form 'Registernummer: Registerinhalt' aus

			return pcCounter;
		//break;
		case(10110):	//DUMPPROG

			//gibt den Programmspeicher �ber den Kanal 2 aus
			return pcCounter;
		//break;
		case(10111):		//ADDIND r
		
		
		default:
		}

		return pcCounter;	//niemals!!!
	}

	
	
	
	
	
	
	
	public String readFromIODebugMode(Scanner scanner) {

		//String s;
		//scanner.useDelimiter(System.lineSeparator());


		System.out.println("Debug Mode y/n : ");

		//scanner.hasNext();
		//String inputString = scanner.nextLine();
		String inputString = "n";
		//double s = Float.valueOf(inputString.trim()).floatValue();

		//String inputString = scanner.nextLine();

		//s = (inputString.trim());


		return inputString;

	}




}

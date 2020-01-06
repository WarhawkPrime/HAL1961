package mmu;

import hal.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import java.util.Scanner;


public class MMU {

	public double akk;
	public int pc;
	private VirtualStorage vm;
	private PageTable pageTable;

	private short register0;			          
	private short register1;			
	private short register2;			                                             
	private short register3;

	private short register4;			          
	private short register5;			
	private short register6;			                                             
	private short register7;

	private short register8;
	private short register9;
	private short register10;
	private short register11;

	private short register12;
	private short register13;
	private short register14;			                                             
	private short register15;

	private short programmStorage0;
	private short programmStorage1;
	private short programmStorage2;
	private short programmStorage3;

	public ArrayList<Commandline> commandlinesInMemory = null;
	boolean debugMode = false;
	Scanner scanner = null;

	public MMU() {
		commandlinesInMemory = new ArrayList<Commandline>();
	}

	//==================== Deklaration der Register ====================
	//Register in float array, von r_00 bis r_15

	//register:

	//Akkumulator

	public int getRegisterNumber() {																								//!

	}
	public double[] getRegisters() {																								//!

	}

	//========== Getter ==========
	public boolean getDebugMode() {return debugMode;}
	public double getAkku() {return akk;}
	public int getPc() {return pc;}
	public ArrayList<Commandline> getCommandlinesInMemory() {return commandlinesInMemory;}
	//========== Setter ==========
	public void setAkku(double akkuContent) {this.akk = akkuContent;}
	public void setPc(int pcContent) {this.pc = pcContent;}

	//==================== Andere Variablen	====================

	//arrayList um alle einzelnen commandZeilen zu speichern
	ArrayList<String> tempcommandLines = new ArrayList<>();


	/**
	 * 
	 * @param filename
	 */
	public void startMMU(String filename) {



		//Programm Counter wird 0 gesetzt
		//r_17 = 0;

		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter(System.lineSeparator());

		//Aufruf um den Debugger nachzufragen und zu setzten
		turnDebugmodeOn(askForDebugMode(scanner));

		//Datei aus der main einlesen und in Strings speichern
		readFile(filename);

		//commandline objekte erstellt und in Array gepackt
		createCommandLines();

		//interpretHatProgramm durchgehen, auf Start warten und dann nacheinander die Schritte abarbeiten, debug mode nicht vergessen
		interpretHalProgram(scanner);

		scanner.close();


	}




	//Methode um die Eingabe fÃ¼r den Debug modus abzufragen
	/**
	 * 
	 * @return
	 */
	public String askForDebugMode(Scanner scanner) {
		String turnOnDebugModeEingabe;

		//System.out.println("Debug-Mode [y/n]?");
		//überarbeitete 
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


	//Methode um das Hal Programm zu bekommen (Ã¼ber eine .txt file)
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

		//Liest die File Zeile fÃ¼r zeile als String pro Zeile in das Array commandLines ein
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
		//FÃ¼r jedes Element aus commandLines
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

			//Wenn es überhaupt einen 3. Wert gibt (wenn es überhaupt einen Parameter wie r, akk, etc gibt)
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


	//cleansedLineCommands durchgehen, auf Start warten und dann nacheinander die Schritte abarbeiten, debug mode nicht vergessen
	/**
	 * 
	 * @return
	 */
	public boolean interpretHalProgram(Scanner scanner) {

		boolean commandsExecuted = false; //variable die bestimmt ob STOP gefunden wurde, wird dabei zurückgegeben 
		pc = 0;	//programm counter zeigt auf aktuellen Befehl
		boolean foundStart = false;

		for(int pcCounter = pc; pcCounter < commandlinesInMemory.size();) { //geht alle Elemente durch nach pc/pcCounter

			if(commandlinesInMemory.get(pcCounter).getCommandName().equals("START") || foundStart == true ) {	//wenn Start gefunden wird

				foundStart = true;

				if(debugMode == true) {	//ist der debugModus angeschaltet?
					showsDebugMode(pcCounter);
				}

				pcCounter = executeCommand(commandlinesInMemory.get(pcCounter), scanner, pcCounter); //PC fehlt noch

				if(pcCounter == -1) { 	//wenn pc von STOP -1 gesetzt wurde, dann:
					System.out.println("Befehl: STOP");
					return commandsExecuted = true;
				}

				if(debugMode == true) {	//ist der debugModus angeschaltet?
					showsDebugMode(pcCounter - 1);
				}


			}
			else {	//Start nicht in diesem Durchlauf erhöt wird
				pcCounter++; //pc / pcCounter wird um 1 erhöt
			}
		}
		System.out.println("Kein Start gefunden, keine Befehle ausgeführt");	//for schleife ist durch, kein Strt gefunden
		return commandsExecuted;
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
				System.out.println("Register " + commandlinesInMemory.get(i).getCommandParameter() + " : " + register[currentRegister] );	//registerausgabe mit inhalt
			}
			//Hier eine Ã¤hnliche abfrage ob der Akku gebraucht wird!!!
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



	//Methode um zu testen o der Akku gebraucht wird, Ã¤hnlich wie testParaForRegister
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



	//Fuehrt den uebergebenen Befehl aus
	/**
	 * 
	 * @param commandline
	 * @return
	 */
	public int executeCommand(Commandline commandline, Scanner scanner, int pcCounter) {

		int commandlineNumber = commandline.getCommandLineNumber();
		String commandName = commandline.getCommandName();
		double commandPara = commandline.getCommandParameter();

		Instruktionssatz instructions = null;

		int registerNumber;
		double registerContent;
		double akkuTemp;


		int eaComponentNumber;
		EA ea = null;

		/*
		 * START			Startet das Programm
		 * STOP				Stoppt das Programm
		 * OUT		s		Druckt den Inhalt vom Akku Ã¼ber E/A Schnittstelle s aus
		 * IN		s		Liest Ã¼ber E/A Schnittstelle s und schreibt den Wert in den Akku
		 * LOAD		r		LÃ¤dt Inhalt von Register r in Akku
		 * LOADNUM	k		LÃ¤dt konstante k in Akku
		 * STORE	r		Speichert Inhalt von Akku in Register r
		 * JUMPNEG	a		springt zu Programmspeicheradresse a, wenn Akkumulator negativen Wert hat
		 * JUMPPOS	a		springt zu Programmspeicheradresse a, wenn Akkumulator positiven Wert hat
		 * JUMPNULL	a		springt zu Programmspeicheradresse a, wenn Akkumulator den Wert 0 hat
		 * JUMP		a		springt zu Programmadresse a
		 * ADD		r		addiert den Inhalt des Registers r zum Inhalt des Akkumulator und speichert Ergebnis im Akkumulator (a = a + r)
		 * ADDNUM	k		addiert Konstante k zum Inhalt des Akkumulator und speichert Ergebnis im Akkumulator (a = a + k)
		 * SUB		r		subtrahiert den Inhalt des Registers r vom Inhalt des Akkumula- tors (a = a - r)
		 * MUL		a
		 * DIV		a
		 * SUBNUM	a
		 * MULNUM	a
		 * DIVNUM	a 
		 */

		//Schnittstelle s
		double s = 0;

		switch(commandName) {

		case("START"):  

			return pcCounter += 1;

		//break;
		case("STOP"): 

			return pcCounter = -1;


		case("OUT"): 

			eaComponentNumber = (int) commandPara;
		//ea = getEAbyNumber(eaComponentNumber, eaComponents );
		s = getAkku();

		//ea.takeInputFromHalInp(s);

		System.out.println(s);
		return pcCounter +=1;

		//break;
		case("IN"): 

			//eaComponentNumber = (int) commandPara;
			//ea = getEAbyNumber(eaComponentNumber, eaComponents );

			//s = ea.sendInputToHalInp();
			//bekommt/holt sich vom ausgewähltem ea Baustein den Wert. Der ea baustein muss sich diesen wert vorher aus dem Buffer holen

			Scanner scanner1ea = new Scanner(System.in);
		scanner1ea.useDelimiter(System.lineSeparator());
		System.out.println("float Input to write in Akkumulator : ");
		scanner1ea.hasNext();
		String inputString = scanner1ea.next();
		s = Float.valueOf(inputString.trim()).floatValue();

		setAkku(s);

		return pcCounter +=1;

		//break;
		case("LOAD"): 

			registerNumber = (int) commandPara;
		registerContent = getRegisters()[registerNumber];
		setAkku(registerContent);
		return pcCounter +=1;

		//break;
		case("LOADNUM"):

			setAkku(commandPara);
		return pcCounter +=1;

		//break;
		case("STORE"): 

			registerNumber = (int) commandPara;
		double akkuContent = getAkku();
		getRegisters()[registerNumber] = akkuContent; 
		return pcCounter +=1;

		//break;
		case("JUMPNEG"): 

			if(getAkku() < 0) {
				int tempPcPosition = (int) commandPara;
				return pcCounter = tempPcPosition;
			}

		return pcCounter +=1;

		//break;
		case("JUMPPOS"): 

			if(getAkku() > 0) {
				int tempPcPosition = (int) commandPara;
				//int pcPos = 0 - tempPcPosition;
				//setPc(tempPcPosition);
				return pcCounter = tempPcPosition;
			}

		return pcCounter +=1;
		//break;
		case("JUMPNULL"): 

			if(getAkku() == 0) {
				int tempPcPosition = (int) commandPara;
				return pcCounter = tempPcPosition;
			}

		return pcCounter +=1;
		//break;
		case("JUMP"): 

			int tempPcPosition = (int) commandPara;
		return pcCounter = tempPcPosition;
		//break;
		case("ADD"): 

			//(a = a + r)
			registerNumber = (int) commandPara;

		registerContent = getRegisters()[registerNumber];


		akkuTemp = getAkku() + registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("ADDNUM"): 

			//(a = a + k)
			akkuTemp = commandPara + getAkku();
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("SUB"): 

			//(a = a - r)
			registerNumber = (int) commandPara;
		registerContent = getRegisters()[registerNumber];
		akkuTemp =getAkku() - registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("MUL"): 

			//(a = a * r)
			registerNumber = (int) commandPara;
		registerContent = getRegisters()[registerNumber];
		akkuTemp =getAkku() * registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("DIV"): 

			//(a = a / r)
			registerNumber = (int) commandPara;
		registerContent = getRegisters()[registerNumber];
		akkuTemp =getAkku() / registerContent;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("SUBNUM"): 

			//(a = a - k)
			akkuTemp = getAkku() - commandPara;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("MULNUM"): 

			//(a = a * k)
			akkuTemp = getAkku() * commandPara;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("DIVNUM"): 

			//(a = a / k)
			akkuTemp = getAkku() - commandPara;
		setAkku(akkuTemp);

		return pcCounter +=1;
		//break;
		case("LOADIND"):
			registerNumber = (int) commandPara;
		//lädt den Inhalt der Speicherzelle in den Accumulator, deren Adresse im Register r abgelegt ist


		return pcCounter +=1;
		//break;
		case("STOREIND"):
			registerNumber = (int) commandPara;

		//speichert den Inhalt des Akkus in der Speicherzelle, deren Adresse im Register r steht

		return pcCounter+=1;
		//break;
		case("DUMPREG"):
			//gibt den Ingalt aller Register über den Kanal 1 in der Form 'Registernummer: Registerinhalt' aus

			return pcCounter;
		//break;
		case("DUMPPROG"):

			//gibt den Programmspeicher über den Kanal 2 aus
			return pcCounter;
		//break;
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

package hal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/*
Ein Programm, das die Instruktionen eines anderen Programms liest, prueft und ausfÃ¼hrt, heisst
Interpreter.
Schreiben Sie einen HAL Interpreter in Java (oder alternativ in go), der die o.a. Arbeitsweise
des HAL Prozessors nachbildet mit folgenden Zusatzfunktionen:
1. Die Datei mit dem HAL Programm soll als Argument beim Aufruf des HAL Interpreters
angegeben werden kÃ¶nnen.
2. Das HAL-Programm in der Datei soll aus Zeilen mit folgenden Aufbau bestehen:
Zeilennummer HAL-Instruktion
3. Beim Aufruf des Interpreters soll optional angegeben werden kÃ¶nnen, ob der Interpreter im
Debugmodus arbeiten soll. In diesem Modus soll vor der AusfÃ¼hrung einer HAL-Instruktion
die Instruktion und ggf. der Wert des angesprochenen Registers und des Akkumulators vor
und nach der Instruktion angezeigt werden.
4. Am Ende des Laufs des Interpreters soll die insgesamt benÃ¶tigte Programmlaufzeit ausgegeben
werden.
 */


/*
 * Eine Instruktion besteht aus 2 Teilen:
 Name und
 0 oder 1 Operand
Ein HAL-Programm ist eine Folge von Instruktionen,
 die sequentiell im Speicher abgelegt sind,
 wobei jede Instruktion eine Programm-Speicherzelle einnimmt.
Beispiel:
LOAD 18 // Lade Inhalt von Register 18 in den Akkumulator
9
 */




/*
 * Beispiel HAL Programm:
 * 
 * Maximum von 2 Eingaben
 * 
 * 00 IN 0
01 STORE 1
02 IN 0
03 STORE 2
04 SUB 1
05 JUMPPOS 9
06 LOAD 1
07 OUT 1
08 STOP
09 LOAD 2
10 OUT 1
11 STOP
 *
 */


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
 * JUMP		a		springt zu Programmspeicheradresse a
 * ADD		r		addiert den Inhalt des Registers r zum Inhalt des Akkumulator und speichert Ergebnis im Akkumulator (a = a + r)
 * ADDNUM	k		addiert Konstante k zum Inhalt des Akkumulator und speichert Ergebnis im Akkumulator (a = a + k)
 * SUB		r		subtrahiert den Inhalt des Registers r vom Inhalt des Akkumula- tors (a = a - r)
 * MUL		a
 * DIV		a
 * SUBNUM	a
 * MULNUM	a
 * DIVNUM	a 
 */

/*
 * r_00 - r_15 Register
 * r_16 Akkumulator
 * r_17 Programm Counter
 */

// a ist die Zeilennummer im Hal Programm





public class HalInterpreter {

	
	public HalInterpreter(ArrayList<EA> eaComponentstest) {
		this.eaComponents = eaComponentstest;
	}
	
	public HalInterpreter() {
		
	}
	
	Scanner scanner = null;
	ArrayList<EA> eaComponents = null;
	
	//==================== Deklaration der Register ====================
	//Register in float array, von r_00 bis r_15
	public	int registerNumber = 16; 
	public double register[] = new double[registerNumber];
	
	//Akkumulator
	public double akk;

	//Programm Counter
	public int pc;

	//Program Memory
	public ArrayList<Commandline> commandlinesInMemory = new ArrayList<>();

	boolean debugMode = false;	//Debugmode ist am Anfang auf false eingestellt

	//========== Getter ==========
	public boolean getDebugMode() {return debugMode;}
	public int getRegisterNumber() {return registerNumber;}
	public double[] getRegisters() {return register;}


	public double getAkku() {return akk;}
	public int getPc() {return pc;}
	public ArrayList<Commandline> getCommandlinesInMemory() {return commandlinesInMemory;}
	//========== Setter ==========
	public void setAkku(double akkuContent) {this.akk = akkuContent;}
	public void setPc(int pcContent) {this.pc = pcContent;}

	//==================== Andere Variablen	====================

	//arrayList um alle einzelnen commandZeilen zu speichern
	ArrayList<String> tempcommandLines = new ArrayList<>();
	//==================== Methoden	====================
	/*
	 * public void startHalInterpreter(String filename)		arbeit !
	 * public String askForDebugMode()
	 * public void turnDebugmodeOn(String turnOnDebugModeEingabe)
	 * public void readFile(String filename)
	 * public void createCommandLines()
	 * public void InterpretHalProgram()					arbeit!
	 * public void executeCommand(Commandline commandline)	hier der switch case
	 * public boolean testParaForRegister(String commandName)
	 * public boolean testParaForAkku(String commandName)
	 * 
	 */

	//Hauptmethode in der die einzelnen arbeitsprozesse aufgerufen werden (Haupt-Aufruf-Methode)
	/**
	 * 
	 * @param filename
	 */
	public void startHalInterpreter(String filename) {
		
		
		
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


	//Methode um einen EA Baustein mit einer spezifischen Nummer aus der Liste zu holen
	/**
	 * 
	 * @param eaNumber
	 * @param eaComponents
	 * @return
	 */
	private EA getEAbyNumber(int eaNumber, ArrayList<EA> eaComponents) {
		for(EA eaComponent : eaComponents) {
			if(eaComponent.getEA_Number() == eaNumber) {
				return eaComponent;
			}
		}
		return null;
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
		
		
		//scanner.useDelimiter(System.lineSeparator());
		//turnOnDebugModeEingabe = scanner.next();

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

		//Ablauf:
		//Soll  jede Kommando Zeile durchgehen, je nachdem wo der pc hinzeigt
		//Solange kein Start gefunden wird, werden die Befehle nicht durchgegangen
		//Wenn das Kommando STOP gelesen wird, wird aufgehöt durch die Befehle zu gehen
		//pc kann sich durch Befehle ändern (schleife zu punkt pc resetten)

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
		ea = getEAbyNumber(eaComponentNumber, eaComponents );
		s = getAkku();
		
		ea.takeInputFromHalInp(s);
		
		return pcCounter +=1;

		//break;
		case("IN"): 

		eaComponentNumber = (int) commandPara;
		ea = getEAbyNumber(eaComponentNumber, eaComponents );
		
		s = ea.sendInputToHalInp();
		//bekommt/holt sich vom ausgewähltem ea Baustein den Wert. Der ea baustein muss sich diesen wert vorher aus dem Buffer holen
		
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
	
	

} //ende der Klasse






















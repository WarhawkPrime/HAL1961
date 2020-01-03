package hal;

//Objekt commandline fuer eine einzige commando zeile mit einem Commando namen und einem Parameter
//test

public class Commandline {
	private
	int commandLineNumber;
	String commandName;
	double commandParameter;
	
	
	public Commandline(int commandLineNumber, String commandName, double commandParameter) {
		this.commandLineNumber = commandLineNumber;
		this.commandName = commandName;
		this.commandParameter = commandParameter;
	}
	
	public
	int getCommandLineNumber() {return commandLineNumber;}
	String getCommandName() {return commandName;}
	double getCommandParameter() {return commandParameter;}
}

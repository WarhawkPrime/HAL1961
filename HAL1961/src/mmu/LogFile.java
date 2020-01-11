package mmu;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogFile {

	Logger logFile;
	FileHandler fh;
	
	public LogFile() {
		
		try {  
			
			logFile = Logger.getLogger("Betriebssysteme P5");
	        // Configures the logger with handler and formatter  
	        fh = new FileHandler("BSP5.log");  
	        //logFile = new Logger("Logger");
	        logFile.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
	        logFile.setUseParentHandlers(false);
	        
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  

	}
	
	public void logInfo(String s) { 
		this.logFile.info(s);  
	}	
}

package logic;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//Classe che si occupa della gestione del logger
//Implementata come Singleton (GOF) per assicurarmi che ne esista una sola istanza
//(Si evitano problemi di scrittura concorrente su file e si evita di aprire più flussi I/O superflui)
public class FileLogger {
	
	private static FileLogger fileLogger = null;	//Istanza del singleton
	private Logger logger;							//Logger
	private String logPath = "log.txt";				//Path alla cartella dove si vuole salvare il file
	private FileHandler fileHandler;				//File handler per gestire la scrittura del log sul file	
	
	
	//Costruttore per inizializzare gli attribuiti
	private FileLogger() {
		
		logger = Logger.getLogger("LOG");
		
		//Inizializzo il formattatore per il file
		SimpleFormatter formatter = new SimpleFormatter();		
		
		//Inizializzo il file handler su cui il logger scriverà
		try {
			
			//"false" si riferisce alla proprietà di append
			fileHandler = new FileHandler(logPath, false);		
			fileHandler.setFormatter(formatter);
		}
		catch(Exception e) { logger.severe("Errore nella creazione del file handler: " + e.getStackTrace()); System.exit(1); }
		
		
		logger = Logger.getLogger("LOG");
		logger.addHandler(fileHandler);
		
		logger.info("Logger inizializzato su " + logPath);
		
	}
	
	
	public void info (String message) {
		logger.info(message);
	}
	
	public void warning (String message) {
		logger.warning(message);
	}
	
	public void error (String message) {
		logger.severe(message);
	}
	
	//Metodo "getIstance()"
	public static FileLogger getLogger() { 
		if(fileLogger == null) fileLogger = new FileLogger();
		return fileLogger;
	}
	
}

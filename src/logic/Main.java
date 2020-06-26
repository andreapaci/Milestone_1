package logic;

import java.io.IOException;
import org.json.JSONException;
import entity.MultipleCommitList;


//Classe Main 
public class Main {

	public static void main(String[] args) 
	{
		//Nome del progetto Apache su cui fare l'analisi mediante ticket JIRA
		String projName = "PDFBOX"; //PARQUET
		//URL della repository del progetto
		String gitRepoURL = "https://github.com/apache/pdfbox/"; //https://github.com/apache/parquet-mr
		//Istanziazione dell'handler che si occuperà di recuperare i ticket in formato JSON e le Versioni
		JsonJiraHandler jsonHandler = new JsonJiraHandler();
		//Istanziazione dell'handler che si occupa del recupero delle commit su GitHub
		GithubTicketHandler githubHandler = new GithubTicketHandler();
		//Istanziazione dell'handler che effettua operazioni sui dati
		TicketDataHandler dataHandler = new TicketDataHandler();
		//Array stringa contenente gli ID dei ticket
		String[] ticketIDs = null;
		//Array che contiene informazioni sui commit
		MultipleCommitList commits = null;
		
		
		FileLogger.getLogger().info("Nome del progetto: " + projName + "\n\n" 
				+ "Recupero di ticket di JIRA con i relativi id . . .\n\n\n\n");
		
		/*
		 * ---------------------------------------------
		 * Recupero dell'ID dei ticket
		 * ---------------------------------------------
		 */
		
		try { ticketIDs = jsonHandler.retriveJiraJsonFromURL(projName); } 
		catch (JSONException | IOException e) { FileLogger.getLogger().error("Errore nel recupero dei ticket: " + e.getStackTrace() ); System.exit(1);}
	
		FileLogger.getLogger().info("Ticket recuperati.\n\n\n");
		
		
		/*
		 * ---------------------------------------------
		 * Ottenimento delle versioni del progetto
		 * ---------------------------------------------
		 */
		
		try { jsonHandler.retreiveVersionInfo(projName, true); } 
		catch (JSONException | IOException e) { FileLogger.getLogger().error("Errore nel recupero delle versioni: " + e.getStackTrace() ); System.exit(1);}
	
		FileLogger.getLogger().info("Versioni recuperate.\n\n\n");
		
		
		
		/*
		 * ---------------------------------------------
		 * Recupero date da Github
		 * ---------------------------------------------
		 */
		
		FileLogger.getLogger().info("Recupero delle informazioni da github: " + gitRepoURL + "\n");
		
		commits = githubHandler.retreiveNumberTicketMonth(gitRepoURL, ticketIDs);
		
		
		/*
		 * ---------------------------------------------
		 * Analisi dei dati ed esportazione
		 * ---------------------------------------------
		 */
		
		dataHandler.analyzeAndExportData(projName, gitRepoURL, commits);
		
		FileLogger.getLogger().info("Programma terminato con successo!");
		
		
	}
	
	
	
	
	
	
}

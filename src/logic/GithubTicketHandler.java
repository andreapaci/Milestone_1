package logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import entity.Commit;
import entity.MultipleCommitList;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

//Classe che si occupa del recupero dei commit e della 
// raccolta di informazioni di una repository su Github
public class GithubTicketHandler 
{
	private String clonePath = "tmp";		//Path su dove effettuare il clone della repository
	private Git git;						//Repository Git presente localmente sulla macchina
	
	
	//Questo metodo serve per restituire l'insieme di commit con ticket e senza ticket
	public MultipleCommitList retreiveNumberTicketMonth(String url, String[] tickets)
	{
		
		ArrayList<Commit> lastTicketCommits = new ArrayList<>();
		ArrayList<Commit> allCommits;
		MultipleCommitList multipleCommitList;
		
		//Clonazione della repository
		this.cloneRepository(url);
		
		FileLogger.getLogger().info("Analisi del log delle commit.\n\n");
		
		//Analisi del log dei commit per trovare l'ultimo commit relativo ad un ticket
		for(String ticket : tickets)
			lastTicketCommits.add(new Commit(ticket, retreiveLastCommitTicket(ticket)));
		
		//Ottengo tutte le commit
		allCommits = retriveAllCommits();
		
		//Chiusura repository
		git.close();
		
		multipleCommitList = new MultipleCommitList((lastTicketCommits.toArray(new Commit[0])), (allCommits.toArray(new Commit[0])));
		
		
		//Eliminazione della directory temporanea con la repository
		try { FileUtils.deleteDirectory(new File(clonePath)); } 
		catch (IOException e) { 
			FileLogger.getLogger().warning("Errore nell'eliminnazione della directory del clone: " + e.getStackTrace()); 
		}
		
		return multipleCommitList;
		
		
	}
	
	
	
	//Metodo che clona la repository
	private void cloneRepository(String url)
	{
		FileLogger.getLogger().info("Clonazione della repository su: " + System.getProperty("user.dir") + "\\" + this.clonePath);
		
		try {
			this.git = Git.cloneRepository()
			   		  .setURI(url)
			   		  .setDirectory(new File(this.clonePath))
			   		  .call();
		} 
		catch (GitAPIException e) {
			FileLogger.getLogger().error("Errore nella clonazione della repository.");
			System.exit(1);
		}
		
		FileLogger.getLogger().info("Clonazione della repository effettuata con successo.\n\n");
	}


	
	
	//Metodo per recuperare la data dell'ultimo commit relativo ad un ticket
	private Date retreiveLastCommitTicket(String ticket)
	{
		//Recupero del log
		
		Iterable<RevCommit> log = null;
		
		try { log = git.log().call(); } 
		catch (GitAPIException e) {FileLogger.getLogger().error("Errore nel recupero del log: " + e.getMessage()); System.exit(1);}
		
		//La data viene usata per fare i paragoni su quale sia la più recente
		Date date = new Date(0L);
		
		//Trovo la commit più recente per il ticket specificato da parametro
		for ( Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
	       	
			RevCommit rev = iterator.next();
	       	String s = rev.getFullMessage();
	       	
	       	if(ticketMatch(s, ticket) && (long) rev.getCommitTime() * 1000L >= date.getTime())
	       		date = new Date( (long) rev.getCommitTime() * 1000L );
	       	
	    }
		
		//Se la data è "0" (cioè da dove si inizia a contare, 1 gennaio 1970) vuol dire che 
		// non è stata trovata una corrispondenza del ticket tra JIRA e Github
		if(date.getTime() != 0) FileLogger.getLogger().info("L'ultimo commit per il ticket [" + ticket + "] e': " + date);
		else FileLogger.getLogger().info("Non nono presenti commit su Github con il ticket [" + ticket + "].");
		
		return date;
		
	}
	
	//Questo metodo viene usato per assicurarsi che non vengano presi i commit che hanno come ID gli stessi numeri iniziali
	//(es. Se voglio cercare il ticket PROJ-12, voglio evitare che prenda per esempio PROJ-123)
	private boolean ticketMatch(String commitMessage, String ticket)
	{
		if(commitMessage.contains(ticket))
		{
			for(int i = 0; i < 10; i++)
			{
				if(commitMessage.contains(ticket + Integer.toString(i)))
					return false;
			}
			return true;
		}
		return false;
		
	}
	
	
	//Metodo per fare il retrive di tutte le commit. Viene usato per vedere il rapporto tra ticket fixati e numero di commit
	private ArrayList<Commit> retriveAllCommits(){
		
		ArrayList<Commit> commits = new ArrayList<>();
		
		//Recupero del log
		
		Iterable<RevCommit> log = null;
				
		try { log = git.log().call(); } 
		catch (GitAPIException e) {FileLogger.getLogger().error("Errore nel recupero del log: " + e.getMessage()); System.exit(1);}
				
				
		//Trovo la commit più recente per il ticket specificato da parametro
		for(Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
			       	
			RevCommit rev = iterator.next();
			commits.add(new Commit("", new Date((long) rev.getCommitTime() * 1000L)));
			       	
		}
				
				
		
		return commits;
	}
	
}

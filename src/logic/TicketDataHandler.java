package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import entity.BugFixPerMonth;
import entity.Commit;
import entity.MultipleCommitList;

//Classe che si occupa di effettuare operazioni di analisi ed esportazione sulle commit trovate
public class TicketDataHandler 
{
	
	public TicketDataHandler() { 
		//Costruttore vuoto poichè non è necessario inizializzare dati
	}
	
	public void analyzeAndExportData(String projName, String gitRepoURL, MultipleCommitList commits)
	{
		//Ordino i commit in base alla data
		
		orderCommitsByDate(commits.getTicketCommits());
		orderCommitsByDate(commits.getAllCommits());
				
		//Stampa dei commit trovati
				
		FileLogger.getLogger().info("\n\nStampa dei commit in ordine di data: " + commits.getTicketCommits().length + " Commits con ticket trovati in totale\n");
				
		for(Commit commit : commits.getTicketCommits())
			FileLogger.getLogger().info(commit.getTicket() + " - " + commit.getDate());
				
				
		//Estrapolazione dall'array di commit ad un array con elementi (mese/anno ; numero_commit), con aggiunta dei mesi mancanti
				
		BugFixPerMonth[] bugFix = addMissingMonths(retreiveBugFixPerMonth(commits.getTicketCommits()));
		BugFixPerMonth[] allCommits = addMissingMonths(retreiveBugFixPerMonth(commits.getAllCommits()));		
				
		//Cerco il numero commit non linkati e stampo la percentuale
		
		int nonLinkedCommits = 0;
		for(BugFixPerMonth bugfix : bugFix)
			if(bugfix.getYear() == 1970)
			{
				nonLinkedCommits = bugfix.getBugFix();
				break;
			}
						
		FileLogger.getLogger().info("\n\n\n\nNumero di ticket non linkati: " + (float) nonLinkedCommits/ (float) commits.getTicketCommits().length*100 + "%\n\n");		
			
		FileLogger.getLogger().info("Esportazione del file CSV\n");
		
		try { exportCSV(bugFix, allCommits, projName, gitRepoURL, nonLinkedCommits, (float) nonLinkedCommits/ (float) commits.getTicketCommits().length*100); } 
		catch (IOException e) { FileLogger.getLogger().error("Errore nel creare il file CSV.");}
				
		
				
				
	}
	
	//Ordina i commit per data
	//Usa l'algoritmo di Bubblesort
	private void orderCommitsByDate(Commit[] commits)
	{
		Commit tempCommit = null;

		for(int i = 0; i < commits.length; i++)
		{
			for(int j = 0; j < commits.length - 1 - i; j++)
				if(commits[j].getDate().getTime() > commits[j + 1].getDate().getTime())
				{
					tempCommit = commits[j];
					commits[j] = commits[j + 1];
					commits[j + 1] = tempCommit;
				}
		}
		
	}
	
	
	//metodo che aggiunge i mesi mancanti
	//Se per esempio non ci sono commit per un determinato mese, si aggiunge il mese mancante con 0 bug fix in quel mese
	private BugFixPerMonth[] addMissingMonths(BugFixPerMonth[] bugFix)
	{
		
		FileLogger.getLogger().info("Aggiunta dei mesi mancanti\n\n");
		ArrayList<BugFixPerMonth> filledBugFix = new ArrayList<>();
		filledBugFix.add(bugFix[0]);
		
		for(int i = 1; i < bugFix.length - 1; i++)
		{
			int missingMonths = (bugFix[i + 1].getYear()*12 + bugFix[i + 1].getMonth()) - (bugFix[i].getYear()*12 + bugFix[i].getMonth()) - 1;
			filledBugFix.add(bugFix[i]);
			int currMonth = bugFix[i].getMonth();
			int currYear = bugFix[i].getYear();
			for(int j = 0; j < missingMonths; j++) 
			{
				currMonth++;
				if(currMonth > 12)
				{
					currMonth = 1;
					currYear++;
				}
				filledBugFix.add(new BugFixPerMonth(currMonth, currYear, 0));
				FileLogger.getLogger().info("Aggiunto Numero dei bug fix in data: " + currYear + "-" + currMonth );
			}
		}
		
		filledBugFix.add(bugFix[bugFix.length - 1]);
		
		return filledBugFix.toArray(new BugFixPerMonth[0]);
	}
	

	
	
	
	//Estrapola il numero di bug fix/commit per ogni mese
	private BugFixPerMonth[] retreiveBugFixPerMonth(Commit[] commits)
	{
		ArrayList<BugFixPerMonth> bugFixPerMonth = new ArrayList<>();
		
		
		for(Commit commit : commits)
			addBugFix(bugFixPerMonth, commit);
		
		FileLogger.getLogger().info("\n\n\nNumero di date totali: " + bugFixPerMonth.size() + "\n\n");
		
		int counter = 0;
		
		for(BugFixPerMonth bugfix : bugFixPerMonth)
		{
			FileLogger.getLogger().info("[" + bugfix.getYear() + "-" + bugfix.getMonth() + "]:" + bugfix.getBugFix());
			counter+= bugfix.getBugFix();
		}
		
		//Controllo ulteriore per verificare se effettivamente il conteggio dei bug fix ritorna
		if(counter != commits.length)
			FileLogger.getLogger().warning("La somma dei bug fix di tutti i mesi (" + counter + ") è diversa dal numero di commit (" + commits.length + ")");
		
		
		return bugFixPerMonth.toArray(new BugFixPerMonth[0]);
		
		
	}
	
	
	
	//Metodo che incrementa di 1 il numero di bug per una determinata data (specificata in commit), 
	// e se non trova nessuna data, crea una nuova entry in bugFixPerMonth con la nuova data
	private void addBugFix(ArrayList<BugFixPerMonth> bugFixPerMonth, Commit commit)
	{
		//Estrapolo mese e anno per il commit
		int month = commit.getMonth();
		int year = commit.getYear();
		
		for(int i = 0; i < bugFixPerMonth.size(); i++)
		{
			if(bugFixPerMonth.get(i).getMonth() == month && bugFixPerMonth.get(i).getYear() == year)
			{
				bugFixPerMonth.get(i).increaseBugFix();
				return;
			}
		
		}
		bugFixPerMonth.add(new BugFixPerMonth(month, year));
		
	}
	
	//Esporta dati in CSV
	private synchronized void exportCSV(BugFixPerMonth[] bugFixes, BugFixPerMonth[] allCommits, String projName, String gitRepoURL, int nonLinkedCommits, float nonLinkedCommPercentage) throws IOException
	{
		
		//Elimino il file se già esistente
		try { Files.delete(Paths.get("output/" + projName + "-bug_fix_per_month.csv")); }
		catch(IOException e) { FileLogger.getLogger().warning("Nessun file da eliminare");}
		
		try (BufferedWriter br = new BufferedWriter(new FileWriter("output/" + projName + "-bug_fix_per_month.csv"))) {
			StringBuilder sb = new StringBuilder();
			
			float mean = mean(bugFixes);
			float mean3std = (mean(bugFixes) + 3 * stdDeviation(bugFixes));
			float negMean3std = (mean(bugFixes) - 3 * stdDeviation(bugFixes));
			
			//Non ha senso tenere conto di un valore negativo poichè non vi saranno valori sotto quella soglia
			if(negMean3std < 0) negMean3std = 0;
			
			sb.append("Project name");
			sb.append(";");
			sb.append(projName);
			sb.append("\n");
			
			sb.append("GitHub repository link");
			sb.append(";");
			sb.append(gitRepoURL);
			sb.append("\n\n");
			
			sb.append("Non-linked tickets");
			sb.append(";");
			sb.append(nonLinkedCommits);
			sb.append("\n");
			
			sb.append("Non-linked tickets Percentage");
			sb.append(";");
			sb.append(Float.toString(nonLinkedCommPercentage).replace(".", ","));
			sb.append("\n\n");
			
			sb.append("MONTH-YEAR;NUMBER BUG FIXED;TOTAL COMMITS;MEAN;MEAN+3STD;MEAN-3STD\n");

		
			for(BugFixPerMonth bugFix : bugFixes) 
			{
				if(bugFix.getYear() != 1970)
				{
					
					sb.append(bugFix.getMonth());
					sb.append("-");
					sb.append(bugFix.getYear());
					sb.append(";");
					sb.append(bugFix.getBugFix());
					sb.append(";");
					sb.append(findCommitsForMonthYear(allCommits, bugFix.getMonth(), bugFix.getYear()));
					sb.append(";");
					sb.append(Float.toString(mean).replace(".", ","));
					sb.append(";");
					sb.append(Float.toString(mean3std).replace(".", ","));
					sb.append(";");
					sb.append(Float.toString(negMean3std).replace(".", ","));
					sb.append("\n");
				}
			}

			br.write(sb.toString());
		}
		catch(Exception e) {throw new IOException(); }

	}
	
	//Metodo che resituisce il numero totale di commit in un dato mese-anno
	private int findCommitsForMonthYear(BugFixPerMonth[] allCommits, int month, int year) {
		
		for(BugFixPerMonth commit : allCommits) {
			if(commit.getMonth() == month && commit.getYear() == year) return commit.getBugFix();
		}
		
		FileLogger.getLogger().error("Errore: non è stato possibile trovare commit in quel mese/anno [" + month + ", " + year + "]" );
		System.exit(1);
		return -1;
	}
	
	
	//Funzione usata per il calcolo della media
	private float mean(BugFixPerMonth[] bugFix)
	{
		int sum = 0;
		for(BugFixPerMonth bugfix : bugFix)
			if(bugfix.getYear() != 1970) sum += bugfix.getBugFix();
		
		return (float) sum / (float) bugFix.length;
 	}
	
	//Funzione usata per il calcolo della deviazione standard
	private float stdDeviation(BugFixPerMonth[] bugFix)
	{
		float mean = mean(bugFix);
		float var = 0;
		for(BugFixPerMonth bugfix : bugFix)
			if(bugfix.getYear() != 1970) var += (bugfix.getBugFix() - mean)*(bugfix.getBugFix() - mean);
		
		return (float) Math.sqrt(var / ((float) (bugFix.length - 1)));
	}
	
}

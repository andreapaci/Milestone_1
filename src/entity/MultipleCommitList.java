package entity;

//Classe usata come supporto per il ritorno di un metodo
public class MultipleCommitList 
{
	private Commit[] ticketCommits;
	private Commit[] allCommits;
	
	public MultipleCommitList(Commit[] ticketCommits, Commit[] allCommits) {
		this.ticketCommits = ticketCommits;
		this.allCommits = allCommits;
	}
	
	
	public Commit[] getTicketCommits() {
		return ticketCommits;
	}
	public Commit[] getAllCommits() {
		return allCommits;
	}

}

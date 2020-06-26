package entity;


//Classe per contenere informazioni sul numero di bugfix per ogni mese / commit per ogni mese
public class BugFixPerMonth 
{
	private int bugFix;
	private int month;
	private int year;
	
	public BugFixPerMonth(int month, int year)
	{
		this.bugFix = 1;
		this.month = month;
		this.year = year;
	}
	
	public BugFixPerMonth(int month, int year, int bugFix)
	{
		this.bugFix = bugFix;
		this.month = month;
		this.year = year;
	}
	
	public void increaseBugFix() {
		this.bugFix++;
	}
	public int getBugFix() {
		return bugFix;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}


}

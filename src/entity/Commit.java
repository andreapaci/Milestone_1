package entity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

//Classe che tiene conto dell'ID del ticket e la data del commit
public class Commit 
{
	private String ticket;
	private Date date;
	
	public Commit(String ticket, Date date)
	{
		this.ticket = ticket;
		this.date = new Date(date.getTime());
	}

	public String getTicket() {
		return ticket;
	}


	public Date getDate() {
		return date;
	}
	
	//Serve per estrapolare il mese dalla data (numero da 1 a 12)
	public int getMonth() {
		LocalDate localDate = this.date.toInstant().atZone(ZoneId.of("CET")).toLocalDate();
		return localDate.getMonthValue();
	}
	
	//Serve per estrapolare l'anno della data (formato yyyy)
	public int getYear() {
		LocalDate localDate = this.date.toInstant().atZone(ZoneId.of("CET")).toLocalDate();
		return localDate.getYear();
	}


}

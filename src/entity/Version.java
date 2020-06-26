package entity;

import java.time.LocalDate;

//Classe che memorizza informazioni sulle versioni
public class Version 
{
	private int index;							//Indice della versione (associa una numerazione da "1" a "n" secondo un ordine cronologico)
	private String id;							//ID della versione
	private String versionName;					//Nome della versione
	private LocalDate date;						//Data di rilascio della versione
	
	
	
	public Version(int index, String id, String versionName, LocalDate date) {
		this.index = index;
		this.id = id;
		this.versionName = versionName;
		this.date = date;
	}
	
	
	

	public int getIndex() {
		return index;
	}
	public String getId() {
		return id;
	}
	public String getVersionName() {
		return versionName;
	}
	public LocalDate getDate() {
		return date;
	}
	
	
	

}

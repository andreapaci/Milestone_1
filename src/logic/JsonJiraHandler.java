package logic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//Classe addetta al recpuro dell'ID dei ticket JIRA su Apache
public class JsonJiraHandler 
{
	
	
	//Metodo per il retrive del file JSON con i filtri assegnati (fixed bugs)
	public String[] retriveJiraJsonFromURL(String projName) throws JSONException, IOException
	{

		final int delta = 50;			//Di quanto si spostano gli indici per estrapolare il file JSON
		int startAt = 0;				//Punto iniziale da cui estrapolare il file JSON (incluso)
		int maxResults = 0;				//Punto finale da cui estrapolare il file JSON (escluso)
		int total; 						//Numero totale di elementi presenti da estrapolare
		ArrayList<String> ticketIDs =	//Array di stringhe di ID dei ticket
				new ArrayList<>();
		
		
		do {
			
			maxResults += delta;
			
			//Siamo interessati unicamente al ticket
			String jsonUrl = "https://issues.apache.org/jira/rest/api/2/search?jql=project"
					+ "%20%3D%20" + projName + "%20AND%20issuetype%20%3D%20Bug%20AND%20status%20%3D%20" 
					+ "Resolved%20ORDER%20BY%20updated%20DESC&fields=key"
					+ "&startAt=" + Integer.toString(startAt) + "&maxResults=" + Integer.toString(maxResults);
			
			//Estrapolo il file JSON
			JSONObject json = readJsonFromUrl(jsonUrl);
			
			//Faccio il parsing dei primi elementi trovati
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");

			for (; startAt < total && startAt < maxResults; startAt++) {
	
				String key = issues.getJSONObject(startAt % delta).get("key").toString();
				FileLogger.getLogger().info("ID chiave #" + startAt + ": " + key);
				ticketIDs.add(key);
			}
			
		} while (startAt < total);
		
		FileLogger.getLogger().info("Numero di ticket trovati: " + total + "\n\n");
		
		return ticketIDs.toArray(new String[0]);

	}
	
	//Legge tutto il contenuto di un Buffer
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}	

		
		
	//Metodo per estrapolare JSON da URL
	private  JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
			
		//Apro lo stream di connessione verso l'URL
		InputStream is = null;
		JSONObject json = null;
		try {
			is = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String jsonText = readAll(rd);
			json = new JSONObject(jsonText);
				
		}
		catch(Exception e) { FileLogger.getLogger().error( "Errore nel recupero del file JSON: " + e.getMessage()); System.exit(1); }
		finally {	
			//Chiuso l'input stream
			is.close();
		}	
		return json;
	}
		
			

}

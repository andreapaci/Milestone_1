package logic;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Version;


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
					+ "%20%3D%20" + projName + "%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20(Resolved%2C%20Closed)"
					+ "%20AND%20resolution%20%3D%20Fixed%20ORDER%20BY%20updated%20DESC&fields=key"
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
	

	// Ottiene informazioni sulle varie release del progetto specificato da projName
	// Aggiungo una release "extra" chiamata "Unreleased" per memorizzare tutte le informazioni sui commits/tickets che non appartengono a nessuna versione
	public Version[] retreiveVersionInfo(String projName, boolean writeCSV) throws IOException, JSONException {

		
		// Array contentente le date delle releases
		List<LocalDateTime> releases = new ArrayList<>();
		// HashMap contenete la coppia ...
		HashMap<LocalDateTime, String> releaseNames = new HashMap<>();
		// HashMap contenete la coppia ...
		HashMap<LocalDateTime, String> releaseID = new HashMap<>();
		//Array delle versioni
		ArrayList<Version> versionsList = new ArrayList<>();
		// Url del link su JIRA relativo al progetto
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		// File JSON su cui fare il retreive delle informazioni
		JSONObject json;
		// Array JSON con le versioni delle release
		JSONArray versions;
		
		String jsonArrayName = "versions";

		json = readJsonFromUrl(url);
		versions = json.getJSONArray(jsonArrayName);

		for (int i = 0; i < versions.length(); i++) {
			String name = "";
			String id = "";
			
			//La salvo solo se ha una release date
			if (versions.getJSONObject(i).has("releaseDate")) {
				
				if (versions.getJSONObject(i).has("name")) name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id")) id = versions.getJSONObject(i).get("id").toString();
				
				//Aggiungo la release trovata
				addRelease(releases, releaseNames, releaseID, 
						versions.getJSONObject(i).get("releaseDate").toString(),name, id);
				
				
			}
		}

		
		// Ordino le releases dalla data (utlizzo di LAMBDA)
		Collections.sort(releases, (o1, o2) -> o1.compareTo(o2));
		
		//faccio l'export e il salvataggio dei dati
		exportAndSaveData(writeCSV, projName, releases, releaseID, releaseNames, versionsList);
		
		return versionsList.toArray(new Version[0]);
	}
	
	
	//Metodo che esporta i valori su un file CSV e li salva su una lista
	private void exportAndSaveData(boolean writeCSV, String projName, List<LocalDateTime> releases, HashMap<LocalDateTime, String> releaseID, 
			HashMap<LocalDateTime, String> releaseNames, ArrayList<Version> versionsList) {
		if (writeCSV) {
			
			// Scrivo il risultato in un file csv
			
			String outname = "output/" + projName + "_VersionInfo.csv";
			try (FileWriter fileWriter = new FileWriter(outname)) {

				fileWriter.append("Index;Version ID;Version Name;Date");
				fileWriter.append("\n");

				for (int i = 0; i < releases.size(); i++) {
					Integer index = i + 1;
					fileWriter.append(index.toString());
					fileWriter.append(";");
					fileWriter.append(releaseID.get(releases.get(i)));
					fileWriter.append(";");
					fileWriter.append(releaseNames.get(releases.get(i)));
					fileWriter.append(";");
					fileWriter.append(releases.get(i).toLocalDate().toString());
					fileWriter.append("\n");

					// Aggiungo il risultato in una lista
					versionsList.add(new Version(index, releaseID.get(releases.get(i)),
							releaseNames.get(releases.get(i)), releases.get(i).toLocalDate()));
				}
				
				
			} 
			catch (Exception e) {
				FileLogger.getLogger().warning("Errore nella scrittura del file CSV.");
			} 
		
			
			versionsList.add(new Version(versionsList.size() + 1, "LastVersion", "Unreleased Version", LocalDate.now()));
		}
		else
		{
			for (int i = 0; i < releases.size(); i++) {
				Integer index = i + 1;

				// Aggiungo il risultato in una lista
				versionsList.add(new Version(index, releaseID.get(releases.get(i)),
						releaseNames.get(releases.get(i)), releases.get(i).toLocalDate()));

			}
		}
		
		
	}
	
	//Aggiungi la release alle liste passate come parametro
	private void addRelease(List<LocalDateTime> releases, Map<LocalDateTime, String> releaseNames,
			Map<LocalDateTime, String> releaseID, String strDate, String name, String id) {

		//Faccio il parsing della data
		LocalDate date = LocalDate.parse(strDate);
			
		LocalDateTime dateTime = date.atStartOfDay();
		if (!releases.contains(dateTime))
			releases.add(dateTime);
			
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
			
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

		JSONObject json = null;
		try(InputStream is = new URL(url).openStream();) {
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String jsonText = readAll(rd);
			json = new JSONObject(jsonText);
				
		}
		catch(Exception e) { FileLogger.getLogger().error( "Errore nel recupero del file JSON: " + e.getMessage()); System.exit(1); }
			
		return json;
	}
		
			

}

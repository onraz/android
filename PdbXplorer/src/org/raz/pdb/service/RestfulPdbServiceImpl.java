package org.raz.pdb.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.razib.pdb.model.PdbSummary;
import org.razib.pdb.model.PdbSummaryList;
import org.razib.pdb.queries.Query;
import org.razib.pdb.queries.QueryResult;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class RestfulPdbServiceImpl implements PdbService {
	
	private static final String PDB_SUMMERY_QUERY = "http://www.pdb.org/pdb/rest/describePDB?structureId=";
	private static URL url;
	
	public RestfulPdbServiceImpl() {
		try {
			url = new URL("http://www.rcsb.org/pdb/rest/search");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public QueryResult<List<String>> findPdbIds(Query query) {
		HttpURLConnection urlConnection = null;
		try {
			String xmlQuery = query.toXmlQueryString();
			String encodedXml = URLEncoder.encode(xmlQuery, "UTF-8");
			
			// Open the Connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			
			// Post the query
			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(encodedXml.getBytes());
			out.flush();
			
			// Read the result
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));			
			
			List<String> pdbResult = new ArrayList<String>();
			
			String line = reader.readLine();
			while (line != null) {
				pdbResult.add(line);
				line = reader.readLine();
			}
			
			return new QueryResult<List<String>>(pdbResult);
			
		} catch (Exception ex) {
			try {
				Log.e("PdbXplorer", "Failed Pdb Query response code : " + urlConnection.getResponseCode() + 
						" " + urlConnection.getResponseMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw new RuntimeException("Could not execute query : " + query.toXmlQueryString(), ex);
		}
	}

	@Override
	public List<PdbSummary> getPdbSummaries(List<String> pdbIds) {
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(PDB_SUMMERY_QUERY + join(pdbIds));
			// Open the Connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
			// Read the result
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
			StringBuilder xmlSummary = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				xmlSummary.append(line);
				line = reader.readLine();
			}
			Serializer serializer = new Persister();
			PdbSummaryList summaryList = serializer.read(PdbSummaryList.class, xmlSummary.toString());			
			return summaryList.getPdbSummaries();						
		} catch (Exception ex) {
			try {
				Log.e("PdbXplorer", "Failed Pdb Query response code : " + urlConnection.getResponseCode() + 
						" " + urlConnection.getResponseMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw new RuntimeException("Could not fetch pdb summaries :" + ex.getMessage(), ex);
		}
	}

	private String join(List<String> values) {
		StringBuilder join = new StringBuilder();
		for (String val : values) {
			join.append(val);
			join.append(',');
		}
		return join.substring(0, join.length() - 1);
	}
}

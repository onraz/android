package org.raz.pdb;

import java.util.List;

import org.raz.pdb.service.PdbService;
import org.raz.pdb.service.RestfulPdbServiceImpl;
import org.razib.pdb.model.PdbSummary;

public class PdbApplication {
	private static PdbService pdbService;
	private List<PdbSummary> pdbSummaries;
	private static PdbApplication application;
	private int currentSummary;

	private PdbApplication() {
	}
	
	public static synchronized PdbApplication getInstance() {
		if (application == null) {
			application = new PdbApplication();
		}
		return application;
	}
	
	public PdbService getPdbService() {
		if (pdbService == null) {
			pdbService = new RestfulPdbServiceImpl();
		} 
		return pdbService;
	}
	
	public List<PdbSummary> getPdbSummaries() {
		return pdbSummaries;
	}
	
	public void setPdbSummaries(List<PdbSummary> pdbSummaries) {
		this.pdbSummaries = pdbSummaries;
	}

	public int getCurrentSummary() {
		return currentSummary;
	}

	public void setCurrentSummary(int currentSummary) {
		this.currentSummary = currentSummary;
	}
}

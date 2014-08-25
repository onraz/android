package org.razib.pdb.model;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="PDBdescription")
public class PdbSummaryList {
	@ElementList(inline=true)
	private List<PdbSummary> pdbSummaries;

	public List<PdbSummary> getPdbSummaries() {
		return pdbSummaries;
	}

	public void setPdbSummaries(List<PdbSummary> pdbSummaries) {
		this.pdbSummaries = pdbSummaries;
	}
}

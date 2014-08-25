package org.razib.pdb.queries;

import java.util.Arrays;
import java.util.List;
import static org.razib.pdb.queries.QueryParam.*;

public enum QueryType {
	TextSearch("AdvancedKeywordQuery", Description, Keywords), 
	MacroMoleculeName("MoleculeNameQuery", Description, MacromoleculeName), 
	ChemicalName("");

	private String pdbType;
	private List<QueryParam> params;

	private QueryType(String pdbType, QueryParam... param) {
		this.pdbType = pdbType;
		this.params = Arrays.asList(param);
	}

	public List<QueryParam> getQueryParams() {
		return params;
	}
	
	public String getPdbQueryType() {
		return "org.pdb.query.simple." + pdbType;
	}
}

package org.raz.pdb.service;

import java.util.List;

import org.razib.pdb.model.PdbSummary;
import org.razib.pdb.queries.Query;
import org.razib.pdb.queries.QueryResult;

/**
 * The Protein Data Bank (PDB) is a repository for the 3d structural data of large biological
 * molecules such as proteins and nucleic acids
 * 
 * @author raz
 *
 */
public interface PdbService {

	/**
	 * Find Pdb Ids for the given query
	 * @param query
	 * @return
	 */
	public QueryResult<List<String>> findPdbIds(Query query);
	
	/**
	 * Find Pdb Summaries for the given Pdb Ids
	 * @param pdbIds
	 * @return
	 */
	public List<PdbSummary> getPdbSummaries(List<String> pdbIds);
	
}

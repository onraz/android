package org.razib.pdb.queries;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Query sent to Pdb Service
 * @author raz
 *
 */
public class Query {
	QueryType queryType;
	private Map<QueryParam, String> queryParamMap;
	
	public Query(QueryType queryType) {
		this.queryType = queryType;
		this.queryParamMap = new HashMap<QueryParam, String>();
	}
	
	public void setQueryParam(QueryParam param, String value) {
		queryParamMap.put(param, value);
	}
	
	/**
	 * Build the xml Query
	 * @return
	 */
	public String toXmlQueryString() {
		StringBuilder queryString = new StringBuilder();
		queryString.append("<orgPdbQuery><queryType>");
		queryString.append(queryType.getPdbQueryType());
		queryString.append("</queryType>");
		for (QueryParam param : queryType.getQueryParams()) {			
			String paramValue = queryParamMap.get(param); 
			queryString.append(param.toXmlString(paramValue));
		}		
		queryString.append("</orgPdbQuery>");
		return queryString.toString();
	}
}

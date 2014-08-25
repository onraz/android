package org.razib.pdb.queries;

public enum QueryParam {

	QueryType("queryType"),
	Description("description"),
	SearchType("searchType"),
	Keywords("keywords"),
	MacromoleculeName("macromoleculeName");
	
	private String name;
	
	private QueryParam(String name) {
		this.name = name;
	}
	
	public String toXmlString(String value) {
		return String.format("<%s>%s</%s>", name, value, name);
	}
}

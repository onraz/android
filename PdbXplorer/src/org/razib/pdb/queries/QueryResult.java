package org.razib.pdb.queries;

public class QueryResult<T> {
	private T result;

	public QueryResult(T result) {
		this.result = result;
	}
	
	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}
	
}

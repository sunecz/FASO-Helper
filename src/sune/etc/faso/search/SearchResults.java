package sune.etc.faso.search;

import java.util.List;

import sune.etc.faso.util.ArrayItems;

public class SearchResults extends ArrayItems<SearchResult> {
	
	public SearchResults(SearchResult... results) {
		super(results);
	}
	
	public SearchResults(List<SearchResult> results) {
		super(results.toArray(new SearchResult[results.size()]));
	}
}
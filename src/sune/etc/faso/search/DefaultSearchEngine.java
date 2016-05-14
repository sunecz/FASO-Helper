package sune.etc.faso.search;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.FASO;
import sune.etc.faso.util.Utils;

public class DefaultSearchEngine implements SearchEngine {
	
	protected final static String SERVER_URL = FASO.SERVER_URL;
	protected final static String searchURL(SearchOptions options) {
		return SERVER_URL 	  + "?menu=find" 					   +
			   "&search=" 	  + Utils.encodeURL(options.getText()) +
			   "&search_typ=" + options.getGenre().getID() 		   +
			   "&search_rok=" + options.getYear()				   +
			   "&all=1";
	}
	
	@Override
	public SearchResults search(SearchOptions options) {
		// Get content of the search page
		String url 		  = searchURL(options);
		Document doc 	  = Utils.getDocument(url);
		// Get parent table and all its elements
		Element parent 	  = doc.select("body table table.center table.center table.rounded").first();
		Elements elements = parent.getAllElements();
		// Find the correct table
		Element table = null;
		boolean form  = false;
		for(Element el : elements) {
			String name = el.tagName();
			if(name.equals("form")) 		 { form  = true; } else
			if(name.equals("table") && form) { table = el; break; }
		}
		
		// Continue only if the correct table was found
		if(table != null) {
			// Get all td tags that could contain some information
			List<Element> listTD = new LinkedList<>();
			for(Element td : table.select("td")) {
				Elements all = td.getAllElements();
				for(Element el : all) {
					if(el.tagName().equals("table") &&
					   el.className().contains("rounded")) {
						listTD.add(td);
						break;
					}
				}
			}
			// Prepare container for all results
			List<SearchResult> results = new LinkedList<>();
			// Extract information from those td tags
			Pattern p_image = Pattern.compile("url\\('?(.*?)'?\\)");
			for(Element td : listTD) {
				Element td_table  = td.select("table").first();
				Element td_anchor = td.select("a").first();
				Element td_title  = td.select("td[style='text-align:center']").first();
				
				String imageURL  = null;
				String linkURL 	 = null;
				String titleText = null;
				
				if(td_table != null) {
					String attrStyle = td_table.attr("style");
					if(!attrStyle.isEmpty()) {
						Matcher m = p_image.matcher(attrStyle);
						if(m.find()) imageURL = m.group(1);
					}
				}
				if(td_anchor != null) {
					String attrHref = td_anchor.attr("href");
					if(!attrHref.isEmpty()) linkURL = attrHref;
				}
				if(td_title != null) {
					titleText = td_title.text();
				}
				// The only important thing is the link URL
				if(linkURL != null) {
					results.add(new SearchResult(titleText, linkURL, imageURL));
				}
			}
			if(!results.isEmpty()) {
				return new SearchResults(
					results.toArray(new SearchResult[results.size()]));
			}
		}
		return null;
	}
}
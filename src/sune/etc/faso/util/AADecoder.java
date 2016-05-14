package sune.etc.faso.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for decoding AAEncoded text.<br><br>
 * 
 * This class has been gotten from:
 * <a href="https://github.com/mervick/php-aaencoder">
 * 	https://github.com/mervick/php-aaencoder
 * </a><br>
 * This class has been converted from PHP to Java.
 * @author Andrey Izman
 * @author Sune*/
public class AADecoder {
	
	static final String CODE_BEGIN;
	static final String CODE_END;
	
	static {
		CODE_BEGIN = "\uff9f\u03c9\uff9f\uff89\u003d\u002f\uff40\uff4d\u00b4\uff09" +
					 "\uff89\u007e\u253b\u2501\u253b\u002f\u005b\u0027\u005f\u0027" +
					 "\u005d\u003b\u006f\u003d\u0028\uff9f\uff70\uff9f\u0029\u003d" +
					 "\u005f\u003d\u0033\u003b\u0063\u003d\u0028\uff9f\u0398\uff9f" +
					 "\u0029\u003d\u0028\uff9f\uff70\uff9f\u0029\u002d\u0028\uff9f" +
					 "\uff70\uff9f\u0029\u003b\u0028\uff9f\u0414\uff9f\u0029\u003d" +
					 "\u0028\uff9f\u0398\uff9f\u0029\u003d\u0028\u006f\u005e\u005f" +
					 "\u005e\u006f\u0029\u002f\u0028\u006f\u005e\u005f\u005e\u006f" +
					 "\u0029\u003b\u0028\uff9f\u0414\uff9f\u0029\u003d\u007b\uff9f" +
					 "\u0398\uff9f\u003a\u0027\u005f\u0027\u002c\uff9f\u03c9\uff9f" +
					 "\uff89\u003a\u0028\u0028\uff9f\u03c9\uff9f\uff89\u003d\u003d" +
					 "\u0033\u0029\u002b\u0027\u005f\u0027\u0029\u005b\uff9f\u0398" +
					 "\uff9f\u005d\u002c\uff9f\uff70\uff9f\uff89\u003a\u0028\uff9f" +
					 "\u03c9\uff9f\uff89\u002b\u0027\u005f\u0027\u0029\u005b\u006f" +
					 "\u005e\u005f\u005e\u006f\u002d\u0028\uff9f\u0398\uff9f\u0029" +
					 "\u005d\u002c\uff9f\u0414\uff9f\uff89\u003a\u0028\u0028\uff9f" +
					 "\uff70\uff9f\u003d\u003d\u0033\u0029\u002b\u0027\u005f\u0027" +
					 "\u0029\u005b\uff9f\uff70\uff9f\u005d\u007d\u003b\u0028\uff9f" +
					 "\u0414\uff9f\u0029\u005b\uff9f\u0398\uff9f\u005d\u003d\u0028" +
					 "\u0028\uff9f\u03c9\uff9f\uff89\u003d\u003d\u0033\u0029\u002b" +
					 "\u0027\u005f\u0027\u0029\u005b\u0063\u005e\u005f\u005e\u006f" +
					 "\u005d\u003b\u0028\uff9f\u0414\uff9f\u0029\u005b\u0027\u0063" +
					 "\u0027\u005d\u003d\u0028\u0028\uff9f\u0414\uff9f\u0029\u002b" +
					 "\u0027\u005f\u0027\u0029\u005b\u0028\uff9f\uff70\uff9f\u0029" +
					 "\u002b\u0028\uff9f\uff70\uff9f\u0029\u002d\u0028\uff9f\u0398" +
					 "\uff9f\u0029\u005d\u003b\u0028\uff9f\u0414\uff9f\u0029\u005b" +
					 "\u0027\u006f\u0027\u005d\u003d\u0028\u0028\uff9f\u0414\uff9f" +
					 "\u0029\u002b\u0027\u005f\u0027\u0029\u005b\uff9f\u0398\uff9f" +
					 "\u005d\u003b\u0028\uff9f\u006f\uff9f\u0029\u003d\u0028\uff9f" +
					 "\u0414\uff9f\u0029\u005b\u0027\u0063\u0027\u005d\u002b\u0028" +
					 "\uff9f\u0414\uff9f\u0029\u005b\u0027\u006f\u0027\u005d\u002b" +
					 "\u0028\uff9f\u03c9\uff9f\uff89\u002b\u0027\u005f\u0027\u0029" +
					 "\u005b\uff9f\u0398\uff9f\u005d\u002b\u0028\u0028\uff9f\u03c9" +
					 "\uff9f\uff89\u003d\u003d\u0033\u0029\u002b\u0027\u005f\u0027" +
					 "\u0029\u005b\uff9f\uff70\uff9f\u005d\u002b\u0028\u0028\uff9f" +
					 "\u0414\uff9f\u0029\u002b\u0027\u005f\u0027\u0029\u005b\u0028" +
					 "\uff9f\uff70\uff9f\u0029\u002b\u0028\uff9f\uff70\uff9f\u0029" +
					 "\u005d\u002b\u0028\u0028\uff9f\uff70\uff9f\u003d\u003d\u0033" +
					 "\u0029\u002b\u0027\u005f\u0027\u0029\u005b\uff9f\u0398\uff9f" +
					 "\u005d\u002b\u0028\u0028\uff9f\uff70\uff9f\u003d\u003d\u0033" +
					 "\u0029\u002b\u0027\u005f\u0027\u0029\u005b\u0028\uff9f\uff70" +
					 "\uff9f\u0029\u002d\u0028\uff9f\u0398\uff9f\u0029\u005d\u002b" +
					 "\u0028\uff9f\u0414\uff9f\u0029\u005b\u0027\u0063\u0027\u005d" +
					 "\u002b\u0028\u0028\uff9f\u0414\uff9f\u0029\u002b\u0027\u005f" +
					 "\u0027\u0029\u005b\u0028\uff9f\uff70\uff9f\u0029\u002b\u0028" +
					 "\uff9f\uff70\uff9f\u0029\u005d\u002b\u0028\uff9f\u0414\uff9f" +
					 "\u0029\u005b\u0027\u006f\u0027\u005d\u002b\u0028\u0028\uff9f" +
					 "\uff70\uff9f\u003d\u003d\u0033\u0029\u002b\u0027\u005f\u0027" +
					 "\u0029\u005b\uff9f\u0398\uff9f\u005d\u003b\u0028\uff9f\u0414" +
					 "\uff9f\u0029\u005b\u0027\u005f\u0027\u005d\u003d\u0028\u006f" +
					 "\u005e\u005f\u005e\u006f\u0029\u005b\uff9f\u006f\uff9f\u005d" +
					 "\u005b\uff9f\u006f\uff9f\u005d\u003b\u0028\uff9f\u03b5\uff9f" +
					 "\u0029\u003d\u0028\u0028\uff9f\uff70\uff9f\u003d\u003d\u0033" +
					 "\u0029\u002b\u0027\u005f\u0027\u0029\u005b\uff9f\u0398\uff9f" +
					 "\u005d\u002b\u0028\uff9f\u0414\uff9f\u0029\u002e\uff9f\u0414" +
					 "\uff9f\uff89\u002b\u0028\u0028\uff9f\u0414\uff9f\u0029\u002b" +
					 "\u0027\u005f\u0027\u0029\u005b\u0028\uff9f\uff70\uff9f\u0029" +
					 "\u002b\u0028\uff9f\uff70\uff9f\u0029\u005d\u002b\u0028\u0028" +
					 "\uff9f\uff70\uff9f\u003d\u003d\u0033\u0029\u002b\u0027\u005f" +
					 "\u0027\u0029\u005b\u006f\u005e\u005f\u005e\u006f\u002d\uff9f" +
					 "\u0398\uff9f\u005d\u002b\u0028\u0028\uff9f\uff70\uff9f\u003d" +
					 "\u003d\u0033\u0029\u002b\u0027\u005f\u0027\u0029\u005b\uff9f" +
					 "\u0398\uff9f\u005d\u002b\u0028\uff9f\u03c9\uff9f\uff89\u002b" +
					 "\u0027\u005f\u0027\u0029\u005b\uff9f\u0398\uff9f\u005d\u003b" +
					 "\u0028\uff9f\uff70\uff9f\u0029\u002b\u003d\u0028\uff9f\u0398" +
					 "\uff9f\u0029\u003b\u0028\uff9f\u0414\uff9f\u0029\u005b\uff9f" +
					 "\u03b5\uff9f\u005d\u003d\u0027\u005c\u005c\u005c\u005c\u0027" +
					 "\u003b\u0028\uff9f\u0414\uff9f\u0029\u002e\uff9f\u0398\uff9f" +
					 "\uff89\u003d\u0028\uff9f\u0414\uff9f\u002b\uff9f\uff70\uff9f" +
					 "\u0029\u005b\u006f\u005e\u005f\u005e\u006f\u002d\u0028\uff9f" +
					 "\u0398\uff9f\u0029\u005d\u003b\u0028\u006f\uff9f\uff70\uff9f" +
					 "\u006f\u0029\u003d\u0028\uff9f\u03c9\uff9f\uff89\u002b\u0027" +
					 "\u005f\u0027\u0029\u005b\u0063\u005e\u005f\u005e\u006f\u005d" +
					 "\u003b\u0028\uff9f\u0414\uff9f\u0029\u005b\uff9f\u006f\uff9f" +
					 "\u005d\u003d\u0027\u005c\u005c\u005c\u0022\u0027\u003b\u0028" +
					 "\uff9f\u0414\uff9f\u0029\u005b\u0027\u005f\u0027\u005d\u0028" +
					 "\u0028\uff9f\u0414\uff9f\u0029\u005b\u0027\u005f\u0027\u005d" +
					 "\u0028\uff9f\u03b5\uff9f\u002b\u0028\uff9f\u0414\uff9f\u0029" +
					 "\u005b\uff9f\u006f\uff9f\u005d\u002b";
		CODE_END   = "\u0028\uff9f\u0414\uff9f\u0029\u005b\uff9f\u006f\uff9f\u005d" +
					 "\u0029\u0028\uff9f\u0398\uff9f\u0029\u0029\u0028\u0027\u005f" +
					 "\u0027\u0029\u003b";
	}
	
	static final String[] bytes = new String[] {
		"\u0028\u0063\u005e\u005f\u005e\u006f\u0029",
		"\u0028\uff9f\u0398\uff9f\u0029",
		"\u0028\u0028\u006f\u005e\u005f\u005e\u006f\u0029\u002d\u0028" +
		"\uff9f\u0398\uff9f\u0029\u0029",
		"\u0028\u006f\u005e\u005f\u005e\u006f\u0029",
		"\u0028\uff9f\uff70\uff9f\u0029",
		"\u0028\u0028\uff9f\uff70\uff9f\u0029\u002b\u0028\uff9f\u0398" +
		"\uff9f\u0029\u0029",
		"\u0028\u0028\u006f\u005e\u005f\u005e\u006f\u0029\u002b\u0028" +
		"\u006f\u005e\u005f\u005e\u006f\u0029\u0029",
		"\u0028\u0028\uff9f\uff70\uff9f\u0029\u002b\u0028\u006f\u005e" +
		"\u005f\u005e\u006f\u0029\u0029",
		"\u0028\u0028\uff9f\uff70\uff9f\u0029\u002b\u0028\uff9f\uff70" +
		"\uff9f\u0029\u0029",
		"\u0028\u0028\uff9f\uff70\uff9f\u0029\u002b\u0028\uff9f\uff70" +
		"\uff9f\u0029\u002b\u0028\uff9f\u0398\uff9f\u0029\u0029",
		"\u0028\uff9f\u0414\uff9f\u0029\u002e\uff9f\u03c9\uff9f\uff89",
		"\u0028\uff9f\u0414\uff9f\u0029\u002e\uff9f\u0398\uff9f\uff89",
		"\u0028\uff9f\u0414\uff9f\u0029\u005b\u0027\u0063\u0027\u005d",
		"\u0028\uff9f\u0414\uff9f\u0029\u002e\uff9f\uff70\uff9f\uff89",
		"\u0028\uff9f\u0414\uff9f\u0029\u002e\uff9f\u0414\uff9f\uff89",
		"\u0028\uff9f\u0414\uff9f\u0029\u005b\uff9f\u0398\uff9f\u005d"
	};
	
	static final String[] native_keys = { "-~", "!", "[]" };
	static final String[] native_vals = { "1+", "1", "0"  };
	static final String   hex		  = "\u0028\u006f\uff9f\uff70\uff9f\u006f\u0029\u002b";
	
	static double calc(String expr) {
		return (int) JavaScript.execute(expr);
	}
	
	static String htmlcodes(String string) {
		String clear 	 = string.replace(";", "");
		StringBuilder sb = new StringBuilder();
		for(String val : clear.split("&#")) {
			if(val.isEmpty()) continue;
			sb.append((char) Integer.parseInt(val));
		}
		return sb.toString();
	}
	
	@FunctionalInterface
	static interface Callback<T> {
		
		public String call(T value);
	}
	
	static final class CheckData {
		
		public final int 	start;
		public final int 	next;
		public final String encoded;
		
		public CheckData(int start, int next, String encoded) {
			this.start 	 = start;
			this.next 	 = next;
			this.encoded = encoded;
		}
	}
	
	static String replace_callback(Pattern pattern, Callback<String> callback, String string) {
		Matcher matcher  = pattern.matcher(string);
		StringBuilder sb = new StringBuilder();
		while(matcher.find()) {
			MatchResult result = matcher.toMatchResult();
			int start 		   = result.start();
			int end 		   = result.end();
			String value	   = string.substring(start, end);
			sb.append(string.substring(0, result.start()));
			sb.append(callback.call(value));
			sb.append(string.substring(result.end()));
			string = sb.toString();
            sb.setLength(0);
            matcher.reset(string);
        }
		return string;
	}
	
	static String trim(String string, String ch) {
		String q = Pattern.quote(ch);
		return string.replaceAll("^" + q + "+", "").replaceAll(q + "+$", "");
	}
	
	static String replace(String[] targets, String[] replacements, String string) {
		for(int i = 0, l = targets.length; i < l; ++i) {
			string = string.replace(targets[i], replacements[i]);
		}
		return string;
	}
	
	static String convert(String block, Callback<Integer> func) {
		Pattern p = Pattern.compile("\\([0-9\\-\\+\\*\\/]+\\)");
		while(p.matcher(block).find()) {
			block = replace_callback(p,
				(String value) -> Double.toString(calc(value)), block);
		}
		
		StringBuilder sb = new StringBuilder();
		for(String num : trim(block, "+").split("\\+")) {
			if(num.isEmpty()) continue;
			sb.append(func.call((int) Double.parseDouble(num.trim())));
		}
		return sb.toString();
	}
	
    static String deobfuscate(String js) {
    	int hexLen = hex.length();
    	for(int i = 0, l = bytes.length; i < l; ++i) {
    		js = String.join(Integer.toString(i),
    						 js.split(Pattern.quote(bytes[i])));
    	}
    	StringBuilder sb = new StringBuilder();
    	for(String block : js.split(Pattern.quote(
        		"\u0028\uff9f\u0414\uff9f\u0029\u005b\uff9f\u03b5\uff9f\u005d\u002b"))) {
    		block = trim(replace(native_keys, native_vals, block), "+").trim();
    		if(block.isEmpty()) continue;
    		int code;
    		if(block.substring(0, hexLen).equals(hex)) {
    			code = Integer.parseInt(convert(block.substring(hexLen),
    				(Integer value) -> Integer.toHexString(value)), 16);
    		} else {
    			code = Integer.parseInt(convert(block,
    				(Integer value) -> Integer.toOctalString(value)), 8);
    		}
    		sb.append("&#");
    		sb.append(code);
    		sb.append(';');
    	}
    	return sb.toString();
    }
    
	static int[] findOccurences(String haystack, String needle, int offset) {
		List<Integer> matches = new LinkedList<>();
		for(int i = 0; i < 6; ++i) {
			if((offset = haystack.indexOf(needle, offset)) > -1) {
				matches.add(offset);
				++offset;
			} else break;
		}
		return matches.size() >= 6 ?
					Utils.IntegerToInt(
						matches.subList(4, 6).toArray(new Integer[2])) :
					null;
	}
	
	public static String decode(String js) {
		CheckData cd;
        if((cd = isAAEncoded(js)) != null) {
        	String decoded = deobfuscate(cd.encoded);
        	if(!decoded.trim().endsWith(";")) {
        		decoded += ';';
        	}
        	return htmlcodes(js.substring(0, cd.start) +
        					 decoded +
        					 decode(js.substring(cd.next)));
        }
        return js;
    }
	
    public static CheckData isAAEncoded(String js) {
    	int start = -1;
    	while((start = js.indexOf("\uff9f\u03c9\uff9f\uff89", start + 1)) > -1) {
    		String code  = js.substring(start);
    		String clear = code.replaceAll("[\\x03-\\x20]", "").replaceAll("\\/\\*.+?\\*\\/", "");
    		int len 	 = CODE_BEGIN.length();
    		
    		int[] matches;
    		if((clear.substring(0, len).equals(CODE_BEGIN)) &&
    		   (clear.indexOf(CODE_END, len) > -1) 			&&
    		   (matches = findOccurences(js, "\uff9f\u006f\uff9f", start)).length > 0) {
    			int beginAt = matches[0];
    			int endAt	= matches[1];
    			beginAt 	= js.indexOf('+', beginAt);
    			int index 	= -js.length()+endAt;
    			if(index < 0) index += js.length();
    			endAt 		= js.lastIndexOf('(', index);
    			int next 	= js.indexOf(';', endAt+1) + 1;
    			return new CheckData(start, next,
    				js.substring(beginAt, endAt).replaceAll("[\\x03-\\x20]", ""));
    		}
    	}
        return null;
    }
}
package sune.etc.faso.util;

import java.io.InputStream;
import java.nio.charset.Charset;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JavaScript {
	
	static final ScriptEngine ENGINE_JS;
	static {
		ENGINE_JS = new ScriptEngineManager().getEngineByName("JavaScript");
	}
	
	static InputStream stream(String path) {
		return JavaScript.class.getResourceAsStream(path);
	}
	
	static String fileContent(String path) {
		StringBuilder builder = new StringBuilder();
		Charset charset 	  = Utils.CHARSET;
		try(InputStream stream = stream("/resources/" + path)){
			int read	  = 0;
			byte[] buffer = new byte[8192];
			while((read = stream.read(buffer)) != -1) {
				builder.append(new String(
					buffer, 0, read, charset));
			}
		} catch(Exception ex) {
		}
		return builder.toString();
	}
	
	static final class StringJoiner {
		
		static final StringBuilder JOINER;
		static {
			JOINER = new StringBuilder();
		}
		
		public static final void append(String text) {
			JOINER.append(text);
		}
		
		public static final void reset() {
			JOINER.setLength(0);
		}
		
		public static final String content() {
			return JOINER.toString();
		}
	}
	
	public static Object execute(String js) {
		try {
			return ENGINE_JS.eval(js);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static Object execute(String jsFile, String js) {
		try {
			StringJoiner.reset();
			StringJoiner.append(fileContent(jsFile));
			StringJoiner.append(js);
			return execute(StringJoiner.content());
		} catch(Exception ex) {
		}
		return null;
	}
}
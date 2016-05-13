package sune.etc.faso.util;

public class UserAgent {
	
	public static final String MOZILLA;
	public static final String MOZILLA2;
	public static final String IPHONE;
	
	static {
		MOZILLA  = "Mozilla";
		MOZILLA2 = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0";
		IPHONE   = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) " +
				   "AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 " +
				   "Safari/528.16";
	}
}
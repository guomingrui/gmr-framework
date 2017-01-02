package gmr.all.framework.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URL处理类
 * 
 * @author gmr
 *
 */
public class UrlUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtil.class);

	/**
	 * 获取统一的url格式
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrl(String url) {
		url = url.replace("\\", "/");
		while (url.contains("//")) {
			url = url.replace("//", "/");
		}
		return url;
	}

	/**
	 * 获取解码后的url格式
	 * 
	 * @param url
	 * @return
	 */
	public static String getDecodeUrl(String url) {
		url = urlDecodde(url).replace("\\", "/");
		while (url.contains("//")) {
			url = url.replace("//", "/");
		}
		return url;
	}

	public static String urlDecodde(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("can't support UTF-8", e);
			throw new RuntimeException();
		}

	}
}

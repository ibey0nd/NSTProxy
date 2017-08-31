package proxy;

import java.util.List;

import com.mysql.jdbc.Connection;

import burp.IParameter;
import db.jdbcUtils;

public class utils {

	public static jdbcUtils db;
	public static Connection conn;
	public static String[] blackExt = {
			".ico",".woff",".flv",".js",".css",".jpg",
			".png",".jpeg",".gif",".pdf",".txt",
			".rar",".zip",".mp4",".svg","woff2",
			".swf",".wmi",".exe",".mpeg",".htm"
	};
	public static String[] url_black_hosts = {".gov","qq.com","so.com","12306.cn","itwzw.cn",
	                  "google","gstatic","cnzz.com","doubleclick","bootcss.com",
	                  "360safe.com","mil.cn","gov.cn","gov.com","cnblogs.com","box3.cn","bdimg.com",
	                  "360.cn","baidu.com","csdn.com","github.com","127.0.0.1","localhost","googleadsserving.cn",".csdn.net"
	                  };
	
	
	public static String iparam2string(List<IParameter> parameters) {
		String params = "";
		for (IParameter iParameter : parameters) {
			if(iParameter.getType()==IParameter.PARAM_BODY) {
				params+=iParameter.getName()+"="+iParameter.getValue()+"&";
			}
		}
		try {
			return params.substring(0, params.length() - 1);
		} catch (Exception e) {
			return "";
		}
	}
	
	public static boolean isblackext(String url) {
		for (String ext : blackExt) {
			//such as : a.js
			if(url.endsWith(ext)) {
				return true;
			}else {
				//such as : as.js?ver=20170101
				if(url.contains("?")){
					String[] urls = url.split("\\?");
					return isblackext(urls[0]);
				}
			}
			
		}
		return false;
	}
	
	public static boolean isblackdomain(String url) {
		for (String string : url_black_hosts) {
			if(url.contains(string)) {
				return true;
			}
		}
		return false;
	}

	
}

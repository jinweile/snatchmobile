package snatchmobile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class Snatch {

	/**
	 * 下载内容(get)
	 * @param url
	 * @param header
	 * @return
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String getdown(String url) throws HttpException, IOException{
		String result = "";
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);

		int statusCode = client.executeMethod(method);
		if(statusCode != HttpStatus.SC_OK){
			return null;
		}
		String content = method.getResponseBodyAsString();
		String Content_Type = method.getResponseHeader("Content-Type").getValue();
		//获取解析的编码格式
		Charset charset = DeCharSetName(Content_Type, content);
		byte[] responseBody = content.getBytes(method.getResponseCharSet());
		//转码
		result = new String(responseBody, charset);
		
		return result;
	}
	
	/**
	 * 解析响应的html编码格式
	 * @param content_type
	 * @param content
	 * @return
	 */
	public static Charset DeCharSetName(String content_type, String content){
		Charset currentCharset = Charset.forName("UTF-8");
		String regexstr = "(?=text/html|text/xml|application/x-javascript).*?charset=([^\"]+)";
		Pattern p = Pattern.compile(regexstr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		String contentType = content_type;
		if (contentType != null || !contentType.isEmpty()) {
			Matcher matchers = p.matcher(contentType);
			if (matchers.find()) {
				String charset = matchers.group(1).toUpperCase();
				try {
					currentCharset = Charset.forName(charset);
				} catch (Exception ex) { }
			}
		}

		String ascii = content;
		Matcher matchers1 = p.matcher(ascii);
		if (matchers1.find()) {
			String charset = matchers1.group(1).toUpperCase();
			try {
				currentCharset = Charset.forName(charset);
			} catch (Exception ex) { }
		}

		return currentCharset;
	}
	
	/**
	 * 规范化并合并URL
	 * 
	 * @param baseUrl
	 * @param urlpath
	 * @return
	 */
	public static String CombineUrl(String baseUrl, String urlpath) {
		if (urlpath == null || urlpath.isEmpty()) {
			return null;
		}

		String lowUrl = urlpath.toLowerCase();
		if (!lowUrl.startsWith("http://") && !lowUrl.startsWith("https://")) {
			try {
				URI uri = new URI(baseUrl);
				return uri.resolve(urlpath).toString();
			} catch (URISyntaxException e) {
				return null;
			}
		} else {
			return urlpath;
		}
	}
	
	public final static int MAX_MATCH_COUNT = 4000;
	
	/**
	 * 正则解析（取多项数据）
	 * @param p
	 * @param content
	 * @return
	 */
	public static List<String> ExtractListRegion(Pattern p, String content){
		if(p == null)
			return null;
		List<String> result_list = new ArrayList<String>();
		Matcher matchers = p.matcher(content);
		int mcount = 0;
		while(matchers.find()){
			String one = null;
			try{
				one = matchers.group("match");
			}catch(Exception e){}
			if(one == null){
				if(matchers.groupCount() > 1)
					one = matchers.group(1);
				else
					one = matchers.group(0);
			}
			if(one != null && !one.isEmpty())
				result_list.add(one);
			mcount++;
			if(mcount > MAX_MATCH_COUNT)
				break;
		}
		
		if(result_list.size() == 0)
			return null;
		
		return result_list;
	}
	
	private static Pattern p;
	
	/**
	 * 获取正则表达式
	 * @return
	 * @throws Exception
	 */
	public static Pattern getReg() throws Exception{
		if(p == null){
			Properties prop = new Properties();
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mobile.properties");
			prop.load(is);
			p = Pattern.compile("(?s)" + prop.getProperty("mobile"), Pattern.CASE_INSENSITIVE);
		}
		return p;
	}
	
	/**
	 * 从入口递归抓取电话号码
	 * @param url
	 * @throws Exception
	 */
	public static void RecursiveSnatch(String url) throws Exception {
		String content = getdown(url);
		List<String> mobile_list = ExtractListRegion(getReg(), content);
		if(mobile_list != null)
			for(String mobile : mobile_list){
				try{
					//InsertDB(mobile);
					System.out.print("获取号码：" + mobile);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		
		//获取a标签的地址
		String areg = "<a.+?href=(?:\"|'|)(?<match>.+?)\1.+?>";
		Pattern p = Pattern.compile("(?s)" + areg, Pattern.CASE_INSENSITIVE);
		List<String> a_label_list = ExtractListRegion(p, content);
		if(a_label_list != null){
			for(String a_label : a_label_list){
				if(!a_label.toLowerCase().endsWith(".jpg")
						&& !a_label.toLowerCase().endsWith(".png")
							&& !a_label.toLowerCase().endsWith(".gif")){
					String newurl = CombineUrl(url,a_label);
					try{
						RecursiveSnatch(newurl);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private static Properties jdbc;
	
	/**
	 * 入库
	 * @param mobile
	 * @throws Exception 
	 */
	private static void InsertDB(String mobile) throws Exception{
		if(jdbc == null){
			jdbc = new Properties();
			InputStream is = Thread.currentThread().getClass().getClassLoader().getResourceAsStream("jdbc.properties");
			jdbc.load(is);
			System.setProperty("jdbc.drivers",	 jdbc.getProperty("db.driverClassName"));
		}
		String dburl = jdbc.getProperty("db.url");
		String username = jdbc.getProperty("db.username");
		String pwd = jdbc.getProperty("db.password");
		Connection conn = DriverManager.getConnection(dburl, username, pwd);
		PreparedStatement ps = conn.prepareStatement("insert into [mobile](mobile) values(?)");
		ps.setString(0, mobile);
		ps.executeUpdate();
		ps.close();
		conn.close();
	}
	
}

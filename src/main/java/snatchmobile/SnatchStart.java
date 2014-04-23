package snatchmobile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class SnatchStart {

	public static void main(String[] args) throws Exception {
		//读取配置文件
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("urlinfo.xml");
		XStream xstream = new XStream();
		List<UrlInfo> urlinfo_list = (List<UrlInfo>) xstream.fromXML(is);
		List<ThreadSnatch> th_list = new ArrayList<ThreadSnatch>();
		for(UrlInfo urlinfo : urlinfo_list){
			ThreadSnatch ts = new ThreadSnatch(urlinfo.getUrl());
			ts.start();
			th_list.add(ts);
		}
		
		for(ThreadSnatch ts : th_list){
			ts.join();
		}
	}
	
	public static class UrlInfo{
		private String url;
		
		public void setUrl(String url){
			this.url = url;
		}
		
		public String getUrl(){
			return this.url;
		}
	}

}

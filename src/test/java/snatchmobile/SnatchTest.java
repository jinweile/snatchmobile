package snatchmobile;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class SnatchTest {

	@Test
	public void testRecursiveSnatch() throws Exception {
		SnatchStart.main(null);
	}
	
	public void test() throws Exception {
		//Snatch.RecursiveSnatch("http://www.jihaoba.com/");
		/*XStream xstream = new XStream();
		String path = "F:\\Temp\\urlinfo.xml";
		List<SnatchStart.UrlInfo> list = new ArrayList<SnatchStart.UrlInfo>();
		for(int i = 0; i < 2; i++){
			SnatchStart.UrlInfo ui = new SnatchStart.UrlInfo();
			ui.setUrl("http://www.jihaoba.com/");
			list.add(ui);
		}
		Files.write(Paths.get(path), xstream.toXML(list).getBytes("UTF-8"),
				StandardOpenOption.WRITE, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);*/
	}

}

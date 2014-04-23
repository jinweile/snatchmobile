package snatchmobile;

public class ThreadSnatch extends Thread {

	private String url;
	
	public ThreadSnatch(String url){
		this.url = url;
	}
	
	public void run() {
		try{
		Snatch.RecursiveSnatch(this.url);
		}catch(Exception e){
			e.printStackTrace();
		}
    }
	
}

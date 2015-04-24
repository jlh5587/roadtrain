
public class ComputerInfo {
	String compName;
	int port;
	
	public ComputerInfo(String compName, int port){
		this.compName = compName;
		this.port = port;
	}
	
	public String getName(){
		return compName;
	}
	
	public int getPort(){
		return port;
	}
}

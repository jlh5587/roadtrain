import java.util.Hashtable;
import java.util.Set;

public class Beacon implements Runnable{
	
	int user;
	Hashtable<Integer, NeighborTableEntry> neighborTable;
	OLSR olsr;
	
	
	public Beacon(int user, OLSR olsr){
		this.user = user;
		this.olsr = olsr;
	}
	
	public void setNeighborTable(Hashtable<Integer, NeighborTableEntry> neighborTable){
		this.neighborTable = neighborTable;
	}
	
	public void run(){
		while(true){
			try{
				Thread.sleep(3000);
				sendHelloMessage();
				
			}catch(Exception e){
				
			}
			
		}
	}
	
	
	public void sendHelloMessage(){
		Set<Integer> keys = neighborTable.keySet();
		String bidirection = "";
		String unidirection = "";
		for(Integer key: keys){
			if(neighborTable.get(key).getStatus() == NeighborStatus.BIDIRECTIONAL)
			{
				bidirection = bidirection +","+ key;
			} else if(neighborTable.get(key).getStatus() == NeighborStatus.UNIDIRECTIONAL){
				unidirection = unidirection + ","+ key ;
			}
			//Others can be added as needed
		}

		//In the OLSR layer I can provide who it comes from.
		//and example of the message would be  1,b,2,3,5,u,1
		String message = "b" + bidirection + ",u" + unidirection;

		olsr.sendHello(message);
	}
	
	
}
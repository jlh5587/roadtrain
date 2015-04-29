import java.util.Hashtable;
import java.util.Set;

public class Beacon{
	
	int user;
	Hashtable<Integer, NeighborTableEntry> neighborTable;
	OLSR olsr;
	
	
	public Beacon(int user, OLSR olsr){
		this.user = user;
		this.olsr = olsr;
		
		neighborTable = new Hashtable<Integer, NeighborTableEntry>();
	}
	
	public void setNeighborTable(Hashtable<Integer, NeighborTableEntry> neighborTable){
		this.neighborTable = neighborTable;
	}
	
	public void sendHelloMessage(){
		Set<Integer> keys = neighborTable.keySet();
		String bidirection = "";
		String unidirection = "";
		String m = "";
		for(Integer key: keys){
			if(neighborTable.get(key).getStatus() == NeighborStatus.BIDIRECTIONAL)
			{
				bidirection = bidirection +"~"+ key;
			} else if(neighborTable.get(key).getStatus() == NeighborStatus.UNIDIRECTIONAL){
				unidirection = unidirection + "~"+ key ;
			} else if(neighborTable.get(key).getStatus() == NeighborStatus.MPR){
				m = m + "~" + key;
			}
			//Others can be added as needed
		}

		//In the OLSR layer I can provide who it comes from.
		//and example of the message would be  1,b,2,3,5,u,1
		String message = "b" + bidirection + "~u" + unidirection + "~m" + m;
		System.out.println("MPR table for : "+user+ " - " + message);
		olsr.sendHello(message);
	}
}
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;


public class MPR {
	
	int user;
	ArrayList<Integer> mprs;
	
	public MPR(int user){
		this.user = user;		
		mprs = new ArrayList<Integer>();
	}
	
	public String getHelloMprs(){
		String r = "";
		
		for(int i = 0; i< mprs.size(); i++){
			r = "," + mprs.get(i);
		}
		
		r = "m" + r;
		
		return r;
	}
	
	public ArrayList<Integer> getMprs(){
		return mprs;
	}
	
	public void findMprs(Hashtable<Integer, NeighborTableEntry> neighborTable){
		 ArrayList<Integer> twoHop = new ArrayList<Integer>();
		 ArrayList<Integer> newMPRS = new ArrayList<Integer>();
		 Set<Integer> keys = neighborTable.keySet();
         for (Integer key : keys) {
        	 Integer[] twoHopNeighbors = neighborTable.get(key).getTwoHopNeighbors();
        	 if(neighborTable.get(key).getStatus() != NeighborStatus.UNIDIRECTIONAL)
	        	 if(twoHopNeighbors.length > 0){
	        		 boolean found = false;
	        		 for(int i = 0; i<twoHopNeighbors.length; i++){
	        			 if(! twoHop.contains(twoHopNeighbors[i])){
	        				 twoHop.add(twoHopNeighbors[i]);
	        				 found = true;
	        			 }
	        			 
	        		 }
	        		 if(found){
	        			 newMPRS.add(key);
	        		 }
	        	 }
         	}
         if(newMPRS.size() == 0){
        	 for (Integer key : keys) {
        		 newMPRS.add(key);
        	 }
         }
         mprs = new ArrayList<Integer>(newMPRS);
	}
	
	
}
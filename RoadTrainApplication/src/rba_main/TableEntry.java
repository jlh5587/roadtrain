public class TableEntry {
	String message;
	int sender, lastHop, numOfForwards, seqNum;
	
	TableEntry(int sender){
		this.sender = sender;
		lastHop = -1;
		numOfForwards = -1;
		seqNum = 0;
		message = "";
	}
	
	TableEntry(int sender, int lastHop, int numOfForwards, int seqNum, String message){
		this.sender = sender;
		this.lastHop = lastHop;
		this.numOfForwards = numOfForwards;
		this.seqNum = seqNum;
		this.message = message;
	}
	
	//setters
	
	public void setLastHop(int lastHop){
		this.lastHop = lastHop;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setNumOfForwards(int numOfForwards){
		this.numOfForwards = numOfForwards;
	}
	
	public void setSeqNum(int seqNum){
		this.seqNum = seqNum;
	}
	
	public int getLastHop(){
		return lastHop;
	}
	
	public int getNumOfForwards(){
		return numOfForwards;
	}
	
	public int getSeqNum(){
		return seqNum;
	}
	
	public String getMessage(){
		return message;
	}
	
	public int getSender(){
		return sender;
	}
}

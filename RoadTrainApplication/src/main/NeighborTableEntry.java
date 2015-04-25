public class NeighborTableEntry {

	NeighborStatus status;
	Integer[] twoHopNeighbors;

	public NeighborTableEntry(NeighborStatus status){
		this.status = status;
	}

	public NeighborStatus getStatus(){
		return status;
	}

	public void setStatus(NeighborStatus status){
		this.status = status;
	}

	public Integer[] getTwoHopNeighbors() {
		return this.twoHopNeighbors;
	}

	public void setTwoHopNeighbors(Integer[] thns) {
		this.twoHopNeighbors = thns;
	}
}

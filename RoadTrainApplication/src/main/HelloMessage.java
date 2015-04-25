import java.util.Hashtable;

public class HelloMessage {
  private Integer senderName;
  private Hashtable<Integer, NeighborTableEntry> neighbors;

  public HelloMessage(Integer name, Hashtable<Integer, NeighborTableEntry> neighbors) {
    this.senderName = name;
    this.neighbors = neighbors;
  }

  public Integer getSenderName() {
    return senderName;
  }

  public Hashtable<Integer, NeighborTableEntry> getNodeNeighbors() {
    return neighbors;
  }

}

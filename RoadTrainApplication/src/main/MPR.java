import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;




public class MPR {

	Hashtable<Integer, TableEntry> cacheTable;
	Hashtable<Integer, NeighborTableEntry> neighborTable;
	Hashtable<Integer, ComputerInfo> computerInfoTable;
	File configFile;
	int user, currentSeqNum,numMessageCreated;
	int currentSender, currentLastHop, currentTimesForwarded, currentPacketType;
	String currentMessage;
	boolean listen;
	DatagramSocket socket;
	int messageRec, droppedPackets;
	
	public MPR(int user, int port, File configFile) throws SocketException{
		
		cacheTable = new Hashtable<Integer, TableEntry>();
		neighborTable = new Hashtable<Integer, NeighborTableEntry>();
		this.configFile = configFile;
		this.user = user;
		currentSeqNum = 0;
		numMessageCreated = 1;
		listen = true;
		socket = new DatagramSocket(port);
	}
	
	public String listenForMessage(){
		byte[] recieved = new byte[4096];
		
		DatagramPacket receivePacket = new DatagramPacket(recieved, recieved.length);
        try {
        	socket.setSoTimeout(1000);
        	socket.receive(receivePacket);
			
			String packetInfo = new String(receivePacket.getData());
			
			parsePacket(packetInfo);
			
			if(currentPacketType == 1){
				//handle hello message
			}else if(currentPacketType == 2){
				//check if you are an MPR and then forward if necessary
				/*IF MPR -> sendMessageAsMPR(packetInfo)*/
				if(cacheTable.get(currentSender).getSeqNum() <= currentSeqNum){
					cacheMessage();
				}
			}
			
			return currentMessage;	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
			
		
            
        return "";
	}
	
	private void cacheMessage(){
		cacheTable.get(currentSender).setLastHop(user);
		cacheTable.get(currentSender).setMessage(currentMessage);
		cacheTable.get(currentSender).setNumOfForwards(currentTimesForwarded+1);
		messageRec++;
		if(cacheTable.get(currentSender).getSeqNum() < currentSeqNum -1){
			droppedPackets++;
			System.out.println("Num of packets recieved: " + messageRec);
			System.out.println("Dropped Packets: " + droppedPackets);
			
		}
		cacheTable.get(currentSender).setSeqNum(currentSeqNum);	
	}
	
	private void parsePacket(String packetInfo){
		Scanner packetScanner = new Scanner(packetInfo);
		packetScanner.useDelimiter(",");
		
		//The packet contains PacketType sender, lastHop, times forwarded, message
		// If packet type = 1 then hello message, if 2 then other message
		currentPacketType = packetScanner.nextInt();
		currentSender = packetScanner.nextInt();
		currentSeqNum = packetScanner.nextInt();
		currentLastHop = packetScanner.nextInt();
		currentTimesForwarded = packetScanner.nextInt();
		currentMessage = packetScanner.next();
		
		packetScanner.close();
	}
	
	
	public void sendHello(){
		ArrayList<Integer> connects = findForwardConnections();
		
		for(int i = 0; i<connects.size(); i++){
			if(computerInfoTable.containsKey(connects.get(i))){
				sendHelloMessage(computerInfoTable.get(connects.get(i)));
			}else{
				computerInfoTable.put(connects.get(i), compInfo(connects.get(i)));
				sendHelloMessage(computerInfoTable.get(connects.get(i)));
			}
			
		}
	}
	
	public void sendHelloMessage(ComputerInfo c){
		try{
			
			//The Hello String should be added to to send the appropriate information to the other nodes.
			String packetInfo = "1,"+ user + ","+ currentSeqNum+","+user+","+0+","+"Hello"; 
		
			byte[] sendData = new byte[4096];
			sendData = packetInfo.getBytes();
			String compName = c.getName();
			int port = c.getPort();
			
			InetAddress IPAddress;
			try {
				IPAddress = InetAddress.getByName(compName);
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(sendPacket);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 	
	
		 }catch(Exception e){
		 //	System.out.println(packetInfo);
		 //	System.out.println(e.toString());
		 }
			
	 }
		 
	//Sends newMessages to MPR's based on status in neighbor table.
	
public void sendMessageAsMPR(String packetInfo){
		
		try{
			 byte[] sendData = new byte[4096];
			 sendData = packetInfo.getBytes();
			 

			 
			 String compName;
			 int port;
			 
			 Set<Integer> keys = neighborTable.keySet();
		     for(Integer key: keys){
				 ComputerInfo c;
				 if(computerInfoTable.containsKey(key)){
					 	c = computerInfoTable.get(key);
					}else{
						computerInfoTable.put(key, compInfo(key));
						c = computerInfoTable.get(key);
					}
				 
				 compName = c.getName();
				 port = c.getPort();
				
				 //For testing purposes. These IP addresses will need to come from the config file.
				InetAddress IPAddress;
				try {
					IPAddress = InetAddress.getByName(compName);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					socket.send(sendPacket);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 	
			}
		 }catch(Exception e){
		 	System.out.println(packetInfo);
		 	System.out.println(e.toString());
		 } 
	}
	
	public void sendNewMessage(String packetInfo){
		
		try{
			 byte[] sendData = new byte[4096];
			 sendData = packetInfo.getBytes();
			 
			 ArrayList<Integer> mprs = new ArrayList<Integer>();
			 
			 Set<Integer> keys = neighborTable.keySet();
		     for(Integer key: keys){
		    	 if(neighborTable.get(key).status.equals("MPR")){
		    		 mprs.add(key);
		    	 }
		     }
			 
			 
			 String compName;
			 int port;
			 
			 for(int i = 0; i<mprs.size();i++){
			 	
				 ComputerInfo c;
				 if(computerInfoTable.containsKey(mprs.get(i))){
					 	c = computerInfoTable.get(mprs.get(i));
					}else{
						computerInfoTable.put(mprs.get(i), compInfo(mprs.get(i)));
						c = computerInfoTable.get(mprs.get(i));
					}
				 
				 compName = c.getName();
				 port = c.getPort();
				
				 //For testing purposes. These IP addresses will need to come from the config file.
				InetAddress IPAddress;
				try {
					IPAddress = InetAddress.getByName(compName);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					socket.send(sendPacket);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 	
			}
		 }catch(Exception e){
		 	System.out.println(packetInfo);
		 	System.out.println(e.toString());
		 } 
	}
	
	//This is the broadcast method that is called from the app layer
	public void broadcast(String message){
		String packetInfo = "2,"+ user + ","+ numMessageCreated+","+user+","+0+","+message;
		numMessageCreated++;
		
		//We will need to add a check to see if this user is an MPR. If they are, then we will
		//use a different method to send the packet.
		sendNewMessage(packetInfo);
	}
	
	
	//Finds the connections based on the config file
	public ArrayList<Integer> findForwardConnections(){
		ArrayList<Integer> toForward = new ArrayList<Integer>();
		
		Scanner scanFile;
		try {
			scanFile = new Scanner(configFile);
			
			while(scanFile.hasNext()){			
				String line = scanFile.nextLine();
				Scanner scanLine = new Scanner(line);
				
					
					int car = scanLine.nextInt();
					
					if(car == user){
						while(scanLine.hasNext()){
							if(scanLine.next().equals("links")){
								while(scanLine.hasNext()){
									toForward.add(scanLine.nextInt());
							
								}
							 
							}
						}
						break;
					}
				
				
				
				scanLine.close();
			}
			
			scanFile.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}

		return toForward;
		
	}
	
	
	public ComputerInfo compInfo(int id){
		String compNm = "";
		Scanner scanFile;
		try {
			scanFile = new Scanner(configFile);
			
			while(scanFile.hasNext()){
				String line = scanFile.nextLine();
				
				Scanner scanLine = new Scanner(line);
				
				
				int car = scanLine.nextInt();
					
					if(car == id){
						compNm = scanLine.next();
						int port = scanLine.nextInt();
						scanLine.close();
						return (new ComputerInfo(compNm, port));
						
					}

				scanLine.close();
			}
			
			scanFile.close();
		}catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		}
		return null;
	}

	public void setListen(boolean listen){
		this.listen = listen;
	}
	
	public void closeSocket(){
		socket.close();
	}
	
}


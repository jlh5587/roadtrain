

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.sql.*;

//import pangolin.compID;


public class RBA {
	ArrayList<TableEntry> cache;
	String currentMessage = "", newMessage;
	int currentSeqNum, currentLastHop, currentTimesForwarded, currentUser, currentSender, numMessageCreated = 1;
	int droppedPackets = 0, messageRec = 0;
	DatagramSocket socket;
	boolean listen;
	File configFile;
	long startTime;
	long currentTime, longestDelay;
	
	public RBA(int currentUser, int port, File configFile) throws SocketException{
		this.currentUser = currentUser;
		cache = new ArrayList<TableEntry>();
		socket = new DatagramSocket(port);
		listen = true;
		this.configFile = configFile;
		longestDelay = 0;
		currentTime = System.currentTimeMillis();
	}
	
	
	//This populates the cache the first time with empty messages. This is necessary but is
	//done so that each nodes message goes in there car-1 value.
	private void populateCache(){
		cache.add(new TableEntry(currentSender, currentLastHop, currentTimesForwarded, currentSeqNum, currentMessage));
	}
	
	private boolean inCache(){
		boolean in = false;
		for(int i = 0; i < cache.size(); i++){
			if(cache.get(i).getSender() == currentSender){
				in = true;
			}
		}
		return in;
	}
	
	private int getCacheLocation(){
		int val = -1;
		for(int i = 0; i < cache.size(); i++){
			if(cache.get(i).getSender() == currentSender){
				val = i;
			}
		}
		
		return val;
	}
	
	
	//loops to listen for a message. 
	public String listenForMessage(){
		byte[] recieved = new byte[4096];
		//while(listen){
			DatagramPacket receivePacket = new DatagramPacket(recieved, recieved.length);
            try {
            	socket.setSoTimeout(1000);
            	socket.receive(receivePacket);
				
				String packetInfo = new String(receivePacket.getData());
				long recieveTime = System.currentTimeMillis();
				//long delay = recieveTime - currentTime;
	            
				//System.out.println(packetInfo);
				parsePacket(packetInfo);
				
				long delay = recieveTime - currentTime;
				if(delay>longestDelay){
	            	longestDelay = delay;
	            }
				//System.out.println("from: " + currentSender + " message: " + currentMessage);
				checkShouldForward();
				return currentMessage;	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
		//}
            
            return "";
	}
	
	
	//Parse the packet that is recieved
	private void parsePacket(String packetInfo){
		Scanner packetScanner = new Scanner(packetInfo);
		packetScanner.useDelimiter(",");
		
		//The packet contains sender, lastHop, times forwarded, message
		currentSender = packetScanner.nextInt();
		currentTime = packetScanner.nextLong();
		currentSeqNum = packetScanner.nextInt();
		currentLastHop = packetScanner.nextInt();
		currentTimesForwarded = packetScanner.nextInt();
		currentMessage = packetScanner.next();
		
		packetScanner.close();
	}
	
	
	//Forwards the message to connecting cars.
	public void forwardMessage(){
		 String packetInfo = currentSender + ","+currentTime+"," +currentSeqNum+","+currentUser+","+(currentTimesForwarded+1)+","+currentMessage;
		 try{
			 byte[] sendData = new byte[4096];
			 sendData = packetInfo.getBytes();
			 
			 ArrayList<Integer> forwardConn = findForwardConnections();
			 String compName;
			 int port;
			 
			 for(int i = 0; i<forwardConn.size();i++){
			 	if(currentLastHop != i && currentSender != i){
					 compID c = compInfo(forwardConn.get(i));
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
			}
		 }catch(Exception e){
		 //	System.out.println(packetInfo);
		 //	System.out.println(e.toString());
		 }
		 
		 
		 
	}
	
	
	//Checks to see if a message should be forwarded
	private void checkShouldForward(){

		
		if(inCache()){
			int cacheLoc = getCacheLocation();
		
			//Checks sequence number. If it's the same, it uses the RBA to determine if it should be forwarded
			if(currentSeqNum == cache.get(cacheLoc).getSeqNum()){
					int forwards = cache.get(cacheLoc).getNumOfForwards();
					double probability = 1;
					
					for(int i = 1; i <= forwards; i++){
						probability = probability/2;
					}
					
					if(new Random().nextDouble() <= probability){
						forwardMessage();
					}
					cache.get(cacheLoc).setNumOfForwards(forwards+1);
				//if the current sequence number is greater, then it automatically caches the message and forwards it.	
			} else if (currentSeqNum > cache.get(cacheLoc).getSeqNum()){
					cacheMessage();
					forwardMessage();
			}else{
				//this is where nothing needs to be done because the sequence num < cached seq num
			}
		}else{
			populateCache();
			forwardMessage();
		}
		
	}
	
	private boolean cacheMessage(){	
		for(int i = 0; i < cache.size(); i++){
			if(cache.get(i).getSender() == currentSender){
				cache.get(i).setMessage(currentMessage);
				cache.get(i).setLastHop(currentUser);
				cache.get(i).setNumOfForwards(currentTimesForwarded+1);
				messageRec++;
				if(cache.get(i).getSeqNum() < currentSeqNum -1){
					droppedPackets++;
					Timestamp st = new Timestamp(startTime);
					Timestamp nt = new Timestamp(System.currentTimeMillis());
					System.out.println("Num of packets recieved: " + messageRec);
					System.out.println("Dropped Packets: " + droppedPackets);
					System.out.println("Longest Delay (ms): " + longestDelay);
					System.out.println("Start time: " + st.toString());
					System.out.println("Current time: " + nt.toString());
				}
				
				cache.get(i).setSeqNum(currentSeqNum);
				return true;
			}
		}
		return false;
	}
	
	
	public void sendNewMessage(String packetInfo){
		
		try{
			 byte[] sendData = new byte[4096];
			 sendData = packetInfo.getBytes();
			 
			 ArrayList<Integer> forwardConn = findForwardConnections();
			 String compName;
			 int port;
			 
			 for(int i = 0; i<forwardConn.size();i++){
			 	
				 compID c = compInfo(forwardConn.get(i));
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
	
	public void setListen(boolean listen){
		this.listen = listen;
	}
	
	public void closeSocket(){
		socket.close();
	}
	
	public ArrayList<Integer> findForwardConnections(){
		ArrayList<Integer> toForward = new ArrayList<Integer>();
		
		Scanner scanFile;
		try {
			scanFile = new Scanner(configFile);
			
			while(scanFile.hasNext()){			
				String line = scanFile.nextLine();
				Scanner scanLine = new Scanner(line);
				
					
					int car = scanLine.nextInt();
					
					if(car == currentUser){
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
	
	public compID compInfo(int id){
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
						return (new compID(compNm, port));
						
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
	
	public String getCurrentMessage(){
		return currentMessage;
	}
	
	public void broadcast(String message){
		String packetInfo = currentUser + ","+System.currentTimeMillis()+"," + numMessageCreated+","+currentUser+","+0+","+message;
		numMessageCreated++;
		sendNewMessage(packetInfo);
	}

}



class compID{
	String compName;
	int port;
	
	public compID(String compName, int port){
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

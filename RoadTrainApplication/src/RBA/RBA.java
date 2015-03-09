package RBA;

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

//import pangolin.compID;


public class RBA {
	ArrayList<TableEntry> cache;
	String currentMessage = "New message", newMessage;
	int currentSeqNum, currentLastHop, currentTimesForwarded, currentUser, currentSender, numMessageCreated = 1;
	DatagramSocket socket;
	boolean listen;
	File configFile;
	
	
	public RBA(int currentUser, int port, File configFile) throws SocketException{
		this.currentUser = currentUser;
		cache = new ArrayList<TableEntry>();
		socket = new DatagramSocket(port);
		listen = true;
		this.configFile = configFile;
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
				socket.receive(receivePacket);
				String packetInfo = new String(receivePacket.getData());
				System.out.println(packetInfo);
				parsePacket(packetInfo);
				checkShouldForward();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
            
            return currentMessage;
	}
	
	
	//Parse the packet that is recieved
	private void parsePacket(String packetInfo){
		Scanner packetScanner = new Scanner(packetInfo);
		packetScanner.useDelimiter(",");
		
		//The packet contains sender, lastHop, times forwarded, message
		currentSender = packetScanner.nextInt();
		currentSeqNum = packetScanner.nextInt();
		currentLastHop = packetScanner.nextInt();
		currentTimesForwarded = packetScanner.nextInt();
		currentMessage = packetScanner.next();
		
		packetScanner.close();
	}
	
	
	//Forwards the message to connecting cars.
	public void forwardMessage(){
		 String packetInfo = currentSender + ","+ currentSeqNum+","+currentUser+","+(currentTimesForwarded+1)+","+currentMessage;
		 byte[] sendData = new byte[4096];
		 sendData = packetInfo.getBytes();
		 
		 ArrayList<Integer> forwardConn = findForwardConnections();
		 String compName = "localhost";
		 int port = 9876;
		 
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
		cache.get(currentSender -1).setMessage(currentMessage);
		cache.get(currentSender-1).setLastHop(currentUser);
		cache.get(currentSender-1).setNumOfForwards(currentTimesForwarded+1);
		cache.get(currentSender-1).setSeqNum(currentSeqNum);
		return true;
	}
	
	
	public void sendNewMessage(String message, int reciever){
		
		//Used a ',' delimited file... The packet contains sender, seqNum, lastHop, times forwarded, message
		String newPacket = currentUser + ","+ numMessageCreated+ ",-1,0," + message;
		byte[] send = new byte[4096];
		send = newPacket.getBytes();
		
		compID c = compInfo(reciever);
		String name = c.getName();
		int port = c.getPort();
		
		try {
			InetAddress IPAddress = InetAddress.getByName(name);
			DatagramPacket sendPacket = new DatagramPacket(send, send.length, IPAddress, port);
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		numMessageCreated++;
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
					}
				
				
				
				scanLine.close();
			}
			
			scanFile.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		currentMessage = message;
		forwardMessage();
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

package main;


import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;

import RBA.RBA;

public class Main {

	static String filePath;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length > 0){
			filePath = args[0];
		}
		
		try {
			File configFile = new File(filePath);
			
			Scanner scan = new Scanner(configFile);
			
			//this port will need to be a command line argument.
			int port = scan.nextInt();
			RBA r = new RBA(1, port, configFile);
			r.sendNewMessage("Hi", 1);
			r.listenForMessage();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}

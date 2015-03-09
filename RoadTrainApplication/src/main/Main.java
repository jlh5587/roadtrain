package main;


import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;
import App.RoadTrainApp;

import RBA.RBA;

public class Main {

	static String filePath;
	static int port;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length > 0){
			filePath = args[0];
			port = Integer.parseInt(args[1]);
		}
		
		try {
			File configFile = new File(filePath);
			RoadTrainApp start = new RoadTrainApp(port, configFile);
			start.ignition();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}

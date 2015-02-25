package App;

import java.io.File;

import RBA.RBA;

public class RoadTrainApp {
	boolean isTruck;
	int id;
	RBA rba;
	File configFile;
		
	public RoadTrainApp(boolean isTruck, int id, int port, File configFile){
		this.isTruck = isTruck;
		this.id = id;
		try{
			rba = new RBA(id, port, configFile);
		} catch (Exception e){
			
		}
	}
	
	
	
	//METHODS FOR CAR
	//ask to join train
	
	//ask to leave train
	
	
	
	//METHODS FOR TRUCK
	//tell cars to slow down or speed up
	
	
	
}

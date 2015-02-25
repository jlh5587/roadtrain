package Vehicle;

import java.io.File;

import App.RoadTrainApp;



public class Car implements Vehicle {

	int id, port;
	RoadTrainApp app;
	File configFile;
	
	public Car(int id, int port, File configFile){
		super();
		this.id = id;
		this.port = port;
		this.configFile = configFile;
	}

	@Override
	public void startApp() {
		// TODO Auto-generated method stub
		app = new RoadTrainApp(false, id, port, configFile);
	}

	@Override
	public void joinTrain() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaveTrain() {
		// TODO Auto-generated method stub
		
	}
	
	
	

}

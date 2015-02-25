package Vehicle;

import java.io.File;

import App.RoadTrainApp;

public class Truck implements Vehicle {
	
	private int id, port;
	RoadTrainApp app;
	File configFile;
	
	public Truck(int id, int port, File configFile){
		this.id = id;
		this.port = port;
		this.configFile = configFile;
		startApp();
	}
	
	@Override
	public void startApp() {
		// TODO Auto-generated method stub
		app = new RoadTrainApp(true, id, port, configFile);
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

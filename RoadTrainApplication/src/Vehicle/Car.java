package Vehicle;

public class Car implements Vehicle, Runnable{

	public int speed = 0;
	public int[] location = {0,0};
	public int port = 0;
	public int dest = 999999999;
	public Car head = null, tail = null;
	public Truck truck = null;
	public int id = 0;
	boolean join = false;
	
	public Car(int id, int port){
		this.id = id;
		this.port = port;
	}
	
	public Car(int id){
		this.id = id;
	}

	public void run() {
		while (true)
		{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			location[0] += speed;
		}
	}
}

package Vehicle;

public class Truck implements Vehicle, Runnable {
	public int speed = 0;
	public int[] location = {0,0};
	public int port = 0;
	public Car tail = null;
	public int id = 0;

	public Truck(int id, int port){
		this.id = id;
		this.port = port;
	}
	
	public Truck(int id){
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

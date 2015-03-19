
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;

public class RoadTrainApp {
	int port, id;
	RBA rba;
	Truck truck;
	Car car;
	String config_name = "";
	File config_File = null;
	boolean joined_Train = false;
	
	public RoadTrainApp(int port, File config){
		this.config_File = config;
		this.port = port;
		try{
			this.setConfigFilePosition();
			rba = new RBA(this.id, port, config);
		} catch (Exception e){
			System.out.println(e.toString());
		}
	}
	

	public void setVehicle(boolean isTruck, int x, int y)
		{
			if (isTruck)
			{
				this.truck = new Truck(id,port,this.config_File);
				this.truck.speed = 40;
				this.truck.location[0] = x;
				this.truck.location[1] = y;
				this.joined_Train = true;
			}
			else
			{
				this.car = new Car(id,port,this.config_File);
				this.car.location[0] = x;
				this.car.location[1] = y;
				car.speed = 45;
			}
			
		}
	
	public void ignition() throws IOException
	{
		if(this.id == 0)
		{
			Thread drive = new Thread(this.truck);
			drive.start();
			int lock_Enter = 0;
			String old_message = "";
			while(true)
			{
				String msg = this.listen();
				if(! msg.equals("") && ! old_message.equals(msg.trim()))
				{
					String[] buffer_message = msg.split("~");
					old_message = msg.trim();
					System.out.println(msg + " Lock: " + lock_Enter);
					if(buffer_message[0].equals("Enter") && (lock_Enter == 0 || Integer.parseInt(buffer_message[1]) == lock_Enter))
					{
						lock_Enter = Integer.parseInt(buffer_message[1]);
						this.talk("Granted" + "~" + this.id + "~" + this.truck.location[0] 
								+ "~" + this.truck.location[1] + "~" + this.truck.speed 
								+ "~" + buffer_message[1]);
						if((this.truck).tail != null)
						{
							this.talk("Make_Room~"+ this.truck.tail.id);
						}
					}
					else if(buffer_message[0].equals("Joined"))
					{
						System.out.println("Yeah we made it here");
						this.truck.tail = new Car(Integer.parseInt(buffer_message[1]));
						lock_Enter = 0;
					}
					else if(buffer_message[0].equals("Dueces"))
					{
						this.talk("GoodBye~" + buffer_message[1]);
						if(buffer_message[1].equals(this.truck.id))
						{
							this.truck.tail = new Car(Integer.parseInt(buffer_message[3]));
						}
					}
					else
						this.talk(this.id + "~" + this.truck.location[0] + "~" + this.truck.location[1] + "~" + this.truck.speed);
				}
				this.talk(this.id + "~" + this.truck.location[0] + "~" + this.truck.location[1] + "~" + this.truck.speed);
			}
		}
	
		else
		{
		Thread drive = new Thread(this.car);
		drive.start();
		String old_Message = "";
		while(! this.joined_Train)
		{
			String msg = this.listen();
			if(!msg.equals("") && ! old_Message.equals(msg.trim()))
			{
				String[] buf = msg.split("~");
				old_Message = msg.trim();
				if(Integer.parseInt(buf[buf.length - 3]) - this.car.location[0] < 100 && Integer.parseInt(buf[buf.length - 3]) - this.car.location[0] > 10)
				{
					while(!joined_Train)
					{
						this.car.speed = 40;
						this.talk("Enter~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
						for(int i = 0; i < 50; i++)
						{
							msg = this.listen();
							if (! msg.equals("") && ! old_Message.equals(msg.trim())) 
							{
								old_Message = msg.trim();
								buf = msg.split("~");
								if(buf[0].equals("Granted") && buf[buf.length - 1].trim().equals(String.valueOf(this.id)))
								{	
									joined_Train = this.joinTrain();
									i = 50;
								}	
							}
						}
						int j = 50;
						while(j > 0 && ! this.car.join)
						{
							this.car.speed = 41;
							j--;
						}
					}
				}
				else if(this.car.location[0] > this.car.dest)
				{
					this.leaveTrain();
				}
			}
			this.talk(this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
		}
		while(true)
		{
			try{
				String msg = this.listen();
				if(! msg.equals("") && ! old_Message.equals(msg.trim()))
				{
					System.out.println(msg);
					old_Message = msg.trim();
					String[] buf = msg.split("~");
					if(this.car.location[0] > this.car.dest)
					{
						this.leaveTrain();
					}
					else if(buf[0].equals("Make_Room") && Integer.parseInt(buf[1]) != this.id && this.car.truck != null)
					{
						long t = System.currentTimeMillis();
						long end = t + 5000;
						this.car.speed = 35;
						while(System.currentTimeMillis() < end) {
							this.talk(this.id + "~" + this.car.location[0] + "~" 
									+ this.car.location[1] + "~" + this.car.speed);
						}
						this.car.speed = 40;
					}
					else if(buf[0].equals("Joined") && Integer.parseInt(buf[1]) != this.id && this.car.truck != null)
					{
						Car head = new Car(Integer.parseInt(buf[1]));
						this.car.head = head;
						this.car.truck = null;
					}
					else if(buf[0].equals("Out") && Integer.parseInt(buf[1]) != this.id && buf[1].equals(car.head.id))
					{
						long t = System.currentTimeMillis();
						long end = t + 5000;
						this.car.speed = 45;
						while(System.currentTimeMillis() < end) 
						{
							this.talk(this.id + "~" + this.car.location[0] + "~" 
									+ this.car.location[1] + "~" + this.car.speed);
						}
						this.car.speed = 40;
						if(buf[2].equals("0"))
						{
							Truck trans = new Truck(Integer.parseInt(buf[2]));
							this.car.truck = trans;
						}
						else
						{
							Car trans = new Car(Integer.parseInt(buf[2]));
							this.car.head = trans;
						}
					}
					else if(this.car.truck != null && Integer.parseInt(buf[buf.length - 4]) == 0){
						System.out.println(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
						this.car.speed = Integer.parseInt(buf[buf.length - 1]);
						this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
					}
					else if(this.car.truck == null && this.car.head.id == Integer.parseInt(buf[buf.length - 4])){
						this.car.speed = Integer.parseInt(buf[buf.length - 1]);
						this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
					}
				}	
			}catch(Exception e){
			}
			}
		}
	}

	public boolean joinTrain() throws IOException
	{
		this.car.location[1] = 1;
		this.car.speed = 43;
		String old_Message = "";
		while(true)
		{
			String msg = this.listen();
			if(!msg.equals("") && ! old_Message.equals(msg.trim()) && !msg.contains("Make_Room~")
			{
				String[] status = msg.split("~");
				if(Integer.parseInt(status[1]) - this.car.location[0] < 15 && Integer.parseInt(status[1]) - this.car.location[0] > 5)
				{
					this.car.location[1] = 0;
					this.car.speed = 40;
					this.talk("Joined~" + this.id + "~" + this.car.location[0]
								+ "~" + this.car.location[1] + "~" + this.car.speed);
					this.car.truck = new Truck(0);
					this.car.join = true;
					return true;
				}
				else if(Integer.parseInt(status[1]) - this.car.location[0] < 5)
					this.car.speed = 38;
				else if(Integer.parseInt(status[1]) - this.car.location[0] > 15)
					this.car.speed = 43;
			}
		}
	}
	
	public boolean leaveTrain() throws IOException
	{
		this.talk("Dueces~" + this.id);
		while(true)
		{
			String msg = this.listen();
			String[] buf = msg.split("~");
			if(buf[0].equals("GoodBye") && Integer.parseInt(buf[1]) == this.id)
			{
			break;
			}
		}
		this.car.speed = 0;
		this.car.location[1] = -1;
		this.joined_Train = false;
		if(this.car.tail == null)
		{
			if(this.car.head.truck == null)
				this.talk("Out~" + this.id + "~" + this.car.head.id);
			else
				this.talk("Out~" + this.id + "~" + this.car.truck.id);
			return true;
		}
		else
		{
			if(this.car.truck == null)
				this.talk("Out~" + this.id + "~" + this.car.head.id);
			else
				this.talk("Out~" + this.id + "~" + this.car.truck.id);
			return true;
		}
	}
	
	public void setConfigFilePosition() throws IOException
	{
		String buffer = "";	
		Scanner scan = new Scanner(config_File);
		int i = 10;
		while (i > 0)
		{
			i--;
			buffer = scan.nextLine();
			if(!buffer.equals(""))
			{
				String[] buffer_string = buffer.split(" ");
				if(Integer.parseInt(buffer_string[2]) == this.port)
				{
					this.id = Integer.parseInt(buffer_string[0]);
					if(this.id == 0)
					{
						this.setVehicle(true,Integer.parseInt(buffer_string[3]),Integer.parseInt(buffer_string[4]));
					}
					else
					{
						this.setVehicle(false,Integer.parseInt(buffer_string[3]),Integer.parseInt(buffer_string[4]));
					}
				return;
				}
			}
		}
	}
	
	public String listen()
	{
		return rba.listenForMessage();
	}
	
	public void talk(String msg)
	{
		rba.broadcast(msg);
	}
}

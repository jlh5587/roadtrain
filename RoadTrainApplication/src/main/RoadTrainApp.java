
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

		public RoadTrainApp(int port, File config, String leave){
		this.config_File = config;
		this.port = port;
		try{
			this.setConfigFilePosition();
			this.car.dest = Integer.valueOf(leave);
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
				this.truck.speed = 20;
				this.truck.location[0] = x;
				this.truck.location[1] = y;
				this.joined_Train = true;
			}
			else
			{
				this.car = new Car(id,port,this.config_File);
				this.car.location[0] = x;
				this.car.location[1] = y;
				car.speed = 25;
			}
		}
	



	public void ignition() throws IOException
	{
		// If this is the truck then it will run the following lines 
		if(this.id == 0)
		{
			Thread drive = new Thread(this.truck);
			drive.start();
			int lock_Enter = 0;
			String joined_Vics = "";
			while(true)
			{
				String msg = this.listen().trim();
				if(! msg.equals(""))
				{
					String[] buffer_message = msg.split("~");
					System.out.println("Lock: " + lock_Enter);
					if(buffer_message[0].equals("Enter") && (lock_Enter == 0 || Integer.parseInt(buffer_message[1]) == lock_Enter))
					{
						lock_Enter = Integer.parseInt(buffer_message[1]);
						for(int i = 0; i < 5; i++)
							this.talk("Granted" + "~" + this.id + "~" + this.truck.location[0] 
									+ "~" + this.truck.location[1] + "~" + this.truck.speed 
									+ "~" + buffer_message[1]);
						if((this.truck).tail != null)
						{
							for(int i = 0; i < 5; i++)
								this.talk("Make_Room~"+ this.truck.tail.id + "~0~0~0~0~0");
						}
					}
					else if(buffer_message[0].equals("Joined") && lock_Enter != 0)
					{
						this.truck.tail = new Car(Integer.parseInt(buffer_message[1]));
						lock_Enter = 0;
						joined_Vics += "~" + buffer_message[1];
					}
					else if(buffer_message[0].equals("Dueces"))
					{
						this.talk("GoodBye~" + buffer_message[1]+ "~0~0~0~0~0");
						if(buffer_message[1].equals(this.truck.id))
						{
							this.truck.tail = new Car(Integer.parseInt(buffer_message[3]));
						}
					}
					else if(buffer_message[0].equals("Out"))
					   {
					      String temp = buffer_message[1];
    				      buffer_message = joined_Vics.split("~");
    				      joined_Vics = "";
		                  for(int i = 0; i < buffer_message.length; i++)
		                  {
		                  	if(!temp.equals(buffer_message[i]))
		                  	   joined_Vics += buffer_message[i] + "~";
		                  }
					   }
					else
						this.talk(this.id + "~" + this.truck.location[0] + "~" + this.truck.location[1] + "~" + this.truck.speed);
				}
				this.talk(this.id + "~" + this.truck.location[0] + "~" + this.truck.location[1] + "~" + this.truck.speed + joined_Vics);
			}
		}
		// if this is a car then it will run these lines
		else
		{
		Thread drive = new Thread(this.car);
		drive.start();
		while(! this.joined_Train)
		{
			String msg = this.listen().trim();
			if(!msg.equals(""))
			{
				String[] buf = msg.split("~");
				if(Integer.parseInt(buf[buf.length - 3]) - this.car.location[0] < 100 && Integer.parseInt(buf[buf.length - 3]) - this.car.location[0] > 10)
				{
					
					this.car.speed = 20;
					while(!joined_Train)
					{
						long t = System.currentTimeMillis();
						long end = t + 2000;
						while(System.currentTimeMillis() < end && ! joined_Train){}
						
						for(int i = 0; i < 5; i++)
							this.talk("Enter~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
						msg = this.listen().trim();
						if (! msg.equals("")) 
						{
							buf = msg.split("~");
							if(buf[0].trim().equals("Granted") && buf[buf.length - 1].trim().equals(String.valueOf(this.id)))
								joined_Train = this.joinTrain();
						}
					}
				}
				else if(this.car.dest > 0 && this.car.location[0] > this.car.dest)
				{
					this.leaveTrain();
				}
			}
		}
		while(true)
		{
			try{
				String msg = this.listen().trim();
				if(! msg.equals(""))
				{
					String[] buf = msg.split("~");
					if(this.car.dest > 0 && this.car.location[0] > this.car.dest)
					{
						this.leaveTrain();
					}
					else if(buf[0].trim().equals("Make_Room") && Integer.valueOf(buf[1]) != this.id)
					{
					   this.car.status = 4;
					   long t = System.currentTimeMillis();
					   long end = t + 10000;
					   while(System.currentTimeMillis() < end) {}
					   this.car.status = 1;
						
					}
					else if(buf[0].trim().equals("0") && this.car.truck != null && Arrays.asList(buf).contains(Integer.toString(this.car.id)) && !buf[buf.length - 1].equals(Integer.toString(this.car.id)))
					{
						Car head = new Car(Integer.parseInt(buf[buf.length - 1]));
						this.car.head = head;
						this.car.truck = null;
					}
					else if(buf[0].equals("Out") && this.car.truck == null)
					{
					    this.car.status = 5;
						long t = System.currentTimeMillis();
						long end = t + 10000;
						while(System.currentTimeMillis() < end){}
						this.car.status = 1;
						if(this.car.head != null && buf[1].equals(Integer.toString(this.car.head.id)))
						{
							Truck trans = new Truck(0);
							this.car.truck = trans;
						}
						else
						{
							Car trans = new Car(Integer.parseInt(buf[2]));
							this.car.head = trans;
						}
					}
					this.talk(this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
				}
			}catch(Exception e){System.out.println(e.toString());}
		}
	}
}

	public boolean joinTrain() throws IOException
	{
		this.car.location[1] = 1;
		this.car.speed = 23;
		while(true)
		{
			String msg = this.listen().trim();
			if(!msg.equals("") && !msg.contains("Make_Room~"))
			{
				String[] status = msg.split("~");
				if(Integer.parseInt(status[1]) - this.car.location[0] < 10 && Integer.parseInt(status[1]) - this.car.location[0] > 3)
				{
					this.car.location[1] = 0;
					this.car.speed = 20;
					for(int i = 0; i < 10; i++)
						this.talk("Joined~" + this.id + "~" + this.car.location[0]
									+ "~" + this.car.location[1] + "~" + this.car.speed);
					this.car.truck = new Truck(0);
					this.car.status = 1;
					return true;
				}
				else if(Integer.parseInt(status[1]) - this.car.location[0] < 3)
					this.car.speed = 18;
				else if(Integer.parseInt(status[1]) - this.car.location[0] > 10)
					this.car.speed = 23;
			}
		}
	}
	
	public void leaveTrain() throws IOException
	{
		for(int i = 0; i < 10; i++)
			this.talk("Dueces~" + this.id + "~0~0~0~0~0~0~0");
		while(true)
		{
			String msg = this.listen().trim();
			String[] buf = msg.split("~");
			if(buf[0].equals("GoodBye") && Integer.parseInt(buf[1]) == this.id)
			{
			break;
			}
		}
		this.car.speed = 0;
		this.car.location[1] = -1;
		this.joined_Train = false;
		for(int i = 0; i < 5; i++)
			this.talk("Out~" + this.id + "~0~0~0~0~0~0~0~0");
		this.car.status = 2;
		System.exit(0);
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
		String check = rba.listenForMessage();
		System.out.println(check);
		return check;
	}

	public void talk(String msg)
	{
		System.out.println(msg);
		rba.broadcast(msg);
	}
}

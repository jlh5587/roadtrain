package App;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import Vehicle.Car;
import Vehicle.Truck;
import RBA.RBA;

public class RoadTrainApp {
	int port, position;
	RBA rba;
	Truck truck;
	Car car;
	String config_name = "", id = "";
	File config_File = null;
	boolean joined_Train = false;
		
	public RoadTrainApp(boolean isTruck, String id, int port, File config){
		setVehicle(isTruck);
		this.config_File = config;
		this.id = id;
		this.port = port;
		try{
			rba = new RBA(id, port);
		} catch (Exception e){
		}
	}
	
	public void setVehicle(boolean isTruck)
		{
			if (isTruck)
			{
				truck = new Truck(id,port);
				truck.speed = 40;
				this.joined_Train = true;
			}
			else
			{
				car = new Car(id,port);
				car.speed = 40;
			}
			
		}
	
	public void ignition(boolean isTruck) throws IOException
	{
		Thread message = new Thread((Runnable) this.rba);
		message.start();
		
		if(isTruck)
		{
			Thread drive = new Thread((Runnable) this.truck);
			drive.start();
			while(true)
			{
				String msg = this.listen();
				String[] buffer_message = msg.substring(1, msg.length() - 1).split("~");
				this.update_config();
				if(buffer_message[0].equals("Enter"))
				{
					this.talk("Granted" + "~" + this.id + "~" + this.truck.location[0] 
							+ "~" + this.truck.location[1] + "~" + this.truck.speed 
							+ buffer_message[1]);
					if(((Truck) this.truck).tail != null)
					{
						this.talk("Make_Room~"+ this.truck.tail.id);
					}
				}
				
				else if(buffer_message[0].equals("Joined"))
				{
					this.truck.tail = new Car(buffer_message[1]);
				}
				else if(buffer_message[0].equals("Dueces"))
				{
					this.talk("GoodBye~" + buffer_message[1]);
					if(buffer_message[1].equals(this.truck.id))
					{
						this.truck.tail = new Car(buffer_message[3]);
					}
				}
				else
					this.talk(this.id + "~" + this.truck.location[0] + "~" + this.truck.location[1] + "~" + this.truck.speed);
			}
		}
	
		else
		{
		Thread drive = new Thread((Runnable) this.car);
		drive.start();
		while(!this.joinTrain())
		{
			String msg = this.listen();
			String[] buf = msg.substring(1, msg.length() - 1).split("~");
			if(Integer.parseInt(buf[buf.length - 3]) < 100)
			{
				this.talk("Enter~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
			}
			if(buf[0].equals("Granted") && buf[buf.length - 1].equals(this.id))
			{
				this.joinTrain();
			}
			else if(this.car.location[0] > this.car.dest)
			{
				this.leaveTrain();
			}
			this.update_config();	
		}
		while(true)
		{
			String msg = this.listen();
			String[] buf = msg.substring(1, msg.length() - 1).split("~");
			this.update_config();
			if(this.car.location[0] > this.car.dest)
			{
				this.leaveTrain();
			}
			else if(buf[0].equals("Make_Room") && this.car.truck != null)
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
			else if(buf[0].equals("Joined") && this.car.truck != null)
			{
				Car head = new Car(buf[1]);
				this.car.head = head;
				this.car.truck = null;
			}
			else if(buf[0].equals("Out") && buf[1].equals(car.head.id))
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
				if(buf[2].equals("Truck"))
				{
					Truck trans = new Truck(buf[2]);
					this.car.truck = trans;
				}
				else
				{
					Car trans = new Car(buf[2]);
					this.car.head = trans;
				}
			}
			else if((buf[buf.length() - 5].equals(this.car.head.id) && this.truck == null) 
			|| (buf[buf.length() - 5].equals(this.truck.id))
			{
			this.car.speed = Integer.parseInt(buf[buf.length - 1]);
			this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
		}
		}
		}
	}

	public boolean joinTrain()
	{
		if(this.car.location[1] == 0)
		{
			this.car.location[1] = 1;
			this.car.speed = 45;
		while(true)
		{
			String[] status = this.listen().split("~");
			int trailing_distance = Integer.parseInt(status[1]);
			if(trailing_distance - this.car.location[0] < 20)
			{
				this.car.speed = 41;
				while(trailing_distance - this.car.location[0] < 10)
				{
					this.car.location[1] = 0;
					this.car.speed = 40;
					this.talk("Joined~" + this.id + "~" + this.car.location[0] + "~" 
					+ "~" + this.car.location[1] + "~" + this.car.speed);
					return true;
				}
				
			}
		}
		}
		return false;
	}
	
	public boolean leaveTrain() throws IOException
	{
		this.talk("Dueces~" + this.id);
		while(true)
		{
			String msg = this.listen();
			String[] buf = msg.substring(1, msg.length() - 1).split("~");
			if(buf[0].equals("GoodBye") && buf[1].equals(this.id))
			{
			break;
			}
			this.update_config();
		}
		this.car.speed = 0;
		this.car.location[1] = -1;
		this.joined_Train = false;
		this.update_config();
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
		
	public void update_config() throws IOException
	{
		Queue<String> config_buffer = new LinkedList<String>();
		FileInputStream in = new FileInputStream(config_File);
		int c = 0;
		try {
			String buffer = "";
			while((c = in.read()) != -1 )
			{
				if (c != 10)
					buffer += (char) c;
				else
				{
				config_buffer.add(buffer);
				buffer = "";
				}
			}
			config_buffer.add(buffer);
		}
		finally
		{
			if(in != null)
			{
				in.close();
			}
		}

		FileOutputStream out = new FileOutputStream(config_File);
		String check = config_buffer.poll();
		c = this.position;
		do
		{		
			c--;
			if (c == 0)
			{
				String[] buf = check.split(" ");
				check = "";
				if(this.truck == null)
					buf[3] =  Integer.toString(this.car.location[1]);
				else
					buf[3] =  Integer.toString(this.truck.location[1]);
				
				for(int i = 0; i < buf.length; i++)
				{
					check += buf[i] + " ";
				}
				
				if(this.joined_Train && !buf[buf.length - 1].equals("connected"))
					check += "connected";
				else if (!this.joined_Train && buf[buf.length - 1].equals("connected"))
					check = check.substring(0, check.length() - " connected".length());
			}
			byte[] conent = check.getBytes();
			out.write(conent);
			out.write(10);
			
		}
		while((check = config_buffer.poll()) != null);
		out.flush();
		out.close();
	}
	
	public void setConfigFilePosition() throws IOException
	{
	FileInputStream in = new FileInputStream(config_File);
		int c = 0;
		String buffer = "";
		while((c = in.read()) != -1 )
		{
			if (c != 10)
			{
				buffer += (char) c;
			}
			else
			{
				if (buffer != "" && buffer.substring(0, 5).equals(this.id))
				{
					break;
				}
				position++;
				buffer = "";
			}
		}
			in.close();
	}
	
	public String listen()
	{
		return rba.getMessage();
	}
	
	public void talk(String msg)
	{
		rba.sendMessage(msg);
	}
}


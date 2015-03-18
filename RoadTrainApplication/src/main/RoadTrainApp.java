
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
				this.truck = new Truck(id,port);
				this.truck.speed = 40;
				this.truck.location[0] = x;
				this.truck.location[1] = y;
				this.joined_Train = true;
			}
			else
			{
				this.car = new Car(id,port);
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
			while(true)
			{
				System.out.println(this.truck.location[0]);
				
				String msg = this.listen();
				System.out.println("Train: " + msg);
				if(! msg.equals(""))
				{
					String[] buffer_message = msg.split("~");
					this.update_config();
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
				this.update_config();
			}
		}
	
		else
		{
		Thread drive = new Thread(this.car);
		drive.start();
		while(! this.joined_Train)
		{
			String msg = this.listen();
			System.out.println("Message: " + msg);
			if(!msg.equals(""))
			{
				String[] buf = msg.split("~");
				if(Integer.parseInt(buf[buf.length - 3]) - this.car.location[0] < 100 && Integer.parseInt(buf[buf.length - 3]) - this.car.location[0] > 10)
				{
					while(!joined_Train)
					{
						this.car.speed = 40;
						this.talk("Enter~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
						for(int i = 0; i < 50; i++)
						{
							this.update_config();
							msg = this.listen();
							if (! msg.equals(""))
							{
								buf = msg.split("~");
								System.out.println(msg);
								System.out.println(buf[buf.length - 1] + " " + String.valueOf(this.id) + "This.car.location[0] " +  this.car.location[0] + "Train:  " + buf[buf.length - 3]);
								if(buf[0].equals("Granted") && buf[buf.length - 1].trim().equals(String.valueOf(this.id)))
								{	
									joined_Train = this.joinTrain();
									this.car.truck = new Truck(0,10100);
									i = 50;
								}	
							}
						}
					}
				}
				else if(this.car.location[0] > this.car.dest)
				{
					this.leaveTrain();
				}
			}
			this.talk(this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
			this.update_config();
		}
		while(true)
		{
			System.out.println(this.car.truck == null);
			this.update_config();
			try{
				String msg = this.listen();
				if(! msg.equals(""))
				{
					System.out.println("Check_1 " + this.car.truck == null);
					String[] buf = msg.split("~");
					System.out.println("Check_2 " + this.car.truck == null);
					if(this.car.location[0] > this.car.dest)
					{
						this.leaveTrain();
					}
					System.out.println("Check_3 " + this.car.truck == null);
					if(buf[0].equals("Make_Room") && this.car.truck != null)
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
					System.out.println("Check_4 " + this.car.truck == null);
					if(buf[0].equals("Joined") && this.car.truck != null)
					{
						Car head = new Car(Integer.parseInt(buf[1]));
						this.car.head = head;
						this.car.truck = null;
					}
					System.out.println("Check_5 " + this.car.truck == null);
					if(buf[0].equals("Out") && buf[1].equals(car.head.id))
					{
						System.out.println("Wait a min");
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
					System.out.println(this.car.truck == null);
					System.out.println(Integer.parseInt(buf[buf.length - 4]));
					if(this.car.truck != null && Integer.parseInt(buf[buf.length - 4]) == 0){
						this.car.speed = Integer.parseInt(buf[buf.length - 1]);
						this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
					}
					System.out.println("Check_7 " + this.car.truck == null);
					if(this.car.truck == null && this.car.head.id == Integer.parseInt(buf[buf.length - 4])){
						this.car.speed = Integer.parseInt(buf[buf.length - 1]);
						this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
					}
					System.out.println("Check_8 " + this.car.truck == null);
				}	
			}catch(Exception e){
				System.out.println(e.toString());	
			}
			}
		}
	}

	public boolean joinTrain() throws IOException
	{
		this.car.location[1] = 1;
		this.car.speed = 45;
		while(true)
		{
			String msg = this.listen();
			this.update_config();
			if(!msg.equals(""))
			{
				System.out.println("Joined MSG: "+ msg + "Location " + this.car.location[0] + " " + this.car.location[1]);
				String[] status = msg.split("~");
				if(status[0].equals("Granted") && Integer.parseInt(status[1]) - this.car.location[0] < 20)
				{
					this.car.speed = 43;
					while(true)
					{
						this.update_config();
						System.out.println(this.car.location + " " + msg);
						msg = this.listen();
						if(!msg.equals(""))
						{
							status = msg.split("~");
							if(Integer.parseInt(status[1]) - this.car.location[0] < 10 && Integer.parseInt(status[1]) - this.car.location[0] > 0)
							{
								this.car.location[1] = 0;
								this.car.speed = 40;
								this.talk("Joined~" + this.id + "~" + this.car.location[0] + "~" 
										+ "~" + this.car.location[1] + "~" + this.car.speed);
								this.car.truck = new Truck(0,10100);
								System.out.println(this.car.truck == null);
								return true;
							}
						}
					}
				}
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
		//Read in the file
		ArrayList<String> lines = new ArrayList<String>();
		Scanner file_scan = new Scanner(config_File);
		int index = 10;
		String links = "";
		String edit_Line = "";
		String[] buf = null;
		//adds each line of the file to the list of lines
		while(file_scan.hasNext() && index != 0)
		{
			index--;
			lines.add(file_scan.nextLine());
		}
		//closes the original scanner
		file_scan.close();
		Iterator<String> itr = lines.iterator();
		//loops through each line to determine the distance from itself to that line.
		while(itr.hasNext())
		{
			buf = itr.next().split(" ");
			if(Integer.parseInt(buf[0]) != this.id)
			{
				if(this.truck == null)
				{	
					if(Integer.parseInt(buf[3]) - this.car.location[0] < 80 && Integer.parseInt(buf[3]) - this.car.location[0] > -80)
					{
						links += Integer.parseInt(buf[0]) + " ";
					}
				}
				else
				{
					if(Integer.parseInt(buf[3]) - this.truck.location[0] < 80 && Integer.parseInt(buf[3]) - this.truck.location[0] > -80)
					{
						links += Integer.parseInt(buf[0]) + " ";
					}
				}
			}
		}
		if(! lines.isEmpty())
		{
			buf = lines.get(this.id).split(" ");
			if(this.truck == null)
				edit_Line = buf[0] + " " + buf[1] + " " + buf[2] + " " + Integer.toString(this.car.location[0]) + " " + Integer.toString(this.car.location[1]) + " " + buf[5] + " " +  links;
			else
				edit_Line = buf[0] + " " + buf[1] + " " + buf[2] + " " + Integer.toString(this.truck.location[0]) + " " + Integer.toString(this.truck.location[1]) + " " + buf[5] + " " + links; 
			lines.set(this.id, edit_Line);

			PrintWriter clear = new PrintWriter(config_File);
			clear.print("");
			clear.close();
			PrintWriter write = new PrintWriter(config_File);
			for(int i = 0; i<lines.size(); i++){
				write.println(lines.get(i));
			}
			write.close();
		}
		
	}
	
	public void setConfigFilePosition() throws IOException
	{
		String buffer = "";	
		Scanner scan = new Scanner(config_File);

		while (scan.hasNextLine())
		{
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
					break;
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

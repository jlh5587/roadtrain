
import java.io.File;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;
import java.util.*;
import java.nio.channels.*;

public class RoadTrainApp {
	int port, id;
	RBA rba;
	Truck truck;
	Car car;
	String config_name = "";
	File config_File = null;
	boolean joined_Train = false;
	FileChannel channel;
	FileLock lock;	
	
	public RoadTrainApp(int port, File config){
		this.config_File = config;
		this.port = port;
		
		try{
			channel = new RandomAccessFile(config, "rw").getChannel();
			lock = channel.lock();
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
				System.out.println(msg);
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
							msg = this.listen();
							if (! msg.equals(""))
							{
								buf = msg.split("~");
								System.out.println(msg);
								System.out.println(buf[buf.length - 1] + " " + String.valueOf(this.id) + "This.car.location[0] " +  this.car.location[0] + "Train:  " + buf[buf.length - 3]);
								if(buf[0].equals("Granted") && buf[buf.length - 1].trim().equals(String.valueOf(this.id)))
								{
									
									joined_Train = this.joinTrain();
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
			System.out.println("joined: " + this.joined_Train);
		}
		while(true)
		{
			System.out.println(car.location[0]);
			try{
				String msg = this.listen();
				if(! msg.equals(""))
				{
					System.out.println(msg);
					String[] buf = msg.split("~");
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
						Car head = new Car(Integer.parseInt(buf[1]));
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
					else if(this.car.truck != null && Integer.parseInt(buf[buf.length - 4] == 0){
						this.car.speed = Integer.parseInt(buf[buf.length - 1]);
						this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
					}
					else if(this.car.head.int == Integer.parseInt(buf[buf.length - 4]){
						this.car.speed = Integer.parseInt(buf[buf.length - 1]);
						this.talk(msg + "~" + this.id + "~" + this.car.location[0] + "~" + this.car.location[1] + "~" + this.car.speed);
					}
					System.out.println("It's Here");
				}	
			}catch(Exception e){
			
			}
			}
		}
	}

	public boolean joinTrain()
	{
		this.car.location[1] = 1;
		this.car.speed = 45;
		while(true)
		{
			String msg = this.listen();
			if(!msg.equals(""))
			{
				System.out.println("Joined MSG: "+ msg + "Location " + this.car.location[0] + " " + this.car.location[1]);
				String[] status = msg.split("~");
				if(status[0].equals("Granted") && Integer.parseInt(status[1]) - this.car.location[0] < 20)
				{
					this.car.speed = 43;
					while(true)
					{
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
		//adds each line of the file to the list of lines
		while(file_scan.hasNext()){
			lines.add(file_scan.nextLine());
		}
		//closes the original scanner
		file_scan.close();
		String links = "links";
		//loops through each line to determine the distance from itself to that line.
		for(int i = 0; i < lines.size(); i++){
			Scanner line_scan = new Scanner(lines.get(i));
			int car_id = line_scan.nextInt();
			line_scan.next();
			line_scan.next();
			int x_val = line_scan.nextInt();

				if(this.truck == null)
				{
					if(car_id != id){
						int d = x_val - this.car.location[0];
						if(d < 80 && d > -80){
							links += " " + Integer.toString(i);
						}
					}
					
				}
				else
				{
					if(car_id != id){
						int d = x_val - this.truck.location[0];
						if(d < 80 && d > -80){
							links += " " + Integer.toString(i);
						}
					}
				}
			line_scan.close();
		}


		//Prepare each line to be written to the file

		Scanner scan = new Scanner(lines.get(id));

		if(this.truck == null)
		{
			String new_line = scan.next() + " " + scan.next() + " " +scan.next()+ " " + Integer.toString(this.car.location[0]) +" "+ Integer.toString(this.car.location[1]) + " " + links;
			lines.set(id, new_line);

		}
		else
		{
			String new_line = scan.next() + " " + scan.next() + " " +scan.next() +" " + Integer.toString(this.truck.location[0]) +" "+ Integer.toString(this.truck.location[1]) + " " + links;
			lines.set(id, new_line);
		}


		//write each line back to the file.
		try{
			channel.tryLock();
			//clears file
			PrintWriter clear = new PrintWriter(config_File);
			clear.print("");
			clear.close();

			PrintWriter write = new PrintWriter(config_File);
			
			for(int i = 0; i<lines.size(); i++){
				write.println(lines.get(i));
			}

			write.close();

		}catch(OverlappingFileLockException e){
			
		}finally{
			lock.release();
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

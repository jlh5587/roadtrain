import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Car implements Runnable{

	public int speed = 0;
	public int[] location = {0,0};
	public int port = 0;
	public int dest = 999999999;
	public Car head = null, tail = null;
	public Truck truck = null;
	public int id = 0;
	public boolean join = false;
	public File config_File = null;
	
	public Car(int id, int port, File config_File){
		this.id = id;
		this.port = port;
		this.config_File = config_File;
	}
	
	public Car(int id){
		this.id = id;
	}

	public void run() {
		while (true)
		{
				try {
					Thread.sleep(2000);
					location[0] += speed + speed;
					this.update_config();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
				if(Integer.parseInt(buf[3]) - this.location[0] < 100 && Integer.parseInt(buf[3]) - this.location[0] > -100)
				{
					links += buf[0] + " ";
				}
			}
		}
		if(! lines.isEmpty())
		{
			buf = lines.get(this.id).split(" ");
			edit_Line = buf[0] + " " + buf[1] + " " + buf[2] + " " + Integer.toString(this.location[0]) + " " + Integer.toString(this.location[1]) + " " + buf[5] + " " +  links;
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
}

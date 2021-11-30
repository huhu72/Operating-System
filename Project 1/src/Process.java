import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;



//Print to a file when a user ask for the status and show what processes are running at what stage.



//Used in the termination stage
class IOStatusInfo{
	
}
class Command{
		String command = "";
		int cycle = 0;
		Command(String command, int cycle){
			this.command = command;
			this.cycle = cycle;
		}
		
		@Override
		   public String toString() {
		        return (this.command + " " + this.cycle);
		   }
		
	}
public class Process {
	
	
	private ArrayList<Integer> cycles = new ArrayList<>();
	private int processCreationCounter = 1;
	//**Not really needed since i will just added it to the job queue after it is created?**
//	private ArrayList<Process> processes = new ArrayList<>();
	private ArrayList<Command> processCommands;
	private String processName;
	private CPU cpu;
	private long pid;
	private int critStart;
	private int critEnd;
	public int timeLimit;
	public int priority;
	private int pidCounter = 0;
	Process(CPU cpu){
		this.cpu = cpu;
	}
		
	Process(String processName,ArrayList<Command> commands, long pid, int critStart, int critEnd){
		this.processName = processName;
		this.processCommands=commands;
		this.pid = pid;
		this.critStart = critStart;
		this.critEnd = critEnd;
		this.timeLimit = 4;
		this.priority = (int) ((Math.random() * (10 - 1)) + 1);
	}
	
	/*public static void main(String[] args) {
		Process p = new Process();
		try {
			p.createProcessesPrompt();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	public Process() {
		// TODO Auto-generated constructor stub
	}

	public void createProcessesPrompt() throws FileNotFoundException {
		Scanner input = new Scanner(System.in);
		String command;
		String argument;
		String [] arguments;
		//showTemplates();
		System.out.print("User: ");
		command = input.next();
		command = command.toLowerCase();
		if(command.equals("help")) showHelp();
		else if(command.equals("template")) {
			System.out.println();
			showTemplates();
		}else if(command.equals("create")) {
			argument = input.nextLine();
			arguments = argument.trim().split(" ");
			//getUserInput(); 
			for(int i = 0; i < arguments.length; i = i+2) {
				createProcesses(arguments[i] + ".txt", Long.parseLong(arguments[i+1]));
				
			}
		}	

	}
	private void showHelp() throws FileNotFoundException {
		System.out.println("\ncreate <template name> <number of processes>... [creates nummber of processes based on the given templates]");
		System.out.println("status [shows the current status of the CPU]");
		System.out.println("template [shows all the available templates]\n");
		//getUserInput();
		createProcessesPrompt();
	}
	private void createCommands(String templateName) throws FileNotFoundException {
		File template = new File("./Templates/" + templateName);
		Scanner file = new Scanner(template);
		int low, high;
		Command commandInfo;
		String command;
		this.processCommands = new ArrayList<>();
		
		while (file.hasNext()) {
			command = file.next();
			if(command.equals("C*")) {
				//This will represent the index number since processes are held in arraylists 
				//so the current line number will be critStart +1.
				//Same logic applies to critEnd
				this.critStart = this.processCommands.size() + 1;
				command = file.next();
			}else if(command.equals("Cn")) {
				this.critEnd = this.processCommands.size() + 1;
				command = file.next();
			}
			low = file.nextInt();
			high = file.nextInt();
			commandInfo = new Command(command,randomNum(low, high));
			this.processCommands.add(commandInfo);
			
		}
		file.close();
		
		
	}
	public void createProcesses(String templateName, long numProcessInput) throws FileNotFoundException {
		//String templateLocation = "./Templates/" + templateName; 
		Process process;
		this.pid = 1;
		for (int i = 0; i < numProcessInput; i++) {
			createCommands(templateName);
			process = new Process("Process" + processCreationCounter,getCommands(), (this.pid + this.pidCounter), this.critStart, this.critEnd);
			this.pidCounter++;
			this.processCreationCounter++;
			//pcb = new PCB(process, this.cpu);
			this.cpu.addToProcessQueue(process);
			this.cpu.addPCB(new PCB(process));
			//System.out.println(this.pid);
	
		}
		
	}

	public ArrayList<Command> getCommands() {
		return this.processCommands;
	}
	private int getCycles(int num) {
		return this.cycles.get(num);
	}
	
	
	public void showTemplates() {
		File[] templates = new File("./Templates").listFiles();
		System.out.println("Here are the available templates:");
		for (File f : templates) {
			System.out.println(f.getName());
		}
		try {
			createProcessesPrompt();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int randomNum(int low, int high) {
		return (int) ((Math.random() * (high - low)) + low);
	}

	public long getPID() {
		return this.pid;
	}
	public int getCritStart() {
		return this.critStart;
	}
	public int getCritEnd() {
		return this.critEnd;
	}
	public String getProcessName(){
		return this.processName;
	}

	@Override
	   public String toString() {
	        return ("Process Name: "+this.processName + "\nProcess PID: " + this.pid + "\n Priority: " + this.priority);
	   }
	/*	private void runProcesses(){
	File[] processes = new File("./Processes/").listFiles();
	int cycles;
	int totalCycles = 0;
	String command;
	Scanner process;
	for (File p : processes) {
		System.out.println("Running " + p.getName());
		try {
			process = new Scanner(p);
			while(process.hasNext()) {
				command = process.next();
				cycles = process.nextInt();
				totalCycles = cycles + totalCycles;
				//System.out.println("Running " + command);
				for(int i = 0; i < cycles; i++) {
					if(i == 0) {
						System.out.println("STATUS: NEW");
					}else if(i == cycles-1) {
						System.out.println("STATUS: EXIT");
					}else {
						System.out.println("STATUS: RUN");
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0; i < totalCycles; i++) {
			if(i == 0) {
				System.out.println("STATUS: NEW");
			}else if(i == totalCycles-1) {
				System.out.println("STATUS: EXIT");
			}else {
				System.out.println("STATUS: RUN");
			}
		}
		
	}
}*/

/*	Process(){
		this.state = STATE.NEW;
		this.processFile = new File("");
		this.programCounter = 1;
		this.memory = 0;
		
	}*/
	
}

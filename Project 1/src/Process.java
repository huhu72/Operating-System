import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

//Print to a file when a user ask for the Status and show what processes are running at what stage.

//Used in the termination stage
class IOStatusInfo {

}

class Command {
	String command = "";
	int cycle = 0;

	Command(String command, int cycle) {
		this.command = command;
		this.cycle = cycle;
	}

	@Override
	public String toString() {
		return (this.command + " " + this.cycle);
	}

}

public class Process extends CPU implements Runnable {

	private ArrayList<Integer> cycles = new ArrayList<>();
	private int processCreationCounter = 1;
	// **Not really needed since i will just added it to the job queue after it is
	// created?**
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
	private long childPID;
	private long parentPID;
	int TOTAL_MEMORY = 1024;
	static int memoryCount;
	int memory;


	Process(CPU cpu) {
		this.cpu = cpu; 
	
	}

	Process(String processName, ArrayList<Command> commands, long pid, int critStart, int critEnd) {
		this.processName = processName;
		this.processCommands = commands;
		this.pid = pid;
		this.critStart = critStart;
		this.critEnd = critEnd;
		this.timeLimit = 4;
		this.priority = (int) ((Math.random() * (10 - 1)) + 1);
		this.memory = (int) ((Math.random() * (1024 - 1)) + 1);
	}

	/*
	 * public static void main(String[] args) { Process p = new Process(); try {
	 * p.createProcessesPrompt(); } catch (FileNotFoundException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */


	public void createProcessesPrompt(int[] arguments) throws FileNotFoundException {
		String command;
		String argument;
		String [] templates = {"cpubound.txt","iobased.txt","longbased.txt","shortbased.txt"};
		for(int i = 0; i < templates.length; i++) {
			createProcesses(templates[i], arguments[i]);	
		}
	

	}

	private void showHelp() throws FileNotFoundException {
		System.out.println(
				"\ncreate <template name> <number of processes>... [creates nummber of processes based on the given templates]");
		System.out.println("Status [shows the current Status of the CPU]");
		System.out.println("template [shows all the available templates]\n");
		// getUserInput();
	//	createProcessesPrompt();
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
			if (command.equals("C*")) {
				// This will represent the index number since processes are held in arraylists
				// so the current line number will be critStart +1.
				// Same logic applies to critEnd
				this.critStart = this.processCommands.size() + 1;
				command = file.next();
			} else if (command.equals("Cn")) {
				this.critEnd = this.processCommands.size() + 1;
				command = file.next();
			}
			low = file.nextInt();
			high = file.nextInt();
			commandInfo = new Command(command, randomNum(low, high));
			this.processCommands.add(commandInfo);

		}
		file.close();

	}

	public void createProcesses(String templateName, long numProcessInput) throws FileNotFoundException {
		// String templateLocation = "./Templates/" + templateName;
		Process process;
		int min = 1;
		int max = 10;
		int randomNum = (int) Math.floor(Math.random() * (max - min + 1) + min);
		this.pid = 1;
		for (int i = 0; i < numProcessInput; i++) {
			createCommands(templateName);
			process = new Process("Process" + processCreationCounter, getCommands(), (this.pid + this.pidCounter),
					this.critStart, this.critEnd);
			this.pidCounter++;
			this.processCreationCounter++;
			process.cpu = this.cpu;
			if (randomNum == 1) {
				Process childProcess = fork();
				process.setChildPID(childProcess.getPID());
			}

			this.cpu.addToProcessQueue(process);
			PCB pcb = new PCB(process);
			pcb.setChildPID(process.childPID);
			CPU.updatePCBList(process, pcb);
			memoryCount += process.memory;
			if (memoryCount > TOTAL_MEMORY) {
				Dispatcher.addToReadyQueue(process, pcb);
			}
		}
		
	}

	public void createCompareProcesses() throws FileNotFoundException {
		Process process;
		long pid = 1;
		long pidCounter = 0;
		int processCreationCounter = 1;
		for (int i = 0; i < 1000; i++) {
			createCommands("compare.txt");
			process = new Process("Process" + processCreationCounter, getCommands(), (pid + pidCounter), this.critStart,
					this.critEnd);
			pidCounter++;
			processCreationCounter++;
			this.cpu.addToCompareQueue(process);
			PCB pcb = new PCB(process);
			this.cpu.addToComparePCBList(pcb);
		}
		// System.out.println(this.cpu.getCompareQueue());

	}

//Implements multi level child parent relationship
	public Process fork() throws FileNotFoundException {
		int min = 1;
		int max = 500;
		int randomNum = (int) Math.floor(Math.random() * (max - min + 1) + min);
		createCommands("child.txt");
		Process childProcess = new Process("Process" + processCreationCounter, getCommands(),
				(this.pid + this.pidCounter), this.critStart, this.critEnd);
		childProcess.setParentPID(this.getPID());
		this.pidCounter++;
		this.processCreationCounter++;
		if (randomNum == 1) {
			Process grandChildProcess = fork();
			childProcess.setChildPID(grandChildProcess.getPID());
		}

		this.cpu.addToProcessQueue(childProcess);
		PCB childPCB = new PCB(childProcess);
		childPCB.setParentPID(childProcess.getParentPID());
		CPU.updatePCBList(childProcess, childPCB);
		return childProcess;
	}

	public ArrayList<Command> getCommands() {
		return this.processCommands;
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

	public String getProcessName() {
		return this.processName;
	}

	public long getChildPID() {
		return this.childPID;
	}

	public void setChildPID(long childPID) {
		this.childPID = childPID;
	}

	public long getParentPID() {
		return this.parentPID;
	}

	public void setParentPID(long parentPID) {
		this.parentPID = parentPID;
	}

	@Override
	public String toString() {
		return ("\nProcess Name: " + this.processName + "\nProcess PID: " + this.pid + "\nPriority: " + this.priority
				+ "\n     Child PID: " + this.childPID + "\n     Parent PID: " + this.parentPID);
	}

	@Override
	public void run() {

		try {
			this.cpu.runProcesses(this);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setCpu(CPU cpu) {
		this.cpu = cpu;
		
	}

}

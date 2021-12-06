import java.io.File;


enum STATE {
		//The program or process is being created or loaded (but not yet in memory).
		  NEW,
		//The program is loaded into memory and is waiting to run on the CPU.
		  READY,
		//Instructions are being executed (or simulated).
		  RUN,
		//The program is waiting for some event to occur (such as an I/O completion).
		  WAIT,
		//The program has finished execution on the CPU (all instructions and I/O complete), releases resources and leaves memory  
		  EXIT;
		}
//Priority is chosen at random. multiple processes can have the same priority
class CPUSchedulingInfo{
	private int priority;
	//only for ready queue
	private int queuePOS;
	
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public void incrementPriority() {
		this.priority = this.priority + 1;
	}
	public void setPOS(int pos) {
		this.queuePOS = pos;
	}
	public int getPOS() {
		return this.queuePOS;
	}
}
class CPUAccountingInfo{
	//
	private int cpuUsage;
	//time since start
	private int runTime;
	//Predetermined by the algorithm(Roundbin)
	private int timeLimit = 5;
	public int getCpuUsage() {
		return cpuUsage;
	}
	public int getRunTime() {
		return runTime;
	}
	public int getTimeLimit() {
		return timeLimit;
	}
	public void setCpuUsage(int cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
	
}
class ProgramCounter{
	private int commandCounter;
	private int cyclesRan;
	public int getCommandCounter() {
		return commandCounter;
	}
	public int getCyclesRan() {
		return cyclesRan;
	}
	public void setCounter(int counter) {
		this.commandCounter = counter;
	}
	public void setCyclesRan(int cyclesRan) {
		this.cyclesRan = cyclesRan;
	}
	public void incrementProgramCounter() {
		this.commandCounter++;
	}
	public void incrementProgramCycle() {
		this.cyclesRan++;
	}
	
}
public class PCB {
	
	private STATE state;
	public ProgramCounter programCounter = new ProgramCounter();
	//Not needed yet
	private int memory;
	private Process process;
	private long processPID;
	public CPUSchedulingInfo scheduleInfo = new CPUSchedulingInfo();
	public CPUAccountingInfo accountingInfo = new CPUAccountingInfo();
	private long childPID;
	private long parentPID;

	PCB(Process process){
		this.state = STATE.NEW;
		//This should also state how many cycles have been ran. Can be a seperate fields
		this.programCounter.setCounter(1); 
		this.programCounter.setCyclesRan(0);
		this.memory = process.memory;
		this.process = process;
		this.processPID = process.getPID();
		this.scheduleInfo.setPriority(process.priority);
		
		//System.out.println(this.toString());
		
	}
/*	public PCB(Process p) {
		this.state = STATE.READY;
		this.memory = 0;
		this.process = p;
		this.processPID = process.getPID();
		this.cpu.addToProcessQueue(p);
		this.scheduleInfo.setPriority(p.priority);
	}*/

	public int getMemory() {
		return this.memory;
	}
	public Process getProcess() {
		return this.process;
	}
	public long getProcessPID() {
		return this.processPID;
	}
	public STATE getState() {
		return this.state;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}
	public void setProcess(Process process) {
		this.process = process;
	}
	public void setProcessPID(long processPID) {
		this.processPID = processPID;
	}
	public void setState(STATE state) {
		this.state = state;
	}
	public int getPriority() {
		return this.scheduleInfo.getPriority();
	}
	public long getChildPID() {
		return this.childPID;
	}
	public long getParentPID() {
		return this.parentPID;
	}

	public void setChildPID(long childPID) {
		this.childPID = childPID;
	}

	public void setParentPID(long parentPID) {
		this.parentPID = parentPID;
	}

	@Override
	public String toString() {
		return ("Process State: " + this.state+
				"\nProcess Program Counter: " + this.programCounter+
				"\nMemory: " + this.memory+
				  "\n" + this.process + 
				"\nPID: " + this.processPID + 
				"\nCritical start line: " + this.process.getCritStart()+
				"\nCritical end line: " + this.process.getCritEnd()+
				"\nPriority: " + getPriority());
	}
}

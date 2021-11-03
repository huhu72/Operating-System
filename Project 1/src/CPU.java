import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

//move all process created relations to the process class/main class.
//Wait for all processes to be put in the ready queue and then start running them.
//Same logic applies to the dispatcher(Wait for all processes to be created before dispatcher puts them in the ready queue and scheduler organizes them).

public class CPU {

	private Queue<Process> processes = new LinkedList<>();// AKA job queue. where does this go if the input for the
	// scheduler is only the readyQueue
	private Scheduler scheduler;
	// Not needed yet
	// private Queue<Process> deviceQueue = new LinkedList<>();//FIFO. after pcb
	// gets processed, it goes back to the ready queue and the deviceQueue runs the
	// next process
	private HashMap<Long, PCB> pcbList = new HashMap<>();
	Scanner input = new Scanner(System.in);
	private Dispatcher dispatcher;
	public Semaphore s = new Semaphore(1);

//CPu tells dispatcher there are no processes running, from that, the dispatcher should send another profess from the scheduler to the cpu
	// When process is recieved from dispatcher, remove its PCB and itself from the
	// jobQueue and pcblist
	public void runCommand(){
		Process process;
		PCB pcb;
		ArrayList<Command> commands;

		while (!dispatcher.getReadyQueue().isEmpty() || !dispatcher.getWaitingQueue().isEmpty()) {

			// Only works for running processes one at a time. Logic Process 1 gets to the
			// I/O command
			// Process 1 -> waiting queue. The waiting queue is no longer empty so it will
			// pull the process from there
			if (dispatcher.getWaitingQueue().isEmpty())
				process = dispatcher.getProcess();
			else
				process = dispatcher.getProcessFromWaitingQueue();
			// The PCB should be updated from the dispatcher class once its sent over there
			// This is so that the current pcb has information on the counters from when it
			// was sent to the dispatcher
			pcb = pcbList.get(process.getPID());
			pcb.setState(STATE.RUN);
			commands = process.getCommands();
			if (commands.get(0).command.equals("I/O") && pcb.programCounter.getCounter() == 1) {
				// System.out.println(process.getProcessName() + " has been sent to the waiting
				// queue");
				dispatcher.addToWaitingQueue(process, pcb);
				break;
			}
			Timer t = new Timer();
			TimerTask tt = new TimerTask() {

				@Override
				public void run() {
					Iterator<PCB> it = pcbList.values().iterator();

					while (it.hasNext()) {
					    it.next().scheduleInfo.incrementPriority();
					}
				}
				
			};
		//System.out.println("Running " + process.getProcessName());
			while (pcb.programCounter.getCounter() <= process.getCommands().size()) {
				// for (int i = pcb.programCounter.getCounter(); i <= commands.size(); i++) {

				//System.out.println("Running " + commands.get(pcb.programCounter.getCounter() - 1).command);
				//System.out.println("On Command: " + pcb.programCounter.getCounter() + "/" + commands.size());
				
				scheduler.startQuantumClock();
				while (scheduler.getQuantumStatus()) {

					// If the process runs all of the cyles in the command, it will increament the
					// program counter
					// and exit out of the loop
					if (pcb.programCounter.getCyclesRan() == commands.get(pcb.programCounter.getCounter() - 1).cycle) {
						pcb.programCounter.incrementProgramCounter();
						pcb.programCounter.setCyclesRan(0);
						
						break;
					} else {

						pcb.programCounter.incrementProgramCycle();
					//	System.out.println("On " + pcb.programCounter.getCyclesRan() + "/"
					//			+ commands.get(pcb.programCounter.getCounter() - 1).cycle + " cycle");
					}
				}
				
				// If the process has ran all the commands, add it to the terminated list
				if (pcb.programCounter.getCounter() > commands.size()) {
					pcb.setState(STATE.EXIT);
					pcbList.put(pcb.getProcessPID(), pcb);
					//System.out.println(process.getProcessName() + " has been terminated");
					break;
					// if it ran all its cycles but not all of the commands add it to the respected
					// queue based on the next command
				} else if (pcb.programCounter.getCyclesRan() < commands.get(pcb.programCounter.getCounter() - 1).cycle
						&& commands.get(pcb.programCounter.getCounter() - 1).command.equals("I/O")) {
					//System.out.println(process.getProcessName() + " has been sent to the waiting queue");
					dispatcher.addToWaitingQueue(process, pcb);
					break;

					// if there are still cycles to be ran in the current command and the command is
					// Calculate
					// Dont need to increment the program counter if the process still have cycles
					// to run but the pcb needs to be updated
					// in the dispatcher class
				} else if (pcb.programCounter.getCyclesRan() < commands.get(pcb.programCounter.getCounter() - 1).cycle
						&& commands.get(pcb.programCounter.getCounter() - 1).command.equals("CALCULATE")) {
				//	System.out.println(process.getProcessName() + " has been sent to the ready queue");
					dispatcher.addToReadyQueue(process, pcb);
					break;
				} else {
					if (commands.get(pcb.programCounter.getCounter()) != null) {
						
						Command nextCommand = commands.get(pcb.programCounter.getCounter() - 1);
						pcb.programCounter.setCyclesRan(0);
						if (nextCommand.command.equals("CALCULATE")) {
							//System.out.println(process.getProcessName()
							//		+ " has been sent to the ready queue based on the next command");
							dispatcher.addToReadyQueue(process, pcb);
							// break;
						} else {
						//	System.out.println(process.getProcessName() + " has been sent to the waiting  queue");
							dispatcher.addToWaitingQueue(process, pcb);
							// break;
						}
					}
					break;
				}

			}

			// }
			// in a couple of cycles, increment the priorities of all processes
			// break;
		}
		// print();
		
	}

	public void print() {
		System.out.println("All processes:");
		while (!processes.isEmpty()) {
			Process p = processes.poll();
			double percentage = (((double) pcbList.get(p.getPID()).programCounter.getCounter() - 1)
					/ p.getCommands().size()) * 100;
			System.out.println(p.getProcessName() + " " + percentage + "% completed");
			System.out.println("     State: " + pcbList.get(p.getPID()).getState());
		}
	}

	public PCB getPCB(Long pid) {
		return this.pcbList.get(pid);
	}

	public void addToProcessQueue(Process p) {
		this.processes.add(p);

		// System.out.println(scheduler.getProcess());

		/*
		 * for(Process process : processes) {
		 * System.out.println(getPCB(process.getPID()));
		 * //System.out.println("PID under CPU:" + process.getPID()); }
		 */

	}

	public void addPCB(PCB pcb) {
		this.pcbList.put(pcb.getProcessPID(), pcb);

	}

	public HashMap<Long, PCB> getPCBList() {
		return this.pcbList;
	}

	public void updatePCBList(Process p, PCB pcb) {
		this.pcbList.put(p.getPID(), pcb);
	}

	public Queue<Process> getJobQueue() {
		return this.processes;
	}

	public void setDispatcher(Dispatcher d) {
		this.dispatcher = d;
	}

	public void setScheduler(Scheduler s) {
		this.scheduler = s;
	}

}

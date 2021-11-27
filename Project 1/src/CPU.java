import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

//move all process created relations to the process class/main class.
//Wait for all processes to be put in the ready queue and then start running them.
//Same logic applies to the dispatcher(Wait for all processes to be created before dispatcher puts them in the ready queue and scheduler organizes them).

public class CPU extends Thread {
	Semaphore S;
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
	Timer t = new Timer();

//CPu tells dispatcher there are no processes running, from that, the dispatcher should send another profess from the scheduler to the cpu
	// When process is recieved from dispatcher, remove its PCB and itself from the
	// jobQueue and pcblist
	public void run() {
		Process process;
		PCB pcb;
		ArrayList<Command> commands;
		int CS;
		int CE;

		while (!dispatcher.getReadyQueue().isEmpty() || !dispatcher.getWaitingQueue().isEmpty()) {

			if (dispatcher.getWaitingQueue().isEmpty()) {
				process = dispatcher.getProcess();
				System.out.println("Grabbing " + process.getProcessName() + " from the ready queue");
			} else {
				process = dispatcher.getProcessFromWaitingQueue();
				System.out.println("Grabbing " + process.getProcessName() + " from the wating queue");
			}
			// The PCB should be updated from the dispatcher class once its sent over there
			// This is so that the current pcb has information on the counters from when it
			// was sent to the dispatcher
			pcb = pcbList.get(process.getPID());
			pcb.setState(STATE.RUN);
			commands = process.getCommands();
			CS = process.getCritStart();
			CE = process.getCritEnd();
			if (CS == pcb.programCounter.getCounter()) {
				System.out.println("Reached the Critical section" + "CS: " + CS + "Program Counter: "
						+ pcb.programCounter.getCounter());
				System.out.println("S value before wait is called " + S.value);
				this.wait(S, process);
				System.out.println("               S value is now " + S.value );
				if(S.value < 0) {
					continue;
				}
			}
			if (commands.get(0).command.equals("I/O") && pcb.programCounter.getCounter() == 1) {
				// System.out.println(process.getProcessName() + " has been sent to the waiting
				// queue");
				dispatcher.addToWaitingQueue(process, pcb);
				System.out.println("Adding process to the waiting queue since the first command is an I/O");
				break;
			}
			TimerTask tt = new TimerTask() {

				@Override
				public void run() {
					Iterator<PCB> it = pcbList.values().iterator();

					while (it.hasNext()) {
						it.next().scheduleInfo.incrementPriority();
					}
					System.out.println("Priority has been incremented");
				}

			};
			t.scheduleAtFixedRate(tt, 10000, 10000);
			 System.out.println("Running " + process.getProcessName());
			while (pcb.programCounter.getCounter() <= process.getCommands().size()) {
				// for (int i = pcb.programCounter.getCounter(); i <= commands.size(); i++) {

				System.out.println("Running " + commands.get(pcb.programCounter.getCounter() - 1).command);
				System.out.println("On Command: " + pcb.programCounter.getCounter() + "/" + commands.size());

				scheduler.startQuantumClock();
				while (scheduler.getQuantumStatus()) {

					// If the process runs all of the cyles in the command, it will increament the
					// program counter
					// and exit out of the loop
					if (pcb.programCounter.getCyclesRan() == commands.get(pcb.programCounter.getCounter() - 1).cycle) {
						pcb.programCounter.incrementProgramCounter();
						pcb.programCounter.setCyclesRan(0);
						scheduler.killQuantumTimer();
						System.out.println("Process has ran all cycles before the quantum time ran out");
						break;
					} else {

						pcb.programCounter.incrementProgramCycle();
						System.out.println("On " + pcb.programCounter.getCyclesRan() + "/"
								+ commands.get(pcb.programCounter.getCounter() - 1).cycle + " cycle");
					}
				}
				if (pcb.programCounter.getCounter() == CE) {
					System.out.println("S value before signal is called " + S.value);
					signal(S);
					System.out.println("               S value is now " + S.value );
					System.out.println(process.getProcessName() + " has reached the end of the critical section" + "CE: " + CE
							+ "Program Counter: " + pcb.programCounter.getCounter());
				}
				if (pcb.programCounter.getCounter() > commands.size()) {
					pcb.setState(STATE.EXIT);
					pcbList.put(pcb.getProcessPID(), pcb);
					System.out.println(process.getProcessName() + " has been terminated");
					break;
					// if it ran all its cycles but not all of the commands add it to the respected
					// queue based on the next command
				} else if (pcb.programCounter.getCyclesRan() < commands.get(pcb.programCounter.getCounter() - 1).cycle
						&& commands.get(pcb.programCounter.getCounter() - 1).command.equals("I/O")) {
					System.out.println(process.getProcessName() + " has been sent to the waiting queue");
					dispatcher.addToWaitingQueue(process, pcb);
					break;

					// if there are still cycles to be ran in the current command and the command is
					// Calculate
					// Dont need to increment the program counter if the process still have cycles
					// to run but the pcb needs to be updated
					// in the dispatcher class
				} else if (pcb.programCounter.getCyclesRan() < commands.get(pcb.programCounter.getCounter() - 1).cycle
						&& commands.get(pcb.programCounter.getCounter() - 1).command.equals("CALCULATE")) {
					System.out.println(process.getProcessName() + " has been sent to the ready queue");
					dispatcher.addToReadyQueue(process, pcb);
					break;
				} else {
					if (commands.get(pcb.programCounter.getCounter()) != null) {

						Command nextCommand = commands.get(pcb.programCounter.getCounter() - 1);
						pcb.programCounter.setCyclesRan(0);
						if (nextCommand.command.equals("CALCULATE")) {
							System.out.println(process.getProcessName()
									+ " has been sent to the ready queue based on the next command");
							dispatcher.addToReadyQueue(process, pcb);
							// break;
						} else {
							System.out.println(process.getProcessName() + " has been sent to the waiting queue");
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
		t.cancel();
		// print();

	}

	public void print() {
		System.out.println("All processes:");
		for (Process p : processes) {
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

	// Semaphore methods
	// S is the semaphore the process has, P is the process thats calling this
	// method
	private synchronized void wait(Semaphore S, Process P) {
		
		if (S.value == 0) {
			S.list.add(P);
			block(P);
			System.out.println(P.getProcessName() + "has been sent to the semaphore waiting queue since S < 0");
		}
		S.value = 0;
	}

	private synchronized void signal(Semaphore S) {
		
		if (S.value == 0) {
			S.value = 1;
			Process P = S.list.get(0);
			S.list.remove(0);
			wakeUp(P);
			System.out.println(P.getProcessName() + "has been put back into the ready queue");
		}
	}

	private void wakeUp(Process P) {
		PCB pcb = pcbList.get(P.getPID());
		pcb.setState(STATE.READY);
		pcbList.put(P.getPID(), pcb);
		scheduler.addToPQ(scheduler.semaphoreWaitingQueue.remove());
	}

	private void block(Process p) {
		scheduler.addToSemaphoreQueue(p);
		PCB pcb = pcbList.get(p.getPID());
		pcb.setState(STATE.WAIT);
		pcbList.put(p.getPID(), pcb);
	}

}

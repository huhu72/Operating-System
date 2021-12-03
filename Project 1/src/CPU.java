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
//Same logic applies to the Dispatcher(Wait for all processes to be created before Dispatcher puts them in the ready queue and Scheduler organizes them).

public class CPU extends Thread {
	static Boolean inCS = false;
	private static Queue<Process> processes = new LinkedList<>();// AKA job queue. where does this go if the input for
																	// the
	// Scheduler is only the readyQueue

	// private Queue<Process> deviceQueue = new LinkedList<>();//FIFO. after pcb
	// gets processed, it goes back to the ready queue and the deviceQueue runs the
	// next process
	private static HashMap<Long, PCB> pcbList = new HashMap<>();
	Scanner input = new Scanner(System.in);
	static Timer t = new Timer();

//CPu tells Dispatcher there are no processes running, from that, the Dispatcher should send another profess from the Scheduler to the cpu
	// When process is recieved from Dispatcher, remove its PCB and itself from the
	// jobQueue and pcblist
	@Override
	public void run() {
		ArrayList<Runnable> runnableProcesses = new ArrayList<>();
		System.out.println("running");
		while (!Dispatcher.getReadyQueue().isEmpty() || !Dispatcher.getWaitingQueue().isEmpty()
				|| !Semaphore.list.isEmpty()) {
			/*
			 * If theres a process in its critical section, it needs to add it back into a
			 * thread so that it can continue running its critical section
			 */

			if (CPU.inCS) {
				runnableProcesses.add(dispatchProcess());
				System.out.println(runnableProcesses.get(0));
				for (int i = 0; i < 3; i++) {
					runnableProcesses.add(dispatchProcess());
					// System.out.println(runnableProcesses.get(i+1));
				}
			} else {
				for (int i = 0; i < 4; i++) {
					runnableProcesses.add(dispatchProcess());
					// System.out.println(runnableProcesses.get(i));
				}
			}
			for (int i = 0; i < runnableProcesses.size(); i++) {
				Thread processThread = new Thread(runnableProcesses.get(i));
				processThread.start();
			}

		}
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				Iterator<PCB> it = pcbList.values().iterator();

				while (it.hasNext()) {
					it.next().scheduleInfo.incrementPriority();
				}
				// System.out.println("Priority has been incremented");
			}

		};
		CPU.t.scheduleAtFixedRate(tt, 10000, 10000);
	}

	public Runnable dispatchProcess() {
		Runnable process;
		// Grabs from the ready queue if the waiting queue is empty(Usually when the cpu
		// is first initiated)
		if (Dispatcher.getWaitingQueue().isEmpty()) {
			process = Dispatcher.getProcess();
			// System.out.println("Grabbing " + process.getProcessName() + " from the ready
			// queue");
			/*
			 * If both the ready and waiting queue is empty, then signal the semahpore so
			 * that it can remove it from its own waiting queue and populate it into the
			 * ready queue. This is needed just in-case there aren't enough signal calls to
			 * pull all the Processes out of its own waiting queue
			 */
		} else if (Dispatcher.getWaitingQueue().isEmpty() && Dispatcher.getReadyQueue().isEmpty()) {
			Semaphore.signal();
			process = Dispatcher.getProcess();
			/*
			 * Grabs the process in the waiting queue since the queue isnt empty
			 */
		} else {
			process = Dispatcher.getProcessFromWaitingQueue();
			// System.out.println("Grabbing " + process.getProcessName() + " from the wating
			// queue");
		}
		return process;

	}

	public static void runProcesses(Process process) throws InterruptedException {
		PCB pcb;
		ArrayList<Command> commands;
		int CS;
		int CE;

		// The PCB should be updated from the Dispatcher class once its sent over there
		// This is so that the current pcb has information on the counters from when it
		// was sent to the Dispatcher
		pcb = CPU.pcbList.get(process.getPID());
		pcb.setState(STATE.RUN);
		commands = process.getCommands();
		CS = process.getCritStart();
		CE = process.getCritEnd();
		System.out.println(pcb.programCounter.getCommandCounter());

		if (commands.get(0).command.equals("I/O") && pcb.programCounter.getCommandCounter() == 1) {
			// System.out.println(process.getProcessName() + " has been sent to the waiting
			// queue");
			Dispatcher.addToWaitingQueue(process, pcb);
			// System.out.println("Adding process to the waiting queue since the first
			// command is an I/O");

		}

		// System.out.println("Running " + process.getProcessName());
		while (pcb.programCounter.getCommandCounter() <= process.getCommands().size()) {
			Scheduler s = new Scheduler();
			s.run();

			while (s.getQuantumStatus()) {
				if (pcb.programCounter.getCommandCounter() == CS) {
					Semaphore.wait(process);
					CPU.inCS = true;
					s.killQuantumTimer();
					System.out.println(process.getProcessName() + " is now in CS and timer should be killed");
					
				}
				// If the process runs all of the cyles in the command, it will increament the
				// program counter
				// and exit out of the loop
				if (pcb.programCounter
						.getCyclesRan() == commands.get(pcb.programCounter.getCommandCounter() - 1).cycle) {
					
					System.out.println("Timer should be killed");
					s.killQuantumTimer();
					pcb.programCounter.incrementProgramCounter();
					pcb.programCounter.setCyclesRan(0);
					

					// System.out.println("Process has ran all cycles before the quantum time ran
					// out");
					break;
				} else {
					System.out.println("Command: " + pcb.programCounter.getCommandCounter());
					pcb.programCounter.incrementProgramCycle();
					System.out.println("On " + pcb.programCounter.getCyclesRan() + "/"
							+ commands.get(pcb.programCounter.getCommandCounter() - 1).cycle + " cycle");
				}

			}
			if (pcb.programCounter.getCommandCounter() == CE) {
				Semaphore.signal();
				CPU.inCS = false;
				System.out.println(process.getProcessName() + " is out of CS");
			}

			// If the process is out of commands to run
			if (pcb.programCounter.getCommandCounter() > commands.size()) {
				pcb.setState(STATE.EXIT);
				CPU.pcbList.put(pcb.getProcessPID(), pcb);
				// System.out.println(pcb.getChildPID());
				if (pcb.getChildPID() != -1) {
					PCB childPCB = pcbList.get(pcb.getChildPID());
					childPCB.programCounter.setCounter(3);
					childPCB.setState(STATE.EXIT);
					CPU.pcbList.put(childPCB.getProcessPID(), childPCB);
				}
				// System.out.println(process.getProcessName() + " has been terminated");
				break;

				// if it ran all its cycles but not all of the commands, add it to the respected
				// queue based on the next command
			}
			if (CPU.pcbList.get(process.getPID()).getState() != STATE.EXIT) {
				Command nextCommand = commands.get(pcb.programCounter.getCommandCounter() - 1);

				if (pcb.programCounter.getCyclesRan() < nextCommand.cycle && nextCommand.command.equals("I/O")) {
					// System.out.println(process.getProcessName() + " has been sent to the waiting
					// queue");
					Dispatcher.addToWaitingQueue(process, pcb);

					// if there are still cycles to be ran in the current command and the command is
					// Calculate
					// Dont need to increment the program counter if the process still have cycles
					// to run but the pcb needs to be updated
					// in the Dispatcher class
				} else if (pcb.programCounter.getCyclesRan() < nextCommand.cycle
						&& nextCommand.command.equals("CALCULATE")) {
					// System.out.println(process.getProcessName() + " has been sent to the ready
					// queue");
					Dispatcher.addToReadyQueue(process, pcb);
					// Process is on its last command
				} else {
					if (commands.get(pcb.programCounter.getCommandCounter()) != null) {

						pcb.programCounter.setCyclesRan(0);
						if (nextCommand.command.equals("CALCULATE")) {
							// System.out.println(process.getProcessName()
							// + " has been sent to the ready queue based on the next command");
							Dispatcher.addToReadyQueue(process, pcb);
							// break;
						} else {
							// System.out.println(process.getProcessName() + " has been sent to the waiting
							// queue");
							Dispatcher.addToWaitingQueue(process, pcb);
							// break;
						}
					}

				}
			}

		}

		// }
		// in a couple of cycles, increment the priorities of all processes
		// break;
		// System.out.println("all processes have been terminated");
		t.cancel();
		// print();

	}

	public void print() {
		System.out.println("All processes:");
		for (Process p : processes) {
			double percentage = (((double) CPU.pcbList.get(p.getPID()).programCounter.getCommandCounter() - 1)
					/ p.getCommands().size()) * 100;
			System.out.println(p.getProcessName() + " " + percentage + "% completed");
			System.out.println("     State: " + CPU.pcbList.get(p.getPID()).getState());
		}
	}

	public static PCB getPCB(Long pid) {
		return CPU.pcbList.get(pid);
	}

	public void addToProcessQueue(Process p) {
		this.processes.add(p);

		// System.out.println(Scheduler.getProcess());

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

	public static void updatePCBList(Process p, PCB pcb) {
		CPU.pcbList.put(p.getPID(), pcb);
	}

	public Queue<Process> getJobQueue() {
		return this.processes;
	}

	public void setPCBList(HashMap<Long, PCB> pcbList) {
		CPU.pcbList = pcbList;
	}

	public static void updatePCBList(PCB pcb) {
		CPU.pcbList.put(pcb.getProcessPID(), pcb);
	}

}

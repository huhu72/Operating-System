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
	static Boolean waitingForCS = false;
	private static Queue<Process> processes = new LinkedList<>();// AKA job queue. where does this go if the input for
																	// the
	Queue<Process> compareQueue = new LinkedList<>();
	Boolean compare = true;
	// private Queue<Process> deviceQueue = new LinkedList<>();//FIFO. after pcb
	// gets processed, it goes back to the ready queue and the deviceQueue runs the
	// next process
	private static HashMap<Long, PCB> pcbList = new HashMap<>();
	static HashMap<Long, PCB> comparePCBList = new HashMap<>();
	Scanner input = new Scanner(System.in);
	static Timer t = new Timer();
	static Process processInCS;
	static int counter = 0;
	Thread processThreadArray[] = new Thread[4];
	public static String scheduler;
	static Boolean status = false;
	Semaphore semaphore = new Semaphore();

//CPu tells Dispatcher there are no processes running, from that, the Dispatcher should send another profess from the Scheduler to the cpu
	// When process is recieved from Dispatcher, remove its PCB and itself from the
	// jobQueue and pcblist
	@Override
	public void run() {

		// System.out.println("running");
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				Iterator<PCB> it = pcbList.values().iterator();

				while (it.hasNext()) {
					it.next().scheduleInfo.incrementPriority();
				}
				if (status)
					System.out.println("Priority has been incremented");
			}

		};
		CPU.t.scheduleAtFixedRate(tt, 10000, 10000);
		if (CPU.inCS) {
			processThreadArray[0] = new Thread(dispatchProcess());
			for (int i = 1; i < 4; i++) {
				processThreadArray[i] = new Thread(dispatchProcess());
			}
			for (Thread t : processThreadArray) {
				t.start();
			}
		} else {
			for (int i = 0; i < 4; i++) {
				processThreadArray[i] = new Thread(dispatchProcess());
			}
			for (Thread t : processThreadArray) {
				t.start();
			}
		}
		int randomNum = (int) Math.floor(Math.random() * (3 - 0 + 1) + 0);
		Timer interruptTimer = new Timer();
		TimerTask interruptTimerTask = new TimerTask() {

			@Override
			public void run() {
				if (processThreadArray[randomNum].getState() == Thread.State.TIMED_WAITING) {
					processThreadArray[randomNum].interrupt();
					if (status) {
						System.out.println("Interrupt thrown");
					}
				}

			}

		};

		interruptTimer.scheduleAtFixedRate(interruptTimerTask, 10, 10);

	}

	public Runnable dispatchProcess() {
		Runnable runnableProcess;
		Process process;
		// Grabs from the ready queue if the waiting queue is empty(Usually when the cpu
		// is first initiated)
		if (Dispatcher.getWaitingQueue().isEmpty() && !Dispatcher.getReadyQueue().isEmpty()) {
			process = Dispatcher.getProcess();
			runnableProcess = process;
			if (process != null) {
				if (CPU.status) {
					System.out.println("						The waiting queue is empty, grabbing "
							+ process.getProcessName() + " from the ready queue");
				}
				return runnableProcess;
			} else {
				return null;
			}

			/*
			 * If both the ready and waiting queue is empty, then signal the semahpore so
			 * that it can remove it from its own waiting queue and populate it into the
			 * ready queue. This is needed just in-case there aren't enough signal calls to
			 * pull all the Processes out of its own waiting queue
			 */
		} else if (Dispatcher.getWaitingQueue().isEmpty() && Dispatcher.getReadyQueue().isEmpty()) {
			semaphore.signal();
			process = Dispatcher.getProcess();
			runnableProcess = process;
			if (process != null) {
				if (status) {
					System.out
							.println("						The waiting and ready queue is empty is empty, grabbing "
									+ process.getProcessName() + " from the ready queue after calling signal");
				}
				return runnableProcess;
			} else {
				return null;
			}
			/*
			 * Grabs the process in the waiting queue since the queue isnt empty
			 */
		} else {
			process = Dispatcher.getProcessFromWaitingQueue();
			runnableProcess = process;
			if (process != null) {
				if (status) {
					System.out.println(
							"						Grabbing " + process.getProcessName() + " from the wating queue");
				}
				return runnableProcess;
			} else {
				return null;
			}

		}

	}

	public void runProcesses(Process process) throws InterruptedException {
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
		Thread currentThread = Thread.currentThread();
		Runnable runnableProcess;
		int randomNum = (int) Math.floor(Math.random() * (100 - 0 + 1) + 0);
		if (randomNum == 1) {
			if (CPU.status) {
				System.out.println("Process is put to sleep");
			}
			Thread.sleep(10);
		}
		if (commands.get(0).command.equals("I/O") && pcb.programCounter.getCommandCounter() == 1) {
			if (status) {
				System.out.println(process.getProcessName()
						+ " has been sent to the waiting queue because the first command is a I/O instruction");
			}
			Dispatcher.addToWaitingQueue(process, pcb);
			runnableProcess = dispatchProcess();
			currentThread = new Thread(runnableProcess);
			if (runnableProcess != null)
				currentThread.start();

		}

		Scheduler s = new Scheduler();
		if (scheduler.equals("RR"))
			s.run(process);

		while (s.getQuantumStatus()) {

			if (inCS && semaphore.list.contains(process)) {
				if (status) {
					System.out.println("						There is a process in cs");
				}
				break;
			}
			if (pcb.programCounter.getCommandCounter() == CS && pcb.programCounter.getCyclesRan() == 0) {
				semaphore.wait(process);
				if (scheduler.equals("RR"))
					s.killQuantumTimer(process);
				if (semaphore.list.contains(process)) {
					break;
				} else {
					CPU.inCS = true;
				}

			}
			// If the process runs all of the cyles in the command, it will increament the
			// program counter
			// and exit out of the loop
			if (pcb.programCounter.getCommandCounter() != commands.size()) {
				if (pcb.programCounter
						.getCyclesRan() == commands.get(pcb.programCounter.getCommandCounter() - 1).cycle) {

					if (scheduler.equals("RR"))
						s.killQuantumTimer(process);
					pcb.programCounter.incrementProgramCounter();
					pcb.programCounter.setCyclesRan(0);
					pcbList.put(pcb.getProcessPID(), pcb);
					if (status) {
						System.out.println(
								process.getProcessName() + " has ran all cycles before the quantum time ran out and "
										+ process.getProcessName() + " timer has been killed");
					}
					if (pcb.programCounter.getCommandCounter() == CE) {
						if (status) {
							System.out.println("												"
									+ pcb.getProcess().getProcessName() + " has called signal()");
						}
						semaphore.signal();
						CPU.inCS = false;
						CPU.pcbList.put(pcb.getProcessPID(), pcb);
					} else {
						break;
					}
				} else {
					if (status) {
						System.out.println("Command for " + process.getProcessName() + ": "
								+ pcb.programCounter.getCommandCounter());
					}
					pcb.programCounter.incrementProgramCycle();
					CPU.pcbList.put(pcb.getProcessPID(), pcb);
					if (status) {
						System.out.println("On " + pcb.programCounter.getCyclesRan() + "/"
								+ commands.get(pcb.programCounter.getCommandCounter() - 1).cycle + " cycle");
					}
				}
			}

		}

		if (!semaphore.list.contains(pcb.getProcess())) {
			//Cascading termination for multilevel child parent relationship
			// If the process is out of commands to run
			if (pcb.programCounter.getCommandCounter() > commands.size()) {
				pcb.setState(STATE.EXIT);
				CPU.pcbList.put(pcb.getProcessPID(), pcb);
				// System.out.println(pcb.getChildPID());
				if (pcbList.get(pcb.getChildPID()) != null) {
					PCB childPCB = pcbList.get(pcb.getChildPID());
					childPCB.programCounter.setCounter(10000);
					childPCB.setState(STATE.EXIT);
					CPU.pcbList.put(childPCB.getProcessPID(), childPCB);
					if (pcbList.get(pcbList.get(pcb.getChildPID()).getChildPID()) != null) {
						PCB grandChildPCB = pcbList.get(childPCB.getChildPID());
						grandChildPCB.programCounter.setCounter(10000);
						grandChildPCB.setState(STATE.EXIT);
						CPU.pcbList.put(grandChildPCB.getProcessPID(), grandChildPCB);
						if (status) {
							System.out.println(process.getProcessName() + ", its child "
									+ childPCB.getProcess().getProcessName() + ", and its grandchildren "
									+ grandChildPCB.getProcess().getProcessName() + " has been terminated");
							Process.memoryCount -= process.memory - childPCB.getProcess().memory;
						}
					} else {
						if (status) {
							System.out.println(process.getProcessName() + " and its child "
									+ childPCB.getProcess().getProcessName() + "has been terminated");
							Process.memoryCount -= process.memory - childPCB.getProcess().memory;
						}
					}
				} else {
					if (status) {
						Process.memoryCount -= process.memory;
						System.out.println(process.getProcessName() + " been terminated");
					}
				}
				if (!Dispatcher.getReadyQueue().isEmpty() || !Dispatcher.getWaitingQueue().isEmpty()
						|| !semaphore.list.isEmpty()) {
					// System.out.println(" Thread is being re assigned from the terminated stage");
					runnableProcess = dispatchProcess();
					currentThread = new Thread(runnableProcess);
					if (runnableProcess != null)
						currentThread.start();
				}
				// if it ran all its cycles but not all of the commands, add it to the respected
				// queue based on the next command
			}
			if (CPU.pcbList.get(process.getPID()).getState() != STATE.EXIT) {
				Command nextCommand = commands.get(pcb.programCounter.getCommandCounter() - 1);

				if (pcb.programCounter.getCyclesRan() < nextCommand.cycle) {

					if (nextCommand.command.equals("I/O")) {
						if (status) {
							System.out.println(process.getProcessName()
									+ " has been sent to the waiting queue because the next command is an I/O command");
						}
						Dispatcher.addToWaitingQueue(process, pcb);
					} else {
						// if there are still cycles to be ran in the current command and the command is
						// Calculate
						// Dont need to increment the program counter if the process still have cycles
						// to run but the pcb needs to be updated
						// in the Dispatcher class
						if (status) {
							System.out.println(process.getProcessName()
									+ " has been sent to the ready queue because the next command is an calculate command");
						}
						Dispatcher.addToReadyQueue(process, pcb);
					}
				}
				// Process is on its last command
				else {
					if (commands.get(pcb.programCounter.getCommandCounter()) != null) {

						pcb.programCounter.setCyclesRan(0);
						if (nextCommand.command.equals("CALCULATE")) {
							if (status) {
								System.out.println(process.getProcessName()
										+ " is on its last command and has been sent to the ready queue based on the next command");
							}
							Dispatcher.addToReadyQueue(process, pcb);

							// break;
						} else {
							if (status) {
								System.out.println(process.getProcessName()
										+ " is on its last command has been sent to the waiting queue");
							}
							Dispatcher.addToWaitingQueue(process, pcb);
							// break;
						}

					}

				}
				runnableProcess = dispatchProcess();
				currentThread = new Thread(runnableProcess);
				if (runnableProcess != null) {
					currentThread.start();
				}

			}
			/*
			 * System.out.println("finished"); System.out.println("The ready queue: " +
			 * Dispatcher.getReadyQueue()); System.out.println("The waiting queue: " +
			 * Dispatcher.getWaitingQueue());
			 */
			// }
			// in a couple of cycles, increment the priorities of all processes
			// break;
			// System.out.println("all processes have been terminated");
			t.cancel();

		}

	}

	public static void print() {

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
		CPU.processes.add(p);

		// System.out.println(Scheduler.getProcess());

		/*
		 * for(Process process : processes) {
		 * System.out.println(getPCB(process.getPID()));
		 * //System.out.println("PID under CPU:" + process.getPID()); }
		 */

	}

	public void addPCB(PCB pcb) {
		CPU.pcbList.put(pcb.getProcessPID(), pcb);

	}

	public HashMap<Long, PCB> getPCBList() {
		return CPU.pcbList;
	}

	public static void updatePCBList(Process p, PCB pcb) {
		CPU.pcbList.put(p.getPID(), pcb);
	}

	public Queue<Process> getJobQueue() {
		return CPU.processes;
	}

	public void setPCBList(HashMap<Long, PCB> pcbList) {
		CPU.pcbList = pcbList;
	}

	public static void updatePCBList(PCB pcb) {
		CPU.pcbList.put(pcb.getProcessPID(), pcb);
	}

	public void addToCompareQueue(Process p) {
		this.compareQueue.add(p);
	}

	public Queue<Process> getCompareQueue() {
		return this.compareQueue;
	}

	public void addToComparePCBList(PCB pcb) {
		this.comparePCBList.put(pcb.getProcessPID(), pcb);
	}

	public HashMap<Long, PCB> getComparePCBList() {
		return this.comparePCBList;
	}

	public int compareRR() {
		int totalCyclesRanForRR = 0;
		Timer compareTimer = new Timer();
		TimerTask compareTimerTask = new TimerTask() {

			@Override
			public void run() {
				compare = false;
			}

		};
		compareTimer.schedule(compareTimerTask, 30);
		// Using round robin first
		while (compare && !Dispatcher.getReadyQueue().isEmpty()) {

			Process p = Dispatcher.getProcess();
			if (p != null) {
				// System.out.println(p);
				PCB pcb = comparePCBList.get(p.getPID());
				ArrayList<Command> commands = p.getCommands();
				Scheduler s = new Scheduler();
				s.run(p);
				while (s.getQuantumStatus() && pcb.programCounter.getCyclesRan() < commands.get(0).cycle) {
					pcb.programCounter.incrementProgramCycle();
					totalCyclesRanForRR++;
					CPU.comparePCBList.put(pcb.getProcessPID(), pcb);

					if (pcb.programCounter.getCyclesRan() < commands.get(0).cycle) {
						Dispatcher.addToReadyQueue(p, pcb);
					}
				}

			}

		}

		return totalCyclesRanForRR;
	}

	public int comparePQ() {
		compare = true;
		int totalCyclesRanForPQ = 0;
		Timer compareTimer = new Timer();
		TimerTask compareTimerTask = new TimerTask() {

			@Override
			public void run() {
				compare = false;
			}

		};
		compareTimer.schedule(compareTimerTask, 30);
		// Using round robin first
		while (compare && !Dispatcher.getReadyQueue().isEmpty()) {
			Process p = Dispatcher.getProcess();
			if (p != null) {
				PCB pcb = comparePCBList.get(p.getPID());
				ArrayList<Command> commands = p.getCommands();

				while (pcb.programCounter.getCyclesRan() < commands.get(0).cycle) {
					pcb.programCounter.incrementProgramCycle();
					totalCyclesRanForPQ++;
					CPU.comparePCBList.put(pcb.getProcessPID(), pcb);
				}
			}
		}

		return totalCyclesRanForPQ;

	}

	public static void updateComparePCBList(Process p, PCB pcb) {
		comparePCBList.put(p.getPID(), pcb);

	}

}

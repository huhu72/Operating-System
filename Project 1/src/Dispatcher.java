import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
//When priority in PCB changes, the priority in process need to change too;

public class Dispatcher {
	// this belongs in the Scheduler. after a process is ran, sent it back to the
	// Scheduler
	/*
	 * class ProcessComparator implements Comparator<Process>{
	 * 
	 * @Override public int compare(Process p1, Process p2) { return
	 * Integer.compare(p1.priority, p2.priority); }
	 * 
	 * 
	 * }
	 */
	// waiting belongs in dispatcher
	private static Queue<Process> waitingQueue = new LinkedList<>();
	// in order to implement aging, the element needs to be removed, increamented,
	// and then added back bc you cant access the element in the pq
	private static Queue<Process> readyQueue;
	private static HashMap<Long, PCB> pcbList;
	private CPU cpu;
	private Scheduler scheduler;

	Dispatcher(CPU cpu) {
		this.cpu = cpu;

	}

	public static void setPCBList(HashMap<Long, PCB> pcbList) {
		Dispatcher.pcbList = pcbList;
	}

	public static void setReadyQueue(Queue<Process> p) {
		readyQueue = new LinkedList<>();
		for (Process processes : p) {
			Dispatcher.pcbList.get(processes.getPID()).setState(STATE.READY);
			Dispatcher.readyQueue.add(processes);
		}
	}

	public void addToCompareQueue(Process p, PCB pcb) {

		this.cpu.updateComparePCBList(p, pcb);
		Dispatcher.readyQueue.add(p);
		sortReadyQueue(Dispatcher.readyQueue);
		Dispatcher.readyQueue = scheduler.getReadyQueue();
	}

	public  void addToReadyQueue(Process process, PCB pcb) {
		Dispatcher.pcbList.put(process.getPID(), pcb);
		this.cpu.updatePCBList(process, pcb);
		Dispatcher.readyQueue.add(process);
		sortReadyQueue(Dispatcher.readyQueue);
		Dispatcher.readyQueue = scheduler.getReadyQueue();
	}

	public Queue<Process> getReadyQueue() {
		return Dispatcher.readyQueue;
	}

	public Process getProcess() {
		return Dispatcher.readyQueue.poll();
		// System.out.println(processReturn.getProcessName() + " is being sent to the
		// cpu to run");
	}

	public void addToWaitingQueue(Process process, PCB pcb) {
		pcbList.put(process.getPID(), pcb);
		this.cpu.updatePCBList(process, pcb);
		Dispatcher.waitingQueue.add(process);
		sortWaitingQueue(Dispatcher.waitingQueue);
		Dispatcher.waitingQueue = scheduler.getWaitingQueue();
	}

	public void sortWaitingQueue(Queue<Process> wq) {
		scheduler.setWaitingQueue(wq, Dispatcher.pcbList);
		Dispatcher.waitingQueue = scheduler.getWaitingQueue();
	}

	public static Queue<Process> getWaitingQueue() {
		return Dispatcher.waitingQueue;
	}

	public static Process getProcessFromWaitingQueue() {
		Process processReturn = Dispatcher.waitingQueue.poll();
		// System.out.println(processReturn.getProcessName() + " is being sent to the
		// cpu to run from the waiting queue");
		return processReturn;
	}

	// Sends the queue into the scheduler and resets the ready
	public void sortReadyQueue(Queue<Process> rq) {
		scheduler.setReadyQueue(rq, Dispatcher.pcbList);
		Dispatcher.readyQueue = scheduler.getReadyQueue();
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;

	}

}

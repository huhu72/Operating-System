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
	private static Queue<Process> readyQueue = new LinkedList<>();
	private static HashMap<Long, PCB> pcbList;

	Dispatcher() {

	}

	public static void setPCBList(HashMap<Long, PCB> pcbList) {
		Dispatcher.pcbList = pcbList;
	}

	public static void setReadyQueue(Queue<Process> p) {
		for (Process processes : p) {
			Dispatcher.pcbList.get(processes.getPID()).setState(STATE.READY);
			Dispatcher.readyQueue.add(processes);
		}
	}

	public static void addToReadyQueue(Process process, PCB pcb) {
		Dispatcher.pcbList.put(process.getPID(), pcb);
		CPU.updatePCBList(process,pcb);
		Dispatcher.readyQueue.add(process);
		sortReadyQueue(Dispatcher.readyQueue);
		Dispatcher.readyQueue = Scheduler.getReadyQueue();
	}

	public static Queue<Process> getReadyQueue() {
		return Dispatcher.readyQueue;
	}

	public static Process getProcess() {
		return Dispatcher.readyQueue.poll();
		//System.out.println(processReturn.getProcessName() + " is being sent to the cpu to run");
	}

	public static void addToWaitingQueue(Process process, PCB pcb) {
		pcbList.put(process.getPID(), pcb);
		CPU.updatePCBList(process,pcb);
		Dispatcher.waitingQueue.add(process);
		sortWaitingQueue(Dispatcher.waitingQueue);
		Dispatcher.waitingQueue = Scheduler.getWaitingQueue();
	}

	public static void sortWaitingQueue(Queue<Process> wq) {
		Scheduler.setWaitingQueue(wq, Dispatcher.pcbList);
		Dispatcher.waitingQueue = Scheduler.getWaitingQueue();
	}

	public static Queue<Process> getWaitingQueue() {
		return Dispatcher.waitingQueue;
	}

	public static Process getProcessFromWaitingQueue() {
		Process processReturn = Dispatcher.waitingQueue.poll();
	//	System.out.println(processReturn.getProcessName() + " is being sent to the cpu to run from the waiting queue");
		return processReturn;
	}


	// Sends the queue into the Scheduler and resets the ready
	public static void sortReadyQueue(Queue<Process> rq) {
		Scheduler.setReadyQueue(rq, Dispatcher.pcbList);
		Dispatcher.readyQueue = Scheduler.getReadyQueue();
	}

}

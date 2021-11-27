import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
//When priority in PCB changes, the priority in process need to change too;

public class Dispatcher {
	// this belongs in the scheduler. after a process is ran, sent it back to the
	// scheduler
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
	private Queue<Process> waitingQueue = new LinkedList<>();
	// in order to implement aging, the element needs to be removed, increamented,
	// and then added back bc you cant access the element in the pq
	private Queue<Process> readyQueue = new LinkedList<>();
	private HashMap<Long, PCB> pcbList;
	private Scheduler scheduler;
	private CPU cpu;

	Dispatcher() {

	}

	public void setPCBList(HashMap<Long, PCB> pcbList) {
		this.pcbList = pcbList;
	}

	public void setReadyQueue(Queue<Process> p) {
		for (Process processes : p) {
			pcbList.get(processes.getPID()).setState(STATE.READY);
			this.readyQueue.add(processes);
		}
	}

	public void addToReadyQueue(Process process, PCB pcb) {
		pcbList.put(process.getPID(), pcb);
		this.cpu.updatePCBList(process,pcb);
		this.readyQueue.add(process);
		sortReadyQueue(this.readyQueue);
		this.readyQueue = scheduler.getReadyQueue();
	}

	public Queue<Process> getReadyQueue() {
		return this.readyQueue;
	}

	public Process getProcess() {
		return this.readyQueue.poll();
		//System.out.println(processReturn.getProcessName() + " is being sent to the cpu to run");
	}

	public void addToWaitingQueue(Process process, PCB pcb) {
		pcbList.put(process.getPID(), pcb);
		this.cpu.updatePCBList(process,pcb);
		this.waitingQueue.add(process);
		sortWaitingQueue(this.waitingQueue);
		this.waitingQueue = scheduler.getWaitingQueue();
	}

	public void sortWaitingQueue(Queue<Process> wq) {
		scheduler.setWaitingQueue(wq, this.pcbList);
		this.waitingQueue = scheduler.getWaitingQueue();
	}

	public Queue<Process> getWaitingQueue() {
		return this.waitingQueue;
	}

	public Process getProcessFromWaitingQueue() {
		Process processReturn = this.waitingQueue.poll();
	//	System.out.println(processReturn.getProcessName() + " is being sent to the cpu to run from the waiting queue");
		return processReturn;
	}

	public Scheduler getScheduler() {
		return this.scheduler;
	}

	public void setScheduler(Scheduler s) {
		this.scheduler = s;
	}

	// Sends the queue into the scheduler and resets the ready
	public void sortReadyQueue(Queue<Process> rq) {
		scheduler.setReadyQueue(rq, this.pcbList);
		this.readyQueue = scheduler.getReadyQueue();
	}

	public void setCPU(CPU cpu) {
		this.cpu = cpu;
	}

}

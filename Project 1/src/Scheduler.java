import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
	private HashMap<Long, PCB> pcbInfo = new HashMap<>();
	// Optimized by the scheduler. Priorities are determined whether if its a sys
	// operation or I/O. This is a subset of the processes queue
	private  Queue<Process> readyQueue = new PriorityQueue<>((p1, p2) -> {
		return pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
	});;
	private  Queue<Process> waitingQueue = new PriorityQueue<>((p1, p2) -> {
		return pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
	});;
	private Boolean quantumStatus = true;
	Timer timer;
	private CPU cpu;

	Scheduler(CPU cpu) {
		this.cpu = cpu;
	}

	public Scheduler() {
		
	}

	public void addToPQ(Process p) {
		this.readyQueue.add(p);
	}

	public void addPCBInfo(PCB pcb) {
		this.pcbInfo.put(pcb.getProcessPID(), pcb);
	}

	public Process getProcess() {
		return this.readyQueue.poll();
	}

	public void setReadyQueue(Queue<Process> rq, HashMap<Long, PCB> pcbList) {
		this.readyQueue = rq;
		this.pcbInfo = pcbList;
	}

	public Queue<Process> getReadyQueue() {
		return readyQueue;
	}

	public void setWaitingQueue(Queue<Process> waitingQueue, HashMap<Long, PCB> pcbList) {
		this.waitingQueue = waitingQueue;
		this.pcbInfo = pcbList;

	}

	public Queue<Process> getWaitingQueue() {
		return this.waitingQueue;
	}

	public Boolean getQuantumStatus() {
		return this.quantumStatus;
	}

	public void run(Process process) {
		if (cpu.status)
			System.out.println("Starting a new timer for " + process.getProcessName());
		quantumStatus = true;
		timer = new Timer();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				quantumStatus = false;
				killQuantumTimer(process);
			}

		};
		timer.schedule(tt, 4);

	}

	public void killQuantumTimer(Process process) {
		timer.cancel();

		if (cpu.status)
			System.out.println(process.getProcessName() + " timer has been terminated");
	}

	public Queue<Process> sortSemaphoreWaitingQueue(Queue<Process> list) {
		Queue<Process> sortedList = new PriorityQueue<>((p1, p2) -> {
			return this.pcbInfo.get(p2.getPID()).getPriority() - this.pcbInfo.get(p1.getPID()).getPriority();
		});
		sortedList.addAll(list);
		return sortedList;
	}

}
//TODO: Re-formated semaphore logic but need to implement the sorting of the list
/*
 * new Comparator<Process>() {
 * 
 * @Override public int compare(Process p1, Process p2) {
 * if(pcbInfo.get(p1.getPID()).getPriority() <
 * pcbInfo.get(p2.getPID()).getPriority() ) { return -1; }
 * if(pcbInfo.get(p1.getPID()).getPriority() >
 * pcbInfo.get(p2.getPID()).getPriority()) { return 1; } return 0; }
 * 
 * }
 */
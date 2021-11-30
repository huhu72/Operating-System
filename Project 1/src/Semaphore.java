import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
	int value = 1;
	public Queue<Process>list = new LinkedList<Process>();
	Scheduler scheduler;
	CPU cpu;
	
	
	public void setCPU(CPU cpu) {
		this.cpu = cpu;
	}
	
	public void setScheduler(Scheduler s) {
		this.scheduler = s;
	}
	// Semaphore methods
	// S is the semaphore the process has, P is the process thats calling this
	// method

	/// All of the below should belong in the semaphore class
	public synchronized void wait( Process P) {
		this.value--;
		if (this.value < 0) {
			this.list.add(P);
			block(P);
			//System.out.println(P.getProcessName() + "has been sent to the semaphore waiting queue since S < 0");
		}
	}

	public synchronized void signal() {
		this.value++;
		if (this.value <= 0) {
			this.value = 1;
			Process P = this.list.poll();
			wakeUp(P);
			//System.out.println(P.getProcessName() + "has been put back into the ready queue");
		}
	}

	private void wakeUp(Process P) {
		PCB pcb = cpu.getPCB(P.getPID());
		pcb.setState(STATE.READY);
		cpu.updatePCBList(pcb);
		scheduler.addToPQ(P);
	}

	private void block(Process p) {
		this.list = scheduler.sortSemaphoreWaitingQueue(this.list);
		PCB pcb = cpu.getPCB(p.getPID());
		pcb.setState(STATE.WAIT);
		cpu.updatePCBList(pcb);
	}
	public void print() {
		System.out.println("All processes:");
		for (Process p : this.list) {
			System.out.println(p.getProcessName() + " Priority: " + p.priority);

		}
	}
}

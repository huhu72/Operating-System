
import java.io.FileNotFoundException;

import java.util.Timer;
import java.util.TimerTask;


public class OS{
	static boolean status = false;
	static Dispatcher dispatcher = new Dispatcher();
	static CPU cpu = new CPU();
	
	public static void main(String[] args) {
		Semaphore S = new Semaphore();
		OS os = new OS();
		Scheduler scheduler = new Scheduler();
		Timer timer = new Timer();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				cpu.print();
				if(dispatcher.getReadyQueue().isEmpty() && dispatcher.getWaitingQueue().isEmpty()) {
					timer.cancel();
				}
			}

		};
		S.setScheduler(scheduler);
		S.setCPU(cpu);
		cpu.setDispatcher(dispatcher);
		cpu.S = S;
		cpu.setScheduler(scheduler);
	
		Process p = new Process(cpu);

		dispatcher.setScheduler(scheduler);
		try {
			p.createProcessesPrompt();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		dispatcher.setCPU(cpu);
		dispatcher.setPCBList(cpu.getPCBList());
		dispatcher.setReadyQueue(cpu.getJobQueue());
		timer.scheduleAtFixedRate(tt, 10, 4000);
		//cpu.runCommand();
		cpu.start();
	
		/*
		 * for(Process p1 : dispatcher.getReadyQueue()) { System.out.println(p1); }
		 */

	}


	/*
	 * public void runCommand() { Process process; PCB pcb; ArrayList<Command>
	 * commands;
	 * 
	 * while (!dispatcher.getReadyQueue().isEmpty() ||
	 * !dispatcher.getWaitingQueue().isEmpty()) {
	 * 
	 * // Only works for running processes one at a time. Logic Process 1 gets to
	 * the // I/O command // Process 1 -> waiting queue. The waiting queue is no
	 * longer empty so it will // pull the process from there if
	 * (dispatcher.getWaitingQueue().isEmpty()) process = dispatcher.getProcess();
	 * else process = dispatcher.getProcessFromWaitingQueue(); // The PCB should be
	 * updated from the dispatcher class once its sent over there // This is so that
	 * the current pcb has information on the counters from when it // was sent to
	 * the dispatcher pcb = pcbList.get(process.getPID()); commands =
	 * process.getCommands(); if (commands.get(0).command.equals("I/O") &&
	 * pcb.programCounter.getCounter() == 1) {
	 * //System.out.println(process.getProcessName() +
	 * " has been sent to the waiting queue"); dispatcher.addToWaitingQueue(process,
	 * pcb); break; } System.out.println("Running " + process.getProcessName()); //
	 * while (pcb.getProgramCounter() <= process.getCommands().size()) { for (int i
	 * = pcb.getProgramCounter(); i <= commands.size(); i++) {
	 * 
	 * System.out.println("Running " + commands.get(i - 1).command);
	 * System.out.println("On Command: " + pcb.getProgramCounter() + "/" +
	 * commands.size()); //scheduler.startQuantumClock(); while
	 * (scheduler.quantumStatus) {
	 * 
	 * // getPCB(process.getPID()).getProgramCounter() - 1; // If the process runs
	 * all of the cyles in the command, it will increament the // program counter //
	 * and exit out of the loop if (pcb.programCounter.getCyclesRan() ==
	 * commands.get(i - 1).cycle) { pcb.programCounter.incrementProgramCounter();
	 * pcb.programCounter.setCyclesRan(0); //i = pcb.programCounter.getCounter();
	 * break; }
	 * 
	 * pcb.programCounter.incrementProgramCycle(); System.out.println( "On " +
	 * pcb.programCounter.getCyclesRan() + "/" + commands.get(i - 1).cycle +
	 * " cycle");
	 * 
	 * } scheduler.quantumStatus = true; // If the process has ran all the commands,
	 * add it to the terminated list if (pcb.programCounter.getCounter() >
	 * commands.size()) { pcb.setState(STATE.EXIT); pcbList.put(pcb.getProcessPID(),
	 * pcb);
	 * 
	 * System.out.println(process.getProcessName() + " has been terminated"); break;
	 * // if it ran all its cycles but not all of the commands add it to the
	 * respected // queue based on the next command } else if
	 * (pcb.programCounter.getCyclesRan() < commands.get(i - 1).cycle &&
	 * commands.get(i - 1).command.equals("I/O")) {
	 * System.out.println(process.getProcessName() +
	 * " has been sent to the waiting queue"); dispatcher.addToWaitingQueue(process,
	 * pcb); break;
	 * 
	 * // if there are still cycles to be ran in the current command and the command
	 * is // Calculate // Dont need to increment the program counter if the process
	 * still have cycles // to run but the pcb needs to be updated // in the
	 * dispatcher class } else if (pcb.programCounter.getCyclesRan() <
	 * commands.get(i - 1).cycle && commands.get(i - 1).command.equals("CALCULATE"))
	 * { System.out.println(process.getProcessName() +
	 * " has been sent to the ready queue"); dispatcher.addToReadyQueue(process,
	 * pcb); break; // if there are still cycles to be ran in the current command
	 * and the command is // Calculate } else { if (commands.get(i) != null) {
	 * Command nextCommand = commands.get(i - 1);
	 * pcb.programCounter.setCyclesRan(0); if
	 * (nextCommand.command.equals("CALCULATE")) {
	 * System.out.println(process.getProcessName() +
	 * " has been sent to the ready queue based on the next command");
	 * dispatcher.addToReadyQueue(process, pcb); //break; } else {
	 * System.out.println(process.getProcessName() +
	 * " has been sent to the waiting  queue");
	 * dispatcher.addToWaitingQueue(process, pcb); //break; } } break; }
	 * 
	 * }
	 * 
	 * //} // in a couple of cycles, increment the priorities of all processes
	 * //break; } print(); }
	 */

}

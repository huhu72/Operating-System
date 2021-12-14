import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;

public class OS extends JFrame implements ActionListener {
	private JFrame frame = new JFrame();
	private JButton runBtn = new JButton("RUN");
	Font font1 = new Font("SansSerif", Font.BOLD, 20);
	Font font2 = new Font("SansSerif", Font.PLAIN, 20);
	JTextPane cpuBoundNum = new JTextPane();
	JTextPane ioBasedNum = new JTextPane();
	JTextPane longBasedNum = new JTextPane();
	JTextPane shortBasedNum = new JTextPane();
	JButton statusBtn = new JButton("STATUS");
	static JTextPane info = new JTextPane();

	OS() {
		this.frame.setBounds(200, 200, 950, 360 * 2);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().setLayout(null);

		JLabel templatePrompt = new JLabel(
				"Here are a list of available templates, please type how many processes you would like to create ");
		templatePrompt.setFont(font1);
		templatePrompt.setBounds(5, 5, 1000, 50);
		frame.getContentPane().add(templatePrompt);

		JLabel cpuBound = new JLabel("cpubound.txt");
		cpuBound.setFont(font2);
		cpuBound.setBounds(5, 40, 800, 50);
		frame.getContentPane().add(cpuBound);

		cpuBoundNum.setBounds(140, 55, 20, 25);
		cpuBoundNum.setEditable(true);
		frame.getContentPane().add(cpuBoundNum);

		JLabel ioBased = new JLabel("iobased.txt");
		ioBased.setFont(font2);
		ioBased.setBounds(5, 70, 800, 50);
		frame.getContentPane().add(ioBased);

		ioBasedNum.setBounds(140, 85, 20, 25);
		ioBasedNum.setEditable(true);
		frame.getContentPane().add(ioBasedNum);

		JLabel longBased = new JLabel("longbased.txt");
		longBased.setFont(font2);
		longBased.setBounds(5, 100, 800, 50);
		frame.getContentPane().add(longBased);

		longBasedNum.setBounds(140, 115, 20, 25);
		longBasedNum.setEditable(true);
		frame.getContentPane().add(longBasedNum);

		JLabel shortBased = new JLabel("shortbased.txt");
		shortBased.setFont(font2);
		shortBased.setBounds(5, 130, 800, 50);
		frame.getContentPane().add(shortBased);

		shortBasedNum.setBounds(140, 145, 20, 25);
		shortBasedNum.setEditable(true);
		frame.getContentPane().add(shortBasedNum);

		runBtn.setBounds(30, 170, 100, 50);
		runBtn.addActionListener(this);
		frame.getContentPane().add(runBtn);

		statusBtn.setBounds(30, 230, 100, 50);
		statusBtn.addActionListener(this);
		frame.getContentPane().add(statusBtn);

		info.setBounds(200, 50, 700, 500);
		info.setEditable(false);
		frame.getContentPane().add(info);

	}

	public static void main(String[] args) {
		OS gui = new OS();
		gui.pack();
		gui.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.frame.setLocationRelativeTo(null);
		gui.frame.setVisible(true);

		/*
		 * for(Process p1 : dispatcher.getReadyQueue()) { System.out.println(p1); }
		 */

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Dispatcher dispatcher = new Dispatcher();
		CPU cpu = new CPU();
		CPU cpu2 = new CPU();

		if (e.getSource() == runBtn) {
			if (cpuBoundNum.getText().equals("")) {
				cpuBoundNum.setText("0");
			}
			if (ioBasedNum.getText().equals("")) {
				ioBasedNum.setText("0");
			}
			if (longBasedNum.getText().equals("")) {
				longBasedNum.setText("0");
			}
			if (shortBasedNum.getText().equals("")) {
				shortBasedNum.setText("0");
			}
			int[] templateNums = { Integer.parseInt(cpuBoundNum.getText()), Integer.parseInt(ioBasedNum.getText()),
					Integer.parseInt(longBasedNum.getText()), Integer.parseInt(shortBasedNum.getText()) };
			Process p = new Process(cpu);
			try {
				p.createCompareProcesses();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Get total number of cycles in all 100 processes that will be used to compare
			// the schedulers

			Dispatcher.setPCBList(cpu.getComparePCBList());
			Dispatcher.setReadyQueue(cpu.getCompareQueue());

			int RRCycles = cpu.compareRR();
			cpu.compareQueue = new LinkedList<Process>();
			CPU.comparePCBList = new HashMap<>();
			try {
				p.createCompareProcesses();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Dispatcher.setPCBList(cpu.getComparePCBList());
			Dispatcher.setReadyQueue(cpu.getCompareQueue());
			int PQCycles = cpu.comparePQ();
			if (RRCycles < PQCycles) {
				CPU.scheduler = "PQ";

			} else {
				CPU.scheduler = "RR";

			}

			try {
				p.createProcessesPrompt(templateNums);
			} catch (FileNotFoundException e1) {

				e1.printStackTrace();
			}
			// statusThread.start();
			Dispatcher.setPCBList(cpu.getPCBList());
			Dispatcher.setReadyQueue(cpu.getJobQueue());
		
			Semaphore s1 = new Semaphore();
			Semaphore s2 = new Semaphore();
			cpu.setSemaphore(s1);
			cpu2.setSemaphore(s2);
			cpu.start();
			cpu2.start();
		} else {
			CPU.status = true;
			cpu.print();
		}

	}

}

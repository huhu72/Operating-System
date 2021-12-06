import java.util.Scanner;

public class Status implements Runnable {
	Scanner scanner = new Scanner(System.in);
	CPU cpu;

	Status(CPU cpu) {
		this.cpu = cpu;
	}

	@Override
	public void run() {
		String input;
		while (true) {
			System.out.print("Command: ");
			input = scanner.nextLine();
			input.toLowerCase().trim();
			if (input.equals("status")) {
				CPU.status = true;
				CPU.print();
				CPU.status = false;
			}
		}

	}

}

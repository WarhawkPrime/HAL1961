import mmu.*;

public class Main {
	
	public static void main(String[] args) throws InterruptedException {
		
		MMU mmu = new MMU();
		mmu.startMMU(args[0]);
	}
}
	
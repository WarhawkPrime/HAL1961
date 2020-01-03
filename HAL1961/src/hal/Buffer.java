package hal;

public class Buffer {
	private boolean available = false;
	private double data = 0;
	
	public synchronized void put ( double x) {
		while ( available ) {
			try {
				wait ();  
			} catch ( InterruptedException e) {}
		}
		data = x;
		available = true ;
		notifyAll ();  
	}
	
	public synchronized double get () {
		while (! available ) {
			try {
				wait ();  
			} catch ( InterruptedException e) {}
		}
		available = false ;
		notifyAll ();  
		return data ;
	}
}
import java.io.*;
import java.util.ArrayList;


public class SquishFactory {
	public void generate(int numthreads, int runs,/* OutputStream Out, InputStream in,*/ String s) throws InterruptedException{
		SquishThread thread;
		int pass = runs/numthreads;
		int hold = 0;
		int num;
		ArrayList<SquishThread> threads = new ArrayList<SquishThread>();
		for(int i=0; i < numthreads; i++){
			if(i<numthreads - 1){
			thread = new SquishThread(hold, hold + pass,/* Out, in*/ i, s);
			hold = hold + pass;
			}
			else{
			thread = new SquishThread(hold, runs,/* Out, in,*/ i, s);
			}
			threads.add(thread);
			thread.start();
		}
		for(Thread t : threads){
			t.join();
		}
		synchronized(this){
			notify();
		}
	}
}

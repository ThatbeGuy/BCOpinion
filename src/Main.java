import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
	public static ThreadDelegate monitor = new ThreadDelegate();;
	public static ArrayList<SimThread> threads = new ArrayList<SimThread>();
	static FileSquash squisher = new FileSquash();
	public static void main(String[] args) throws InterruptedException{
		if(args.length > 0){
			ArgCheck.check(args);
		}
		System.out.println(Constants._trials + " " + Constants._numnodes);
		if(!Constants.files.isEmpty()){
			Constants.files.clear();
		}
		if(!threads.isEmpty()){
			threads.clear();
		}
		SimData.initialize();
		System.out.println("There are supposed to be " + Constants.numThreads + " threads");
		SimFactory.spawn(Constants.numThreads);
		System.out.println("Using " + threads.size() + " threads");
		for(SimThread t : threads){
			t.join();
		}
		System.out.println("Simulation complete.");
		System.out.println("Squashing files....");
		try {
			squisher.squash(monitor.getRuns());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Files squashed");
		System.out.println("Cookie time!");
	}
	
}

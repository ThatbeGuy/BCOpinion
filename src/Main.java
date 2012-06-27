import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Main { 
	public static ArrayList<SimThread> threads = new ArrayList<SimThread>();
	static FileSquash squisher = new FileSquash();
	public static volatile ArrayList<TickDataCollector> tDC = new ArrayList<TickDataCollector>();
	private static Constants mainConst = new Constants();
	public static ThreadDelegate monitor;
	
	public static void main(String[] args) throws InterruptedException{
		if(args.length > 0){
			ArgCheck acheck = new ArgCheck();
			acheck.Constants = mainConst;
			acheck.check(args);
		}
		
		monitor = new ThreadDelegate(mainConst);
		System.out.println(Constants._trials + " " + Constants._numnodes);
		if(!Constants.files.isEmpty()) {
			Constants.files.clear();
		}
		if(!threads.isEmpty()){
			threads.clear();
		}
		SimData.initialize();
		TickDataCollector.initialize();
		SimFactory.spawn(Constants.numThreads);
		System.out.println("Using " + threads.size() + " threads");
		for(SimThread t : threads) {
			t.join();
		}
		System.out.println("Simulation complete.");
		
		//write the data in tDC to file
		if(Constants.measureTicks)
		try {
			for(TickDataCollector t: tDC) t.getData();
		} catch(IOException e) {
			e.printStackTrace();
		}
		TickDataCollector.close();
		
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

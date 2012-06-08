import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
	public static ThreadDelegate monitor = new ThreadDelegate();
	public static ArrayList<SimThread> threads = new ArrayList<SimThread>();
	static FileSquash squisher = new FileSquash();
	public static void main(String[] args) throws InterruptedException{
		if(args.length == 2) {
			Constants.resetVals(Integer.parseInt(args[0]),
								Integer.parseInt(args[1]));
		}
		if(args.length == 7){
			Constants.resetVals(Integer.parseInt(args[0]),
								Integer.parseInt(args[1]));
			Constants._murand = Boolean.parseBoolean(args[2]);
			Constants.randMuStart = Double.parseDouble(args[3]);
			Constants.randMuEnd = Double.parseDouble(args[4]);
			Constants.Repulsive = Boolean.parseBoolean(args[5]);
			Constants.repuslivePer = Integer.parseInt(args[6]);
		}
		if(args.length == 8) {
			Constants.resetVals(Integer.parseInt(args[0]),
								Integer.parseInt(args[1]));
			Constants._murand = Boolean.parseBoolean(args[2]);
			Constants.randMuStart = Double.parseDouble(args[3]);
			Constants.randMuEnd = Double.parseDouble(args[4]);
			Constants.Repulsive = Boolean.parseBoolean(args[5]);
			Constants.repuslivePer = Integer.parseInt(args[6]);
			Constants._OUTPUT_PATH = args[7];
		}
		if(args.length == 9) {
			Constants.resetVals(Integer.parseInt(args[0]),
					Integer.parseInt(args[1]));
			Constants._murand = Boolean.parseBoolean(args[2]);
			Constants.randMuStart = Double.parseDouble(args[3]);
			Constants.randMuEnd = Double.parseDouble(args[4]);
			Constants.Repulsive = Boolean.parseBoolean(args[5]);
			Constants.repuslivePer = Integer.parseInt(args[6]);
			Constants._OUTPUT_PATH = args[7];
			Constants.muCheck = Boolean.parseBoolean(args[8]);
		}
		File file = new File(Constants._OUTPUT_PATH + "potato");
		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!Constants.files.isEmpty()){
			Constants.files.clear();
		}
		SimData.initialize();
		SimThread t1 = new SimThread(.025, .05, 0, false);
		SimThread t2 = new SimThread(.075, .15, 1, true);
		SimThread t3 = new SimThread(.175,.25,2,false);					//(.525, .75, 2, false);
		SimThread t4 = new SimThread(.275,.5,3,false);					//(.775, 1, 3, false);
		t1.setPriority(10);
		t2.setPriority(9);
		t3.setPriority(8);
		t4.setPriority(7);
		threads.add(t1);
		threads.add(t2);
		threads.add(t3);
		threads.add(t4);
		monitor.threadStart(t1);
		monitor.threadStart(t2);
		monitor.threadStart(t3);
		monitor.threadStart(t4);
		t1.join();
		t2.join();
		t3.join();
		t4.join();
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

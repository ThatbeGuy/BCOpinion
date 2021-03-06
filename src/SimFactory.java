
public class SimFactory {
	public static void spawn(int threadnum){
		for(int i = 0; i < threadnum; i ++){
			if(Constants.verbose){
				if(Constants.numThreadsVerbose > 0){
					if(i + 1 <= Constants.numThreadsVerbose){
						SimThread t1 = new SimThread(.025, .05, 0, true);
						Main.threads.add(t1);
						Main.monitor.threadStart(t1);
					}
					else{
						SimThread t1 = new SimThread(.025, .05, 0, false);
						Main.threads.add(t1);
						Main.monitor.threadStart(t1);
					}
				}
				else if(Constants.threadVerbose > -1 && Constants.threadVerbose < i){
					if(i == Constants.threadVerbose){
						SimThread t1 = new SimThread(.025, .05, 0, true);
						Main.threads.add(t1);
						Main.monitor.threadStart(t1);
					}
					else{SimThread t1 = new SimThread(.025, .05, 0, false);
					Main.threads.add(t1);
					Main.monitor.threadStart(t1);
					}
				}
				else{
					SimThread t1 = new SimThread(.025, .05, 0, false);
					Main.threads.add(t1);
					Main.monitor.threadStart(t1);
				}
			}
			else{
				SimThread t1 = new SimThread(.025, .05, 0, false);
				Main.threads.add(t1);
				Main.monitor.threadStart(t1);
			}
		}
	}
}

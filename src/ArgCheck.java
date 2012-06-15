import java.util.ArrayList;


public class ArgCheck {
	Constants Const = new Constants();
	
	public static void check(String[] argss, Constants con){
		ArrayList<String> args = new ArrayList<String>();
		for(String s : argss) args.add(s);
		String key;
		if(args.contains("nomigrate")){
			Constants.migrateSwitch = false;
		}
		if(args.contains("dynamicgroups")){
			Constants.DynamicGroups = true;
		}
		if(args.contains(key = "randommu")){
			Constants._murand = true;
			if(args.indexOf(key) < args.size() - 2){
				con.randMuStart = Double.parseDouble(args.get(args.indexOf(key)+1));
				con.randMuEnd = Double.parseDouble(args.get(args.indexOf(key)+2));
			}
		}
		if(args.contains("repulse")){
			Constants.Repulsive = true;
			if(args.indexOf("repulse") < args.size() - 1){
				Constants.repuslivePer = Integer.parseInt(args.get(args.indexOf("repulse") + 1 ));
			}
		}
		
		if(args.contains(key = "epsilon")) con.setEpsilon(Double.parseDouble(args.get(args.indexOf(key)+1)));
		else if(args.contains(key = "indpepsilon")){
			double start = Double.parseDouble(args.get(args.indexOf(key)+1));
			double end = Double.parseDouble(args.get(args.indexOf(key)+2));
			double step = Double.parseDouble(args.get(args.indexOf(key)+3));
			con.addIndpVar(Constants.indpVarName.epsilon, start, end, step);
		}
		
		if(args.contains(key = "mu")) con.setMu(Double.parseDouble(args.get(args.indexOf(key)+1)));
		else if(args.contains(key = "indpmu")){
			double start = Double.parseDouble(args.get(args.indexOf(key)+1));
			double end = Double.parseDouble(args.get(args.indexOf(key)+2));
			double step = Double.parseDouble(args.get(args.indexOf(key)+3));
			con.addIndpVar(Constants.indpVarName.mu, start, end, step);
		}
		
		if(args.contains(key = "nodes")){
			con.setNumNodes(Integer.parseInt(args.get(args.indexOf(key)+1)));
		}
		if(args.contains(key = "trials")){
			Constants._trials = Integer.parseInt(args.get(args.indexOf(key)+1));
		}
		if(args.contains(key = "numthreads")){
			Constants.numThreads = Integer.parseInt(args.get(args.indexOf(key) + 1));
		}
		
		if(args.contains(key = "verbose")){
			Constants.verbose = true;
			if(args.indexOf(key) < args.size() - 1){
				if(args.get(args.indexOf(key) + 1) == "thread"){
					Constants.numThreadsVerbose = 0;
					Constants.threadVerbose = Integer.parseInt(args.get(args.indexOf(key) + 2));
				}
				else if(args.get(args.indexOf(key) + 1) == "num"){
					Constants.numThreadsVerbose = Integer.parseInt(args.get(args.indexOf(key) + 2));
				}
			}
		}
		
		if(args.contains(key = "exdegree")){
			con.setAvgDegree(Integer.parseInt(args.get(args.indexOf(key) + 1)));
		}
		if(args.contains(key = "groupratio")){
			con.groupRatio = Integer.parseInt(args.get(args.indexOf(key) + 1));
		}
		if(args.contains(key = "groups")){
			con.setGroups(Integer.parseInt(args.get(args.indexOf(key) + 1)));
		}
		
		if(args.contains(key = "output")){
			con._OUTPUT_PATH = args.get(args.indexOf(key) + 1);
		}
	}
}

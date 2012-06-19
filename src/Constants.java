import java.util.ArrayList;


public class Constants {
	//debug stuff (run one interation for each indepedent value)
	public final static boolean debug = false;
	
	//indepedent variable control junk
	public static enum indpVarName { epsilon, mu, nodes, groups, avgdegree, groupratio }; 
	public ArrayList<indpVar> independent = new ArrayList<indpVar>();
	
	//important value junk
	public double _epsilon = .20;
	public static int _numnodes = 100;
	public double _avgdegree = 5;
	public int _edges = (int)(_numnodes * _avgdegree)/2;
	public int groupRatio = 50;
	public int _groups = _numnodes / groupRatio;
	public double _mu = .5;
	public int minAgents = 5;
	public double _p_ext = (double) _avgdegree / _numnodes;
	public double _threshold_ratio = 1000;
	public double _threshold = _epsilon * _mu / _threshold_ratio;
	
	//some other cool stuff you can do
	//turn migration on or off
	public static boolean migrateSwitch = true;
	
	//gives agents the ability to create their own groups
	public static boolean DynamicGroups = false;
	
	//determines what range an agent might be affected in the other direction from someone's opinion
	public static boolean Repulsive = false;
	public static int repuslivePer = 50;
	
	//random mu values! and range
	public static boolean _murand = false;
	public double randMuStart = 0;
	public double randMuEnd = 1;
	
	//multithreading stuff
	public static int numThreads = 4;
	public static boolean verbose = true; //Specifies if any threads are verbose
	public static int threadVerbose = 1; // Specifies a specific thread num to be verbose(only one)
	public static int numThreadsVerbose = 1; // Specifies a number of threads to be verbose
	/**NumThreadsVerbose will make the first n threads verbose, I.E. 2 will make 0 and 1
	 verbose **/
	
	//Data collection stuff
	public static int _trials = 5;
	public static String _OUTPUT_PATH = "test\\";
	public static ArrayList<String> files = new ArrayList<String>();
	
	//initializes constants object with default values
	public Constants() {}
	
	public Constants(Constants con) {
		_epsilon = con._epsilon;
		_avgdegree = con._avgdegree;
		_edges = con._edges;
		_groups = con._groups;
		_mu = con._mu;
		_numnodes = con._numnodes;
		_p_ext = con._p_ext;
		_threshold = con._threshold;
		_threshold_ratio = con._threshold_ratio;
		groupRatio = con.groupRatio;
		indpVar temp;
		for(indpVar i : con.independent) {
			temp = (indpVar)i.clone();
			temp.setConstants(this);
			independent.add(temp);
		}//*/
		minAgents = con.minAgents;
	}
	
	//some important setters
	public void setEpsilon(double e) { _epsilon = e; _threshold = _epsilon * _mu / _threshold_ratio; }
	public void setMu(double mu) { _mu = mu; _threshold = _epsilon * _mu / _threshold_ratio; }
	public void setAvgDegree(double aD) { _avgdegree = (int)aD;  _p_ext = (double) _avgdegree / _numnodes; }
	public void setGroups(int g) { _groups = g; }
	public void setGroupRatio(int gr) { groupRatio = gr; }
	public void setNumNodes(int n) { _numnodes = n; _edges = (int)(_numnodes * _avgdegree)/2; _groups = _numnodes / groupRatio;
									 _p_ext = (double) _avgdegree / _numnodes; }
	public void setThresholdRatio(double t) { _threshold_ratio = t; _threshold = _epsilon * _mu / _threshold_ratio; }
	
	public void resetVals(int nodes, int trials) {
		_numnodes = nodes;
		_edges = (int) ((_numnodes * _avgdegree)/2);
		_groups = _numnodes / 50;
		_p_ext = _avgdegree / (_numnodes-1);
		_trials = trials;
	}
	
	public void addIndpVar(indpVarName iv, double start, double end, double step) {
		indpVar v;
		switch(iv) {
		case epsilon:
			v = new indpEpsilon(this);
			break;
		case mu:
			v = new indpMu(this);
			break;
		default:
			v = new indpAvgDegree(this);
			break;
		}
		v.initialize(start,end,step);
		independent.add(v);
	}
	
	public boolean increment() {
		indpVar temp;
		boolean isIncremented = false;
		for(int i = 0; i < independent.size(); i++) {
			temp = independent.get(i);
			if(!(isIncremented = temp.increment()) && i == independent.size()-1) return false;
			else if(isIncremented) break;
			else temp.reset();
		}
		return true;
	}
	
	public abstract static class indpVar implements Cloneable {
		protected boolean initialized = false;
		private final String name;
		protected Constants con;
		
		public abstract void initialize(double start, double fin, double step);
		private indpVar(Constants con1, String s) { con = con1; name = s; }
		public indpVar(Constants con1, indpVar i) { con = con1; initialized = i.initialized; name = i.name; }
		
		public abstract boolean increment();
		public abstract void reset();
		public abstract double getValue();
		public String getName() { return name; }
		public void setConstants(Constants con1) { con = con1; }
		
		public Object clone() {
			try { return super.clone(); } 
			catch (CloneNotSupportedException e) { e.printStackTrace(); }
			return null;
		}
		/*public abstract double getStart();
		public abstract double getFinal();
		public abstract double getStep();//*/
	}
	
	private class indpEpsilon extends indpVar {
		private double _epsilon_start = .025;
		private double _epsilon_final = 1;
		private double _epsilon_step = .0025;
		
		public indpEpsilon(Constants con1) { super(con1, "Epsilon"); }
		
		public void initialize(double start, double fin, double step) {
			if(initialized) return;
			initialized = true;
			_epsilon_start = start;
			_epsilon_final = fin;
			_epsilon_step = step;
			_epsilon = start;
		}
		
		public void reset() {
			con._epsilon = _epsilon_start;
		}
		
		public boolean increment() {
			if(_epsilon >= _epsilon_final) return false;
			con._epsilon += _epsilon_step;
			return true;
		}
		
		public double getValue() { return con._epsilon; }
	}
	
	private class indpMu extends indpVar {
		private double _mu_start = 0;
		private double _mu_final =  0.98;
		private double _mu_step = 0.01;
		
		public indpMu(Constants con1) { super(con1, "Mu"); }
		
		public void initialize(double start, double fin, double step) {
			if(initialized) return;
			initialized = true;
			_mu_start = start;
			_mu_final = fin;
			_mu_step = step;
		}
		
		public boolean increment() {
			if(_mu >= _mu_final) return false;
			con._mu += _mu_step;
			return true;
		}
		
		public void reset() {
			con._mu = _mu_start;
		}
		
		public double getValue() { return con._mu; }
	}
	
	private class indpAvgDegree extends indpVar {
		private double _avgdegree_start = 0;
		private double _avgdegree_final = 50;
		private double _avgdegree_step = 5;
		
		public indpAvgDegree(Constants con1) { super(con1, "Average Degree"); }
		
		public void initialize(double start, double fin, double step) {
			if(initialized) return;
			initialized = true;
			_avgdegree_start = start;
			_avgdegree_final = fin;
			_avgdegree_step = step;
			
			con._avgdegree = _avgdegree_start;
			resetVals();
		}
		
		public boolean increment() {
			if(_avgdegree >= _avgdegree_final) return false;
			con._avgdegree += _avgdegree_step;
			resetVals();
			return true;
		}
		
		public void reset() {
			con._avgdegree = _avgdegree_start;
			resetVals();
		}
		
		private void resetVals() {
			con._edges = (int)(con._numnodes * con._avgdegree)/2;
			con._p_ext = (double)con._avgdegree / (con._numnodes-1);
		}
		
		public double getValue() { return (double)con._avgdegree; }
	} //*/
	
	
}

import java.util.ArrayList;


public class Constants {
	public static int _numnodes = 5000;
	public static final int _avgdegree = 5;
	public static int _edges = (_numnodes * _avgdegree)/2;
	public static int _groups = _numnodes / 50;
	
	public double _SIM_epsilon_start = .025;
	public double _SIM_epsilon_final = 1;
	public static final double _SIM_epsilon_step = .0125;
	
	public static final int _iterations = 100;
	public static int _trials = 10;
	public double _epsilon = _SIM_epsilon_start;
	public static boolean _murand = false;
	public static final double _mu = .5;
	
	public static double _p_ext = (double)_avgdegree / (_numnodes-1);
	public static boolean Repulsive = false;
	public static int repuslivePer = 50;
	
	//stuff for data collection
	public static String _OUTPUT_PATH = "test\\";
	public final double threshold = _epsilon * _mu / 10;
	public final static boolean debug = false;
	public static ArrayList<String> files = new ArrayList<String>();
	public static double randMuStart = 0;
	public static double randMuEnd = 1;
	public static double muIncS = 0;
	public static double muIncUp = .1;
	public static boolean muCheck = false;
	
	public static void resetVals(int nodes, int trials) {
		_numnodes = nodes;
		_edges = (_numnodes * _avgdegree)/2;
		_groups = _numnodes / 50;
		_p_ext = (double)_avgdegree / (_numnodes-1);
		_trials = trials;
	}
}

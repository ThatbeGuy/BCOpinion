import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class SimData {
	//The id of the thread SimData is being run in
	public final int ThreadNum;
	//The current trial being processed (SimData runs after every trial)
	public int cTrial = 1;
	
	//Data writers
	public ArrayList<DataWriter> DataWriters;
	
	//The data being collected
	//public ArrayList<Double> opinionSet = new ArrayList<Double>();
	protected double avgExternalNeighbors = 0;
	protected ArrayList<Graph.OpinionCluster> ocGroup;
	ArrayList<Graph.OpinionCluster> ocPopulation;
	
	
	//other constants
	//public final double epsValue;
	//Constants Constants = new Constants();
	private final boolean silent = false;
	private final boolean isVerbose;
	private final Number indVar;
	
	public static void initialize() {
		//make sure the files object is cleared beforehand
		Constants.files.clear();
		
		//create the subfolders yo
		String str[] = (Constants._OUTPUT_PATH + "\\gdist").split("[\\\\]|[/]");
		File fObj;
		String temp = "";
		for(int i = 0; i < str.length; i++) {
			if(str[i].isEmpty() && i++ == str.length) break;
			fObj = new File(temp + str[i]);
			if(!fObj.exists()) {
				fObj.mkdir();
			}
			temp += str[i] + "\\";
		}
		//assign each
		Constants.files.add("OpinionDensity");
		Constants.files.add("NCRR_N" + Constants._numnodes);
		Constants.files.add("OpinionClusters");
		Constants.files.add("OCPopDist");
		Constants.files.add("OCGroupDist");
		Constants.files.add("extneighbors");
		Constants.files.add("Metrics");//*/
	}
	
	public SimData(int tNum, Number indp, boolean verbose) {
		ThreadNum = tNum;
		indVar = indp;
		isVerbose = verbose;
		printToConsole("Initializing SimData object for independent variable value: " + round(indVar.doubleValue()));
		
		try {
			DataWriters = new ArrayList<DataWriter>();
			DataWriters.add(new OpinionDensityDataWriter(Constants.files.get(OpinionDensityDataWriter.id)));
			DataWriters.add(new OpinionClusterDataWriter(Constants.files.get(OpinionClusterDataWriter.id)));
			DataWriters.add(new RealizationFractionByOCDataWriter(Constants.files.get(RealizationFractionByOCDataWriter.id)));
			DataWriters.add(new OCDistDataWriter(Constants.files.get(OCDistDataWriter.id), 0));
			DataWriters.add(new OCDistDataWriter(Constants.files.get(OCDistDataWriter.id+1), 1));
			DataWriters.add(new GroupSizeDistributionDataWriter("GroupSizeDistribution"));
			DataWriters.add(new ExternalNeighborDataWriter(Constants.files.get(ExternalNeighborDataWriter.id)));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processEpsilonValue() {
		
		forcePrintToConsole("Finalizing data for independent variable value: " + round(indVar.doubleValue())
				+ "\n\t\t" + "Avg number of external neighbors: " + round(avgExternalNeighbors)
				//+ "\n\t\t" + ""
				);
		/*printToConsole("Statistics gathered for epsilon value " + epsilon + ":\n"
				+ " opAverageTotal: " + opAverageTotal + "\n"
				+ " ocOccurranceByPopulation: " + ocOccurranceByPopulation + "\n"
				+ " ocOccurranceByGroup: " + ocOccurranceByGroup + "\n"
				+ " ocNonConsensusRatio: " + ocNonConsensusRatio + "\n"
				);*/
		try {
			for(DataWriter dw : DataWriters) {
				if(dw.active) dw.addNewRow();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processTrial(Graph g) {
		if(cTrial % (Constants._trials / 10 + 1) == 0) printToConsole("Adding results from trial " + cTrial + ".");
		
		//calculateCumulativeGroupSize(g.getGroups());
		
		ocGroup = g.calculateOpinionClustersByGroup();
		ocPopulation = g.calculateOpinionClustersByPopulation();
		
		for(DataWriter dw: DataWriters) {
			if(dw.active) dw.appendData(g);
		}
		
		calculateExtNeighborAvg(g.getAgents());
		
		//move on to the next trial
		cTrial++;
	}
	
	public void finish() {
		for(DataWriter dw : DataWriters) {
			if(dw.active) dw.finish();
		}
	}
	
	private void calculateExtNeighborAvg(ArrayList<Agent> agents) {
		int totalExtNeighbors = 0;
		for(Agent a : agents) {
			totalExtNeighbors += a.getNumExternalNeighbors();
		}
		double trAvg = (double)totalExtNeighbors / agents.size();
		avgExternalNeighbors += trAvg / Constants._trials;
	}
	
	protected void printToConsole(String s) {
		if(!silent && isVerbose) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	protected void forcePrintToConsole(String s) {
		if(!silent) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	//returns only values less than 1.
	private static String round(double d, int exact) {
		//long rval = Math.round(d * Math.pow(10, exact));
		return String.format("%1$,." + exact + "f", d);
		
	}
	
	protected static String round(double d) {
		return round(d,5);
	}
	
	private abstract class DataWriter {
		protected final String fileName;
		public final boolean active;
		protected File fileObj;
		protected FileWriter fileWriter;
		protected BufferedWriter fOutput;
		
		public DataWriter(String fName, boolean isActive) throws IOException {
			fileName = new String(fName);
			active = isActive;
		}
		
		protected void initialize() throws IOException {
			fileObj = new File(Constants._OUTPUT_PATH + fileName + ThreadNum);
			fileWriter = new FileWriter(fileObj);
			fOutput = new BufferedWriter(fileWriter);
			if(!fileObj.exists()) {
				fileObj.createNewFile();
			}
		}
		
		protected abstract void addNewRow() throws IOException;
		protected abstract void appendData(Graph g);
		
		protected void finish() {
			try {
				fOutput.close();
				fileWriter.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class OpinionDensityDataWriter extends DataWriter {
		public static final int id = 0;
		private int exact = 100;
		private double[] opAverageSet = new double[exact+1];
		
		
		public OpinionDensityDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}
		
		protected void appendData(Graph g) {
			int opPos;
			double[] opTrial = new double[exact+1];
			for(int i = 0; i < opTrial.length; i++) {
				opTrial[i] = 0;
			}
			for(Agent a : g.getAgents()) {
				opPos = (int) Math.round(a.opinion*exact);
				opTrial[opPos] += 1;
			}
			
			for(int i = 0; i < opTrial.length; i++) {
				opTrial[i] /= Constants._numnodes;
				opAverageSet[i] += opTrial[i] / Constants._trials;
			}
		}
		
		//do this for every value of epsilon
		protected void addNewRow() throws IOException {
			//printToConsole("Writing data from epsilon value " + round(epsilon) + " to file.");
			String newRow = "";
			for(int i = 0; i < opAverageSet.length; i++) {
				newRow += round(indVar.doubleValue()) + " ";
				newRow += round((double)i / opAverageSet.length) + " ";
				newRow += round(opAverageSet[i]) + " ";
				newRow += "\n";
			}
			fOutput.write(newRow);
		}
	}
	
	private class RealizationFractionByOCDataWriter extends DataWriter {
		public static final int id = 1;
		
		private double ocNonConsensusRatio = 0;
		
		public RealizationFractionByOCDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}
		
		protected void appendData(Graph g) {
			int theshold = 0;
			int numClusters = 0;
			for(Graph.OpinionCluster oc: ocPopulation) if(oc.occurance > theshold) numClusters++;
			if(numClusters > 1) ocNonConsensusRatio += 1 / Constants._trials;
		}
		
		protected void addNewRow() throws IOException {
			String newRow = "";
			
			newRow += round(indVar.doubleValue()) + " ";
			newRow += round(ocNonConsensusRatio) + "\n";
			//newRow += "HERLLO WORLD";
			
			fOutput.write(newRow);
		}
	}
	
	private class OpinionClusterDataWriter extends DataWriter {
		public static final int id = 2;
		protected double ocOccurranceByPopulation = 0;
		protected double ocOccurranceByGroup = 0;
		
		public OpinionClusterDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}
		
		protected void appendData(Graph g) {
			appendOCPop(ocPopulation);
			appendOCGroup(ocGroup, g.getGroups());
		}
		
		protected void appendOCPop(ArrayList<Graph.OpinionCluster> ocSet) {
			int numClusters = 0;
			int sizeThreshold = 1;//Constants.groupRatio;
			for(Graph.OpinionCluster o : ocSet) if(o.occurance > sizeThreshold) numClusters++;
			
			ocOccurranceByPopulation += (double)numClusters / Constants._trials;
		}
		
		protected void appendOCGroup(ArrayList<Graph.OpinionCluster> ocSet, ArrayList<Group> gs) {
			int numClusters = 0;
			numClusters = ocSet.size();
			
			int numGroups = 0;
			for(Group g: gs) {
				if(g.getAgents().size() > 0) numGroups++;
			}
			double average = (double)numClusters / numGroups;
			
			ocOccurranceByGroup += average / Constants._trials;
		}
		
		protected void addNewRow() throws IOException {
			String newRow = "";
			//column 1: epsilon value being measured
			newRow += round(indVar.doubleValue()) + " ";
			
			//column 2: average number of opinion clusters per group
			newRow += round(ocOccurranceByGroup) + " ";
			
			//column 3: average number of opinion clusters by population
			newRow += round(ocOccurranceByPopulation) + "\n";
			
			fOutput.write(newRow);
		}
	}
	
	private class GroupSizeDistributionDataWriter extends DataWriter {
		private double[] cumulativeGroupSize = new double[Constants._groups];
		
		public GroupSizeDistributionDataWriter(String fName) throws IOException {
			super("gdist\\" + fName + round(indVar.doubleValue(), 4), true);
			fileObj = new File(Constants._OUTPUT_PATH + fileName + ".txt");
			fileWriter = new FileWriter(fileObj);
			fOutput = new BufferedWriter(fileWriter);
			if(!fileObj.exists()) {
				fileObj.createNewFile();
			}
		}
		
		protected void appendData(Graph g) {
			int[] gSizes = new int[Constants._groups];
			for(int i = 0; i < gSizes.length; i++) {
				//if(i >= g.getGroups().size()) gSizes[i] = 0;
				gSizes[i] = g.getGroups().get(i).getAgents().size();
			}
			Arrays.sort(gSizes);
			
			/*int[] cGroupSize = new int[Constants._groups]; 
			cGroupSize[0] = gSizes[0];
			for(int i = 1; i < cGroupSize.length; i++) {
				cGroupSize[i] = cGroupSize[i-1] + gSizes[i];
			}//*/
			
			for(int i = 1; i <= Constants._groups; i++) {
				cumulativeGroupSize[Constants._groups-i] += (double)gSizes[i-1] / Constants._trials;
			}
		}
		
		//needs to be completed
		protected void addNewRow() throws IOException {
			String str = "";
			for(int i = 0; i < Constants._groups; i++) {
				str +=  i + "\t" + cumulativeGroupSize[i] + "\n";
			}
			fOutput.write(str);
		}
		
	}

	private class OCDistDataWriter extends DataWriter {
		public static final int id = 3;
		private final int type;
		
		private int exact = 50;
		private double[] ocPopAverageSet = new double[exact+1];
		private double[] ocGroupAverageSet = new double[exact+1];
		
		public OCDistDataWriter(String fName, int t) throws IOException {
			super(fName, true);
			type = t;
			initialize(); 
		}

		protected void appendData(Graph g) {
			calculateOCPopDist(ocPopulation);
			calculateOCGroupDist(ocGroup, g.getGroups());
		}
		
		protected void calculateOCPopDist(ArrayList<Graph.OpinionCluster> ocSet) {
			int ocPos;
			double[] ocTrial = new double[exact+1];
			for(Graph.OpinionCluster o : ocSet) {
				ocPos = (int) Math.round(o.opVal*exact);
				ocTrial[ocPos] += 1;
			}
			
			for(int i = 0; i < ocTrial.length; i++) {
				ocPopAverageSet[i] += ocTrial[i] / Constants._trials;
			}
		}
		
		protected void calculateOCGroupDist(ArrayList<Graph.OpinionCluster> ocSet, ArrayList<Group> gs) {
			int numGroups = 0;
			for(Group g: gs) if(g.getAgents().size() > 0) numGroups++;
			
			int ocPos;
			double[] ocTrial = new double[ocGroupAverageSet.length];
			for(Group g: gs) {
				for(Graph.OpinionCluster o : ocSet) {
					ocPos = (int) Math.round(o.opVal*(ocGroupAverageSet.length-1));
					ocTrial[ocPos] += (double)1 / numGroups;
				}
			}
			
			for(int i = 0; i < ocTrial.length; i++)
				ocGroupAverageSet[i] += ocTrial[i] / Constants._trials;
		}
		
		public void addNewRow() throws IOException {
			if(type == 0) addNewRow(ocPopAverageSet);
			else if (type == 1) addNewRow(ocGroupAverageSet);
		}
		
		protected void addNewRow(double[] ocDist) throws IOException {
			String newRow = "";
			for(int i = 0; i < ocDist.length; i++) {
				newRow += round(indVar.doubleValue()) + " ";
				newRow += round((double)i / ocDist.length) + " ";
				newRow += round(ocDist[i]) + " ";
				newRow += "\n";
			}
			fOutput.write(newRow);
		}
	}
	
	private class ExternalNeighborDataWriter extends DataWriter {
		public static final int id = 5;
		protected final ArrayList<ExtNeighbor> frequencies = new ArrayList<ExtNeighbor>();
		
		public ExternalNeighborDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}

		public void appendData(Graph g) {
			for(Agent a: g.getAgents()) {
				boolean added = false;
				for(ExtNeighbor e: frequencies) {
					if(a.getNumExternalNeighbors() == e.ExtVal) {
						added = true;
						e.increment();
						break;
					}
				}
				if(!added) {
					frequencies.add(new ExtNeighbor(a.getNumExternalNeighbors()));
				}
			}
		}
		
		public void addNewRow() throws IOException {
			String s = "";
			for(ExtNeighbor e: frequencies) {
				s += round(indVar.doubleValue()) + "\t" + e.ExtVal + "\t" + round((double)e.frequency / Constants._trials) + "\n";
			}
			fOutput.write(s);
		}
		
		private class ExtNeighbor {
			protected int frequency = 1;
			protected final int ExtVal;
			
			protected ExtNeighbor(int val) { ExtVal = val; }
			protected void increment() { frequency++; }
		}
	}
	
}

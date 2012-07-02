import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TickDataCollector {
	private final int tickNum;
	private final int trial;
	private String ret = "";
	private String opret = "";
	
	private final ArrayList<DataCollector> writers;
	private ArrayList<BufferedWriter> fOutputs;
	
	public String getStr(){
		return ret;
	}
	
	public void initialize(ArrayList<BufferedWriter> list) {
		fOutputs = list;
		/*File tFile;
		FileWriter fWriter;
		try {
			String[] filenames = {"opdensity" , "grdensity", "ocpopdensity", "ocgrdensity"} ;
			File dirs = new File(Constants._OUTPUT_PATH + "/trial" + trial + "/");
			if(!dirs.exists()){
				dirs.mkdirs();
			}
			for(int i = 0; i < filenames.length; i++) {
				tFile = new File(Constants._OUTPUT_PATH + "/trial" + trial + "/" + "ticks_" + filenames[i] + "_trial" + trial + ".txt");
				if(tickNum < 2){
				fWriter = new FileWriter(tFile);
				}
				else fWriter = new FileWriter(tFile, true);
				fOutputs.add(new BufferedWriter(fWriter));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public void close() {
		try {
			for(BufferedWriter fOutput: fOutputs) {
				fOutput.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public TickDataCollector(int tick, int trial) {
		tickNum = tick;
		this.trial = trial;
		
		writers = new ArrayList<DataCollector>();
		writers.add(new DCOpinionDensity());
		//writers.add(new DCGroupDensity());
		//writers.add(new DCOCPopDensity());
		//writers.add(new DCOCGroupDensity());
	}
	
	public String process(Graph g) {
		for(DataCollector d: writers) d.appendData(g); 
		try {
			getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return opret;
	}
	
	public void flush(){
		try {
			for(BufferedWriter fOutput: fOutputs) {
				fOutput.flush();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getData() throws IOException {
		for(int i = 0; i < writers.size(); i++) writers.get(i).writeRow(fOutputs.get(i));
	}
	
	private abstract class DataCollector {
		public DataCollector() { }
		
		public abstract void appendData(Graph g);
		public abstract void writeRow(BufferedWriter f) throws IOException;
	}
	
	private class DCOpinionDensity extends DataCollector {
		private final int exact = 1000;
		private double[] opDensityVals = new double[exact + 1];
		
		public void appendData(Graph g) {
			int opPos;
			double[] opDensityTemp = new double[exact + 1];
			for(Agent a: g.getAgents()) {
				opPos = (int)Math.round(a.getOpinion()*exact);
				opDensityTemp[opPos] += 1;
			}
			for(int i = 0; i <= exact; i++) {
				opDensityVals[i] += opDensityTemp[i];
			}
		}
		
		public void writeRow(BufferedWriter f) throws IOException {
			for(int i = 0; i <= exact; i++) {
			f.write(tickNum + "\t" + (double)i / exact + "\t" + opDensityVals[i] + "\n");
                            /*opret += tickNum + "\t";
				opret += (double)i / exact + "\t";
				opret += opDensityVals[i] + "\n";*/
			}
                        f.write("\n");
			//opret += "\n";
			//char[] pass = ret.toCharArray();
			//f.write(pass, 0, pass.length);
			//f.write(ret);
		}
	}
	
	private class DCGroupDensity extends DataCollector {
		private final int exact = 1000;
		private double[] opDensityVals = new double[exact + 1];
		
		public void appendData(Graph gr) {
			int opPos;
			double[] opDensityTemp = new double[exact + 1];
			for(Group g: gr.getGroups()) {
				opPos = (int)Math.round(g.getavg()*exact);
				opDensityTemp[opPos] += 1;
			}
			for(int i = 0; i <= exact; i++) {
				opDensityVals[i] += opDensityTemp[i];
			}
		}
		
		public void writeRow(BufferedWriter f) throws IOException {
			ret = "";
			for(int i = 0; i <= exact; i++) {
				ret += tickNum + "\t";
				ret += (double)i / exact + "\t";
				ret += opDensityVals[i] + "\n";
			}
			ret += "\n";
			//f.write(ret);
		}
	}
	
	private class DCOCPopDensity extends DataCollector {
		private final int exact = 100;
		private double[] opDensityVals = new double[exact + 1];
		
		public void appendData(Graph gr) {
			int opPos;
			ArrayList<Graph.OpinionCluster> ocs = gr.calculateOpinionClustersByPopulation();
			double[] opDensityTemp = new double[exact + 1];
			for(Graph.OpinionCluster oc: ocs) {
				opPos = (int)Math.round(oc.opVal*exact);
				opDensityTemp[opPos] += 1;
			}
			for(int i = 0; i <= exact; i++) {
				opDensityVals[i] += opDensityTemp[i] / Constants._trials;
			}
		}
		
		public void writeRow(BufferedWriter f) throws IOException {
			String ret = "";
			for(int i = 0; i <= exact; i++) {
				ret += tickNum + "\t";
				ret += (double)i / exact + "\t";
				ret += opDensityVals[i] + "\n";
			}
			ret += "\n";
			f.write(ret);
		}
	}
	
	private class DCOCGroupDensity extends DataCollector {
		private final int exact = 100;
		private double[] opDensityVals = new double[exact + 1];
		
		public void appendData(Graph gr) {
			int opPos;
			ArrayList<Graph.OpinionCluster> ocs = gr.calculateOpinionClustersByGroup();
			double[] opDensityTemp = new double[exact + 1];
			for(Graph.OpinionCluster oc: ocs) {
				opPos = (int)Math.round(oc.opVal*exact);
				opDensityTemp[opPos] += 1;
			}
			for(int i = 0; i <= exact; i++) {
				opDensityVals[i] += opDensityTemp[i] / Constants._trials;
			}
		}
		
		public void writeRow(BufferedWriter f) throws IOException {
			String ret = "";
			for(int i = 0; i <= exact; i++) {
				ret += tickNum + "\t";
				ret += (double)i / exact + "\t";
				ret += opDensityVals[i] + "\n";
			}
			ret += "\n";
			f.write(ret);
		}
	}
}

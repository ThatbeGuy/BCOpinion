import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TickDataCollector {
	private final int tickNum;
	
	private final ArrayList<DataCollector> writers;
	
	public TickDataCollector(int tick) {
		tickNum = tick;
		
		writers = new ArrayList<DataCollector>();
		writers.add(new DCOpinionDensity());
	}
	
	public void process(Graph g) {
		for(DataCollector d: writers) d.appendData(g);
	}
	
	public void getData(int i, BufferedWriter f) throws IOException {
		writers.get(i).writeRow(f);
	}
	
	private abstract class DataCollector {
		public DataCollector() {}
		
		public abstract void appendData(Graph g);
		public abstract void writeRow(BufferedWriter f) throws IOException;
	}
	
	private class DCOpinionDensity extends DataCollector {
		public static final int id = 0;
		private final int exact = 100;
		private double[] opDensityVals = new double[exact + 1];
		
		public void appendData(Graph g) {
			int opPos;
			double[] opDensityTemp = new double[exact + 1];
			for(Agent a: g.getAgents()) {
				opPos = (int)Math.round(a.getOpinion()*exact);
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

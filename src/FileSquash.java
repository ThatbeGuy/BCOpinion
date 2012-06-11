import java.io.*;


public class FileSquash {
	private int iteration = 0;
	SquishFactory gen = new SquishFactory();
	int threads;
	public synchronized void squash(int runs) throws IOException, InterruptedException{
		threads = Constants.numThreads;
		for(String s : Constants.files){
			gen.generate(Constants.numThreads, runs, s);
			for(int i = 0; i < threads; i++){
				File file = new File(Constants._OUTPUT_PATH + s + "_final.txt");
				if(!file.exists()) {
					file.createNewFile();
				}
				OutputStream out = new FileOutputStream(file);
				for(int j = 0; j < threads ; j++){
					File copy = new File(fileIterator(s));
				    InputStream in = new FileInputStream(copy);
				    byte[] buf = new byte[1024 * 10];//[1024];
				    int len;
				    while ((len = in.read(buf)) > 0) {
				       out.write(buf, 0, len);
				    }
				    in.close();
				    copy.deleteOnExit();
				}
				this.iteration=0;
				out.close();
			}
		}
	}
	private String fileIterator(String str){
		iteration++;
		return Constants._OUTPUT_PATH + str + "t" + (iteration - 1);
	}
}

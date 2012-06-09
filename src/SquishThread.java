import java.io.*;
import java.math.*;


public class SquishThread extends Thread {
	int start;
	int end;
	OutputStream out;
	InputStream in;
	int self;
	String s;
	int iteration = 0;
	public SquishThread(int start, int end,/* OutputStream out, InputStream in*/ int self, String s){
		this.start = start;
		this.end = end;
		/*this.out = out;
		this.in = in;*/
		this.self = self;
		this.s = s;
		iteration = start;
	}

	public void run() {
		for(int i = start; i < end; i++){
			File file = new File(Constants._OUTPUT_PATH + s +"t" + self);
			if(!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int j = start; j < end; j++){
				File copy = new File(fileIterator(s));
				try {
					in = new FileInputStream(copy);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    byte[] buf = new byte[1024];
			    int len;
			    try {
					while ((len = in.read(buf)) > 0) {
					   try {
						out.write(buf, 0, len);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			    try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    copy.deleteOnExit();
			}
			this.iteration=start;
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private String fileIterator(String str){
		iteration++;
		return Constants._OUTPUT_PATH + str + (iteration - 1);
	}
}

package laptracker;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JPanel;

//import org.opencv.core.Mat;
//import org.opencv.core.Size;
//import org.opencv.videoio.VideoCapture;

class ContentPanel extends JPanel {
//	
//	private static final long serialVersionUID = -7955884606032800957L;
//	private VideoCapture videoCap = null;
//	public BufferedImage capImage = null;	
//	public Mat mImage = new Mat();
//	public Long timeBeforeRead, timeAfterRead, timeAfterMat;
//
//	public ContentPanel() {
//		super();
//		videoCap = new VideoCapture();
//		videoCap.open(1);
//		videoCap.release();  // added to allow new video code to run
//		// TODO : change cameras
//	}
//
//	public void close(){
//		videoCap.release();
//	}
//
//	public void paint(Graphics g) {
//
//		// if capturing grab an image
//		if (videoCap != null){
//			videoCap.read(mImage);
//			capImage = matToBufferedImage(mImage, null);
//		}
//
//		// draw captured image in panel
//		if (capImage != null){
//			float iw,ih,tw,th;
//			iw = capImage.getWidth();
//			ih = capImage.getHeight();
//			tw = this.getWidth();
//			th = this.getHeight();
//			float scale = Math.min(tw/iw,th/ih);
//			scale = scale == 0?1:scale;
//
//			g.setColor(Color.black);
//			g.fillRect(0, 0, (int)tw, (int)th);
//
//			int x,y,w,h;
//			w = (int)(iw*scale+0.5);
//			h = (int)(ih*scale+0.5);
//			x = (int) (tw/2-w/2);
//			y = (int) (th/2-h/2);
//			g.drawImage(capImage,x,y,w,h,this);
//
//		}
//	}
//
//
//	public int getFourCC(){
//		if (videoCap != null)
//			return (int) videoCap.get(6);
//		return -1;
//	}
//
//
//	public Size getInputSize() {
//		Size S = new Size();
//		S.width = videoCap.get(3);    // Acquire input size
//		S.height = videoCap.get(4);
//		return S;
//	}  	     	
//
//	public void changeCameraParam(int index, boolean selected) {
//		System.out.println("Setting #" + index + " previous value:" + videoCap.get(index));
//		if (selected)
//			videoCap.set(index, 1);
//		else
//			videoCap.set(index, -1);
//
//		System.out.println("\t current value:" + videoCap.get(index));
//	}		
//
//	public void changeCameraParam(int index, int value){
//		System.out.println("Setting #" + index + " previous value:" + videoCap.get(index));
//		videoCap.set(index, value);
//		System.out.println("\t current value:" + videoCap.get(index));
//
//	}		
//	public void loadPrefs() {
//		// create a Preferences instance (somewhere later in the code)
//		File fp = new File("./pref.txt");
//		Scanner sc;
//		try {
//			sc = new Scanner(fp, "UTF-8");
//			sc.useDelimiter("\n"); // regex matching nothing 
//
//			for(int i=0;i< 37; i++ ){
//				String text = sc.next();
//				videoCap.set(i,Double.parseDouble(text));
//			}
//			sc.close(); 
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//	}		
//
//	public void savePrefs(){
//		File fp = new File("./pref.txt");
//		FileWriter fw = null;
//		try {
//			fw = new FileWriter(fp.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			for(int i=0;i< 37; i++ ){
//				bw.write(Double.toString(videoCap.get(i)) + "\n");
//			}
//			bw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//	}
//
//	//    	    VideoWriter outputVideo = new VideoWriter();  
//	//    	    CvVideoWriter videoWriter = cvCreateVideoWriter("out.avi", -1, fps, csize.byValue(), 1);
//
//	/**  
//	 * Converts/writes a Mat into a BufferedImage.  
//	 *  
//	 * @param matrix Mat of type CV_8UC3 or CV_8UC1  
//	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
//	 */  
//	public BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg)
//	{
//		if ( matrix != null ) { 
//			int cols = matrix.cols();  
//			int rows = matrix.rows();  
//			int elemSize = (int)matrix.elemSize();  
//			byte[] data = new byte[cols * rows * elemSize];  
//			int type;  
//			matrix.get(0, 0, data);  
//			switch (matrix.channels()) {  
//			case 1:  
//				type = BufferedImage.TYPE_BYTE_GRAY;  
//				break;  
//			case 3:  
//				type = BufferedImage.TYPE_3BYTE_BGR;  
//				// bgr to rgb  
//				byte b;  
//				for(int i=0; i<data.length; i=i+3) {  
//					b = data[i];  
//					data[i] = data[i+2];  
//					data[i+2] = b;  
//				}  
//				break;  
//			default:  
//				return null;  
//			}  
//
//			// Reuse existing BufferedImage if possible
//			if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
//				bimg = new BufferedImage(cols, rows, type);
//			}        
//			bimg.getRaster().setDataElements(0, 0, cols, rows, data);
//		} else { // mat was null
//			bimg = null;
//		}
//		return bimg;  
//	}
//	
//	public Long getTimeBeforeRead(){
//		return(timeBeforeRead);				// Used to check the video reading time
//	}
//	
//	public Long getTimeAfterRead(){
//		return(timeAfterRead);				// Used to check the video reading time
//	}
//	
//	public Long getTimeAfterMat(){
//		return(timeAfterMat);				// Used to check the video reading time
//	}
//
//


}

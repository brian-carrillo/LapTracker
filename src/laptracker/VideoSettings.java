package laptracker;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JPanel;


import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacpp.videoInputLib.videoInput;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

class VideoSettings extends JPanel {
	private static final long serialVersionUID = -7955884606032800957L;
	private int videoDevices;
	private static int WEBCAM_DEVICE_INDEX;     // Change depending on PC, sometime 0 sometimes 
	public VideoCapture videoCap = null;
	public Frame mFrame = null;
	public Mat mImage = new Mat();
	public BufferedImage capImage = null;
	public boolean sizeChange = false;
	OpenCVFrameConverter converter = new OpenCVFrameConverter.ToMat();
	private Java2DFrameConverter buffConverter = new Java2DFrameConverter();
	 protected double inverseGamma = 1.0;

	public VideoSettings() {
		super();
		// Select the proper camera
		videoDevices = videoInput.listDevices();
		for (int i = 0; i < videoDevices; i++) {
            String info = videoInput.getDeviceName(i).getString();
            System.out.println(info);
            if(info.startsWith("Microsoft")){                // choose which camera to use
            	WEBCAM_DEVICE_INDEX = i;
            }
        }
		videoCap = new VideoCapture();
		videoCap.open(WEBCAM_DEVICE_INDEX);
		//this.loadPrefs();
		this.loadSettings();
		// TODO : change cameras
	}

	public void close(){
		videoCap.release();
		//cFrame.dispose();
	}
	
	public void readImage(){
	// This method reads the data from the camera and converts it to a frame and a buffered image
	// The frame is used when recording the video and the buffered image is used when displaying the video to the screen
		if(videoCap!=null){
			videoCap.read(mImage);
			// convert mat to frame
			mFrame = converter.convert(mImage);
			// convert frame to buffered image
			capImage = buffConverter.getBufferedImage(mFrame, Java2DFrameConverter.getBufferedImageType(mFrame) == BufferedImage.TYPE_CUSTOM ? 1.0 : inverseGamma, false, null);
		}
	}

	public void paint(Graphics g) {
		// draw captured image in panel
		if (capImage != null){
			float iw,ih,tw,th;
			iw = capImage.getWidth();
			ih = capImage.getHeight();
			tw = this.getWidth();
			th = this.getHeight();
			float scale = Math.min(tw/iw,th/ih);
			scale = scale == 0?1:scale;

			g.setColor(Color.black);
			g.fillRect(0, 0, (int)tw, (int)th);

			int x,y,w,h;
			w = (int)(iw*scale+0.5);
			h = (int)(ih*scale+0.5);
			x = (int) (tw/2-w/2);
			y = (int) (th/2-h/2);
			g.drawImage(capImage,x,y,w,h,this);

		}
	}
	public void startResizing() {
		sizeChange = true;
	}
	public void doneResizing() {
		sizeChange = false;
	}

	/*public Size getInputSize() {
		Size S = new Size();
		S.width = videoCap.get(3);    // Acquire input size
		S.height = videoCap.get(4);
		return S;
	}  	 */    	

	public void changeCameraParam(int index, boolean selected) {
		System.out.println("Setting #" + index + " previous value:" + videoCap.get(index));
		if (selected)
			videoCap.set(index, 1);
		else
			videoCap.set(index, -1);
		System.out.println("\t current value:" + videoCap.get(index));
	}		

	public void changeCameraParam(int index, int value){
		System.out.println("Setting #" + index + " previous value:" + videoCap.get(index));
		videoCap.set(index, value);
		System.out.println("\t current value:" + videoCap.get(index));

	}		
	public void loadPrefs() {
		// create a Preferences instance (somewhere later in the code)
		File fp = new File("./pref.txt");
		Scanner sc;
		try {
			sc = new Scanner(fp, "UTF-8");
			sc.useDelimiter("\n"); // regex matching nothing 
			for(int i=0;i< 37; i++ ){
					String text = sc.next();
					videoCap.set(i,Double.parseDouble(text));
					System.out.println(i);
					System.out.println(text);
			}
			sc.close(); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void loadSettings() {
		// create a Preferences instance (somewhere later in the code)
		StringBuilder settings = new StringBuilder();
		File fp = new File("./settings.txt");
		Scanner sc;
		try {
			sc = new Scanner(fp, "UTF-8");
			//sc.useDelimiter("\n"); // regex matching nothing 
			readSettings:
			while(true){
					String index = sc.next();
					if (index.equals("eof")){
						break readSettings;
					}
					String text = sc.next();
					videoCap.set(Integer.parseInt(index),Double.parseDouble(text));
			}
			sc.close(); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}	

	public void savePrefs(){
		File fp = new File("./pref.txt");
		FileWriter fw = null;
		try {
			fw = new FileWriter(fp.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i=0;i< 37; i++ ){
				bw.write(Double.toString(videoCap.get(i)) + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public void saveSettings(){
		File fp = new File("./settings.txt");
		FileWriter fw = null;
		try {
			fw = new FileWriter(fp.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("15 -11 10 30 28 0 12 0 11 0 ");
			bw.write("15 " + Double.toString(videoCap.get(15)) + " ");
			bw.write("10 " + Double.toString(videoCap.get(10)) + " ");
			bw.write("28 " + Double.toString(videoCap.get(28)) + " ");
			bw.write("12 " + Double.toString(videoCap.get(12)) + " ");
			bw.write("13 " + Double.toString(videoCap.get(13)) + " ");
			bw.write("11 " + Double.toString(videoCap.get(11)) + " eof");
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	

	//    	    VideoWriter outputVideo = new VideoWriter();  
	//    	    CvVideoWriter videoWriter = cvCreateVideoWriter("out.avi", -1, fps, csize.byValue(), 1);

	/**  
	 * Converts/writes a Mat into a BufferedImage.  
	 *  
	 * @param matrix Mat of type CV_8UC3 or CV_8UC1  
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
	 */  
	/*public BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg)
	{
		if ( matrix != null ) { 
			int cols = matrix.cols();  
			int rows = matrix.rows();  
			int elemSize = (int)matrix.elemSize();  
			byte[] data = new byte[cols * rows * elemSize];  
			int type;  
			matrix.get(0, 0, data);  
			switch (matrix.channels()) {  
			case 1:  
				type = BufferedImage.TYPE_BYTE_GRAY;  
				break;  
			case 3:  
				type = BufferedImage.TYPE_3BYTE_BGR;  
				// bgr to rgb  
				byte b;  
				for(int i=0; i<data.length; i=i+3) {  
					b = data[i];  
					data[i] = data[i+2];  
					data[i+2] = b;  
				}  
				break;  
			default:  
				return null;  
			}  

			// Reuse existing BufferedImage if possible
			if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
				bimg = new BufferedImage(cols, rows, type);
			}        
			bimg.getRaster().setDataElements(0, 0, cols, rows, data);
		} else { // mat was null
			bimg = null;
		}
		return bimg;  
	}*/



}

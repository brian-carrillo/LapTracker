package laptracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
//import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.core.Size;
//import org.opencv.videoio.VideoCapture;
import org.bytedeco.javacpp.avcodec;
//import org.bytedeco.javacpp.opencv_core.Mat;
//import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
//import com.googlecode.javacv.cpp.opencv_highgui;
//import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;
//import com.googlecode.javacv.cpp.opencv_core.IplImage;
//import org.bytedeco.javacv.OpenCVFrameConverter;

import laptracker.utils.RecordButton;
import laptracker.utils.SerialInputHandler;
import laptracker.utils.TwoWaySerialComm;
//import laptracker.utils.Fourcc;;

public class MyFrame extends CanvasFrame implements SerialInputHandler{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6679073889115877966L;
	/**
	 * 
	 */
	private static VideoSettings contentPane;
	private RecordButton startRecordingButton;
	private JButton loadPref;
	PlaceholderTextField videoNameField;	
	private JSlider focusSlider;
	private JSlider brightnessSlider;
	private JSlider exposureSlider;
	private JSlider saturationSlider;
	private JSlider contrastSlider;
	private JSlider zoomSlider;
	private JSlider wBalanceSlider;
	private JCheckBox exposureCheck;
	private JCheckBox wBalanceCheck;
	private JCheckBox trueColorCheck;
	private JMenuBar menuBar;
	
	private JToggleButton arduinoDataCheck;
	private JButton forceDataCheck;
	private JButton imuLDataCheck;
	private JButton imuRDataCheck;
	private JButton serialCheck;
	
	private TwoWaySerialComm serial;
	boolean sensorConnected = false;
	private boolean firstStart = true;
	
	// Webcam Recorder variables ------------------------------------------
	FFmpegFrameRecorder recorder;
	
    private static int FRAME_RATE = 30;
   // private static int GOP_LENGTH_IN_FRAMES = 60;
    private static int VIDEO_BIT_RATE = 2000000;

    //private static long startTime = 0;
    private static long videoTS = 0;
    
    static int captureWidth = 640;//320; //960; //800; //1280;
    static int captureHeight = 480;//240; //544; //448; //720;
	private static videoUpdate vu;
    
    
    //---------------------------------------------------------------------------------
    
    
	//public int saveVideoWidth = 320;    
	//public int saveVideoHeight = 240;
	
	protected int dataSleepLength= 5; // 4ms data interval
	//protected int videoSleepLength=40; // 20ms data interval

	private boolean trackingEnabled = true;
	private JTextArea trackerString;
	private JTextArea forceString;
	
	private File outputFile = null;
	private BufferedWriter fileWriter = null;
	private String dateTime;
	//private PLSVideoWriter videoWriter = null;
	private Long frameCount =0l, currTime=0l, diff=(long) 01;
	private Long prevTime=(long) 01;
	private boolean isRunning = true;
	
	private boolean recordingNow = false;
	private boolean debug = false;
	
	
	// Space Mouse Variables-----------------------------------------
	private SpaceMouse s = new SpaceMouse();
	private boolean firstPoll = true;
	private boolean spaceMouseConnected = false;
	String Fstr = new String("0,0,0,0,0,0");  // The string used to write the force data
	boolean startPolling = false;
	int pollCounter = 5;
	boolean firstRun = true;
	boolean secondRun = false;
	float[] F1, F2;               // Poll output for first and second run
	float[] Avg = {0,0,0,0,0,0}; // Average of F1 and F2
	float[] F = {0,0,0,0,0,0};   // Force calculated from Out
	
	float a = (float) 0.373063, b = (float) 16.1425; // coefficiants of the regression analysis for Fz
	
	// Calibration variables
	private File calibrationFile = null;	// Create calibration file to store data
	private BufferedWriter calFileWriter = null;  // Calibration file writer
	boolean calibrationOn = false;   // Used to stop arduino from connecting once we stop recording
	boolean calSetup = false;
	ImageIcon image;
	JLabel calLabel;
	JPanel calPanel;
	JPanel calPanel1;
	JPanel calPanel2;
	JFrame calFrame;
	JLabel calText;
	int countDown=5;
	
	// Data Check Variables
	private boolean forceDataReceived = false;
	private boolean arduinoDataReceived = false;
	private boolean imuLOK = false;
	private boolean imuROK = false;
	private int checkHeader = 0;
	private JPanel iconPanel;
	private JLabel forceIcon;
	private JLabel positionIcon;
	private JLabel imuLeftIcon;
	private JLabel imuRightIcon;
	private JLabel serialIcon;
	
	private ImageIcon redCirc;
	private ImageIcon greenCirc;
	
	// Loading Screen Variables
	private boolean loading  = false;
	private JFrame loadingFrame;
	private JPanel loadingPanel;
	private JLabel loadingText;
	
	
	private MyFrame self;
	
	private ChangeListener cameraSliderListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			JSlider slider = (JSlider) e.getSource();
			if (slider.equals(brightnessSlider)) {
				contentPane.changeCameraParam(10,slider.getValue());

			} else if (slider.equals(contrastSlider)) {
				contentPane.changeCameraParam(11,slider.getValue());

			} else if (slider.equals(exposureSlider)) {
				contentPane.changeCameraParam(15,slider.getValue());
				
			} else if (slider.equals(saturationSlider)) {
				contentPane.changeCameraParam(12,slider.getValue());
				
			} else if (slider.equals(zoomSlider)){
				contentPane.changeCameraParam(27,slider.getValue());
				
			} else if (slider.equals(wBalanceSlider)) {
				contentPane.changeCameraParam(17,slider.getValue());
				
			}
		}
	};
	
	private ActionListener loadPrefListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			contentPane.loadSettings();
		}
	};

	
	private ActionListener startRecordingListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			if (startRecordingButton.isSelected()) {
				startRecordingButton.setText("Stop Recording");
				self.menuBar.setVisible(false);
				frameCount = 0l;
		        Date dNow = new Date( );
		        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd'_'hh.mm.ss");
		        dateTime = ft.format(dNow);
		        //contentPane.savePrefs();
		        contentPane.saveSettings();
		        videoNameField.getText();
				outputFile = new File(videoNameField.getText() + "_" + dateTime + ".csv");
				try {
					fileWriter = new BufferedWriter(new FileWriter(outputFile));
					fileWriter.write("Time,");
					fileWriter.write("VideoFrame,");
					
					fileWriter.write("F tX,");
					fileWriter.write("F tY,");
					fileWriter.write("F tZ,");
					fileWriter.write("F rX,");
					fileWriter.write("F rY,");
					fileWriter.write("F rZ,");
					
					fileWriter.write("IMU-L Status,");
					fileWriter.write("IMU-R Status,");

					fileWriter.write("L aX,");
					fileWriter.write("L aY,");
					fileWriter.write("L aZ,");
					fileWriter.write("L gX,");
					fileWriter.write("L gY,");
					fileWriter.write("L gZ,");
					fileWriter.write("L mX,");
					fileWriter.write("L mY,");
					
					fileWriter.write("R aX,");
					fileWriter.write("R aY,");
					fileWriter.write("R aZ,");
					fileWriter.write("R gX,");
					fileWriter.write("R gY,");
					fileWriter.write("R gZ,");
					fileWriter.write("R mX,");
					fileWriter.write("R mY,");
					
					fileWriter.write("Arduino Time,");	
					
					fileWriter.newLine();					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				
				// Original video writing code, replaced with FFmpeg Recorder
				/*videoWriter = new PLSVideoWriter();
				String filename = new String(videoNameField.getText() + "_" + dateTime + ".avi");
				Fourcc fcc = new Fourcc("x264");
				int fourCC = fcc.toInt();
				int FPS = 1000/videoSleepLength;
				Size s = contentPane.getInputSize();
				videoWriter.open(filename, -1, FPS, s);*/
				
				//New Video Writing Code
			
				recorder = new FFmpegFrameRecorder(videoNameField.getText() + "_" + dateTime + ".avi",captureWidth, captureHeight);
		        //recorder.setInterleaved(true);
		    
		        recorder.setVideoOption("preset", "ultrafast");
		        // Constant Rate Factor (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
		        recorder.setVideoOption("crf", "28");
		        // 2000 kb/s, reasonable "sane" area for 720
		        recorder.setVideoBitrate(VIDEO_BIT_RATE);
		        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		        recorder.setFormat("avi");
		        // FPS (frames per second)
		        recorder.setFrameRate(FRAME_RATE);
		        try{
		        	recorder.start();
		        }catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        serial.startDevice();  // Resets the arduino
		        
		        new calibration().start(); // Begin calibration thread
		    
		        // This is where the recording used to start, it has been moved to the calibration thread
				//startTime = (System.nanoTime())/1000;  // Time in micro seconds
				//recordingNow = true;

			} else {
				recordingNow = false;
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(500);				// Sleep to allow time for arduino to read the stop code
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
				try{
					Thread.sleep(100);     // give time for the video thread to stop recording
        			recorder.stop();
        		}catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				startRecordingButton.setText("Start Recording");
				self.menuBar.setVisible(true);
			}
		}
	};
	private ActionListener menuButtonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if("1280x720".equals(e.getActionCommand())){
				
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					contentPane.startResizing();
					MyFrame.captureWidth = 1280;
					MyFrame.captureHeight = 720;
					//self.setCanvasSize(captureWidth,captureHeight);
					self.setBounds(100, 100, captureWidth+245, captureHeight+125);
					contentPane.videoCap.set(3, captureWidth);
					contentPane.videoCap.set(4, captureHeight);
					contentPane.doneResizing();

			} else if("960x544".equals(e.getActionCommand())){
				
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					contentPane.startResizing();;
					MyFrame.captureWidth = 960;
					MyFrame.captureHeight = 544;
					//self.setCanvasSize(captureWidth,captureHeight);
					self.setBounds(100, 100, captureWidth+245, captureHeight+125);
					contentPane.videoCap.set(3, captureWidth);
					contentPane.videoCap.set(4, captureHeight);
					contentPane.doneResizing();

			} else if("800x448".equals(e.getActionCommand())){
				
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					contentPane.startResizing();;
					MyFrame.captureWidth = 800;
					MyFrame.captureHeight = 448;
					//self.setCanvasSize(captureWidth,captureHeight);
					self.setBounds(100, 100, captureWidth+245, captureHeight+125);
					contentPane.videoCap.set(3, captureWidth);
					contentPane.videoCap.set(4, captureHeight);
					contentPane.doneResizing();
				
			} else if("640x480".equals(e.getActionCommand())){
				
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					contentPane.startResizing();;
					MyFrame.captureWidth = 640;
					MyFrame.captureHeight = 480;
					//self.setCanvasSize(captureWidth,captureHeight);
					self.setBounds(100, 100, captureWidth+245, captureHeight+125);
					contentPane.videoCap.set(3, captureWidth);
					contentPane.videoCap.set(4, captureHeight);
					contentPane.doneResizing();
				
			} else if("Fast".equals(e.getActionCommand())){
				MyFrame.VIDEO_BIT_RATE = 2000000;
				MyFrame.FRAME_RATE = 30;
			} else if("Slow".equals(e.getActionCommand())){
				MyFrame.VIDEO_BIT_RATE = 1000000;
				MyFrame.FRAME_RATE = 15;
			}
		}
	};
	private ChangeListener focusSliderListener = new ChangeListener() {
		public void stateChanged(ChangeEvent arg0) {
			contentPane.changeCameraParam(28,focusSlider.getValue());
		}
	};
	
	private ActionListener cameraCheckListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JCheckBox check = (JCheckBox) e.getSource();
			if (check.equals(exposureCheck)) {
				System.out.println("AutoExposure");
				contentPane.changeCameraParam(21,check.isSelected());
			} else if (check.equals(wBalanceCheck)) {
				System.out.println("AutoWhiteBalance");
			} else if (check.equals(trueColorCheck)) {
				System.out.println("AutoColor");
				try {
					int i;
					if (check.isSelected())
						i = 0;
					else
						i = 1;
					Runtime.getRuntime().exec("reg add HKCU\\Software\\Microsoft\\LifeCam\\ /v TruecolorOff /t REG_DWORD /d " + i + " /f");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	};
	//private JTextArea fpsString;


  /**
  * Launch the application.
  */
	public static void main(String[] args){
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		MyFrame frame = new MyFrame(); // Begin the main GUI and set visible
		frame.setVisible(true);
		frame.createContent();   // Initialize the video camera, and load all settings
		
		frame.startThreads();		// begin all threads
		contentPane.loadSettings(); // reload the video settings
		
	}


/**
  * Create the frame.
  */
    public MyFrame() {
    	super("New Window");
    	loading = true;
    	//new loadingWindow().start();
		self = this;
    	
    	
    	self.addWindowListener( new WindowAdapter()
    	{
    	    public void windowClosing(WindowEvent e)
    	    {
    	    	try {
					serial.disconnect();
				} catch (java.lang.Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    	    	isRunning = false;
				contentPane.close();
    	    }
    	});
    	
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        

        this.setLayout(new BorderLayout());
        
        menuBar = new JMenuBar();
        JMenu resMenu = new JMenu("Display Resolution");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem button = new JRadioButtonMenuItem("1280x720");
        button.addActionListener(menuButtonListener);
        group.add(button);
        resMenu.add(button);
        
        button = new JRadioButtonMenuItem("960x544");
        button.addActionListener(menuButtonListener);
        group.add(button);
        resMenu.add(button);
        
        button = new JRadioButtonMenuItem("800x448");
        button.addActionListener(menuButtonListener);
        group.add(button);
        resMenu.add(button);
        
        button = new JRadioButtonMenuItem("640x480");
        button.addActionListener(menuButtonListener);
        button.setSelected(true);
        group.add(button);
        resMenu.add(button);
        
        menuBar.add(resMenu);
        
        // Create menu option for selecting recording speed
        JMenu fpsMenu = new JMenu("Recording Speed");
        ButtonGroup group2 = new ButtonGroup();
        JRadioButtonMenuItem speed1 = new JRadioButtonMenuItem("Fast");
        speed1.addActionListener(menuButtonListener);
        group2.add(speed1);
        fpsMenu.add(speed1);
        
        speed1 = new JRadioButtonMenuItem("Slow");
        speed1.addActionListener(menuButtonListener);
        group2.add(speed1);
        fpsMenu.add(speed1);
        
        menuBar.add(fpsMenu);
        
        this.add(menuBar,BorderLayout.NORTH);
        
        //this.add(contentPane,BorderLayout.CENTER);
  
        Box buttonPane = new Box(BoxLayout.X_AXIS);
        startRecordingButton = new RecordButton("Start Recording");
        startRecordingButton.addActionListener(startRecordingListener);
        startRecordingButton.setPreferredSize(new Dimension(130,22));
        buttonPane.add(startRecordingButton);
        
        videoNameField = new PlaceholderTextField(30);
        videoNameField.setPlaceholder("Video title");

        videoNameField.setMaximumSize(new Dimension(200,22));
        buttonPane.add(videoNameField);
        
        buttonPane.add(Box.createHorizontalGlue());
        
        focusSlider = new JSlider(0, 40);
        focusSlider.addChangeListener(focusSliderListener);
        focusSlider.setMinorTickSpacing(5);
        focusSlider.setPaintTicks(true);
        focusSlider.setToolTipText("Slide to adjust focus");
        buttonPane.add(focusSlider);
        
        
        this.add(buttonPane,BorderLayout.SOUTH);
        ///--------------------------------------------
        
        
        // Create thread to initiate recorder
        new Thread() {
        	public void run(){
        		recorder = new FFmpegFrameRecorder("Temp.avi",captureWidth, captureHeight);
        	}		
        }.start();
        
        
        if (trackingEnabled) {
        	Box toolPane  = new Box(BoxLayout.Y_AXIS);
        	
//        	forceDataCheck = new JButton("Force Data");
//        	forceDataCheck.setPreferredSize(new Dimension(200,22));
//        	forceDataCheck.setBackground(Color.RED);
//        	forceDataCheck.setEnabled(false);
//        	forceDataCheck.setForeground(Color.black);
//        	
//        	arduinoDataCheck = new JToggleButton("Position Data");
//        	arduinoDataCheck.setPreferredSize(new Dimension(200,22));
//        	arduinoDataCheck.setBackground(Color.RED);
//        	arduinoDataCheck.setForeground(Color.black);
//        	arduinoDataCheck.setEnabled(false);
//        	
//        	imuLDataCheck = new JButton("IMU Left");
//        	imuLDataCheck.setPreferredSize(new Dimension(200,22));
//        	imuLDataCheck.setBackground(Color.RED);
//        	imuLDataCheck.setForeground(Color.black);
//        	imuLDataCheck.setEnabled(false);
//        	
//        	imuRDataCheck = new JButton("IMU Right");
//        	imuRDataCheck.setPreferredSize(new Dimension(200,22));
//        	imuRDataCheck.setBackground(Color.RED);
//        	imuRDataCheck.setForeground(Color.black);
//        	imuRDataCheck.setEnabled(false);
//        	
//        	serialCheck = new JButton("Serial Connection");
//        	serialCheck.setPreferredSize(new Dimension(200,22));
//        	serialCheck.setBackground(Color.RED);
//        	serialCheck.setForeground(Color.black);
//        	serialCheck.setEnabled(false);
//        
//        
//        	toolPane.add(Box.createRigidArea(new Dimension(0,5)));
//        	toolPane.add(forceDataCheck);
//        	toolPane.add(Box.createRigidArea(new Dimension(0,5)));
//        	toolPane.setBorder(new MatteBorder(0,0,0,1,Color.gray));
//        	toolPane.add(arduinoDataCheck);
//        	toolPane.add(Box.createRigidArea(new Dimension(0,5)));
//        	toolPane.add(imuLDataCheck);
//        	toolPane.add(Box.createRigidArea(new Dimension(0,5)));
//        	toolPane.add(imuRDataCheck);
//        	toolPane.add(Box.createRigidArea(new Dimension(0,5)));
//        	toolPane.add(serialCheck);
        	
        	
        	
        	//Box cameraPane = new Box(BoxLayout.PAGE_AXIS);
        	
        	redCirc = new ImageIcon("redCircle.jpg");
        	greenCirc = new ImageIcon("greenCircle.jpg");
        	forceIcon = new JLabel("Force Data", redCirc, JLabel.LEFT);
        	positionIcon = new JLabel("Position Data", redCirc, JLabel.LEFT);
        	imuLeftIcon = new JLabel("IMU Left", redCirc, JLabel.LEFT);
        	imuRightIcon = new JLabel("IMU Right", redCirc, JLabel.LEFT);
        	serialIcon = new JLabel("Serial Connection", redCirc, JLabel.LEFT);
			iconPanel = new JPanel();
			forceIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
			
			
			iconPanel.add(forceIcon);
			iconPanel.add(Box.createRigidArea(new Dimension(0,5)));
			iconPanel.add(positionIcon);
			iconPanel.add(Box.createRigidArea(new Dimension(0,5)));
			iconPanel.add(imuLeftIcon);
			iconPanel.add(Box.createRigidArea(new Dimension(0,5)));
			iconPanel.add(imuRightIcon);
			iconPanel.add(Box.createRigidArea(new Dimension(0,5)));
			iconPanel.add(serialIcon);
			
			
			iconPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(Box.createRigidArea(new Dimension(0,5)));
        	toolPane.add(iconPanel);
        	
        	JLabel l = new JLabel("True Color");
        	Box miniPane = new Box(BoxLayout.X_AXIS);
        	l.setAlignmentX(Component.CENTER_ALIGNMENT);
        	miniPane.add(l);
        	miniPane.add(Box.createHorizontalGlue());
        	trueColorCheck = new JCheckBox();
        	trueColorCheck.addActionListener(cameraCheckListener);
        	miniPane.add(trueColorCheck);
        	miniPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(miniPane);
        	
        	l = new JLabel("Brightness");
        	l.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(l);
        	brightnessSlider = new JSlider(30,255);
        	brightnessSlider.addChangeListener(cameraSliderListener);
        	brightnessSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(brightnessSlider);
        	
        	
        	//toolPane.add(Box.createVerticalStrut(20));
        	l = new JLabel("White Balance");
        	l.setAlignmentX(Component.LEFT_ALIGNMENT);
        	//toolPane.add(l);
        	miniPane = new Box(BoxLayout.X_AXIS);
        	wBalanceSlider = new JSlider(2800,10000);
        	wBalanceSlider.addChangeListener(cameraSliderListener);
        	//miniPane.add(wBalanceSlider);
        	wBalanceCheck = new JCheckBox();
        	wBalanceCheck.addActionListener(cameraCheckListener);
        	//miniPane.add(wBalanceCheck);
        	miniPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(miniPane);
        	
        	
        	toolPane.add(Box.createVerticalStrut(20));
        	l = new JLabel("Saturation");
        	l.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(l);
        	saturationSlider = new JSlider(0,200);
        	saturationSlider.addChangeListener(cameraSliderListener);
        	saturationSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(saturationSlider);
        	
        	
        	toolPane.add(Box.createVerticalStrut(20));
        	l = new JLabel("Exposure");
        	l.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(l);
        	miniPane = new Box(BoxLayout.X_AXIS);
        	exposureSlider = new JSlider(-11,1);
        	exposureSlider.addChangeListener(cameraSliderListener);
        	miniPane.add(exposureSlider);
        	exposureCheck = new JCheckBox();
        	exposureCheck.addActionListener(cameraCheckListener);
        	//miniPane.add(exposureCheck);
        	miniPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(miniPane);
        	
        	
        	toolPane.add(Box.createVerticalStrut(20));
        	l = new JLabel("Contrast");
        	l.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(l);
        	contrastSlider = new JSlider(0,10);
        	contrastSlider.addChangeListener(cameraSliderListener);
        	contrastSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(contrastSlider);
        	
        	toolPane.add(Box.createVerticalStrut(20));
        	l = new JLabel("Zoom");
        	l.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(l);
        	zoomSlider = new JSlider(0,9);
        	zoomSlider.addChangeListener(cameraSliderListener);
        	zoomSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(zoomSlider);
        	
        	
        	toolPane.add(Box.createVerticalStrut(20));
        	loadPref = new JButton("Load Preferences");
        	loadPref.addActionListener(loadPrefListener);
        	loadPref.setPreferredSize(new Dimension(130,22));
        	loadPref.setAlignmentX(Component.LEFT_ALIGNMENT);
        	toolPane.add(loadPref);
        	
        	
        	toolPane.add(Box.createVerticalGlue()); 
        	
        	
        	
        	
        	
        	toolPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        	
        	JTabbedPane tabPanel = new JTabbedPane();
        	tabPanel.addTab("Data", toolPane);
        	//tabPanel.addTab("Camera", cameraPane);
        	this.add(tabPanel, BorderLayout.WEST);
        	
        	this.setBounds(0, 100, captureWidth+245, captureHeight+125);
        	loading = false;
        }
    }
    
    private void startThreads(){
    	// This method starts the video, serial, and data checker threads
    	new videoUpdate().start();
    	checkSerial.start();
    	new dataCheck().start();
    }
    
    private void createContent(){
    	// This method initializes the video camera
    	contentPane = new VideoSettings();
        contentPane.videoCap.set(3,captureWidth);
        contentPane.videoCap.set(4,captureHeight);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(contentPane, BorderLayout.CENTER);
        
        zoomSlider.setValue((int) contentPane.videoCap.get(27));   // set the slider to starting value displayed in the settings file
        focusSlider.setValue((int) contentPane.videoCap.get(28));    
        brightnessSlider.setValue((int) contentPane.videoCap.get(10));
        wBalanceSlider.setValue((int) contentPane.videoCap.get(17));
        saturationSlider.setValue((int) contentPane.videoCap.get(12));
        exposureSlider.setValue((int) contentPane.videoCap.get(15));
        contrastSlider.setValue((int) contentPane.videoCap.get(11));
        
    }
 /* This thread paints the video to the video panel, and records the video being captured
  * 
  */
    public class videoUpdate extends Thread{
        @Override
        public void run() {
        	try {
				Thread.sleep(200);					// give time for the frame to build
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        	boolean hasStarted = false;   // Initialize flags used while recording
        	boolean setup = true;
        	
            while(isRunning){ 
            	if(setup){       // set default size on startup
            		self.setSize(captureWidth+244, captureHeight+124);
            		setup = false;
            	}
            	if(!contentPane.sizeChange){  // Paint image to screen only if not resizing
            		contentPane.readImage();
            		contentPane.repaint();		// grab image from camera and convert from mat to frame
            	}
            	if (recordingNow){   // Enter this if statement only if the recording button has been pressed
            		
            		hasStarted = true;
            		
            		try{
            			// Check if frames a available and then save them using frame recorder
            			if(contentPane.mFrame != null)
            			recorder.record(contentPane.mFrame);
            			frameCount++;
            			//System.out.println(System.nanoTime()/1000);
            		}catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
						e.printStackTrace();
					}
            	}
            }
            contentPane.close();
        } 
    }
    /* This thread begins the calibration, and writes the register information from the sensors
     * 
     */
    class calibration extends Thread{
    	@Override
    	public void run(){
    		// Create calibration file and save sensor settings
			calibrationFile = new File(videoNameField.getText() + "_" + dateTime + "_calibration" + ".csv");
			try {
				calFileWriter = new BufferedWriter(new FileWriter(calibrationFile));
				calFileWriter.write("Data Rate (ms),");
				calFileWriter.write("R_Mouse_ID,");
				calFileWriter.write("L_Mouse_ID,");
				calFileWriter.write("IMU-R ID,");
				calFileWriter.write("IMU-L ID,");
				calFileWriter.write("R_Mouse_Config1,");
				calFileWriter.write("L_Mouse_Config1,");
				calFileWriter.write("R_Acc_Ctrl,");
				calFileWriter.write("L_Acc_Ctrl,");
				calFileWriter.write("R_Gyro_Ctrl,");
				calFileWriter.write("L_Gyro_Ctrl,");
				calFileWriter.write("IMU-L Status,");
				calFileWriter.write("IMU-R Status,");
				
				calFileWriter.newLine();
				calSetup = true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    		// Begin calibration 
    		calibrationOn = true;						// This flag will allow for the serial thread to connect
    		System.out.println("calOn");
    		while(!serial.connectedPort){				// Wait for serial port to connect
    			try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		if(serial.connectedPort){
    			// disable the recording button while calibrating
    			startRecordingButton.setEnabled(false);
    			
    			// Create calibration frame to display instructions for calibration
    			image = new ImageIcon("ConfigImage3.jpg");
    			calLabel = new JLabel("", image, JLabel.CENTER);
    			calPanel = new JPanel();
    			calPanel1 = new JPanel();
    			calPanel2 = new JPanel();
    			calFrame = new JFrame();
    			calText = new JLabel("<html><div style='text-align: center;'>To begin Calibration<br>fully insert the instruments as shown above<html>");
    			Font font = new Font("Arial",Font.PLAIN,20);
    			calText.setFont(font);
    			calPanel1.add(calLabel);
    			calPanel1.setSize(448, 311);
    			calPanel2.add(calText);
    			calPanel2.setSize(448, 100);
    			calFrame.add(calPanel1, BorderLayout.NORTH);
    			calFrame.add(calPanel2, BorderLayout.SOUTH);
    			calFrame.setVisible(true);
    			calFrame.setBounds(100,100,448,411);
    			
    			calFrame.addWindowListener(new WindowAdapter(){
    				public void windowClosing(WindowEvent e)
    	    	    {
    					try {
							calFileWriter.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
    					calibrationOn = false;
    					startRecordingButton.setEnabled(true);
    					startRecordingButton.doClick();
    	    	    }
    			});
			
    			System.out.println("begin cal");
    			// Tell user how to calibrate
    			for(int i=0; i<12; i++){
    				if(calibrationOn){
    					try {
    						Thread.sleep(250);
    					}catch (InterruptedException e1) {
    			    		e1.printStackTrace();
    			    	}	
    				}else{
    					break;
    				}
    			} 
    			startTime = (System.nanoTime())/1000;  			// Time in micro seconds
				recordingNow = true;
    			if(calibrationOn){
    				for(int i=3;i>=0;i--){
    					calText.setText("<html><div style='text-align: left;'>HOLD FOR: "+i+"<html>");
    					try {
    						Thread.sleep(1000);
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
    					if(!calibrationOn){
    						break;
    					}
    				}	
    			}
    			
    			try {
					calFileWriter.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
    			
			
    			// Begin recording after count down and remove calibration window
//    			if(calibrationOn){								// check if calibration was stopped midway
//    				startTime = (System.nanoTime())/1000;  			// Time in micro seconds
//    				recordingNow = true;
    				
//    				calText.setText("<html><div style='text-align: centre;'>Calibration Complete<html>");
//        			try {
//        				Thread.sleep(1000);
//        			} catch (InterruptedException e) {
//        				e.printStackTrace();
//        			}
//    			}
    			calFrame.dispose();
    			startRecordingButton.setEnabled(true);
    			calibrationOn = false;
    			System.out.println("cal Done");
    		}
    	}
    }
     /* This thread checks if sensors are connected and sending data
      * 
      */
    class dataCheck extends Thread{
    	// This thread continuously checks every 3 seconds if the incoming data is valid
        @Override
        public void run() {
        	try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        	while(true){
        		if(arduinoDataReceived){
        			//arduinoDataCheck.setBackground(Color.green);
        			positionIcon.setIcon(greenCirc);
        			arduinoDataReceived = false;
        		}else{
        			//arduinoDataCheck.setBackground(Color.red);
        			positionIcon.setIcon(redCirc);
        		}
        		if(forceDataReceived){
        			//forceDataCheck.setBackground(Color.green);
        			forceIcon.setIcon(greenCirc);
        			forceDataReceived = false;
        		}else{
        			//forceDataCheck.setBackground(Color.red);
        			forceIcon.setIcon(redCirc);
        		}
        		if(imuLOK){
        			//imuLDataCheck.setBackground(Color.green);
        			imuLeftIcon.setIcon(greenCirc);
        			imuLOK = false;
        		}else{
        			//imuLDataCheck.setBackground(Color.red);
        			imuLeftIcon.setIcon(redCirc);
        		}
        		if(imuROK){
        			//imuRDataCheck.setBackground(Color.green);
        			imuRightIcon.setIcon(greenCirc);
        			imuROK = false;
        		}else{
        			//imuRDataCheck.setBackground(Color.red);
        			imuRightIcon.setIcon(redCirc);
        		}
        		if(serial.connectedPort){
        			//serialCheck.setBackground(Color.green);
        			serialIcon.setIcon(greenCirc);
        			
        		}else{
        			//serialCheck.setBackground(Color.red);
        			serialIcon.setIcon(redCirc);
        		}
        		
        		// Check if force sensor is connected, if not try to connect
        		if(!s.isConnected()){
        			System.out.println("Space mouse disconnected");
        			forceDataReceived = false;				
        			s.checkController();						// check if force sensor is plugged into PC
        			if(s.isConnected()){						// if connected reset pollCounter
        				pollCounter = 0;
        			}
        		}
        		try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        } 
    }
    
    class loadingWindow extends Thread{
    	// This thread is currently not being used. It is a loading window
        @Override
        public void run(){
    		loadingFrame = new JFrame();
    		loadingPanel = new JPanel();
    		loadingText = new JLabel("<html><div style='text-align: center;'>Please Wait, Loading...<html>");
    		Font font = new Font("Arial",Font.PLAIN,20);
    		loadingText.setFont(font);
    		
    		
    		loadingPanel.add(loadingText);
    		loadingFrame.add(loadingPanel);
    		loadingFrame.setBounds(100,100,448,411);
    		loadingFrame.setVisible(true);
    		while(loading){
    			try {
    				Thread.sleep(100);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    		}
    		try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		loadingFrame.dispose();
    	}
    }
    
	/**
	  * This thread deals with the serial communications
	  * It constantly checks for connections, or if the current connection was lost, and creates or destroys connections as needed
	  * It will always connect to the FIRST AVAILABLE arduino
	  */
		Thread checkSerial = new Thread()
		{
			public void run ()
			{
				serial = new TwoWaySerialComm(new WeakReference<SerialInputHandler>(self));			// creates a new serial communications object, with this optoSim as it's handler

				try {
					while (isRunning)			// runs forever
					{
						String[] ports;						// list of the ports available (not all of them are arduinos however)

						while (!serial.connectedPort)			// while we haven't connected and have begun calibration
						{
							ports = serial.getPortList();		// gets a list of the ports available
							for (String s:ports)
							{
								try
								{
									while (serial.connectedPort)// kills time to avoid overwriting a connected port
										Thread.sleep(100);
									

									serial.disconnect();

									if (System.getProperty("os.name").equals("Mac OS X") && !s.contains("usb"))
										continue;
									System.out.println("Attempting to connect to port "+s);		// attempts to connect to the port
									serial.connect(s, dataSleepLength);

									Thread.sleep(100*TwoWaySerialComm.timeout);					// waits to allow the port to connect
									if (serial.portLost)		// if we lost that port while trying to connect, closes any dangling connections
										serial.disconnect();
								}
								catch (Exception e){
									e.printStackTrace();
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (java.lang.Exception e) {
									e.printStackTrace();
								}
							}
							try {								// sleeps for a bit before checking again, to not kill the cpu or serial port
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					try // BRI - delay for CPU WASTAGE
					{
						Thread.sleep(200);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					}
				} finally {
					try {
						serial.disconnect();
					} catch (Exception ignored){} catch (java.lang.Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
	private Long startTime;    
    
	public void didConnectToPort(String portName) {
		sensorConnected = true;
	}

	public void didBeginWaitingForInitMessage() {
	}

	public void didReceiveInitMessage() {
	}

	public void didReceiveMessage(String str) {
		// This methods is called in the TwoWaySerialComm Class whenever the serial ports receives a full line of data
		arduinoDataReceived = true;						// Set flag for position data
		
		if(s.isConnected()){
			if(firstPoll){								// first poll after connecting force sensor to check if it is working
				s.poll();								// poll will collect new force data as well as set flag for s.isConnected
				firstPoll = false;
			}
		}
		
		// Poll every five runs to match max speed of 3d mouse (62.5Hz);
		if(pollCounter == 5 && s.isConnected()){		
			
			s.poll();						// poll force sensor
			forceDataReceived = true;		// set flag for force data
			pollCounter = 0;		
			Fstr = s.getFstr();				// convert poll data to string
		}
		
		if(checkHeader == 50){							// check data from arduino to see if both IMU's are active and sending data
			if (str.substring(0,1).equals("7")){
				imuLOK = true;
			}
			if (str.substring(2,3).equals("7")){
				imuROK = true;
			}
			checkHeader = 0;
			//System.out.println(Fstr);
		}
		checkHeader++;
	
		// begin writing data to file
		if (recordingNow) {				
			try {
				currTime = (System.nanoTime())/1000; // Time in micro seconds
				
				fileWriter.write(Long.toString(currTime-startTime) + ',');
				fileWriter.write(frameCount.toString() + ',');
				fileWriter.write(Fstr + ',');
				fileWriter.write(str);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		pollCounter++;
	}

	public void didDisconnectFromPort() {
	}

	public void setPeripheralVersionNumber(float version) {
	}

	public void incorrectPeripheralTypeInserted() {
	}
	public void sendTiming(Long[] times, int m){
		String temp = new String();
		for(int i=0; i<m; i++){
			temp = times[i].toString();
			try{
				fileWriter.write(temp + ',');
				fileWriter.newLine();
				if(i==m){
					fileWriter.write(m + ',');
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("printing Done.");
	}
	public void writeCalibration(String str){
		// This method is used to write the sensor calibration data to a file
		if(calSetup){
			try {
				calFileWriter.write(str);
				System.out.println("Cal written");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}

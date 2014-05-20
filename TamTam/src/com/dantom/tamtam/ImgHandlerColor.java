package com.dantom.tamtam;

import java.util.Collections;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;

import android.util.Log;

public class ImgHandlerColor implements CvCameraViewListener2 {

	final static String TAG = "tam";
	BackgroundSubtractor bgfg;
	
	// arguments for background substractor model
	int history = 10; 
	int varThreshold = 10;
	boolean bShadowDetection = false;
	int learningRate = 12;
	
	//
	boolean firstFrame = true;
	
	// 
	Mat src;
	Mat hsv;
	Mat hue;
	Mat backproj;
	
	int bins = 25;
	int[] ch = {0,0};
	MatOfInt fromto;
	public ImgHandlerColor() {
		
	}
	
	
	
	public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	src=inputFrame.rgba();
    	
    	if (firstFrame) {
    		firstFrame = false;
    		 fromto = new MatOfInt(ch);
    		 hsv = new Mat();
    		 backproj = new Mat();
       	}
        // return inputFrame.rgba();
    	manipulation();
    	return src;
    }
    private void manipulation(){
    	Log.d(TAG, "start maninpulation");
    	Mat temp = new Mat();
    	Imgproc.cvtColor(src, temp, Imgproc.COLOR_RGBA2RGB);
    	Imgproc.cvtColor(temp, hsv, Imgproc.COLOR_RGB2HSV);
    	//Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
    	hue = new Mat(hsv.size(), hsv.type());
    	
    	
    	Core.mixChannels(Collections.singletonList(hsv), Collections.singletonList(hue), fromto);
    	
    	histAndBackproj();
    }
    
    private void histAndBackproj(){
    	Mat hist = new Mat();
    	MatOfInt histSize = new MatOfInt(180,256);
    	float[] hue_range = {0,180,0,256};
    	MatOfFloat ranges = new MatOfFloat(hue_range);
    	
    	MatOfInt hCh = new MatOfInt(0,1); 
    	Mat mask = new Mat();
    	Imgproc.calcHist(Collections.singletonList(hue), hCh, mask, hist, histSize, ranges);
    	Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX, -1, new Mat());

    	Imgproc.calcBackProject(Collections.singletonList(hue), fromto, hist, backproj, ranges, 1);
    	
    }

}

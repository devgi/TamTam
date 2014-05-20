package com.dantom.tamtam;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class ImgHandlerSimple implements CvCameraViewListener2 {

	final static String TAG = "tam";
	//	
	// 
	Mat frameRGBA;
	Mat firstFrame;
	Mat result;
	int cnt = 0;
	double lastMean = 0;
	boolean douwn = false;
	
	
	public ImgHandlerSimple() {
		
	}
	
	
	
	public void onCameraViewStarted(int width, int height) { 
		result = new Mat();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	if (firstFrame == null && cnt > 5) {
    		firstFrame = inputFrame.gray().clone();
    		return firstFrame;
    	} else if (cnt <= 5) {
    		cnt++;
    		return inputFrame.rgba();
    	} else {
    		Core.subtract(inputFrame.gray(), firstFrame, result);
    		Scalar newMean = Core.mean(result);
    		double newMeanVal = newMean.val[0];
    		return result;
    	}
    }
}
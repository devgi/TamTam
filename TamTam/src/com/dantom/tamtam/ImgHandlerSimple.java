package com.dantom.tamtam;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.content.Context;
import android.util.Log;

public class ImgHandlerSimple implements CvCameraViewListener2 {

	final static String TAG = "tam";
	//	
	// 
	Mat frameRGBA;
	Mat firstFrame;
	Mat result;
	int cnt = 0;
	double lastMean = 0;
	boolean down = false;
	
	//for playing sound
	private Context context;
	
	
	public ImgHandlerSimple(Context context) {
		this.context = context;
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
    		if (5 < newMeanVal - lastMean) {
    			down=true;
    		} else if(newMeanVal < lastMean) {
    			if (down) {
    				Utils.PlaySound(R.raw.bongo_1, context);
    				down = false;
    			}
    		}
    		Log.d("tamMean","mean " + newMeanVal);
    		lastMean = newMeanVal;
    		return result;
    	}
    }
}
package com.dantom.tamtam;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;

import android.util.Log;

public class ImgHandler implements CvCameraViewListener2 {

	final static String TAG = "tam";
	BackgroundSubtractor bgfg;
	
	// arguments for background substractor model
//	int history = 10; 
//	int varThreshold = 10;
//	boolean bShadowDetection = false;
//	int learningRate = 12;
	
	//	
	// 
	Mat frameRGBA;
	Mat fgmask;
	Mat result;
	Mat temp;
	Mat temp2;
	Mat temp3;
	
	public ImgHandler() {
		
	}
	
	
	
	public void onCameraViewStarted(int width, int height) {
		bgfg = makeMOG2();
		fgmask = new  Mat();//Mat.zeros(frameRGBA.size(), frameRGBA.type());
		result = new Mat();//Mat.zeros(frameRGBA.size(), frameRGBA.type());
		temp = new Mat();
		temp2 = new Mat();
		temp3 = new Mat();
    }
	
	public BackgroundSubtractor makeMOG() {
		int history = 3;
		int nmixtures = 10;
		double backgroundRatio = 0.7;
		double noiseSigma = 15.0;
		
		return new BackgroundSubtractorMOG(history, nmixtures, backgroundRatio, noiseSigma);		
	}
	
	public BackgroundSubtractor makeMOG2() {
		
		int history = 10;
		float varThreshold = 200;
		boolean bShadowDetection = false;
		return new BackgroundSubtractorMOG2(history, varThreshold, bShadowDetection);
	}

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	frameRGBA=inputFrame.rgba();
        // return inputFrame.rgba();
    	manipulation();
    	return result;
    }
    private void manipulation(){
    	Log.d(TAG, "start maninpulation");
    	//Log.d(TAG, "frame rgba ch-" + frameRGBA.channels());
    	
    	Size origFrameSize = frameRGBA.size();
    	int shrinkFactor = 2;
    	Size newFrameSize = new Size(frameRGBA.width() / shrinkFactor, frameRGBA.height() / shrinkFactor);
    	Imgproc.resize(frameRGBA, temp2, newFrameSize);
    	Log.d(TAG, "resize image.");
    	
    	Imgproc.cvtColor(temp2, temp, Imgproc.COLOR_RGBA2RGB);
    	
    	//Log.d(TAG, "frame rgb ch-" + temp.channels());
        
    	
    	//Log.d(TAG, "mean: temp " + Core.mean(temp));
    	bgfg.apply(temp, fgmask); //apply() exports a gray image by definition
    	Log.d(TAG, "mean: fgmask " + Core.mean(fgmask));
    	
    	//Log.d(TAG, "frame fgmask ch-" + fgmask.channels());
    	
        Log.d(TAG, "After apply");
        
        Imgproc.cvtColor(fgmask, temp3, Imgproc.COLOR_GRAY2RGBA);
        Log.d(TAG, "After convertion");
        
        Imgproc.resize(temp3, result, origFrameSize);
        //Log.d(TAG, "R: " + Core.mean(frameRGBA));
    }

}

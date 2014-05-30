package com.dantom.tamtam;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.content.Context;
import android.util.Log;

/**
 * This class is using threshold to detect hands,  and find the moment of impact(with the virtual drum) 
 * by calculating the size of each hand.
 */
public class ImgHandlerTresh implements CvCameraViewListener2 {
	
	// The tag of the logging of this class
	final static String TAG = "tam";
	
	//The minimal diff in hand size, before updating the hand size.
	private static final double CONTOUR_SIZE_DIFF_TRESHOLD = 150;
	
	// The frame took from the camera
	private Mat frameRGBA;
	
	// The sounds the drums will make
	private int[] drums = {R.raw.bongo_1,R.raw.bongo_2};
	
	// The context of the main activity, used to play the drum sounds.
	private Context c;

	// This array uses to save the hands sizes between to different frames.
	private double[] sizes = {0, 0};
	
	// This array is used to save the current direction of each hand, true means down.
	private boolean[] directions = {false, false};
	
	// This is the array of colors for each hand contour.
	private Scalar[] colors = {new Scalar(0,0,255,0), new Scalar(255,0,0,0)};

	/** The image resizer. */
	private ImageResizer imageResizer;
	
	/**
	 * Constructor.
	 * 
	 * @param c The context of the main activity, used to play the drum sounds.
	 */
	public ImgHandlerTresh(Context c){
		this.c = c;
	}
	
	public void onCameraViewStarted(int width, int height) {
		this.imageResizer = new ImageResizer(10, new Size(width, height));
    }
	
    public void onCameraViewStopped() {
    	// not in use
    }

    /**
     * This method is called when there is a new frame.
     * 
     * @see org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2#onCameraFrame(org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame)
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	frameRGBA = inputFrame.rgba();
    	
    	manipulation();
    	
    	return frameRGBA;
    }
    
    
    private void manipulation(){
    	Log.d(TAG, "start maninpulation");
    	
    	Mat smallFrame = new Mat();
    	imageResizer.shrinkImage(frameRGBA, smallFrame);
    	Core.flip(smallFrame, smallFrame, 1);
    	
    	Mat result = new Mat();
		Imgproc.cvtColor(smallFrame, result , Imgproc.COLOR_RGBA2GRAY);
    	
    	Log.d(TAG, "blur");
    	Imgproc.blur(result, result, new Size(5, 5));
    	
    	Log.d(TAG, "tresh");
    	Imgproc.threshold(result, result, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
    	
		List<MatOfPoint> allContours = new ArrayList<MatOfPoint>();
		Mat tempMat = new Mat();
		Imgproc.findContours(result, allContours  , tempMat  , Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.i(TAG, "counters " + allContours.size());
		
		double maxArea = 0;
		double maxArea1 = 0;
		double area = 0;
		MatOfPoint maxCnt = null;
		MatOfPoint maxCnt1 = null;
		
		//Finding the 2 largest contours
		for (MatOfPoint cnt : allContours) {
			area = Imgproc.contourArea(cnt);
			if (area > maxArea1 && area < 5000 && area > 170) {
				if (area > maxArea){
					maxArea1 = maxArea;
					maxCnt1 = maxCnt;
					maxArea = area;
					maxCnt = cnt;
				} else {
					maxArea1 = area;
					maxCnt1 = cnt;
				}
			}
		}
		
		Log.i(TAG+"-size:", ""+maxArea);
		
		MatOfPoint[] contours = new MatOfPoint[2];
		
		if (maxCnt  != null) {
			Moments p = Imgproc.moments(maxCnt);
			int x = (int) (p.get_m10() / p.get_m00());
	        int w = smallFrame.width();
	        if (x < w/2) {
	        	contours[0] = maxCnt;
	        	updateHand(maxArea,0);
	        } else {
	        	contours[1] = maxCnt;
	        	updateHand(maxArea,1);
	        }
		}
		if (maxCnt1  != null) {
			Moments p = Imgproc.moments(maxCnt1);
			int x = (int) (p.get_m10() / p.get_m00());
	        int w = smallFrame.width();
	        if (x < w/2) {
        		contours[0] = maxCnt1;
        		updateHand(maxArea1, 0);
	        } else {
	        	contours[1] = maxCnt1;
	        	updateHand(maxArea1, 1);
	        }
		}
		
		drawContours(smallFrame, contours);
		
		Log.i(TAG, "max area " + maxArea );
		
        imageResizer.rescaleImage(smallFrame, frameRGBA);
    }

	private void drawContours(Mat frame, MatOfPoint[] contours) {
		if (contours[0] != null) {
			drawContour(contours[0],frame, 0);
		} else {
			sizes[0] = 0;
		}
		if (contours[1] != null) {
			drawContour(contours[1],frame, 1);
		} else {
			sizes[1] = 0;
		}
	}

	private void drawContour(MatOfPoint contour, Mat smallFrame, int handIndex) {
		Moments p = Imgproc.moments(contour);
		int x = (int) (p.get_m10() / p.get_m00());
		int y = (int) (p.get_m01() / p.get_m00());
		
		Core.circle(smallFrame, new Point(x, y), 4, colors[handIndex]);
		MatOfInt hull = new MatOfInt();
		Imgproc.convexHull(contour, hull);
		List<MatOfPoint> contorsToDraw = new ArrayList<MatOfPoint>();
		contorsToDraw.add(contour);
		Imgproc.drawContours(smallFrame, contorsToDraw, 0, colors[handIndex], 1);
	}

	private void updateHand(double maxArea, int i) {
    	if (Math.abs(sizes[i] - maxArea) > CONTOUR_SIZE_DIFF_TRESHOLD) {
    		if (sizes[i] > maxArea) {
    			directions[i] = true;
    		} else if (directions[i]) {
    			Utils.PlaySound(drums[i], c);
    			directions[i] = false;
    		}
    		sizes[i] = maxArea;
    	}
	}

}

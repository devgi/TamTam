package com.dantom.tamtam;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageResizer {
	
	/** The image realSize. */
	private Size imageRealSize;
	
	/** The wanted shrinking factor **/
	private Size smallSize;

	public ImageResizer(double shrinkingFactor, Size imageRealSize) {
		this.smallSize = new Size(imageRealSize.width/shrinkingFactor, imageRealSize.height/shrinkingFactor);
		this.imageRealSize = imageRealSize;
	}
	
	public void shrinkImage(Mat src, Mat dst) {
		Imgproc.resize(src, dst , smallSize, 0, 0, Imgproc.INTER_NEAREST);
	}
	
	public void rescaleImage(Mat src, Mat dst) {
		Imgproc.resize(src, dst , imageRealSize);
	}

}

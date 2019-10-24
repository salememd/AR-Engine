/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine.models;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

public class ImageData {

    public Mat descriptor;
    public MatOfKeyPoint keypoints;
    public int cols;
    public int rows;

    public ImageData(Mat descriptor, MatOfKeyPoint keypoints, int cols, int rows) {
        this.descriptor = descriptor;
        this.keypoints = keypoints;
        this.cols = cols;
        this.rows = rows;

    }

}

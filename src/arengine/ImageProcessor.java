/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine;

import arengine.config.IConfig;
import arengine.models.ImageData;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.ORB;

public class ImageProcessor {

    private ORB detector;
    private DescriptorMatcher matcher;

    public ImageProcessor() {
        detector = ORB.create();
        //detector.setMaxFeatures(12000);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    // ORB compute image descriptor
    public ImageData computeDescriptor(Mat img) {
        Mat descriptors = new Mat();
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(img, keypoints);
        detector.compute(img, keypoints, descriptors);
        return new ImageData(descriptors, keypoints, img.cols(), img.rows());
    }

        // match two descriptors
    public List<DMatch> matchImages(Mat templateDescriptors, Mat imgDescriptors) {
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(imgDescriptors, templateDescriptors, matches);

        List<DMatch> goodMatches = matches.toList();
        matches.release();
        if (goodMatches.size() < 450) {
            return null;
        }

        Comparator<DMatch> bySizeDifferent = Comparator.comparing(similarity -> similarity.distance);
        goodMatches.sort(bySizeDifferent);

        System.out.println("----" + (100 - goodMatches.get(0).distance) + "----" + goodMatches.size());
        if ((100 - goodMatches.get(0).distance) <= IConfig.similarityPercent) {
            return null;
        }

        return goodMatches;

    }

    // drow boundy box when AR experiance used
    public Point[] findHomography(ImageData template, ImageData img, List<DMatch> matches) {
        LinkedList<Point> objList = new LinkedList<>();
        LinkedList<Point> sceneList = new LinkedList<>();

        List<KeyPoint> keypoints_sceneList = template.keypoints.toList();
        List<KeyPoint> keypoints_objectList = img.keypoints.toList();

        for (int i = 0; i < matches.size(); i++) {
            objList.addLast(keypoints_sceneList.get(matches.get(i).trainIdx).pt);
            sceneList.addLast(keypoints_objectList.get(matches.get(i).queryIdx).pt);
        }
        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);

        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);

        Mat hg = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 3);

        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

        obj_corners.put(0, 0, new double[]{0, 0});
        obj_corners.put(1, 0, new double[]{template.cols, 0});
        obj_corners.put(2, 0, new double[]{template.cols, template.rows});
        obj_corners.put(3, 0, new double[]{0, template.rows});

        Core.perspectiveTransform(obj_corners, scene_corners, hg);

        Point p1 = new Point(scene_corners.get(0, 0));
        Point p2 = new Point(scene_corners.get(1, 0));
        Point p3 = new Point(scene_corners.get(2, 0));
        Point p4 = new Point(scene_corners.get(3, 0));
        obj.release();
        scene.release();
        hg.release();
        obj_corners.release();
        scene_corners.release();

        /*System.out.println("p1X : " + p1.x);
        System.out.println("p1Y : " + p1.y);
        System.out.println("p2X : " + p2.x);
        System.out.println("p2Y : " + p2.y);
        System.out.println("p3X : " + p3.x);
        System.out.println("p3Y : " + p3.y);
        System.out.println("p4X : " + p4.x);
        System.out.println("p4Y : " + p4.y);*/
        if (((p3.x > p2.x) && (p4.x > p1.x) && (p4.y > p3.y) && (p1.y > p2.y)) || ((p1.x > p4.x) && (p2.x > p3.x) && (p3.y > p4.y) && (p2.y > p1.y)) || ((p2.x > p1.x) && (p3.x > p4.x) && (p3.y > p2.y) && (p4.y > p1.y))) {
            Mat src = new MatOfPoint2f(p1, p2, p3, p4);
            double size = img.cols * img.rows;
            double contourArea = Imgproc.contourArea(src);
            src.release();
            if ((contourArea * 100) / size < 5) {
                return null;
            }
            Point[] points = new Point[4];
            points[0] = p1;
            points[1] = p2;
            points[2] = p3;
            points[3] = p4;
            System.out.println("found");
            return points;
        }
        return null;
        /* 
        double size = img.cols * img.rows;
        double contourArea = Imgproc.contourArea(src);
        System.out.println("contour area " + contourArea);
        System.out.println("img size area " + size);
        System.out.println("Precent " + (contourArea * 100) / size);
        if ((contourArea * 100) / size < 10) {
            return null;
        }
        Point[] points = new Point[4];
        points[0] = p1;
        points[1] = p2;
        points[2] = p3;
        points[3] = p4;

        return points;
         */
    }

}

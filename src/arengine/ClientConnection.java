/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine;

import arengine.models.Response;
import arengine.models.ImageData;
import arengine.models.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

class ClientConnection extends Thread {

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int clientNumber;
    private HashSet<Resource> resources;
    private ImageProcessor matcher;
    private Resource currentResource = null;
    private int topScore;

    public ClientConnection(Socket socket, int clientNumber, HashSet<Resource> resources, ImageProcessor matcher) throws IOException {
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.matcher = matcher;
        this.resources = resources;
        System.out.println("New Connection");
    }

     // thread start, receive data from client 
    public void run() {
        try {

            byte[] resultBuff = new byte[0];
            byte[] buff = new byte[4];
            int k = -1;
            boolean isHeaderRead = false;
            int msgSize = 0, tempsize = 0;
            while ((k = in.read(buff, 0, buff.length)) > -1) {
                if (!isHeaderRead) {
                    msgSize = ByteBuffer.wrap(buff).getInt();
                    System.out.println("msg size " + msgSize);
                    isHeaderRead = true;
                    buff = new byte[15024];
                    k = -1;
                } else {
                    tempsize = tempsize + k;
                    byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
                    System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
                    System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
                    resultBuff = tbuff; // call the temp buffer as your result buff 

                    if (tempsize >= msgSize) {
                        isHeaderRead = false;
                        try {
                            Response data = handle(resultBuff);
                            if (data != null) {
                                this.out.write(createResponse(data));
                            } else {
                                byte[] res = {-1};
                                this.out.write(res);
                            }
                        } catch (Exception e) {
                            byte[] res = {-1};
                            this.out.write(res);
                        }
                        resultBuff = new byte[0];
                        tempsize = 0;
                        k = -1;
                        buff = new byte[4];

                    }

                }
            }
            this.out.close();
            this.in.close();
            this.socket.close();

        } catch (Exception e) {
            System.out.println("Error handling client# " + clientNumber + ": " + e);
        }
    }

    // prepare the response packet  
    public byte[] createResponse(Response data) {
        byte[] preview = null;
        byte[] resourceId = null;
        byte[] combined = null;
        byte[] header = null;
        byte[] previewType = new byte[1];
        byte[] responceType = new byte[1];

        if (data.points == null || data.resource.previewFile == null) {
            responceType[0] = 0;
            resourceId = new byte[8];
            ByteBuffer.wrap(resourceId).putLong(data.resource.resourceFile.id);
            combined = new byte[responceType.length + resourceId.length];
            System.arraycopy(responceType, 0, combined, 0, responceType.length);
            System.arraycopy(resourceId, 0, combined, responceType.length, resourceId.length);
            return combined;
        }

        if (data.includePreview) {
            preview = ResourceManager.fetchPreview(data.resource);
            if (preview == null) {
                return ByteBuffer.allocate(4).putInt(-1).array();
            }
        } else {
            preview = new byte[0];
            resourceId = new byte[0];
        }
        responceType[0] = 1;
        header = ByteBuffer.allocate(4).putInt(preview.length).array();

        byte[] p1x = new byte[8];
        ByteBuffer.wrap(p1x).putDouble(data.points[0].x);
        byte[] p1y = new byte[8];
        ByteBuffer.wrap(p1y).putDouble(data.points[0].y);
        byte[] p2x = new byte[8];
        ByteBuffer.wrap(p2x).putDouble(data.points[1].x);
        byte[] p2y = new byte[8];
        ByteBuffer.wrap(p2y).putDouble(data.points[1].y);
        byte[] p3x = new byte[8];
        ByteBuffer.wrap(p3x).putDouble(data.points[2].x);
        byte[] p3y = new byte[8];
        ByteBuffer.wrap(p3y).putDouble(data.points[2].y);
        byte[] p4x = new byte[8];
        ByteBuffer.wrap(p4x).putDouble(data.points[3].x);
        byte[] p4y = new byte[8];
        ByteBuffer.wrap(p4y).putDouble(data.points[3].y);

        previewType[0] = (byte) data.resource.type.getValue();
        if (resourceId == null) {
            resourceId = new byte[8];
        }

        combined = new byte[responceType.length + header.length + (p1x.length * 8) + preview.length + previewType.length + resourceId.length];
        System.arraycopy(responceType, 0, combined, 0, responceType.length);
        System.arraycopy(header, 0, combined, responceType.length, header.length);
        System.arraycopy(p1x, 0, combined, 5, p1x.length);
        System.arraycopy(p1y, 0, combined, 13, p1y.length);
        System.arraycopy(p2x, 0, combined, 21, p2x.length);
        System.arraycopy(p2y, 0, combined, 29, p2y.length);
        System.arraycopy(p3x, 0, combined, 37, p3x.length);
        System.arraycopy(p3y, 0, combined, 45, p3y.length);
        System.arraycopy(p4x, 0, combined, 53, p4x.length);
        System.arraycopy(p4y, 0, combined, 61, p4y.length);
        System.arraycopy(previewType, 0, combined, 69, previewType.length);
        if (resourceId.length == 8) {
            ByteBuffer.wrap(resourceId).putLong(data.resource.resourceFile.id);
            System.arraycopy(resourceId, 0, combined, 70, resourceId.length);
            System.arraycopy(preview, 0, combined, 78, preview.length);
        } else {
            System.arraycopy(preview, 0, combined, 70, preview.length);
        }

        System.out.println("response size: " + combined.length);
        return combined;
    }

    
        // handle the quary image,   
    private Response handle(byte[] data) {
        Mat frame = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_UNCHANGED);
        ImageData img = matcher.computeDescriptor(frame);
        frame.release();
        if (this.currentResource != null) {
            List<DMatch> matches = matcher.matchImages(this.currentResource.templateData.descriptor, img.descriptor);
            if (matches != null) {
                int score = (int) (100 - matches.get(0).distance) + matches.size();
                int simlirityPercent = (score * 100) / topScore;
                if (simlirityPercent > 85) { 
                    Point[] points = null;
                    if (this.currentResource.previewFile != null) {
                        matches = matches.subList(0, 50);
                        points = matcher.findHomography(this.currentResource.templateData, img, matches);
                    }
                    Response response = new Response();
                    response.points = points;
                    response.resource = this.currentResource;
                    response.includePreview = false;
                    return response;
                }
            }
        }

        int topScore = 0;
        Resource bestReource = null;
        List<DMatch> bestMatches = null;
        Iterator iterator = resources.iterator();
        long startTime = System.currentTimeMillis();

        while (iterator.hasNext()) {
            try {
                Resource resource = ((Resource) iterator.next());
                if (this.currentResource == resource) {
                    continue;
                }

                List<DMatch> matches = matcher.matchImages(resource.templateData.descriptor, img.descriptor);
                if (matches == null) {
                    continue;
                }

                int score = (int) (100 - matches.get(0).distance) + matches.size();
                if (score > topScore) {
                    topScore = score;
                    bestReource = resource;
                    bestMatches = matches;
                }
            } catch (Exception e) {
                System.out.println("error");
            }
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        if (bestReource == null || topScore < 550) {
            return null;
        }
        System.out.println("-----------------");
        System.out.println("top score : " + topScore);
        System.out.println("Image found : " + bestReource.resourceFile.name);
        System.out.println("Elapsed Time : " + elapsedTime);
        System.out.println("-----------------");
        Point[] points = null;
        if (bestReource.previewFile != null) {
            bestMatches = bestMatches.subList(0, 50);
            points = matcher.findHomography(bestReource.templateData, img, bestMatches);
            if(points == null){
            return null;
            }
        }
        Response response = new Response();
        response.resource = bestReource;
        response.points = points;
        response.includePreview = true;
        this.currentResource = bestReource;
        this.topScore = topScore;

        return response;
    }

}

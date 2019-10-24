/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine;

import arengine.config.IConfig;
import arengine.models.File;
import arengine.models.Resource;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ResourceManager {

    private HashSet<Resource> resources;
    private ImageProcessor processor;

    public ResourceManager(ImageProcessor processor) {
        this.resources = new HashSet<>();
        this.processor = processor;
    }

    
    public HashSet<Resource> getResources() {
        return this.resources;
    }
  int i = 0;

    /*public void update(String path) {

        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(n -> {
                try {
                    byte[] data = Files.readAllBytes(Paths.get(path + n.getFileName().toString()));
                    // File templateFile = new File(results.getLong("template"), results.getString("templateimg_path"), results.getString("templateimg_filename"));
                    long resourceId = i++;
                    File templateFile = null;
                    File previewFile = null;
                    Resource.PreviewType previewType = null;

                    File resourceFile = new File(123, path, n.getFileName().toString());

                    Resource resource = new Resource(resourceId, previewType, templateFile, previewFile, resourceFile);
                    if (resources.add(resource)) {
                        Mat templateMat = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_UNCHANGED);
                    //    Imgproc.resize(templateMat, templateMat, new Size(new Point(1920, 1080)));
                        resource.setTemplateData(processor.computeDescriptor(templateMat));
                        templateMat.release();
                    }
                    System.out.println("sdsd " + resources.size());

                } catch (Exception e) {
                }
            });
        } catch (Exception e) {

        }
    }*/
   

        // get latest updates from DB
    public void update() {

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + IConfig.mysqlHostname + ":" + IConfig.mysqlPort + "/" + IConfig.mysqldbName, IConfig.mysqlUsername, IConfig.mysqlPassword);
            PreparedStatement stmt = conn.prepareStatement("SELECT `resources`.`id` as rid , `preview_type`, `template`,`preview`,`resourse` , attachment.`file_name` as preview_filename, attachment.`storage_path` as preview_path, res.`file_name` as resource_filename, res.`storage_path` as resource_path, templateimg.`file_name` as templateimg_filename, templateimg.`storage_path` as templateimg_path FROM `resources` left JOIN `user_uploads` AS attachment ON attachment.id = `preview` JOIN `user_uploads` AS res ON res.id = resources.`resourse` JOIN `user_uploads` AS templateimg ON templateimg.id = resources.`template` where `resources`.`state` = 1");

            LinkedList<Long> listOfIds = new LinkedList<>();
            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                long resourceId = results.getLong("rid");
                listOfIds.add(resourceId);
                File templateFile = new File(results.getLong("template"), results.getString("templateimg_path"), results.getString("templateimg_filename"));
                File previewFile = null;
                Resource.PreviewType previewType = null;
                long prevId = results.getLong("preview");
                if (prevId == 0) {
                    previewFile = null;
                    previewType = null;
                } else {
                    previewFile = new File(prevId, results.getString("preview_path"), results.getString("preview_filename"));
                    previewType = Resource.PreviewType.valueOf(results.getInt("preview_type"));
                }

                File resourceFile = new File(results.getLong("resourse"), results.getString("resource_path"), results.getString("resource_filename"));

                Resource resource = new Resource(resourceId, previewType, templateFile, previewFile, resourceFile);
                if (resources.add(resource)) {
                    byte[] img = fetchTemplateImage(resource);
                    if (img == null) {
                        continue;
                    }

                    Mat templateMat = Imgcodecs.imdecode(new MatOfByte(img), Imgcodecs.IMREAD_UNCHANGED);
                    resource.setTemplateData(processor.computeDescriptor(templateMat));
                    templateMat.release();
                }

            }
            stmt.close();
            conn.close();

            if (resources.size() != listOfIds.size()) {
                resources = (HashSet<Resource>) resources.stream()
                        .filter(i -> listOfIds.contains(i.id))
                        .collect(Collectors.toSet());
            }

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());

        }

    }

     // get template images
    public static byte[] fetchTemplateImage(Resource resource) {

        try {
            URL url = new URL(IConfig.storageURL + IConfig.storagePath + resource.templateFile.path + resource.templateFile.name);
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();
            return response;
        } catch (Exception e) {
            return null;
        }
    }
    
 // get preview file
    public static byte[] fetchPreview(Resource resource) {

        try {
            URL url = new URL(IConfig.storageURL + IConfig.storagePath + resource.previewFile.path + resource.previewFile.name);
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();
            return response;
        } catch (Exception e) {
            return null;
        }
    }

}

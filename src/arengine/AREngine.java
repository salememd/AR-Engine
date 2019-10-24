/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine;

import java.net.ServerSocket;
import org.opencv.core.Core;

/**
 *
 * @author Salem F. Elmrayed
 */
public class AREngine {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }

        ImageProcessor processor = new ImageProcessor();
        ResourceManager rm = new ResourceManager(processor);
        System.out.println("syncing..");
        rm.update();
        //rm.update("C:\\Users\\Dexter\\Desktop\\");
        System.out.println("Ready..");

        // sync worker
        Thread sync = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(10000);
                         rm.update();
                    } catch (InterruptedException ex) {
                    }
                }

            }
        });
        sync.start();
        
        
        try {
            int clientNumber = 0;
            ServerSocket listener = new ServerSocket(1232);
            try {
                while (true) {
                    try {
                        new ClientConnection(listener.accept(), clientNumber++, rm.getResources(), processor).start();
                    } catch (Exception e) {
                    }
                }
            } finally {
                listener.close();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}

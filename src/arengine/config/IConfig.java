/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine.config;

/**
 *
 * @author Salem F. Elmrayed
 *
 * System configuration
 */
public interface IConfig {

    // MySQL database config
    public static final String mysqlUsername = "root";
    public static final String mysqlPassword = ""; 
    public static final String mysqldbName = "arplatform_db";
    public static final String mysqlHostname = "127.0.0.1";
    public static final String mysqlPort = "3306";

    public static final int similarityPercent = 65;

    public static final String storageURL = "http://127.0.0.1:8085/";
    public static final String storagePath = "ARplatform/";

}

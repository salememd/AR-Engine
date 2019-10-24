/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arengine.models;

public class File {
    public long id;
    public String path;
    public String name;
    
    public File(long id, String path, String name){
        this.id = id;
        this.path = path;
        this.name = name;
    }
}

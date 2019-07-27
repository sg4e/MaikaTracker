/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg4e.maikatracker;

import java.io.File;
import javax.swing.filechooser.*;

/**
 *
 * @author CaitSith2
 */
public class TextFiles extends FileFilter {    
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        
        return extension != null && 
              extension.equals("json");
    }
    
    public File getFile(File f) {
        if(accept(f))
            return f;
        return new File(f.getPath() + ".json");
    }
    
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    @Override
    public String getDescription() {
        return "FF4FE State Files (*.json)";
    }
}

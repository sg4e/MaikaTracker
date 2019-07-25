/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg4e.maikatracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
              (extension.equals("txt") /*|| 
               extension.equals("json")*/);
    }
    
    public File getFile(File f) {
        if(accept(f))
            return f;
        return new File(f.getPath() + ".txt");
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
        return "Text Files (*.txt)" /*, *.json)"*/;
    }
    
    public static String readFile(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader (f.getPath()));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }
}

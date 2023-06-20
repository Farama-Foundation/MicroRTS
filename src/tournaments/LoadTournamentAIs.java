/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tournaments;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ai.core.AIWithComputationBudget;

/**
 *
 * @author santi
 */
public class LoadTournamentAIs {

    public static List<Class> loadTournamentAIsFromFolder(String path) throws Exception {
        List<Class> cl = new ArrayList<>();
        File f = new File(path);
        
        if (f.isDirectory()) {
            for(File f2:f.listFiles()) {
                if (f2.getName().endsWith(".jar")) {
                    cl.addAll(loadTournamentAIsFromJAR(f2.getAbsolutePath()));
                }
            }
        }
        
        return cl;
    }
    
    
    public static List<Class> loadTournamentAIsFromJAR(String jarPath) throws IOException, ClassNotFoundException {
        ClassLoader loader = URLClassLoader.newInstance(new URL[]{new File(jarPath).toURI().toURL()},
                LoadTournamentAIs.class.getClassLoader()
        );
        List<Class> cs = new ArrayList<>();
        
        URL jar = new File(jarPath).toURI().toURL();
          ZipInputStream zip = new ZipInputStream(jar.openStream());
          while(true) {
            ZipEntry e = zip.getNextEntry();
            if (e == null)
              break;
            String name = e.getName();
            if (name.endsWith(".class")) {
            	try {
            		Class<?> c = loader.loadClass(name.substring(0, name.length() - 6).replace('/', '.'));
	                if (!Modifier.isAbstract(c.getModifiers()) && 
	                    isTournamentAIClass(c)) cs.add(c);
            	} catch (final NoClassDefFoundError error) {
            		System.err.println("Skipping class which could not be loaded: " + name);
            		if (name.equals("org/jdom/xpath/JaxenXPath$NSContext.class")) {
            			System.err.println("NOTE: this is a common error when packaging all of MicroRTS inside the .jar file with your bot(s)!");
            		}
            	}
            }
          }
        return cs;        
    }

    
    public static boolean isTournamentAIClass(Class c) {
        if (c == AIWithComputationBudget.class) return true;
        c = c.getSuperclass();
        if (c!=null) return isTournamentAIClass(c);
        return false;
    }
}

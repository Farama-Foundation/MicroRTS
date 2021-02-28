/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree.utils;

import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author rubens
 */
public class SerializableController {

    public static void saveSerializable(iDSL t, String fileName, String path) {
        try {
            FileOutputStream fout = new FileOutputStream(path+fileName, false);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(t);
            out.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static iDSL recoverySerializable(String fileName, String path) {
        iDSL dsl = null;
        try {
            FileInputStream fIn = new FileInputStream(path+fileName);
            ObjectInputStream in = new ObjectInputStream(fIn);
            dsl = (iDSL) in.readObject();
            in.close();
            fIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsl;
    }
}

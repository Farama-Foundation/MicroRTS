/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.IOException;
import java.io.Writer;
import javax.swing.JTextArea;

/**
 *
 * @author santi
 */
public class JTextAreaWriter extends Writer {

    JTextArea m_ta = null;
    
    public JTextAreaWriter(JTextArea ta) {
        m_ta = ta;
    }
            
    public void write(char[] cbuf, int off, int len) throws IOException {
        StringBuffer tmp = new StringBuffer();
        for(int i = off;i<off+len;i++) {
            tmp.append(cbuf[i]);
        }
        m_ta.append(tmp.toString());
        m_ta.repaint();
    }

    public void flush() throws IOException {
        m_ta.repaint();
    }

    public void close() throws IOException {
    }
    
}

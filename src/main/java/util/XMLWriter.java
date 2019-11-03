package util;

/* Copyright 2010 Santiago Ontanon and Ashwin Ram */



import java.io.IOException;
import java.io.Writer;

/**
 * XMLWriter is a class used to easily output XML.
 *  
 * @author Andrew Trusty
 */
public class XMLWriter {
	private static final int tabsize = 2;
        private String lineSeparator = "\n";

	/**
	 * Number of spaces indentation currently being used.
	 */
	private int spaces;
	/**
	 * The PrintWriter used to output the XML.
	 */
	private Writer writer;
	
	/**
	 * XMLWriter constructor.
	 *  
	 * @param w Writer to use for XML output.
	 */
	public XMLWriter(Writer w) {
		writer = w;
		spaces = 0;
	}	

        
	/**
	 * XMLWriter constructor.
	 *  
	 * @param w Writer to use for XML output.
         * @param a_lineSeparator is the character to be used to separate lines
	 */
	public XMLWriter(Writer w, String a_lineSeparator) {
		writer = w;
                lineSeparator = a_lineSeparator;
		spaces = 0;
	}	

        
        /**
	 * Reset the number of spaces being used for indentation.
	 */
	public void resetTab() {
		spaces = 0;
	}
	/**
	 * Increase the indentation by the tabsize.
	 */
	public void tab() {
		spaces += tabsize;
	}
	/**
	 * Decrease the indentation by the tabsize.
	 */
	private void untab() {
		spaces -= tabsize;
	}
	/**
	 * Write out indentation.
	 */
	private void indent() {
		for (int i = 0; i < spaces; ++i)
			try {
				writer.write(" ");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	/**
	 * Set the number of tabs to use.
	 * @param t the number of tabs
	 */
	public void setTab(int t) {
		if (t >= 0)	spaces += t * tabsize;
	}
	
	/**
	 * Public XML writing methods.
	 */	
	public void tag(String tagname, Object val) {
		tag(tagname, val.toString());
	}
	public void tag(String tagname, String value) {
		indent();
		try {
			writer.write("<"+tagname+">"+value+"</"+tagname+">" + lineSeparator);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void tag(String tagname, int value) {
		tag(tagname, ""+value);
	}
	public void tag(String tagname, long value) {
		tag(tagname, ""+value);
	}
	public void tag(String tagname, double value) {
		tag(tagname, ""+value);
	}
	/**
	 * Writes an opening or closing tag and increased or decreases the 
	 * indentation depending on if it is an opening or closing tag.
	 * 
	 * @param tagname the tag to write, a closing tag should start with '/'
	 */
	public void tag(String tagname) {
		if (tagname.charAt(0) == '/') untab();
		indent();
		try {
			writer.write("<"+tagname+">" + lineSeparator);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (tagname.charAt(0) != '/') tab();
	}

	public void tagWithAttributes(String tagname, String attributesString) {
		if (tagname.charAt(0) == '/') untab();
		indent();
		try {
			writer.write("<"+tagname+" "+attributesString+">" + lineSeparator);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (tagname.charAt(0) != '/') tab();
	}

	/**
	 * Writes the given string assuming it is raw XML.  Appends a newline.
	 * 
	 * @param xml string to write
	 */
	public void rawXML(String xml) {
		//indent();
		try {
			writer.write(xml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Close the Writer used to output the XML. 
	 */
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Flush the XML output.
	 */
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Writer getWriter() {
		return writer;
	}
}

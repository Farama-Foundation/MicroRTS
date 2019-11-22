/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.visualization;

import ai.ahtn.domain.DomainDefinition;
import ai.ahtn.domain.HTNMethod;
import ai.ahtn.domain.MethodDecomposition;
import ai.ahtn.domain.Symbol;
import ai.ahtn.domain.Term;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author santi
 */
public class HTNDomainVisualizer {
    public static int DEBUG = 0;
    
    public static void main(String args[]) throws Exception {
//        DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-lowest-level.lisp");
//        DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-low-level.lisp");
        DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-portfolio.lisp");
        
        System.out.println(dd);
        
        HTNDomainVisualizer v = new HTNDomainVisualizer();
        BufferedImage img = v.visualizeHTNDomain(dd, new Symbol("destroy-player"));
        ImageIO.write(img, "png", new File("HTN-domain.png"));
    }    
    
    int hpadding = 8;
    int vpadding = 4;
    int hMethodPadding = 16;
    int vMethodPadding  = 16;
    Font font;
    FontMetrics fm;
    
    
    public HTNDomainVisualizer() {
        font = new Font("Arial", Font.PLAIN, 10);
        Canvas c = new Canvas();
        fm = c.getFontMetrics(font);
    }

   
    public BufferedImage visualizeHTNDomain(DomainDefinition d, Symbol root) throws Exception {
        List<Symbol> alreadyProcessed = new LinkedList<>();
        
        return visualizeHTNTask(d,root,alreadyProcessed);
    }
    
    
    public BufferedImage visualizeHTNOperator(Symbol root) throws Exception {
        int textWidth = fm.stringWidth(root.get());
        BufferedImage img = new BufferedImage(hpadding*2+textWidth, vpadding*2+fm.getHeight() , BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setColor(new Color(0,127,0));
        g2d.fillRect(0, 0,img.getWidth(), img.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.drawString(root.get(), hpadding, vpadding+fm.getHeight());
        return img;
    }    


    public BufferedImage visualizeHTNOperator(Term t) throws Exception {
        String name = t.toString();
        int textWidth = fm.stringWidth(name);
        BufferedImage img = new BufferedImage(hpadding*2+textWidth, vpadding*2+fm.getHeight() , BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setColor(new Color(0,127,0));
        g2d.fillRect(0, 0,img.getWidth(), img.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.drawString(name, hpadding, vpadding+fm.getHeight());
        return img;
    }    
    
    
    
    public BufferedImage visualizeHTNTask(DomainDefinition d, Symbol root, List<Symbol> alreadyProcessed) throws Exception {
        if (alreadyProcessed.contains(root)) {
            if (DEBUG>=1) System.out.println("visualizeHTNTask (already processed): " + root);
            int textWidth = fm.stringWidth(root.get());
            BufferedImage img = new BufferedImage(hpadding*2+textWidth, vpadding*2+fm.getHeight() , BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setFont(font);
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0,img.getWidth(), img.getHeight());
            g2d.setColor(Color.WHITE);
            g2d.drawString(root.get(), hpadding, vpadding+fm.getHeight());
            return img;
        } else {        
            if (DEBUG>=1) System.out.println("visualizeHTNTask: " + root);
            alreadyProcessed.add(root);
            List<HTNMethod> methods = d.getMethodsForGoal(new Symbol(root));

            int textWidth = fm.stringWidth(root.get());
            int methodWidth = 0;
            int methodHeight = 0;

            List<BufferedImage> images = new LinkedList<>();
            for(HTNMethod m:methods) {
                BufferedImage img2 = visualizeHTNMethod(d, m, alreadyProcessed);
                images.add(img2);
                methodWidth+=img2.getWidth();
                methodHeight = Math.max(methodHeight, img2.getHeight());
            }
            methodWidth += (methods.size())*hMethodPadding;

            int width = Math.max(textWidth+2*hpadding, methodWidth);
            BufferedImage img = new BufferedImage(width, fm.getHeight()+2*vpadding + vMethodPadding+methodHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setFont(font);
            if (DEBUG>=1) System.out.println("  visualizeHTNTask img: " + img.getWidth() + " x " + img.getHeight() + " (methodHeight: " + methodHeight + ")");  
            
            int x = 0;
            for(BufferedImage img2:images) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(width/2, fm.getHeight() + vpadding*2, x+img2.getWidth()/2, fm.getHeight() + vpadding*2 + vMethodPadding);
                
                g2d.drawImage(img2,x,fm.getHeight() + vpadding*2 + vMethodPadding, null);
                x+=img2.getWidth()+hMethodPadding;
            }            
            
            g2d.setColor(Color.BLACK);
            g2d.fillRect(width/2 - (textWidth+2*hpadding)/2, 0, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
            g2d.setColor(Color.WHITE);
            g2d.drawString(root.get(), width/2 - (textWidth+2*hpadding)/2 + hpadding, vpadding+fm.getHeight());
            
            return img;
        }
    }

        
    public BufferedImage visualizeHTNMethod(DomainDefinition d, HTNMethod m, List<Symbol> alreadyProcessed) throws Exception {
        List<Symbol> children = new LinkedList<>();
        List<BufferedImage> images = new LinkedList<>();

        if (DEBUG>=1) System.out.println("visualizeHTNMethod: " + m.getName());        
        
        // generate the children:
        List<MethodDecomposition> stack = new LinkedList<>();
        stack.add(m.getDecomposition());
        while(!stack.isEmpty()) {
            MethodDecomposition md = stack.remove(0);
            switch(md.getType()) {
                case MethodDecomposition.METHOD_OPERATOR:
                        children.add(md.getTerm().getFunctor());
                        break;
                case MethodDecomposition.METHOD_METHOD:
                        children.add(md.getTerm().getFunctor());
                        break;
                case MethodDecomposition.METHOD_SEQUENCE:
                    stack.addAll(Arrays.asList(md.getSubparts()));
                        break;
                case MethodDecomposition.METHOD_PARALLEL:
                    stack.addAll(Arrays.asList(md.getSubparts()));
                        break;
                case MethodDecomposition.METHOD_CONDITION:
                        break;
            }
        }
        

        int textWidth = fm.stringWidth(m.getName());
        int childrenWidth = 0;
        int childrenHeight = 0;
        
        for(Symbol c:children) {
            if (c.get().startsWith("!")) {
                // operator:
                BufferedImage img = visualizeHTNOperator(c);
                images.add(img);
                childrenWidth+=img.getWidth();
                childrenHeight = Math.max(childrenHeight, img.getHeight());
            } else {
                // task:
                BufferedImage img = visualizeHTNTask(d, c, alreadyProcessed);
                images.add(img);
                childrenWidth+=img.getWidth();
                childrenHeight = Math.max(childrenHeight, img.getHeight());
            }
        }
        childrenWidth += (children.size())*hMethodPadding;

        int width = Math.max(textWidth+2*hpadding, childrenWidth);
        BufferedImage img = new BufferedImage(width, fm.getHeight() + vpadding*2 + vMethodPadding+childrenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        if (DEBUG>=1) System.out.println("  visualizeHTNMethod img: " + img.getWidth() + " x " + img.getHeight());  

        int x = 0;
        for(BufferedImage img2:images) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(width/2, fm.getHeight() + vpadding*2, x+img2.getWidth()/2, fm.getHeight() + vpadding*2 + vMethodPadding);

            g2d.drawImage(img2,x,fm.getHeight() + vpadding*2 + vMethodPadding, null);
            x+=img2.getWidth()+hMethodPadding;
        }            

        g2d.setColor(Color.GRAY);
        g2d.fillRect(width/2 - (textWidth+2*hpadding)/2, 0, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(m.getName(), width/2 - (textWidth+2*hpadding)/2 + hpadding, vpadding+fm.getHeight());

        return img;
    }
    
    
    /*
    The difference between this and the previous methods is that this one visualizes an instantiated
    plan, whereas the previous one is designed to visualize the domain definition of a given method.
    */
    public BufferedImage visualizeHTNPlan(HTNMethod m) throws Exception {
        List<BufferedImage> images = new LinkedList<>();
        String m_head_name = m.getHead().toString();
        String m_name = m.getName();
        int textWidth = Math.max(fm.stringWidth(m_name),fm.stringWidth(m_head_name));
        int childrenWidth = 0;
        int childrenHeight = 0;

        if (DEBUG>=1) System.out.println("visualizeHTNPlan: " + m_name);
        
        // generate the children:
        List<MethodDecomposition> stack = new LinkedList<>();
        if (m.getDecomposition()!=null) stack.add(m.getDecomposition());
        while(!stack.isEmpty()) {
            MethodDecomposition md = stack.remove(0);
            switch(md.getType()) {
                case MethodDecomposition.METHOD_OPERATOR:
                        {
                            BufferedImage img2 = visualizeHTNOperator(md.getTerm());
                            images.add(img2);
                            childrenWidth+=img2.getWidth();
                            childrenHeight = Math.max(childrenHeight, img2.getHeight());
                        }
                        break;
                case MethodDecomposition.METHOD_METHOD:
                        {
                            BufferedImage img2 = visualizeHTNPlan(md);
                            images.add(img2);
                            childrenWidth+=img2.getWidth();
                            childrenHeight = Math.max(childrenHeight, img2.getHeight());
                        }
                        break;
                case MethodDecomposition.METHOD_SEQUENCE:
                    stack.addAll(Arrays.asList(md.getSubparts()));
                        break;
                case MethodDecomposition.METHOD_PARALLEL:
                    stack.addAll(Arrays.asList(md.getSubparts()));
                        break;
                case MethodDecomposition.METHOD_CONDITION:
                        break;
            }
        }
        
        childrenWidth += (images.size())*hMethodPadding;

        int width = Math.max(textWidth+2*hpadding, childrenWidth);
        BufferedImage img = new BufferedImage(width, (fm.getHeight() + vpadding*2)*2 + vMethodPadding+childrenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        if (DEBUG>=1) System.out.println("  visualizeHTNMethod img: " + img.getWidth() + " x " + img.getHeight());  

        int x = 0;
        for(BufferedImage img2:images) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(width/2, (fm.getHeight() + vpadding*2)*2, x+img2.getWidth()/2, (fm.getHeight() + vpadding*2)*2 + vMethodPadding);

            g2d.drawImage(img2,x, (fm.getHeight() + vpadding*2)*2 + vMethodPadding, null);
            x+=img2.getWidth()+hMethodPadding;
        }            

        g2d.setColor(Color.GRAY);
        g2d.fillRect(width/2 - (textWidth+2*hpadding)/2, 0, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(m_head_name, width/2 - (textWidth+2*hpadding)/2 + hpadding, vpadding+fm.getHeight());

        g2d.setColor(Color.BLACK);
        g2d.fillRect(width/2 - (textWidth+2*hpadding)/2, fm.getHeight() + vpadding*2, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(m_name, width/2 - (textWidth+2*hpadding)/2 + hpadding, fm.getHeight() + vpadding*2 + vpadding+fm.getHeight());

        return img;        
    }    
    
    
    public BufferedImage visualizeHTNPlan(MethodDecomposition m) throws Exception {
        if (m.getType() == MethodDecomposition.METHOD_METHOD && 
            m.getMethod()!=null) return visualizeHTNPlan(m.getMethod());
            
        List<BufferedImage> images = new LinkedList<>();
        String m_name = "-";
        if (m.getTerm()!=null) m_name = m.getTerm().toString();
        int textWidth = fm.stringWidth(m_name);
        int childrenWidth = 0;
        int childrenHeight = 0;

        if (DEBUG>=1) System.out.println("visualizeHTNPlan: " + m_name);
        
        // generate the children:
        List<MethodDecomposition> stack = new LinkedList<>();
        stack.add(m);
        while(!stack.isEmpty()) {
            MethodDecomposition md = stack.remove(0);
            switch(md.getType()) {
                case MethodDecomposition.METHOD_OPERATOR:
                        {
                            BufferedImage img2 = visualizeHTNOperator(md.getTerm());
                            images.add(img2);
                            childrenWidth+=img2.getWidth();
                            childrenHeight = Math.max(childrenHeight, img2.getHeight());
                        }
                        break;
                case MethodDecomposition.METHOD_METHOD:
                        {
                            if (md.getMethod()!=null) {
                                BufferedImage img2 = visualizeHTNPlan(md.getMethod());
                                images.add(img2);
                                childrenWidth+=img2.getWidth();
                                childrenHeight = Math.max(childrenHeight, img2.getHeight());
                            } else {
                                if (md!=m) {
                                    BufferedImage img2 = visualizeHTNPlan(md);
                                    images.add(img2);
                                    childrenWidth+=img2.getWidth();
                                    childrenHeight = Math.max(childrenHeight, img2.getHeight());
                                }
                            }
                        }
                        break;
                case MethodDecomposition.METHOD_SEQUENCE:
                    stack.addAll(Arrays.asList(md.getSubparts()));
                        break;
                case MethodDecomposition.METHOD_PARALLEL:
                    stack.addAll(Arrays.asList(md.getSubparts()));
                        break;
                case MethodDecomposition.METHOD_CONDITION:
                        break;
            }
        }
        
        childrenWidth += (images.size())*hMethodPadding;

        int width = Math.max(textWidth+2*hpadding, childrenWidth);
        BufferedImage img = new BufferedImage(width, fm.getHeight() + vpadding*2 + vMethodPadding+childrenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        if (DEBUG>=1) System.out.println("  visualizeHTNMethod img: " + img.getWidth() + " x " + img.getHeight());  

        int x = 0;
        for(BufferedImage img2:images) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(width/2, fm.getHeight() + vpadding*2, x+img2.getWidth()/2, fm.getHeight() + vpadding*2 + vMethodPadding);

            g2d.drawImage(img2,x,fm.getHeight() + vpadding*2 + vMethodPadding, null);
            x+=img2.getWidth()+hMethodPadding;
        }            

        g2d.setColor(Color.GRAY);
        g2d.fillRect(width/2 - (textWidth+2*hpadding)/2, 0, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(m_name, width/2 - (textWidth+2*hpadding)/2 + hpadding, vpadding+fm.getHeight());

        return img;        
    }      
    
}

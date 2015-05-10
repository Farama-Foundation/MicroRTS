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
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author santi
 */
public class HTNDomainVisualizerVertical {
    public static void main(String args[]) throws Exception {
        {
            DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-lowest-level.lisp");
            HTNDomainVisualizerVertical v = new HTNDomainVisualizerVertical();
            BufferedImage img = v.visualizeHTNDomain(dd, new Symbol("destroy-player"));
            ImageIO.write(img, "png", new File("HTN-domain-lowest-level.png"));
        }
        {
            DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-low-level.lisp");
            HTNDomainVisualizerVertical v = new HTNDomainVisualizerVertical();
            BufferedImage img = v.visualizeHTNDomain(dd, new Symbol("destroy-player"));
            ImageIO.write(img, "png", new File("HTN-domain-low-level.png"));
        }
        {
            DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-portfolio.lisp");
            HTNDomainVisualizerVertical v = new HTNDomainVisualizerVertical();
            BufferedImage img = v.visualizeHTNDomain(dd, new Symbol("destroy-player"));
            ImageIO.write(img, "png", new File("HTN-domain-portfolio.png"));
        }
        {
            DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-flexible-portfolio.lisp");
            HTNDomainVisualizerVertical v = new HTNDomainVisualizerVertical();
            BufferedImage img = v.visualizeHTNDomain(dd, new Symbol("destroy-player"));
            ImageIO.write(img, "png", new File("HTN-domain-flexible-portfolio.png"));
        }
        {
            DomainDefinition dd = DomainDefinition.fromLispFile("ahtn/microrts-ahtn-definition-flexible-single-target-portfolio.lisp");
            HTNDomainVisualizerVertical v = new HTNDomainVisualizerVertical();
            BufferedImage img = v.visualizeHTNDomain(dd, new Symbol("destroy-player"));
            ImageIO.write(img, "png", new File("HTN-domain-flexible-single-target-portfolio.png"));
        }
    }    
    
    
    int hpadding = 8;
    int vpadding = 4;
    int hMethodPadding = 32;
    int vMethodPadding  = 8;
    Font font = null;
    FontMetrics fm = null;
    
    
    public HTNDomainVisualizerVertical() {
        font = new Font("Arial", Font.PLAIN, 16);
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
        g2d.drawString(root.get(), hpadding, vpadding+fm.getHeight() - 2);
        return img;
    }    

    
    public BufferedImage visualizeHTNTask(DomainDefinition d, Symbol root, List<Symbol> alreadyProcessed) throws Exception {
        if (alreadyProcessed.contains(root)) {
            System.out.println("visualizeHTNTask (already processed): " + root);
            int textWidth = fm.stringWidth(root.get());
            BufferedImage img = new BufferedImage(hpadding*2+textWidth, vpadding*2+fm.getHeight() , BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setFont(font);
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0,img.getWidth(), img.getHeight());
            g2d.setColor(Color.WHITE);
            g2d.drawString(root.get(), hpadding, vpadding+fm.getHeight() - 2);
            return img;
        } else {        
            System.out.println("visualizeHTNTask: " + root);
            alreadyProcessed.add(root);
            List<HTNMethod> methods = d.getMethodsForGoal(new Symbol(root));

            int textWidth = fm.stringWidth(root.get());
            int methodWidth = 0;
            int methodHeight = 0;

            List<BufferedImage> images = new LinkedList<>();
            for(HTNMethod m:methods) {
                BufferedImage img2 = visualizeHTNMethod(d, m, alreadyProcessed);
                images.add(img2);
                methodWidth = Math.max(methodWidth, img2.getWidth());
                methodHeight += img2.getHeight();
            }
            methodHeight += (methods.size()-1)*vMethodPadding;

            int width = textWidth+2*hpadding + hMethodPadding + methodWidth;
            int height = Math.max(fm.getHeight()+2*vpadding,methodHeight);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setFont(font);
            System.out.println("  visualizeHTNTask img: " + img.getWidth() + " x " + img.getHeight() + " (methodHeight: " + methodHeight + ")");  
            
            int y = 0;
            for(BufferedImage img2:images) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(textWidth+2*hpadding, height/2, textWidth+2*hpadding + hMethodPadding, y+img2.getHeight()/2);
                
                System.out.println("  drawing image at: " + (textWidth+2*hpadding + hMethodPadding) + ", " + y);
                g2d.drawImage(img2,textWidth+2*hpadding + hMethodPadding,y, null);
                y+=img2.getHeight()+vMethodPadding;
            }            
            
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0,height/2 - (fm.getHeight()+2*vpadding)/2, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
            g2d.setColor(Color.WHITE);
            g2d.drawString(root.get(), hpadding, height/2 - (fm.getHeight()+2*vpadding)/2 + fm.getHeight() + vpadding - 2);
            
            return img;
        }
    }
    
    public BufferedImage visualizeHTNMethod(DomainDefinition d, HTNMethod m, List<Symbol> alreadyProcessed) throws Exception {
        List<Symbol> children = new LinkedList<>();
        List<BufferedImage> images = new LinkedList<>();

        System.out.println("visualizeHTNMethod: " + m.getName());        
        
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
                        for(MethodDecomposition md2:md.getSubparts()) {
                            stack.add(md2);
                        }
                        break;
                case MethodDecomposition.METHOD_PARALLEL:
                        for(MethodDecomposition md2:md.getSubparts()) {
                            stack.add(md2);
                        }
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
                BufferedImage img2 = visualizeHTNOperator(c);
                images.add(img2);
                childrenWidth = Math.max(childrenWidth, img2.getWidth());
                childrenHeight += img2.getHeight();
            } else {
                // task:
                BufferedImage img2 = visualizeHTNTask(d, c, alreadyProcessed);
                images.add(img2);
                childrenWidth = Math.max(childrenWidth, img2.getWidth());
                childrenHeight += img2.getHeight();
            }
        }
        childrenHeight += (children.size()-1)*vMethodPadding;

        int width = textWidth+2*hpadding + hMethodPadding + childrenWidth;
        int height = Math.max(fm.getHeight()+2*vpadding,childrenHeight);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);        
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        System.out.println("  visualizeHTNMethod img: " + img.getWidth() + " x " + img.getHeight());  

        int y = 0;
        for(BufferedImage img2:images) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(textWidth+2*hpadding, height/2, textWidth+2*hpadding + hMethodPadding, y+img2.getHeight()/2);

            g2d.drawImage(img2,textWidth+2*hpadding + hMethodPadding,y, null);
            y+=img2.getHeight()+vMethodPadding;
        }            

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0,height/2 - (fm.getHeight()+2*vpadding)/2, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(m.getName(), hpadding, height/2 - (fm.getHeight()+2*vpadding)/2 + fm.getHeight() + vpadding - 2);

        return img;
    }
    
    
    /*
    The difference between this and the previous methods is that this one visualizes an instantiated
    plan, whereas the previous one is designed to visualize the domain definition of a given method.
    */
    public BufferedImage visualizeHTNPlan(HTNMethod m) throws Exception {
        List<BufferedImage> images = new LinkedList<>();
        String m_name = m.getName();
        int textWidth = fm.stringWidth(m_name);
        int childrenWidth = 0;
        int childrenHeight = 0;

        System.out.println("visualizeHTNPlan: " + m_name);
        
        // generate the children:
        List<MethodDecomposition> stack = new LinkedList<>();
        stack.add(m.getDecomposition());
        while(!stack.isEmpty()) {
            MethodDecomposition md = stack.remove(0);
            switch(md.getType()) {
                case MethodDecomposition.METHOD_OPERATOR:
                        {
                            BufferedImage img2 = visualizeHTNOperator(md.getTerm().getFunctor());
                            images.add(img2);
                            childrenWidth = Math.max(childrenWidth, img2.getWidth());
                            childrenHeight += img2.getHeight();
                        }
                        break;
                case MethodDecomposition.METHOD_METHOD:
                        {
                            if (md.getMethod()!=null) {
                                BufferedImage img2 = visualizeHTNPlan(md.getMethod());
                                images.add(img2);
                                childrenWidth = Math.max(childrenWidth, img2.getWidth());
                                childrenHeight += img2.getHeight();
                            }
                        }
                        break;
                case MethodDecomposition.METHOD_SEQUENCE:
                        for(MethodDecomposition md2:md.getSubparts()) {
                            stack.add(md2);
                        }
                        break;
                case MethodDecomposition.METHOD_PARALLEL:
                        for(MethodDecomposition md2:md.getSubparts()) {
                            stack.add(md2);
                        }
                        break;
                case MethodDecomposition.METHOD_CONDITION:
                        break;
            }
        }
        
        childrenHeight += (images.size()-1)*vMethodPadding;

        int width = textWidth+2*hpadding + hMethodPadding + childrenWidth;
        int height = Math.max(fm.getHeight()+2*vpadding,childrenHeight);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);        
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        System.out.println("  visualizeHTNPlan img: " + img.getWidth() + " x " + img.getHeight());  

        int y = 0;
        for(BufferedImage img2:images) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(textWidth+2*hpadding, height/2, textWidth+2*hpadding + hMethodPadding, y+img2.getHeight()/2);

            g2d.drawImage(img2,textWidth+2*hpadding + hMethodPadding,y, null);
            y+=img2.getHeight()+vMethodPadding;
        }            

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0,height/2 - (fm.getHeight()+2*vpadding)/2, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(m_name, hpadding, height/2 - (fm.getHeight()+2*vpadding)/2 + fm.getHeight() + vpadding - 2);

        return img;
    }    
    
    
    public BufferedImage visualizeHTNPlan(MethodDecomposition m) throws Exception {
        List<BufferedImage> images = new LinkedList<>();
        String m_name = "-";
        if (m.getTerm()!=null) m_name = m.getTerm().toString();
        int textWidth = fm.stringWidth(m_name);
        int childrenWidth = 0;
        int childrenHeight = 0;

        System.out.println("visualizeHTNPlan: " + m_name);
        
        // generate the children:
        List<MethodDecomposition> stack = new LinkedList<>();
        stack.add(m);
        while(!stack.isEmpty()) {
            MethodDecomposition md = stack.remove(0);
            switch(md.getType()) {
                case MethodDecomposition.METHOD_OPERATOR:
                        {
                            BufferedImage img2 = visualizeHTNOperator(md.getTerm().getFunctor());
                            images.add(img2);
                            childrenWidth = Math.max(childrenWidth, img2.getWidth());
                            childrenHeight += img2.getHeight();
                        }
                        break;
                case MethodDecomposition.METHOD_METHOD:
                        {
                            if (md.getMethod()!=null) {
                                BufferedImage img2 = visualizeHTNPlan(md.getMethod());
                                images.add(img2);
                                childrenWidth = Math.max(childrenWidth, img2.getWidth());
                                childrenHeight += img2.getHeight();
                            }
                        }
                        break;
                case MethodDecomposition.METHOD_SEQUENCE:
                        for(MethodDecomposition md2:md.getSubparts()) {
                            stack.add(md2);
                        }
                        break;
                case MethodDecomposition.METHOD_PARALLEL:
                        for(MethodDecomposition md2:md.getSubparts()) {
                            stack.add(md2);
                        }
                        break;
                case MethodDecomposition.METHOD_CONDITION:
                        break;
            }
        }
        
        childrenHeight += (images.size()-1)*vMethodPadding;

        int width = textWidth+2*hpadding + hMethodPadding + childrenWidth;
        int height = Math.max(fm.getHeight()+2*vpadding,childrenHeight);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);        
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        System.out.println("  visualizeHTNPlan img: " + img.getWidth() + " x " + img.getHeight());  

        int y = 0;
        for(BufferedImage img2:images) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(textWidth+2*hpadding, height/2, textWidth+2*hpadding + hMethodPadding, y+img2.getHeight()/2);

            g2d.drawImage(img2,textWidth+2*hpadding + hMethodPadding,y, null);
            y+=img2.getHeight()+vMethodPadding;
        }            

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0,height/2 - (fm.getHeight()+2*vpadding)/2, textWidth+2*hpadding, fm.getHeight() + vpadding*2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(m_name, hpadding, height/2 - (fm.getHeight()+2*vpadding)/2 + fm.getHeight() + vpadding - 2);

        return img;
    }    
    
}

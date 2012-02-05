import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Mandelbrot2 extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    
    private JMenuItem[] menuItem;
    private JFrame frame;
    private int panelWidth, panelHeight;
    private int iterations;
    private Rectangle2D.Double region;
    private BufferedImage MSet;
    private int xValue, yValue, rectWidth, rectHeight;
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(MSet,0,0,null);
        g.setColor(Color.RED);
        g.drawRect(xValue,yValue,rectWidth,rectHeight);
        g.drawRect(xValue+1,yValue+1,rectWidth-2,rectHeight-2);
        drawInfo(g);
    }
    public void drawInfo(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Courier",Font.PLAIN,12));
        String regionInfo1 = "topLeftX = " + region.getX() + "  topLeftY = " + region.getY();
        String regionInfo2 = "width = " + region.getWidth() + "  height = " + region.getHeight();
        g.drawString(regionInfo1,10,panelHeight-20);
        g.drawString(regionInfo2,10,panelHeight-5);
    }
    public void mousePressed(MouseEvent e) {
        xValue = e.getX();
        yValue = e.getY();
    }
    public void mouseReleased(MouseEvent e) {
        if (rectWidth > 10) {
            region = getRegion(xValue,yValue,rectWidth,rectHeight);
            MSet = new BufferedImage(panelWidth,panelHeight,BufferedImage.TYPE_INT_RGB);
            updateImage(MSet);
        }
        xValue = 0;
        yValue = 0;
        rectWidth = -1;
        rectHeight = -1;
        repaint();
    }
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {
        if (e.getX() < getWidth() && e.getY() < getHeight()) {
           rectWidth = e.getX()-xValue;
           rectHeight = 2*rectWidth/3;
        }
        repaint();
    }
    public void actionPerformed(ActionEvent e) {
       Object source = e.getSource();
       if (source == menuItem[0]) 
           saveImage();
       else if (source == menuItem[1])
           System.exit(0);
       else if (source == menuItem[2]) {
           iterations = 256;
       }
       else if (source == menuItem[3]) {
           iterations = 512;
       }
       else if (source == menuItem[4]) {
           iterations = 1024;
       }
       else if (source == menuItem[5]) {
           region = new Rectangle2D.Double(-2.0,1.0,3.0,2.0);
           MSet = new BufferedImage(panelWidth,panelHeight,BufferedImage.TYPE_INT_RGB);
           updateImage(MSet);
           repaint();
       }
       else if (source == menuItem[6]) {
           frame.setSize(606,451);
       }
       else if (source == menuItem[7]) {
           frame.setSize(906,651);
       }
    }
    public Point2D.Double scalePoint(int x, int y) {
        Point2D.Double c = new Point2D.Double();
        double scale = panelWidth/region.getWidth();
        double transX = -scale*region.getX();
        double transY = scale*region.getY();
        c.setLocation((x-transX)/scale,(transY-y)/scale);
        return c;
    }
    public Color checkValue(Point2D.Double c) {
        Point2D.Double z = new Point2D.Double(0.0,0.0);
        for (int i = 0; i < iterations; i++) {
            z = getSum(getSquare(z),c);
            if (getModulus(z)>4) {
                for (int n = 0; n < 20; n++) {
                    if (i >= 0 && i < Math.pow(2,(double)n/2+1)) {
                        return new Color(n*14,n*14,255);
                    }
                }
            }
        }
        return new Color(0,0,0);
    }
    public Point2D.Double getSquare(Point2D.Double z) {
        Point2D.Double square = new Point2D.Double();
        square.setLocation(z.getX()*z.getX()-z.getY()*z.getY(),2*z.getX()*z.getY());
        return square;
    }
    public Point2D.Double getSum(Point2D.Double z1, Point2D.Double z2) {
        Point2D.Double sum = new Point2D.Double();
        sum.setLocation(z1.getX()+z2.getX(),z1.getY()+z2.getY());
        return sum;
    }
    public double getModulus(Point2D.Double z) {
        double reZ = z.getX();
        double imZ = z.getY();
        double modulus = reZ*reZ+imZ*imZ;
        return modulus;
    }
    public Rectangle2D.Double getRegion(int xValue, int yValue, int rectWidth, int rectHeight) {
        Point2D.Double topLeft = new Point2D.Double();
        topLeft.setLocation(scalePoint(xValue,yValue));
        Point2D.Double bottomRight = new Point2D.Double();
        bottomRight.setLocation(scalePoint(xValue+rectWidth,yValue+rectHeight));
        double width = Math.abs(bottomRight.getX()-topLeft.getX());
        double height = Math.abs(bottomRight.getY()-topLeft.getY());
        return new Rectangle2D.Double(topLeft.getX(),topLeft.getY(),width,height);
    }
    public void updateImage(BufferedImage image) {
        Graphics g = image.getGraphics();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                g.setColor(checkValue(scalePoint(x,y)));
                g.drawLine(x,y,x,y);
            }
        }
        g.dispose();
    }
    public void saveImage() {
        try {
            ImageIO.write(MSet,"PNG",new File("MSet.png"));
        }
        catch (IOException e) {}
    }
    public Mandelbrot2() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu toolsMenu = new JMenu("Tools");
        JMenu iterationsMenu = new JMenu("Iterations");
        JMenu viewMenu = new JMenu("View");
        menuItem = new JMenuItem[8];
        menuItem[0] = new JMenuItem("Save Image");
        menuItem[1] = new JMenuItem("Exit");
        menuItem[2] = new JMenuItem("256");
        menuItem[3] = new JMenuItem("512");
        menuItem[4] = new JMenuItem("1024");
        menuItem[5] = new JMenuItem("Reset");
        menuItem[6] = new JMenuItem("600 x 400");
        menuItem[7] = new JMenuItem("900 x 600");
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(viewMenu);
        toolsMenu.add(iterationsMenu);
        toolsMenu.add(menuItem[5]);
        for (int i = 0; i < 2; i++)
            fileMenu.add(menuItem[i]);
        for (int i = 2; i < 5; i++)
            iterationsMenu.add(menuItem[i]);
        for (int i = 6; i < 8; i++)
            viewMenu.add(menuItem[i]);
        for (int i = 0; i < 8; i++)
            menuItem[i].addActionListener(this);
        frame = new JFrame("Mandelbrot 2.0");
        panelWidth = 900;
        panelHeight = 600;
        iterations = 1024;
        region = new Rectangle2D.Double(-2.0,1.0,3.0,2.0);
        MSet = new BufferedImage(panelWidth,panelHeight,BufferedImage.TYPE_INT_RGB);
        updateImage(MSet);
        addMouseListener(this);
        addMouseMotionListener(this);
        frame.setJMenuBar(menuBar);
        frame.setContentPane(this);
        frame.setSize(panelWidth+6,panelHeight+51);
        frame.setLocation(400,100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        new Mandelbrot2();
    }
    
}
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Scanner;

public class Mandelbrot3 extends JPanel implements MouseListener, MouseMotionListener, ActionListener, Runnable {
    
	private JMenuBar menuBar;
	private JMenu fileMenu;
    private JMenu toolsMenu;
    private JMenu iterationsMenu;
	private JMenu helpMenu;
    private JMenuItem[] menuItem;
    private JFrame frame;
    private int panelWidth, panelHeight;
    private int iterations;
    private Rectangle2D.Double region;
    private BufferedImage MSet, subimage, originalImage;
    private int xValue, yValue, rectWidth, rectHeight;
	private boolean imageIsAvailable;
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
		// Draw the MSet;
		// Draw the zoom rectangle;
		if (imageIsAvailable) {
			g.drawImage(MSet,0,0,null);
			g.setColor(Color.BLUE);
			g.drawRect(xValue,yValue,rectWidth,rectHeight);
			g.drawRect(xValue+1,yValue+1,rectWidth-2,rectHeight-2);
		}
		// Draw the subimage;
		// Draw the "please wait..." string;
		// Load the image in a background thread;
		else {
			g.drawImage(subimage,0,0,panelWidth,panelHeight,null);
			g.setFont(new Font("TimesRoman",Font.BOLD,20));
			g.setColor(Color.WHITE);
			g.drawString("Please wait while image loads...",600,20);
			new Thread(this).start();
		}
    }
    public void mousePressed(MouseEvent e) {
		// Record the starting location on the screen;
		if (imageIsAvailable) {
			xValue = e.getX();
			yValue = e.getY();
		}
    }
    public void mouseReleased(MouseEvent e) {
		// Store the zoomed region in subimage;
		// Calculate the actual region of the Complex Plane represented by the zoom rectangle;
		// Reset zoom rectangle parameters;
		if (imageIsAvailable) {
			if (rectWidth > 10) {
				imageIsAvailable = false;
				subimage = MSet.getSubimage(xValue,yValue,rectWidth,rectHeight);
				region = getRegion(xValue,yValue,rectWidth,rectHeight);
				MSet = new BufferedImage(panelWidth,panelHeight,BufferedImage.TYPE_INT_RGB);
			}
			xValue = 0;
			yValue = 0;
			rectWidth = -1;
			rectHeight = -1;
			repaint();
		}
    }
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {
		// Keep updating the zoom rectangle until the user stops dragging.
		if (imageIsAvailable) {
			if (e.getX() < getWidth() && e.getY() < getHeight()) {
			rectWidth = e.getX()-xValue;
			rectHeight = 2*rectWidth/3;
			}
			repaint();
		}
    }
    public void actionPerformed(ActionEvent e) {
		// Respond to Menu events
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
           MSet = originalImage;
		   repaint();
       }
	   else if (source == menuItem[6]) {
			new MandelbrotDialog().setVisible(true);
	   }
    }
	public void run() {
		// This code will execute in the background thread.
		// Call the method that loads the Mandelbrot Set using the new region provided.
		updateImage(MSet);
		imageIsAvailable = true;
		repaint();
	}
    public Point2D.Double scalePoint(int x, int y) {
		// Scales a given pixel into a complex number. 
        Point2D.Double c = new Point2D.Double();
        double scale = panelWidth/region.getWidth();
        double transX = -scale*region.getX();
        double transY = scale*region.getY();
        c.setLocation((x-transX)/scale,(transY-y)/scale);
        return c;
    }
    public Color checkValue(Point2D.Double c) {
		// This is the main algorithm of the program that generates the fractal.
		// Returns a color based on iteration results.
        Point2D.Double z = new Point2D.Double(0.0,0.0);
        for (int i = 0; i < iterations; i++) {
            z = getSum(getSquare(z),c);
            if (getModulus(z)>4) {
                for (int n = 0; n < 20; n++) {
                    if (i >= 0 && i < Math.pow(2,(double)n/2+1)) {
                        return new Color(255,n*14,n*14);
                    }
                }
            }
        }
        return new Color(0,0,0);
    }
    public Point2D.Double getSquare(Point2D.Double z) {
		// Returns the square of the given complex number.
        Point2D.Double square = new Point2D.Double();
        square.setLocation(z.getX()*z.getX()-z.getY()*z.getY(),2*z.getX()*z.getY());
        return square;
    }
    public Point2D.Double getSum(Point2D.Double z1, Point2D.Double z2) {
		// Returns the sum of the two given complex numbers.
        Point2D.Double sum = new Point2D.Double();
        sum.setLocation(z1.getX()+z2.getX(),z1.getY()+z2.getY());
        return sum;
    }
    public double getModulus(Point2D.Double z) {
		// Returns the modulus of the given complex number.
        double reZ = z.getX();
        double imZ = z.getY();
        double modulus = reZ*reZ+imZ*imZ;
        return modulus;
    }
    public Rectangle2D.Double getRegion(int xValue, int yValue, int rectWidth, int rectHeight) {
		// Calculate region of Complex Plane by scaling the points provided.
        Point2D.Double topLeft = new Point2D.Double();
        topLeft.setLocation(scalePoint(xValue,yValue));
        Point2D.Double bottomRight = new Point2D.Double();
        bottomRight.setLocation(scalePoint(xValue+rectWidth,yValue+rectHeight));
        double width = Math.abs(bottomRight.getX()-topLeft.getX());
        double height = Math.abs(bottomRight.getY()-topLeft.getY());
        return new Rectangle2D.Double(topLeft.getX(),topLeft.getY(),width,height);
    }
    public void updateImage(BufferedImage image) {
		// Update the Mandelbrot Set by scaling pixels and running them through the algorithm.
        Graphics g = image.getGraphics();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                g.setColor(checkValue(scalePoint(x,y)));
                g.drawLine(x,y,x,y);
            }
        }
        g.dispose();
		if (originalImage == null) {
			originalImage = image.getSubimage(0,0,image.getWidth(),image.getHeight());
		}
    }
    public void saveImage() {
		// Saves the current image.
		if (imageIsAvailable) {
			try {
				ImageIO.write(MSet,"PNG",new File("MSet.png"));
			}
			catch (IOException e) {}
		}
    }
	public class MandelbrotDialog extends JDialog {
		// This is a custom dialog box that displays the entire source code.
      JTextArea textArea;
      JScrollPane scrollPane;
      String code;
      Scanner in = null;
      public MandelbrotDialog() {
         super(frame,"Mandelbrot 3.0 Source Code",true);
         textArea = new JTextArea();
         scrollPane = new JScrollPane(textArea);
         setContentPane(scrollPane);
         setLocation(frame.getX()-90,frame.getY()+100);
         setSize(780,350);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setResizable(false);
         try {
            Scanner in = new Scanner(new File("Mandelbrot3.java"));
            textArea.setFont(new Font("Courier",Font.PLAIN,12));
            textArea.setMargin(new Insets(5,5,5,5));
            textArea.setEditable(false);
            while (in.hasNextLine()) {
				textArea.append(in.nextLine()+"\n");
			}
         }
         catch (IOException exp) {}
         finally {
            if (in != null)
				in.close();
         }
      }
   }
	public void setupMenu() {
		// Setup the menu items.
		menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        toolsMenu = new JMenu("Tools");
        iterationsMenu = new JMenu("Iterations");
		helpMenu = new JMenu("Help");
        menuItem = new JMenuItem[7];
        menuItem[0] = new JMenuItem("Save Image");
        menuItem[1] = new JMenuItem("Exit");
        menuItem[2] = new JMenuItem("256");
        menuItem[3] = new JMenuItem("512");
        menuItem[4] = new JMenuItem("1024");
        menuItem[5] = new JMenuItem("Reset");
		menuItem[6] = new JMenuItem("Source Code");
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        toolsMenu.add(iterationsMenu);
		menuBar.add(helpMenu);
        toolsMenu.add(menuItem[5]);
        for (int i = 0; i < 2; i++)
            fileMenu.add(menuItem[i]);
        for (int i = 2; i < 5; i++)
            iterationsMenu.add(menuItem[i]);
		helpMenu.add(menuItem[6]);
        for (int i = 0; i < 7; i++)
            menuItem[i].addActionListener(this);
	}
    public Mandelbrot3() {
		// Setup the GUI components;
		// Assign a region of the complex plane for the initial display.
		setupMenu();
        frame = new JFrame("Mandelbrot 3.0");
        panelWidth = 900;
        panelHeight = 600;
        iterations = 1024;
        region = new Rectangle2D.Double(-2.0,1.0,3.0,2.0);
        MSet = new BufferedImage(panelWidth,panelHeight,BufferedImage.TYPE_INT_RGB);
		subimage = new BufferedImage(panelWidth,panelHeight,BufferedImage.TYPE_INT_RGB);
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
        new Mandelbrot3();
    }
    
}
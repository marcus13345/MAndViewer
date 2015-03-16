import static MAndEngine.Engine.ANIMATION_CONSTANT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import MAndEngine.AppHelper;
import MAndEngine.BasicApp;
import MAndEngine.Engine;
import MAndEngine.Variable;

/**
 * does literally just about everything relating to rendering the images on the screens
 * to taking in key events to navigate around. only thing it doesn't do is load in the
 * images themselves, thats a job for item.
 * 
 * @author mgosselin
 *
 */
public class Viewer implements BasicApp {

	public static final int THUMBNAIL_SIZE = 80;

	private static int WIDTH = 800, HEIGHT = 600;

	// we need to have a home in case all else fails.
	private static final String defaultSearchDirectory = System.getenv("USERPROFILE") + "\\Desktop";

	// current selection
	private static int selection = 0;

	// rendering animation number thing that
	// looks like selection but is rendered
	// horizontally and stuff. in pixels. BC ANIMATION
	private static double scroll;

	// items list
	private ArrayList<Item> items;

	private static File currentDirectoryFile;
	private static Variable currentDirectoryVariable;

	public static int THUMB_MARGIN = 30;
	public static int FULL_WIDTH = THUMBNAIL_SIZE + THUMB_MARGIN;
	public static int Y_OFFSET = 470;

	public static int X_OFFSET_SELECTION = (int) (THUMB_MARGIN + (selection * FULL_WIDTH) - (scroll * FULL_WIDTH));

	public static int LEFT_BAR_WIDTH = FULL_WIDTH + THUMB_MARGIN;
	public static int INNER_FRAME_HEIGHT = (int) (((WIDTH - LEFT_BAR_WIDTH) / 4d) * 3d);
	public static int TOP_BAR_HEIGHT = HEIGHT - INNER_FRAME_HEIGHT;
	public static int INNER_FRAME_WIDTH = WIDTH - LEFT_BAR_WIDTH;

	private static Thread populationThread;

	//public static Encryptor encryptor = new Encryptor("");
	
	@Override
	public Dimension getResolution() {
		return new Dimension(WIDTH, HEIGHT);
	}

	private void setCurrentDir(String path) {
		currentDirectoryVariable.setValue(path);
		if (!path.equals("\\drives"))
			currentDirectoryFile = new File(path);
		else
			currentDirectoryFile = null;
	}

	@Override
	public void initialize() {
		// make sure we have our variable and file
		// we can explicitly set them as we are trying to pick up where we left
		// off
		currentDirectoryVariable = new Variable("MAndWorks\\MAndViewer", "currentDirectory", defaultSearchDirectory, false);
		currentDirectoryFile = new File(currentDirectoryVariable.getValue());

		// which we use, be it variable or file, is completely arbitrary here.
		reload();
	}

	private void reload() {

		// we need to reset ERRYTHANG well, important stuff.
		items = new ArrayList<Item>();
		selection = 0;

		try {
			populationThread.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (currentDirectoryFile != null) {
			populationThread = new Thread(new Runnable() {
				public void run() {

					repopulate();

				}
			});
			populationThread.start();
		} else {
			for (File f : File.listRoots()) {
				Item item = new Item(f.getAbsolutePath());
				if(item.getSeemsLegit()) {
					items.add(item);
				}
			}
		}

	}

	private void repopulate() {

		if (currentDirectoryFile.isDirectory()) {

			// parent?
			String parent = currentDirectoryFile.getParent();
			if (parent != null) {
				Item item = new Item(parent);
				if (item.getSeemsLegit()) {
					items.add(item);
				}
			} else {
				Item item = new Item("\\drives");
				items.add(item);
			}

			// folderssss
			for (String path : currentDirectoryFile.list()) {
				File file = new File(currentDirectoryFile.getAbsolutePath() + "\\" + path);
				if (file.isDirectory()) {
					Item item = new Item(file.getAbsolutePath());
					if (item.getSeemsLegit())
						items.add(item);
				}
			}

			// filezzzz
			for (String path : currentDirectoryFile.list()) {
				File file = new File(currentDirectoryFile.getAbsolutePath() + "\\" + path);
				if (!file.isDirectory()) {
					Item item = new Item(file.getAbsolutePath());
					if (item.getSeemsLegit())
						items.add(item);
				}
			}

		} else {

			// how did you manage that... this is specified by the system...
			// whatever, i'll reset the variable for you and we can retry...
			setCurrentDir(defaultSearchDirectory);
			repopulate();

		}
	}

	@Override
	public void resumeApp() {

	}

	@Override
	public void pauseApp() {

	}

	@Override
	public void tick() {
		int desiredScroll = selection;

		scroll -= (scroll - desiredScroll) / (ANIMATION_CONSTANT);

	}

	@Override
	public void render(Graphics2D g) {

		try {

			// this is the black box
			// NOW IN GREY
			g.setColor(new Color(200, 200, 200));
			g.fillRect(0, 0, WIDTH, HEIGHT);

			try {
				g.drawImage(getScaledImage(items.get(selection).getImage(), INNER_FRAME_WIDTH, INNER_FRAME_HEIGHT), LEFT_BAR_WIDTH, TOP_BAR_HEIGHT, null);
			} catch (Exception e) {
				// e.printStackTrace();
				// between ticks, nothing really big
			}

			g.setColor(new Color(225, 225, 225));
			g.fillRect(0, 0, WIDTH, TOP_BAR_HEIGHT);
			g.setColor(new Color(255, 127, 0));
			g.drawLine(0, TOP_BAR_HEIGHT - 1, WIDTH, TOP_BAR_HEIGHT - 1);

			g.setColor(new Color(240, 240, 240));
			g.fillRect(0, 0, FULL_WIDTH + THUMB_MARGIN, HEIGHT);
			g.setColor(new Color(255, 127, 0));
			g.drawLine(LEFT_BAR_WIDTH - 1, 0, LEFT_BAR_WIDTH - 1, HEIGHT);

			g.setColor(new Color(35, 35, 35));
			// g.setFont(Main.largerFont);

			g.drawString(currentDirectoryVariable.getValue(), LEFT_BAR_WIDTH + THUMB_MARGIN, TOP_BAR_HEIGHT / 2 - 3);
			g.drawString(items.get(selection).getName(), LEFT_BAR_WIDTH + THUMB_MARGIN, TOP_BAR_HEIGHT / 2 + 9);

			final int SIZE = Viewer.THUMBNAIL_SIZE + THUMB_MARGIN;
			int derp = 0;
			
			for (int i = ((int)(scroll - 1)); i < items.size(); i++) {
				if(i >= 0)
				g.drawImage(items.get(i).getThumbnail(), THUMB_MARGIN, -1 + (int) (THUMB_MARGIN + (i * FULL_WIDTH) - (scroll * FULL_WIDTH)), null);
				derp ++;
			}
			
			//g.drawString("" + derp, LEFT_BAR_WIDTH + THUMB_MARGIN, TOP_BAR_HEIGHT / 2 - 12);
			
			/*
			 * g.setColor(Color.WHITE); g.drawRect(THUMB_MARGIN - 2, Y_OFFSET -
			 * 2, THUMB_WIDTH + 3, THUMB_WIDTH + 3);
			 * 
			 * g.setColor(Color.BLACK); g.drawRect(THUMB_MARGIN - 1, Y_OFFSET -
			 * 1, THUMB_WIDTH + 1, THUMB_WIDTH + 1); g.drawRect(THUMB_MARGIN -
			 * 3, Y_OFFSET - 3, THUMB_WIDTH + 5, THUMB_WIDTH + 5);
			 */
		} catch (Exception e) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 800, 600);
			g.setColor(Color.WHITE);
			g.drawString("Please wait...", 0, 600);
			e.printStackTrace();
		}

	}

	private void down() {
		if (selection == items.size() - 1)
			selection = 0;
		else
			selection++;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_S) {
			down();
		}
		if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_W) {
			selection--;
			if (selection < 0)
				selection = items.size() - 1;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (items.get(selection).isEnterable()) {
				setCurrentDir(items.get(selection).getPath());
				reload();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

			if (selection == 0) {
				setCurrentDir(items.get(selection).getPath());
				reload();
			} else {
				selection = 0;
			}

		}
		if (e.getKeyCode() == KeyEvent.VK_F5) {

			reload();

		}
		if(e.getKeyCode() == KeyEvent.VK_R) {
			new Thread(new Runnable(){public void run() {
				
				while(true) {
					try{
					Thread.sleep(2000);
					}catch(Exception e) {
						
					}
					down();
				}
				
			}}).start();
		}
	}

	/**
	 * gets called every tick because of scaling, we dont particularly know the
	 * res...
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleY = (double) height / imageHeight;
		double scaleX = (double) width / imageWidth;

		// fill or fit bit
		if (scaleX > scaleY)
			scaleX = scaleY;
		else
			scaleY = scaleX;

		// give us the transform object thing
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);

		// then make the scaling algorithm thing.
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		// out new image that we need to crop onto the buffer with the right
		// dimensions.
		BufferedImage newImage = bilinearScaleOp.filter(image, new BufferedImage((int) (imageWidth * scaleX), (int) (imageHeight * scaleY), image.getType()));
		// Image newImage = image.getScaledInstance((int) (imageWidth * scaleX),
		// (int) (imageWidth * scaleY), Image.SCALE_SMOOTH);

		// make the buffer
		BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffer.getGraphics();

		int newImageWidth = newImage.getWidth(null);
		int newImageHeight = newImage.getHeight(null);

		// do math, shove it on.
		g.drawImage(newImage, (width - newImageWidth) / 2, (height - newImageHeight) / 2, null);

		// return dat
		return buffer;
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public String getTitle() {
		return "Settings";
	}

	@Override
	public Color getColor() {
		return new Color(255, 127, 0);
	}

	@Override
	public int getFramerate() {
		return 30;
	}

	@Override
	public boolean getResizable() {
		return true;
	}

	@Override
	public boolean visibleInMenu() {
		return true;
	}

	@Override
	public void resized(int width, int height) {

		WIDTH = width;
		HEIGHT = height;

		THUMB_MARGIN = 30;
		FULL_WIDTH = THUMBNAIL_SIZE + THUMB_MARGIN;
		Y_OFFSET = 470;

		X_OFFSET_SELECTION = (int) (THUMB_MARGIN + (selection * FULL_WIDTH) - (scroll * FULL_WIDTH));

		LEFT_BAR_WIDTH = FULL_WIDTH + THUMB_MARGIN;
		TOP_BAR_HEIGHT = 50;
		INNER_FRAME_HEIGHT = HEIGHT - TOP_BAR_HEIGHT;
		INNER_FRAME_WIDTH = WIDTH - LEFT_BAR_WIDTH;

	}

	@Override
	public void click() {
		// TODO Auto-generated method stub

	}

}

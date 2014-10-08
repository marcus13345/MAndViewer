

import static MAndEngine.Engine.ANIMATION_CONSTANT;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import MAndEngine.AppHelper;
import MAndEngine.BasicApp;
import MAndEngine.Engine;
import MAndEngine.Variable;

public class Viewer implements BasicApp {

	private static final int WIDTH = 800, HEIGHT = 600;
	
	// we need to have a home in case all else fails.
	private static final String defaultSearchDirectory = System.getenv("APPDATA") + "\\MAndWorks\\MAndApps\\Backgrounds\\";

	// current selection
	private int selection = 0;

	// rendering animation number thing that
	// looks like selection but is rendered
	// horizontally and stuff. in pixels. BC ANIMATION
	private double scroll;

	// TIME. pretty self explanitory.
	private static double time = 0;

	// items list
	private ArrayList<Item> items;

	private File currentDirectoryFile;
	private Variable currentDirectoryVariable;

	final int THUMB_WIDTH = 80;
	final int THUMB_MARGIN = 30;
	final int FULL_WIDTH = THUMB_WIDTH + THUMB_MARGIN;
	final int Y_OFFSET = 470;
	
	final int X_OFFSET_SELECTION = (int)(THUMB_MARGIN + (selection * FULL_WIDTH) - (scroll * FULL_WIDTH));
	
	final int LEFT_BAR_WIDTH = FULL_WIDTH + THUMB_MARGIN;
	final int INNER_FRAME_HEIGHT = (int)(((WIDTH - LEFT_BAR_WIDTH) / 4d) * 3d);
	final int TOP_BAR_HEIGHT = HEIGHT - INNER_FRAME_HEIGHT;
	final int INNER_FRAME_WIDTH = WIDTH - LEFT_BAR_WIDTH;
	
	//YES IT WORKED
	
	@Override
	public Dimension getResolution() {
		return new Dimension(WIDTH, HEIGHT);
	}

	private void setCurrentDir(String path) {
		currentDirectoryVariable.setValue(path);
		currentDirectoryFile = new File(path);
	}

	@Override
	public void initialize() {
		Item.setImageHeight(INNER_FRAME_HEIGHT); //NOW WITH MODULAR GOODIES
		// make sure we have our variable and file
		// we can explicitly set them as we are trying to pick up where we left
		// off
		currentDirectoryVariable = new Variable("MAndWorks\\MAndApps\\Settings", "WallpaperSearchDirectory", defaultSearchDirectory, false);
		currentDirectoryFile = new File(currentDirectoryVariable.getValue());

		// which we use, be it variable or file, is completely arbitrary here.
		reload();
	}

	private void reload() {

		// we need to reset ERRYTHANG well, important stuff.
		items = new ArrayList<Item>();
		selection = 0;

		new Thread(new Runnable() {
			public void run() {

				repopulate();

			}
		}).start();
	}

	private void repopulate() {

		if (currentDirectoryFile.isDirectory()) {

			// parent?
			String parent = currentDirectoryFile.getParent();
			if (parent != null) {
				Item item = new Item(parent);
				if (item.getSeemsLegit())
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
		
		
		time += 1 / 60d;
		time = Math.PI + 1; // PI IS TOP, 0/2PI is bottom
	}

	@Override
	public void render(Graphics2D g) {

		try {
			
			
			//this is the black box
			//NOW IN GREY
			g.setColor(new Color(200, 200, 200));
			g.fillRect(0, 0, WIDTH, HEIGHT);

			try{
				g.drawImage(items.get(selection).getImage(), WIDTH - items.get(selection).getImage().getWidth()/2 - INNER_FRAME_WIDTH/2, TOP_BAR_HEIGHT, null);
			}catch(Exception e) {
				//between ticks, nothing really big
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
			//g.setFont(Main.largerFont);
			int dirWidth = g.getFontMetrics().stringWidth(currentDirectoryVariable.getValue());
			
			g.drawString(currentDirectoryVariable.getValue(), LEFT_BAR_WIDTH + THUMB_MARGIN, TOP_BAR_HEIGHT / 2 + 5);
			
			
			
			for (int i = 0; i < items.size(); i++)
				g.drawImage(items.get(i).getThumbnail(), THUMB_MARGIN,  -1 + (int)(THUMB_MARGIN + (i * FULL_WIDTH) - (scroll * FULL_WIDTH)), null);

			/*
			g.setColor(Color.WHITE);
			g.drawRect(THUMB_MARGIN - 2, Y_OFFSET - 2, THUMB_WIDTH + 3, THUMB_WIDTH + 3);
			
			g.setColor(Color.BLACK);
			g.drawRect(THUMB_MARGIN - 1, Y_OFFSET - 1, THUMB_WIDTH + 1, THUMB_WIDTH + 1);
			g.drawRect(THUMB_MARGIN - 3, Y_OFFSET - 3, THUMB_WIDTH + 5, THUMB_WIDTH + 5);
			*/
		} catch (Exception e) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 800, 600);
			g.setColor(Color.WHITE);
			g.drawString("Please wait...", 0, 600);
			e.printStackTrace();
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {

		Engine.log("" + e.getKeyCode());
		
		if (e.getKeyCode() == KeyEvent.VK_D ||
				e.getKeyCode() == KeyEvent.VK_S) {
			if (selection == items.size() - 1)
				selection = 0;
			else
				selection++;
		}
		if (e.getKeyCode() == KeyEvent.VK_A ||
				e.getKeyCode() == KeyEvent.VK_W) {
			selection--;
			if (selection < 0)
				selection = items.size() - 1;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (new File(items.get(selection).getPath()).isDirectory()) {
				setCurrentDir(items.get(selection).getPath());
				reload();
			} else {
				Engine.switchApps(AppHelper.getIDbyClass("MainMenu"));
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			
			if(selection == 0) {
				Engine.switchApps(AppHelper.getIDbyClass("MainMenu"));
			} else {
				selection = 0;
			}
			
		}
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
		return false;
	}

	@Override
	public boolean visibleInMenu() {
		return true;
	}

}

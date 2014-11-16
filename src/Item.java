import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Item {

	// to track when to advance frame
	private long lastTime = System.currentTimeMillis();
	// every how many ms?
	private long interval = 100;

	// because we need something to tell us which image we're looking at!
	private int pointer = 0;

	// cropped to a thumb nail, of size 80*80
	private final BufferedImage thumbnail;

	// the original image, not cropped or anything.
	private BufferedImage[] image;

	// Absolute path, i think.
	private final String path;
	private String name;

	private boolean enterable = true;

	// we need this because im scared.
	// if literally anything in the creating of an item
	// goes MODERATELY wrong, you set this to FALSE.
	// then we don't try and render something that
	// makes no sense.
	// set this to false by the end of the constructor
	// and when it comes back to be added to the list,
	// it'll get thrown out.
	private boolean seemsLegit = true;

	public Item(String path) {

		BufferedImage thumbnail = null;
		BufferedImage[] images = null;

		try {
			final File file = new File(path);
			if (file.isDirectory()) {
				try {
					//so many locals, had to split it up
					//TODO methodize this or something
					{
						// just... like its not hard but its not properly spaced
						// out to be readable
						// so just trust drunk on life marcus that it tooootally
						// works.
						// doesn't mess with image yet though so feel free to
						// implement that.
						// yeah, TODO...
						Font font = new Font("Serif", Font.PLAIN, 50);
						thumbnail = new BufferedImage(Viewer.THUMBNAIL_SIZE, Viewer.THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = (Graphics2D) thumbnail.getGraphics();
						g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						g.setFont(font);
						FontMetrics metrics = g.getFontMetrics();
						String string = "" + file.getName();
						if (string.length() > 8) {
							string = string.substring(0, 9) + "...";
						}
						int padding = 5;
						int frameSize = Viewer.THUMBNAIL_SIZE - 2 * padding;
						Rectangle2D bounds = metrics.getStringBounds(string, g);
						BufferedImage text = new BufferedImage((int) bounds.getWidth() + 2, (int) bounds.getHeight() + 2 + metrics.getMaxDescent(), BufferedImage.TYPE_INT_ARGB);
						Graphics2D textGraphics = (Graphics2D) text.getGraphics();
						textGraphics.setFont(font);
						textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						textGraphics.setColor(Color.BLACK);
						textGraphics.drawString(string, 1, (int) bounds.getHeight());
						text = fitImageScale(text, frameSize, frameSize);
						g.setColor(new Color(220, 220, 220));
						g.fillRect(0, 0, Viewer.THUMBNAIL_SIZE, Viewer.THUMBNAIL_SIZE);
						g.setColor(new Color(170, 170, 170));
						g.drawRect(0, 0, Viewer.THUMBNAIL_SIZE - 1, Viewer.THUMBNAIL_SIZE - 1);
						g.drawImage(text, padding, padding, null);

						// now try to make a mini album thing showing us what
						// we're missing!
						// queue collage mode
					}
					
					// the image block for creating a mini album
					
					new Thread(new Runnable() {public void run() {
						createAlbum(file);
					}}).start();
					

				} catch (Exception e) {
					e.printStackTrace();
				}
				name = file.getName();

			} else if (file.isFile()) {
				enterable = false;
				if (!path.endsWith(".gif")) {
					// try and do the image thing!
					images = new BufferedImage[] { ImageIO.read(file) };
				} else {
					// do da gif ting! O YA, I CAN DO DAT NAO
					ArrayList<BufferedImage> imageList = getGif(path);
					images = new BufferedImage[imageList.size()];
					for (int i = 0; i < imageList.size(); i++)
						images[i] = imageList.get(i);
				}
				thumbnail = (fillImageScale(images[0], Viewer.THUMBNAIL_SIZE, Viewer.THUMBNAIL_SIZE));
				path = file.getAbsolutePath();

				name = file.getName();
				if(name.endsWith(".enc")) {
					name = Viewer.encryptor.nameTable.decrypt(name.substring(0, name.length() - 4));
				}
			} else if (path.equals("\\drives")) {

				name = "Drives";

				// just... like its not hard but its not properly spaced out to
				// be readable
				// so just trust drunk on life marcus that it tooootally works.
				// doesn't mess with image yet though so feel free to implement
				// that.
				// yeah, TODO...
				Font font = new Font("Serif", Font.PLAIN, 50);
				thumbnail = new BufferedImage(Viewer.THUMBNAIL_SIZE, Viewer.THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) thumbnail.getGraphics();
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.setFont(font);
				FontMetrics metrics = g.getFontMetrics();
				String string = "Drives";
				int padding = 5;
				int frameSize = Viewer.THUMBNAIL_SIZE - 2 * padding;
				Rectangle2D bounds = metrics.getStringBounds(string, g);
				BufferedImage text = new BufferedImage((int) bounds.getWidth() + 2, (int) bounds.getHeight() + 2 + metrics.getMaxDescent(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D textGraphics = (Graphics2D) text.getGraphics();
				textGraphics.setFont(font);
				textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				textGraphics.setColor(Color.BLACK);
				textGraphics.drawString(string, 1, (int) bounds.getHeight());
				text = fitImageScale(text, frameSize, frameSize);
				g.setColor(new Color(220, 220, 220));
				g.fillRect(0, 0, Viewer.THUMBNAIL_SIZE, Viewer.THUMBNAIL_SIZE);
				g.setColor(new Color(170, 170, 170));
				g.drawRect(0, 0, Viewer.THUMBNAIL_SIZE - 1, Viewer.THUMBNAIL_SIZE - 1);
				g.drawImage(text, padding, padding, null);

			}
		} catch (Exception e) {
			seemsLegit = false;
		}

		this.image = images;
		this.path = path;
		this.thumbnail = thumbnail;
	}
	
	private void createAlbum(final File file) {
		
		File[] files = file.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File file, String name) {
				return name.endsWith(".png") || name.endsWith(".jpg");
			}
		});
		
		
		BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		this.image = new BufferedImage[] {image};
		Graphics2D graphics = (Graphics2D)image.getGraphics();
		
		//could be derived but imma lazy personnn
		int currentFileCount = 0; //TODO do dat later
		
		final int ROWS = 2;
		final int COLUMNS = 2;
		
		
		for(int i = 0; i < ROWS; i ++) {
			
			for(int j = 0; j < COLUMNS; j ++) {
				
				boolean failed = false;
				
				try{
					File currentFile = files[currentFileCount];
					BufferedImage current = ImageIO.read(currentFile);
					current = fillImageScale(current, (1000 - (15*(2 + 1))) / 2, (1000 - (15*(2 + 1))) / 2);
					graphics.drawImage(current, (j * (15 + ((1000 - (15*(2 + 1))) / 2))), (i * (15 + ((1000 - (15*(2 + 1))) / 2))), null);
				}catch(Exception e) {
					failed = true;
				}
				
				if(failed) j --;
				if(currentFileCount == files.length) i = j = 2;
				currentFileCount ++;
			}
			
		}
		
	}

	public boolean getSeemsLegit() {
		return seemsLegit;
	}

	private static BufferedImage fillImageScale(BufferedImage image, int width, int height) throws IOException {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleY = (double) height / imageHeight;
		double scaleX = (double) width / imageWidth;

		// fill or fit bit
		if (scaleX < scaleY)
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

	private static BufferedImage fitImageScale(BufferedImage image, int width, int height) throws IOException {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleY = (double) height / imageHeight;
		double scaleX = (double) width / imageWidth;

		// fill or fit bit
		// heh, hutch
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

	public BufferedImage getImage() {

		if (System.currentTimeMillis() > lastTime + interval) {
			pointer++;
			if (pointer == image.length) {
				pointer = 0;
			}
			lastTime = System.currentTimeMillis();
		}

		return image[pointer];
	}

	public BufferedImage getThumbnail() {
		return thumbnail;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public static ArrayList<BufferedImage> getGif(String path) {
		try {

			ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

			String[] imageatt = new String[] { "imageLeftPosition", "imageTopPosition", "imageWidth", "imageHeight" };

			ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
			ImageInputStream ciis = ImageIO.createImageInputStream(new File(path));
			reader.setInput(ciis, false);

			int noi = reader.getNumImages(true);

			BufferedImage master = null;

			for (int i = 0; i < noi; i++) {

				BufferedImage image = reader.read(i);
				IIOMetadata metadata = reader.getImageMetadata(i);

				Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");

				NodeList children = tree.getChildNodes();

				for (int j = 0; j < children.getLength(); j++) {

					Node nodeItem = children.item(j);

					if (nodeItem.getNodeName().equals("ImageDescriptor")) {

						Map<String, Integer> imageAttr = new HashMap<String, Integer>();

						for (int k = 0; k < imageatt.length; k++) {

							NamedNodeMap attr = nodeItem.getAttributes();

							Node attnode = attr.getNamedItem(imageatt[k]);

							imageAttr.put(imageatt[k], Integer.valueOf(attnode.getNodeValue()));

						}

						// if first time round
						if (i == 0)
							master = new BufferedImage(imageAttr.get("imageWidth"), imageAttr.get("imageHeight"), BufferedImage.TYPE_INT_RGB);

						master.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"), imageAttr.get("imageTopPosition"), null);

					}
				}

				BufferedImage newThing = new BufferedImage(master.getWidth(), master.getHeight(), master.getType());
				newThing.getGraphics().drawImage(master, 0, 0, null);
				images.add(newThing);

			}

			return images;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEnterable() {
		return enterable;
	}

}
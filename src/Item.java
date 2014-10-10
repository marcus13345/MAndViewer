import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import MAndEngine.Engine;
import MAndEngine.ImageCreator;

public class Item {

	private static int scaleToHeight;

	// cropped to a thumb nail, of size 80*80
	private final BufferedImage thumbnail;

	// the original image, not cropped or anything.
	private final BufferedImage[] image;

	// Absolute path, i think.
	private final String path;
	private String name;

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
			File file = new File(path);
			if (file.isDirectory()) {
				thumbnail = ImageCreator.creatImageWithStripes(Viewer.THUMBNAIL_SIZE, Viewer.THUMBNAIL_SIZE, Color.BLUE);

				name = file.getName();

			} else {
				if (!path.endsWith(".gif")) {
					// try and do the image thing!
					images = new BufferedImage[] { ImageIO.read(file) };
				} else {
					//images = ImageIO.
					seemsLegit = false;
				}
				thumbnail = (getScaledImage(images[0], 80, 80));
				path = file.getAbsolutePath();

				name = "";
			}
		} catch (Exception e) {
			seemsLegit = false;
			System.out.println("wat: " + e.getMessage());
			System.out.println("wat was in " + path);
		}

		this.image = images;
		this.path = path;
		this.thumbnail = thumbnail;
	}

	public boolean getSeemsLegit() {
		return seemsLegit;
	}

	private static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {

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

		Engine.log("original: " + imageWidth + " x " + imageHeight);
		Engine.log("new:      " + width + " x " + height);
		Engine.log("after:    " + newImageWidth + " x " + newImageHeight);

		// do math, shove it on.
		g.drawImage(newImage, (width - newImageWidth) / 2, (height - newImageHeight) / 2, null);

		// return dat
		return buffer;
	}

	public BufferedImage getImage() {
		return image[0];
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
}
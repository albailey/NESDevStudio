package gameTools.imageHelper;

import java.awt.image.ShortLookupTable;

public class ImageFilterFactory {

	public final static int BRIGHTEN_FILTER = 1;
	
	public static ImageFilter createNewFilter(int filterType) {
		return createBrightenFilter();
	}
	
	public static ImageFilter createBrightenFilter() {
		float brightness = 0.4f;
		short brighten[] = new short[256];
        for (int i = 0; i < 256; i++) {
            short pixelValue = (short) (i * brightness);
            if (pixelValue > 255)
                pixelValue = 255;
            else if (pixelValue < 0)
                pixelValue = 0;
            brighten[i] = pixelValue;
        }
        return new ImageFilter("Brighten", new ShortLookupTable(0, brighten));
	}
}

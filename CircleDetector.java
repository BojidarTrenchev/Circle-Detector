import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class CircleDetector {
	private int[][] pixels;
	private int width;
	private int height;
	
	public static void main(String[] args) {
		CircleDetector d = new CircleDetector();
		
		d.detect("src/image.jpg");

	}
	

	public void detect(String imgPath) {
		BufferedImage img = getImage(imgPath);
		pixels = getPixelMatrix(img);
		pixels = toGreyScale(pixels);
		pixels = applySobel(pixels, 150);
		img = toImage(pixels, img);
		img = detectCircles(img);
		saveImage(img, "src/result.jpg", "jpg");
	}
	
	private BufferedImage toImage(int[][] allPixels, BufferedImage image) {

		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				image.setRGB(j, i, allPixels[j][i]);
			}
		}
		
		return image;
	}
	
	/*
	 * Makes the image greyscale
	 */
	private int[][] toGreyScale(int[][] allPixels) {
		int rgb;
		int a;
		int r;
		int g;
		int b;
		int avg;
		
		//Get every pixel
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				rgb = allPixels[x][y];

				//Get its RGB values using bitwise operations
				a = (rgb>>24)&0xff;
                r = (rgb>>16)&0xff;
                g = (rgb>>8)&0xff;
                b = rgb&0xff;
                
                //Find the average of red, green and blue
                avg = (r + g + b) / 3;
                
                //Assign the average back to the pixel
                rgb = (a<<24) | (avg<<16) | (avg<<8) | avg;
                allPixels[x][y] = rgb;

			}
		}
		
		return allPixels;
	}
	
	/*
	 * Returns the image by providing a file path
	 */
	private BufferedImage getImage(String path) {
		
		BufferedImage image = null;
		File f = null;
		
		try {
			f = new File(path);
			image = ImageIO.read(f);
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
		
		return image;
	}
	
	/*
	 * Saves the image in a given file path
	 */
	private void saveImage(BufferedImage img, String path, String type) {
		File f = null;
		
		try {
			f = new File(path);
			ImageIO.write(img, type, f);
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private BufferedImage detectCircles(BufferedImage img) {
				
		int radiusMin = 50;
		int radiusMax = 200;
		int a = 0;
		int b = 0;
		int[][][] A = new int[width][height][(radiusMax - radiusMin)];

		int radiusStep = 2;
		int thetaStep = 1;
		int[] sinTable = sinTable(thetaStep);
		int[] cosTable = cosTable(thetaStep);

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(this.pixels[x][y] == 1) {		

					for(int r = radiusMin; r < radiusMax; r += radiusStep) {

						for(int t = 0; t < 360 / thetaStep ; t += thetaStep) {				         
							a = x + r* cosTable[t]; //polar coordinate for center
							b = y + r* sinTable[t];  //polar coordinate for center 
							//if(pixels[a][b] == 1)

                            if((b >= 0) && (b < height) && (a >= 0) && (a < width)) {
                            	A[a][b][r - radiusMin] += 1;

                            }

						}
					}
				}
			}
		}


		int max = 0;
		int newX = 0; // circle center x
		int newY = 0; // circle center y
		int r = 0;// radius of that circle
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				for(int k = 0; k < A[i][j].length; k++) {
					if(max < A[i][j][k]) {
						max = A[i][j][k];
						newX = i;
						newY = j;
						r = k;
					}
				}
			}
		}
		
		System.out.println(newX);
		System.out.println(newY);
		System.out.println(r);

		
		return img;
//		System.out.println(max);
	}
	
	/*
	 * A table of all sine from 0 to 360 degrees for a given step.
	 * E.g. if step is 2, then I'll get the the values of sin(0), sine(2), sin(4)...
	 */
	private int[] sinTable(int step) {
		int[] sinTable = new int[360 / step];
		
		for(int i = 0; i < sinTable.length; i += step) {
			sinTable[i] = (int) Math.round(Math.sin(i));
		}
		
		return sinTable;
	}
	
	 /* A table of all cosine from 0 to 360 degrees for a given step.
	 /* E.g. if step is 2, then I'll get the the values of cos(0), cos(2), cos(4)...
	  */
	private int[] cosTable(int step) {
		int[] cosTable = new int[360 / step];
		
		for(int i = 0; i < cosTable.length; i += step) {
			cosTable[i] = (int) Math.round(Math.cos(i));
		}
		return cosTable;
	}
	
	/*
	 * A faster way to get a matrix of all the pictures of an image.
	 * Code from: https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
	 */
	private int[][] getPixelMatrix(BufferedImage image) {

//	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//	      final int width = image.getWidth();
//	      final int height = image.getHeight();
//	      final boolean hasAlphaChannel = image.getAlphaRaster() != null;
//
//	      int[][] result = new int[height][width];
//	      if (hasAlphaChannel) {
//	         final int pixelLength = 4;
//	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
//	            int argb = 0;
//	            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
//	            argb += ((int) pixels[pixel + 1] & 0xff); // blue
//	            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
//	            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
//	            result[row][col] = argb;
//	            col++;
//	            if (col == width) {
//	               col = 0;
//	               row++;
//	            }
//	         }
//	      } else {
//	         final int pixelLength = 3;
//	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
//	            int argb = 0;
//	            argb += -16777216; // 255 alpha
//	            argb += ((int) pixels[pixel] & 0xff); // blue
//	            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
//	            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
//	            result[row][col] = argb;
//	            col++;
//	            if (col == width) {
//	               col = 0;
//	               row++;
//	            }
//	         }
//	      }
		this.width = image.getWidth();
		this.height = image.getHeight();
		int[][] result = new int[image.getWidth()][image.getHeight()];
		
		for(int y = 0; y < image.getHeight(); y++) {
			for(int x = 0; x < image.getWidth(); x++) {
				result[x][y] = image.getRGB(x, y);
			}
		}
	      return result;
	   }
	
	private int[][] applySobel(int[][] pixels, int treshold) {		
		
		int gX = 0;
		int gY = 0;
		int g = 0;
		
		int[][] pixels2D = new int[width][height];

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				pixels2D[x][y] = pixels[x][y];
			}
		}
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				if(x == 0 || x >= width - 1 || y == 0 || y >= height - 1) {
					gX = 0;
					gY = 0;
					g = 0;
					
				}
				else {
					gX = -pixels2D[x - 1][y - 1] + pixels2D[x+1][y-1] 
							   - pixels2D[x - 1][y] * 2 + 2 * pixels2D[x + 1][y]
							   -pixels2D[x - 1][y + 1] + pixels2D[x+1][y+1];
					
					gY = pixels2D[x - 1][y - 1] + 2 * pixels2D[x][y - 1] + pixels2D[x+1][y-1]
							   -pixels2D[x - 1][y + 1] - 2 * pixels2D[x][y+1] - pixels2D[x+1][y+1];
					g = Math.abs(gX) + Math.abs(gY);
			
				}
				
                int red = (g>>16)&0xff;				
                if(red < treshold) {
                	g = 0;
                    pixels[x][y] = 0;
                }
                else {
                	g = (255<<24) | (255<<16) | (255<<8) | 255;
                	pixels[x][y] = 1;//g;
                }                
			}
		}
		
		return pixels;
	}
}

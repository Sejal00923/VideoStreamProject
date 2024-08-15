package com.videostream.application;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.MatOfRect;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoStreamGUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private JLabel videoLabel;
	private VideoCapture camera;

	public VideoStreamGUI() {
		setTitle("Real-Time Video Stream");
		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		videoLabel = new JLabel();
		add(videoLabel, BorderLayout.CENTER);
		setVisible(true);

		camera = new VideoCapture(0);
		if (!camera.isOpened()) {
			System.out.println("Error: Could not open camera.");
			return;
		}

		String haarCascadePath = "resources/haarcascade_frontalface_default.xml";
		CascadeClassifier faceDetector = new CascadeClassifier(haarCascadePath);
		if (faceDetector.empty()) {
			System.out.println("Error loading cascade classifier");
			return;
		}

		new Thread(() -> {
			Mat frame = new Mat();
			while (camera.read(frame)) {

				if (frame.empty()) {
					System.out.println("Error: Frame is empty");
					continue;
				}

				Mat grayFrame = new Mat();
				Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

				MatOfRect faces = new MatOfRect();
				faceDetector.detectMultiScale(grayFrame, faces, 1.1, 3);
				Rect[] facesArray = faces.toArray();

				for (Rect face : facesArray) {
					Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
				}

				ImageIcon imageIcon = new ImageIcon(matToBufferedImage(frame));
				videoLabel.setIcon(imageIcon);
			}
		}).start();
	}

	private BufferedImage matToBufferedImage(Mat mat) {
		int width = mat.width();
		int height = mat.height();
		int channels = mat.channels();

		BufferedImage image;

		byte[] data = new byte[width * height * channels];
		mat.get(0, 0, data);

		if (channels == 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			image.getRaster().setDataElements(0, 0, width, height, data);
		} else {

			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[width * height];

			for (int i = 0; i < data.length; i += 3) {
				int b = data[i] & 0xff; // Blue
				int g = data[i + 1] & 0xff; // Green
				int r = data[i + 2] & 0xff; // Red
				pixels[i / 3] = (r << 16) | (g << 8) | b; // RGB format
			}

			image.getRaster().setDataElements(0, 0, width, height, pixels);
		}

		return image;
	}

	public static void main(String[] args) {
		new VideoStreamGUI();
	}
}

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageProcessing extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private JPanel canvas;

	public ImageProcessing() {
		try {
			this.image = ImageIO.read(new File("example.jpg"));
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Image not found!");
		}

		JButton chooseImage = new JButton("Choose an image");
		chooseImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"JPEG PNG BMP files", "jpg", "png", "bmp");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						image = ImageIO.read(new File(chooser.getSelectedFile()
								.getAbsolutePath()));
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(canvas, "Image not found!");
					}
				}
			}
		});

		this.canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
			}
		};
		canvas.add(chooseImage);
		canvas.setPreferredSize(new Dimension(image.getWidth(), image
				.getHeight()));
		JScrollPane sp = new JScrollPane(canvas);
		setLayout(new BorderLayout());
		add(sp, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		JPanel p = new ImageProcessing();
		JFrame f = new JFrame();
		f.setContentPane(p);
		f.setSize(800, 500);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}
}
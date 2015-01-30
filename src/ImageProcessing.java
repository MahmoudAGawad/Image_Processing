import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EventListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageProcessing extends JPanel {
	private static final long serialVersionUID = 1L;
	private static Dimension dimension = java.awt.Toolkit.getDefaultToolkit()
			.getScreenSize();
	
	private static JFrame mainFrame;
	private BufferedImage image;
	private JPanel canvas;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem exit;
	private JPanel toolBar, east, west;
	private JScrollPane sp;
	private JButton chooseImage, crop, undo, reset;
	private ImageIcon icon, close;
	private JSlider zoom, rotate;

	public ImageProcessing() {
		
		Container pane = mainFrame.getContentPane();
		
		
		mainFrame.setLocation(dimension.width / 10, dimension.height / 10);
		mainFrame.setSize(dimension.width - 2 * mainFrame.getX(), dimension.height - 2
				* mainFrame.getY());
		

		pane.setLayout(new BorderLayout());

		loadImage("example.jpg");
		initGUI();

		sp.setPreferredSize(new Dimension(pane.getWidth(),
				pane.getHeight() - 50));
		this.add(sp, BorderLayout.CENTER);

		toolBar.add(chooseImage);

		west.add(rotate);
		toolBar.add(zoom);
		
		pane.add(east, BorderLayout.EAST);
		pane.add(west, BorderLayout.WEST);
		pane.add(toolBar, BorderLayout.SOUTH);

		menuBar.add(fileMenu);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(exit);
		pane.add(menuBar, BorderLayout.NORTH);
	}

	private void initGUI() {
		icon = new ImageIcon(
				((new ImageIcon("open-file-icon.png").getImage()
						.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH))));
		close = new ImageIcon(
				((new ImageIcon("close.png").getImage().getScaledInstance(64,
						64, java.awt.Image.SCALE_SMOOTH))));
		menuBar = new JMenuBar();

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				reshape();
			}
		});

		chooseImage = new JButton("open", icon);
		chooseImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"JPEG PNG BMP", "jpg", "png", "bmp");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					loadImage(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		this.canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (image != null)
					g.drawImage(image, 0, 0, null);
			}
		};

		sp = new JScrollPane(canvas);

		toolBar = new JPanel();
		west = new JPanel(new GridLayout(1, 1));
		east = new JPanel(new GridLayout(1, 1));

		fileMenu = new JMenu("File");
		exit = new JMenuItem(close);
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		zoom = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
		zoom.setPaintTicks(true);
		zoom.setMajorTickSpacing(2);
		
		rotate = new JSlider(SwingConstants.VERTICAL, 0, 720, 360);
		rotate.setPaintTicks(true);
		rotate.setMajorTickSpacing(15);
	}

	private void loadImage(String path) {
		try {
			this.image = ImageIO.read(new File(path));
			if (canvas != null)
				canvas.repaint();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Image not found!");
		}
	}

	public static void main(String[] args) {

		mainFrame = new JFrame("Image Viewer");
		mainFrame.add(new ImageProcessing(), BorderLayout.CENTER);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	private void reshape() {
		int min = Math.max(this.getHeight() / 10, 70);
		sp.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()
				- min));
		toolBar.setPreferredSize(new Dimension(this.getWidth() / 2, min));

		if (canvas != null && image != null)
			canvas.setPreferredSize(new Dimension(image.getWidth(), image
					.getHeight()));

	}

}
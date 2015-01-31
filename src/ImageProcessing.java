import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EventListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageProcessing extends JPanel {

	private static Point startPoint;
	private static Point rectLocale;
	private static Dimension rectSize;
	private int zoomValue;
	private BufferedImage selected;
	private BufferedImage originalImage;
	private BufferedImage unrotatedImage;
	

	private static final long serialVersionUID = 1L;
	private static Dimension dimension = java.awt.Toolkit.getDefaultToolkit()
			.getScreenSize();

	private static JFrame mainFrame;
	private BufferedImage image;
	private JPanel canvas;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem exit, openFile;
	private JPanel toolBar, east, west;
	private JScrollPane sp;
	private JButton cropButton, undoButton, resetButton;
	private ImageIcon openIcon, closeIcon, cropIcon, undoIcon, resetIcon;
	private JSlider zoomSlider, rotateSlider;

	public ImageProcessing() {
		initSelection();
		mainFrame.setLocation(dimension.width / 10, dimension.height / 10);
		mainFrame.setSize(dimension.width - 2 * mainFrame.getX(),
				dimension.height - 2 * mainFrame.getY());
		mainFrame.setLayout(new BorderLayout());

		loadImage("icon.jpg");
		initGUI();

		addComponent();
	}

	private void addComponent() {

		// sp.setPreferredSize(new Dimension(mainFrame.getWidth(),
		// mainFrame.getHeight() - 50));
		this.add(sp, BorderLayout.CENTER);

		toolBar.add(resetButton);
		toolBar.add(undoButton);
		toolBar.add(cropButton);

		west.add(rotateSlider);
		toolBar.add(zoomSlider);

		mainFrame.add(east, BorderLayout.EAST);
		mainFrame.add(west, BorderLayout.WEST);
		mainFrame.add(toolBar, BorderLayout.SOUTH);

		menuBar.add(fileMenu);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(openFile);
		fileMenu.add(exit);
		mainFrame.add(menuBar, BorderLayout.NORTH);
	}

	private void initButtons() {

		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 2000, zoomValue);
		zoomSlider.setMajorTickSpacing(2);
		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public synchronized void stateChanged(ChangeEvent e) {
				zoomValue = zoomSlider.getValue();
				repaint();
			}
		});
		rotateSlider = new JSlider(SwingConstants.VERTICAL, -360, 360, 0);
		rotateSlider.setPaintTicks(true);
		rotateSlider.setMajorTickSpacing(15);
		rotateSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				image=rotate(unrotatedImage, rotateSlider.getValue());
				repaint();
				
			}
		});
		

		openFile = new JMenuItem("Open", openIcon);
		openFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"JPEG PNG BMP", "jpg", "png", "bmp");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					loadImage(chooser.getSelectedFile().getAbsolutePath());
					initSelection();
					zoomSlider.setValue(zoomValue);
					reshape();
				}
			}
		});

		exit = new JMenuItem("Exit", closeIcon);
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		cropButton = new JButton("crop", cropIcon);
		cropButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selected != null) {
					image = selected;
					unrotatedImage=image;
					initSelection();
					zoomSlider.setValue(zoomValue);
					repaint();
				}
			}
		});
		resetButton = new JButton("reset", resetIcon);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image=originalImage;
				unrotatedImage=image;
				initSelection();
				zoomSlider.setValue(zoomValue);
				repaint();
			}
		});
		undoButton = new JButton("undo", undoIcon);

	}

	private void initGUI() {

		loadIcons();
		initButtons();

		mainFrame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				reshape();
				repaint();
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

		MouseBehavior behavior = new MouseBehavior();
		this.canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = ((Graphics2D) g);

				if (selected != null) {
					g2d.drawImage(image, 0, 0, null);
					int width2 = (int) (rectSize.width + rectSize.width
							* (zoomValue / 500d));
					int height2 = (int) (rectSize.height + rectSize.height
							* (zoomValue / 500d));
					int x2 = rectLocale.x - ((width2 - rectSize.width) / 2);
					int y2 = rectLocale.y - ((height2 - rectSize.height) / 2);
					Image scaledInstance = selected.getScaledInstance(width2,
							height2, Image.SCALE_AREA_AVERAGING);
					g2d.drawImage(scaledInstance, x2, y2, null);
					g2d.drawRect(x2, y2, width2, height2);
				} else {
					int width2 = (int) (image.getWidth() + image.getWidth()
							* (zoomValue / 500d));
					int height2 = (int) (image.getHeight() + image.getHeight()
							* (zoomValue / 500d));

					Image scaledInstance = image.getScaledInstance(width2,
							height2, Image.SCALE_AREA_AVERAGING);
					g2d.drawImage(scaledInstance, 0, 0, null);
					g2d.draw(new Rectangle(rectLocale, rectSize));
				}
			}
		};
		canvas.addMouseMotionListener(behavior);
		canvas.addMouseListener(behavior);
		canvas.addMouseWheelListener(behavior);

		menuBar = new JMenuBar();

		sp = new JScrollPane(canvas);

		toolBar = new JPanel();
		west = new JPanel(new GridLayout(1, 1));
		east = new JPanel(new GridLayout(1, 1));

		fileMenu = new JMenu("File");


	}

	private void loadImage(String path) {
		try {
			this.image = ImageIO.read(new File(path));
			this.originalImage=image;
			this.unrotatedImage=image;
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
	 private  BufferedImage rotate(BufferedImage pic,
             double angle) {
     int width = pic.getWidth();
     int height = pic.getHeight();
     double rotationRequired = Math.toRadians(angle);
     double locationX = pic.getWidth() / 2;
     double locationY = pic.getHeight() / 2;
     AffineTransform tx = AffineTransform.getRotateInstance(
                     rotationRequired, locationX, locationY);
     AffineTransformOp op = new AffineTransformOp(tx,
                     AffineTransformOp.TYPE_BILINEAR);

     BufferedImage img2 = new BufferedImage(width, height, 8);

     img2 = op.filter(pic, null);
     // rotation
     return img2;
}
	private void reshape() {
		int min = Math.max(this.getHeight() / 10, 70);
		this.setPreferredSize(new Dimension(mainFrame.getWidth(), mainFrame
				.getHeight() - min));

		if (canvas != null && image != null)
			canvas.setPreferredSize(new Dimension(image.getWidth(), image
					.getHeight()));
		sp.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		toolBar.setPreferredSize(new Dimension(this.getWidth(), min));
	}

	private ImageIcon loadIcon(String path) {
		return new ImageIcon(
				((new ImageIcon(path).getImage().getScaledInstance(64, 55,
						java.awt.Image.SCALE_SMOOTH))));
	}

	private void loadIcons() {
		openIcon = loadIcon("open-file-icon.png");
		closeIcon = loadIcon("close.png");
		cropIcon = loadIcon("crop.png");
		undoIcon = loadIcon("undo.png");
		resetIcon = loadIcon("reset.png");
	}

	private void initSelection() {
		startPoint = new Point(0, 0);
		rectLocale = new Point();
		rectSize = new Dimension();
		zoomValue = 0;
		selected = null;
	}

	private class MouseBehavior extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			zoomValue = 0;
			startPoint = e.getPoint();
			rectLocale = new Point();
			rectSize = new Dimension();
			selected = null;
			zoomSlider.setValue(zoomValue);
			// if (e.getSource() instanceof JComponent) {
			// ((JComponent) e.getSource()).repaint();
			// }
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point currentPoint = e.getPoint();
			rectSize.width = Math.abs(currentPoint.x - startPoint.x);
			rectSize.height = Math.abs(currentPoint.y - startPoint.y);
			if (e.isShiftDown()) {
				rectSize.width = rectSize.height = Math.min(rectSize.width,
						rectSize.height);
				int dx = startPoint.x - rectSize.width;
				int dy = startPoint.y - rectSize.height;
				rectLocale.x = startPoint.x < currentPoint.x ? startPoint.x
						: Math.max(dx, dy);
				rectLocale.y = startPoint.y < currentPoint.y ? startPoint.y
						: Math.min(dx, dy);
			} else {
				rectLocale.x = Math.min(currentPoint.x, startPoint.x);
				rectLocale.y = Math.min(currentPoint.y, startPoint.y);
			}
			if (e.getSource() instanceof JComponent) {
				((JComponent) e.getSource()).repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (rectSize.width <= 0 || rectSize.height <= 0) {
				selected = null;
			} else {
				selected = image.getSubimage(Math.max(0, rectLocale.x),
						Math.max(0, rectLocale.y), rectSize.width,
						rectSize.height);
			}
			if (e.getSource() instanceof JComponent) {
				((JComponent) e.getSource()).repaint();
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			zoomValue = Math.min(2000,
					Math.max(0, zoomValue + e.getUnitsToScroll() * 10));
			zoomSlider.setValue(zoomValue);
			if (e.getSource() instanceof JComponent) {
				((JComponent) e.getSource()).repaint();
			}
		}
	}

}
package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.elements.PlayBin;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.sun.jna.Platform;

import lib.SimpleVideoComponent;

public class PlayerFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static JSlider positionSlider = new JSlider();

	private boolean reproducir = false;

	private JPanel contentPane;

	private double segundosInicio;

	private double segundosFin;

	private PlayBin playbin;

	private final JFileChooser fileChooser = new JFileChooser();

	private JTextField duracionVideo;

	private JTextField anchoVideoTxt;

	private JTextField largoVideoTxt;

	private JTextField frameRateTxt;

	private JTextField textField_6;

	private JTextField textField_8;

	private String video;

	private int anchoVideo, largoVideo;

	private float frameRate;

	private static JLabel duracion;

	private static JLabel tiempo;

	private static long segundosActual;

	private static long duration;

	private static double position = 0;

	final JLabel playPauseButton = new JLabel("");
	private JTextField inicio;
	private JTextField fin;
	private JTextField textField;

	double convertirASegundos(String duracionVideo) {

		double horas = Double.parseDouble(duracionVideo.substring(0, duracionVideo.indexOf(":")));

		if (horas > 0) {
			horas *= 3600f;
		}

		double minutos = Double
				.parseDouble(duracionVideo.substring(duracionVideo.indexOf(":") + 1, duracionVideo.lastIndexOf(":")));

		if (minutos > 0) {
			minutos *= 60f;
		}

		double segundos = Double
				.parseDouble(duracionVideo.substring(duracionVideo.lastIndexOf(":") + 1, duracionVideo.length()));

		return horas + minutos + segundos;
	}

	private void play() {

		try {

			duration = playbin.queryDuration(TimeUnit.NANOSECONDS);

			if (reproducir || duration > 0) {

				boolean playing = playbin.isPlaying();

				if (playing) {

					playbin.pause();
				}

				else {
					playbin.play();
				}

				ponerTiempos();

				playPauseButton.setIcon(new ImageIcon(getClass().getResource("/play-pause.png")));
			}
		}

		catch (Exception e) {
		}

	}

	private void abrirVideo() throws ImageProcessingException, IOException {

		int returnValue = fileChooser.showOpenDialog(contentPane);

		reproducir = true;

		if (returnValue == JFileChooser.APPROVE_OPTION) {

			video = fileChooser.getSelectedFile().toURI().toString();

			video = video.replace("%20", " ");

			video = video.replace("file:", "");

			video = video.trim();

			playbin.stop();

			InputStream inputstream = new FileInputStream(video);

			Metadata metadata = ImageMetadataReader.readMetadata(inputstream);

			String etiqueta = "";

			for (Directory directory : metadata.getDirectories()) {

				for (com.drew.metadata.Tag tag : directory.getTags()) {

					etiqueta = tag.toString();

					etiqueta = etiqueta.replace(" pixels", "");

					if (etiqueta.contains("[MP4 Video] Width - ")) {

						anchoVideo = Integer
								.parseInt(etiqueta.substring(etiqueta.indexOf("Width - ") + 8, etiqueta.length()));

					}

					if (etiqueta.contains("[MP4 Video] Height - ")) {

						largoVideo = Integer
								.parseInt(etiqueta.substring(etiqueta.indexOf("Height - ") + 9, etiqueta.length()));

					}

					if (etiqueta.contains("[MP4 Video] Frame Rate - ")) {
						etiqueta = etiqueta.replace(",", ".");
						frameRate = Float.parseFloat(
								etiqueta.substring(etiqueta.indexOf("Frame Rate - ") + 13, etiqueta.length()));

					}
				}
			}

			anchoVideoTxt.setText("" + anchoVideo);

			largoVideoTxt.setText("" + largoVideo);

			frameRateTxt.setText("" + frameRate);

			playbin.setURI(fileChooser.getSelectedFile().toURI());

			playbin.play();

		}

		verTiempos(false);

		play();

		play();

	}

	static LinkedList<Object> saberTiempo() {

		LinkedList<Object> tiempo = new LinkedList<Object>();

		position = positionSlider.getValue() / 1000.0;

		long segundos = duration / 1000000000;

		tiempo.add(position);

		tiempo.add(segundos);

		return tiempo;

	}

	private String calcularPosicionVideo() {

		String positionVideo = "";

		if (!tiempo.getText().isEmpty()) {

			double segundos = Double.parseDouble(tiempo.getText()
					.substring(tiempo.getText().lastIndexOf(":") + 1, tiempo.getText().length()).trim());

			segundos += position;

			positionVideo = tiempo.getText().substring(0, tiempo.getText().lastIndexOf(":") + 1) + segundos;
		}

		return positionVideo;
	}

	static String calcularSegundosActual(Object obj1, Object obj2) {

		double inicio = (double) obj1;

		long segundos = (long) obj2;

		segundosActual = Math.round((inicio / segundos) * Math.pow(segundos, 2));

		return calcularTiempo(segundosActual);

	}

	public static String calcularTiempo(long segundos) {

		int minutos = 0;

		int horas = 0;

		if (segundos == 60) {
			minutos = 1;
			segundos = 0;
		}

		minutos = (int) (segundos / 60);

		int calculoSegundos = 0;

		calculoSegundos = 60 * minutos;

		segundos -= calculoSegundos;

		if (minutos == 60) {
			horas = 1;
			minutos = 0;
			segundos = 0;
		}

		if (minutos > 60) {

			if (minutos % 60 == 0) {
				horas = minutos / 60;
				minutos = 0;
				segundos = 0;
			}

			else {

				int contador = 0;

				int horaProxima = 120;

				int siguienteHora = 0;

				while (contador == 0) {

					if (minutos < horaProxima) {
						contador = horaProxima;
					}

					else {

						siguienteHora = horaProxima + 60;

						if (minutos > horaProxima && minutos < siguienteHora) {
							contador = siguienteHora;
						}

						horaProxima = siguienteHora;

					}
				}

				horas = minutos / 60;

				minutos = 60 - (horaProxima - minutos);

			}

		}

		String ceroHoras = "";
		String ceroMinutos = "";

		String ceroSegundos = "";

		if (horas <= 9) {
			ceroHoras = "0";
		}

		if (minutos <= 9) {
			ceroMinutos = "0";
		}

		if (segundos <= 9) {
			ceroSegundos = "0";
		}

		return ceroHoras + horas + " : " + ceroMinutos + minutos + " : " + ceroSegundos + segundos;

	}

	private void verTiempos(boolean adjust) {

		duration = playbin.queryDuration(TimeUnit.NANOSECONDS);

		if (duration > 0 && video != null) {

			try {

				String resultado;

				ProcessBuilder processBuilder = new ProcessBuilder("ffprobe", video);

				processBuilder.redirectErrorStream(true);

				Process process = processBuilder.start();

				StringBuilder processOutput = new StringBuilder();

				try (BufferedReader processOutputReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));) {

					String readLine;

					while ((readLine = processOutputReader.readLine()) != null) {

						if (readLine.contains("Duration")) {
							processOutput.append(readLine + System.lineSeparator());
						}

					}

				}

				resultado = processOutput.toString().trim();

				resultado = resultado.substring(resultado.indexOf("Duration") + 9, resultado.indexOf(","));

				resultado = resultado.trim();

				convertirANanoSegundos(resultado, true);

			}

			catch (Exception e) {
				e.printStackTrace();
			}

		}

		LinkedList<Object> cuenta = new LinkedList<Object>();

		cuenta = ponerTiempos();

		if (adjust)

		{

			// duration = (long) segundosFin;

			System.out.println("resultado: " + saberSegundosActual(inicio.getText(), duration / 1000000000));

			duration = (long) convertirASegundos(fin.getText()) * 1000000000;

			playbin.seek((long) (saberSegundosActual(inicio.getText(), duration / 1000000000) * duration),
					TimeUnit.NANOSECONDS);

		}

		else {
			System.out.println("duracion: " + duration);
			playbin.seek((long) ((double) cuenta.get(0) * duration), TimeUnit.NANOSECONDS);
		}

		if (adjust) {
			play();
		}

	}

	private double saberSegundosActual(String inicio, long segundosTotales) {

		double resultado = 0;

		resultado = convertirASegundos(inicio) / segundosTotales;

		return resultado;

	}

	private long convertirANanoSegundos(String duracionVideo, boolean filtro) {

		long nanosegundos;

		nanosegundos = (long) convertirASegundos(duracionVideo) * 1000000000;

		if (filtro) {

			duration = nanosegundos;

			tiempo.setText(calcularSegundosActual(0D, nanosegundos));

			duracion.setText("" + calcularTiempo((long) nanosegundos));

		}

		return nanosegundos;

	}

	static LinkedList<Object> ponerTiempos() {

		LinkedList<Object> cuenta;

		cuenta = saberTiempo();

		tiempo.setText(calcularSegundosActual(cuenta.get(0), cuenta.get(1)));

		duracion.setText("" + calcularTiempo((long) cuenta.get(1)));

		return cuenta;
	}

	private void verVideo(boolean adjust) {

		if (duration > 0 && (adjust || positionSlider.getValueIsAdjusting())) {

			verTiempos(adjust);

		}

	}

	public static void initialize(boolean windows) throws Exception {

		System.setProperty("awt.useSystemAAFontSettings", "lcd");

		System.setProperty("swing.aatext", "true");

		if (windows) {
			System.setProperty("gstreamer.GstNative.nameFormats", "%s-1.0-0|%s-1.0|%s-0|%s|lib%s|lib%s-0");
		}

		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {

			if ("Nimbus".equals(info.getName())) {

				UIManager.setLookAndFeel(info.getClassName());

				break;

			}
		}

	}

	public static void main(String[] args) {

		positionSlider = new JSlider(0, 1000);

		positionSlider.setBorder(null);

		Gst.init();

		try {

			initialize(Platform.isWindows());

			PlayerFrame frame = new PlayerFrame();

			frame.setVisible(true);

			if (args.length > 0) {
				frame.openFile(args[0]);
			}

			frame.pack();

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// make the frame half the height and width

			frame.setSize(700, 680);

			// center the jframe on screen
			frame.setLocationRelativeTo(null);

			frame.setVisible(true);

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public PlayerFrame() {
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(PlayerFrame.class.getResource("/imagenes/video_2_frame.png")));

		addKeyListener(new KeyAdapter() {

			@Override

			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}

			}

		});

		setTitle("Easy Video 2 GIF");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();

		setJMenuBar(menuBar);

		JMenuItem mntmNewMenuItem = new JMenuItem("Abrir Video (CTRL+O)\n");

		mntmNewMenuItem.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				try {
					abrirVideo();
				} catch (ImageProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});

		menuBar.add(mntmNewMenuItem);

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Configuración");

		menuBar.add(mntmNewMenuItem_1);

		contentPane = new JPanel();

		contentPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}

			}

		});

		contentPane.setForeground(Color.WHITE);

		contentPane.setBackground(Color.DARK_GRAY);

		setContentPane(contentPane);

		final SpringLayout sl_contentPane = new SpringLayout();

		contentPane.setLayout(sl_contentPane);

		final String[] videoExts = new String[] { "asf", "avi", "3gp", "mp4", "mov", "flv", "mpg", "ts", "mkv", "webm",
				"mxf", "ogg" };

		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Video File", videoExts));

		JLabel openFileButton = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.WEST, openFileButton, 6, SpringLayout.EAST, positionSlider);
		sl_contentPane.putConstraint(SpringLayout.EAST, openFileButton, -21, SpringLayout.EAST, contentPane);

		openFileButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				try {
					abrirVideo();
				} catch (ImageProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		});

		openFileButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), "open");

		openFileButton.getActionMap().put("open", new AbstractAction("open") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {

				int returnValue = fileChooser.showOpenDialog(contentPane);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					playbin.stop();
					playbin.setURI(fileChooser.getSelectedFile().toURI());
					playbin.play();
				}

			}

		});

		openFileButton.setToolTipText("");

		openFileButton.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/open-file.png")));

		contentPane.add(openFileButton);

		sl_contentPane.putConstraint(SpringLayout.WEST, positionSlider, 218, SpringLayout.WEST, contentPane);

		sl_contentPane.putConstraint(SpringLayout.EAST, positionSlider, -45, SpringLayout.EAST, contentPane);

		positionSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				verVideo(false);

			}

		});

		positionSlider.setValue(0);

		positionSlider.setBackground(Color.DARK_GRAY);

		contentPane.add(positionSlider);

		new Timer(50, new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (playbin == null || positionSlider == null)

					return;

				if (!positionSlider.getValueIsAdjusting() && playbin.isPlaying()) {

					long dur = playbin.queryDuration(TimeUnit.NANOSECONDS);

					long pos = playbin.queryPosition(TimeUnit.NANOSECONDS);

					if (dur > 0) {

						double relPos = (double) pos / dur;

						positionSlider.setValue((int) (relPos * 1000));

					}

					if (dur == pos && dur > 0) {

						playbin.seek(0);

						playbin.stop();
					}

				}

			}

		}).start();

		SimpleVideoComponent videoOutput = new SimpleVideoComponent();
		sl_contentPane.putConstraint(SpringLayout.NORTH, openFileButton, 6, SpringLayout.SOUTH, videoOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, openFileButton, 0, SpringLayout.EAST, videoOutput);
		sl_contentPane.putConstraint(SpringLayout.NORTH, positionSlider, 15, SpringLayout.SOUTH, videoOutput);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, videoOutput, -269, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, videoOutput, 10, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, videoOutput, 184, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, videoOutput, -10, SpringLayout.EAST, contentPane);
		contentPane.add(videoOutput);

		JLabel playPauseButton_2 = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.NORTH, playPauseButton_2, 279, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, playPauseButton_2, 630, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, playPauseButton_2, -254, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, playPauseButton_2, -43, SpringLayout.EAST, contentPane);
		playPauseButton_2.setVerticalAlignment(SwingConstants.BOTTOM);
		playPauseButton_2.setToolTipText("Play/Pause (SPACE)");
		playPauseButton_2.setHorizontalAlignment(SwingConstants.LEFT);
		contentPane.add(playPauseButton_2);

		JButton btnNewButton_1_1 = new JButton("<|");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnNewButton_1_1, 34, SpringLayout.SOUTH, positionSlider);
		btnNewButton_1_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}
			}
		});

		btnNewButton_1_1.addMouseListener(new MouseAdapter() {
			@Override

			public void mousePressed(MouseEvent e) {
				try {
					String positionVideo = calcularPosicionVideo();

					segundosInicio = convertirASegundos(positionVideo);

					if (segundosFin > 0f && (segundosInicio > segundosFin)) {

						segundosInicio = 0;

						positionVideo = "00:00:0.0";

					}
					System.out.println(positionVideo + " - " + duration);
					inicio.setText(positionVideo);
				} catch (Exception e1) {
				}
			}

		});
		btnNewButton_1_1.setHorizontalAlignment(SwingConstants.LEFT);
		btnNewButton_1_1.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(btnNewButton_1_1);

		JButton btnNewButton_1_1_1 = new JButton("|>");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnNewButton_1_1_1, 0, SpringLayout.NORTH, btnNewButton_1_1);

		btnNewButton_1_1_1.addKeyListener(new KeyAdapter() {

			@Override

			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}

			}

		});

		btnNewButton_1_1_1.addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {

				String positionVideo = calcularPosicionVideo();

				segundosFin = convertirASegundos(positionVideo);

				if (segundosFin > 0f && (segundosInicio > segundosFin)) {

					segundosFin = convertirASegundos(duracion.getText());

					positionVideo = duracion.getText();

				}

				fin.setText(positionVideo);

			}

		});
		btnNewButton_1_1_1.setHorizontalAlignment(SwingConstants.LEFT);
		btnNewButton_1_1_1.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(btnNewButton_1_1_1);

		JLabel lblDuracin = new JLabel("Duración");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin, -5, SpringLayout.WEST, videoOutput);
		lblDuracin.setHorizontalAlignment(SwingConstants.CENTER);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDuracin, 10, SpringLayout.NORTH, contentPane);
		lblDuracin.setFont(new Font("Dialog", Font.PLAIN, 18));
		lblDuracin.setForeground(Color.WHITE);
		contentPane.add(lblDuracin);

		anchoVideoTxt = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, anchoVideoTxt, 37, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, anchoVideoTxt, -33, SpringLayout.WEST, videoOutput);
		anchoVideoTxt.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				try {
					if (Integer.parseInt(anchoVideoTxt.getText()) <= 0) {
						anchoVideoTxt.setText("" + anchoVideo);
					}
				} catch (Exception e1) {
					anchoVideoTxt.setText("" + anchoVideo);
				}
			}
		});

		largoVideoTxt = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.EAST, largoVideoTxt, -33, SpringLayout.WEST, videoOutput);
		largoVideoTxt.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (Integer.parseInt(largoVideoTxt.getText()) <= 0) {
						largoVideoTxt.setText("" + largoVideo);
					}
				} catch (Exception e1) {
					largoVideoTxt.setText("" + largoVideo);
				}
			}
		});

		duracionVideo = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, duracionVideo, -114, SpringLayout.EAST, anchoVideoTxt);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, duracionVideo, 31, SpringLayout.SOUTH, lblDuracin);
		sl_contentPane.putConstraint(SpringLayout.EAST, duracionVideo, 0, SpringLayout.EAST, anchoVideoTxt);

		duracionVideo.setHorizontalAlignment(SwingConstants.CENTER);
		duracionVideo.setEditable(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, duracionVideo, 6, SpringLayout.SOUTH, lblDuracin);
		contentPane.add(duracionVideo);
		duracionVideo.setColumns(10);

		JLabel lblDuracin_1 = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDuracin_1, 7, SpringLayout.SOUTH, duracionVideo);
		lblDuracin_1.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/width.png")));
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin_1, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin_1, -5, SpringLayout.WEST, videoOutput);
		lblDuracin_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDuracin_1.setForeground(Color.WHITE);
		lblDuracin_1.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(lblDuracin_1);

		sl_contentPane.putConstraint(SpringLayout.NORTH, anchoVideoTxt, 6, SpringLayout.SOUTH, lblDuracin_1);
		anchoVideoTxt.setHorizontalAlignment(SwingConstants.CENTER);
		anchoVideoTxt.setColumns(10);
		contentPane.add(anchoVideoTxt);

		JLabel lblDuracin_1_1 = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDuracin_1_1, 155, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, anchoVideoTxt, -6, SpringLayout.NORTH, lblDuracin_1_1);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, largoVideoTxt, 31, SpringLayout.SOUTH, lblDuracin_1_1);
		lblDuracin_1_1.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/height.png")));
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin_1_1, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin_1_1, -5, SpringLayout.WEST, videoOutput);
		lblDuracin_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDuracin_1_1.setForeground(Color.WHITE);
		lblDuracin_1_1.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(lblDuracin_1_1);

		largoVideoTxt.setHorizontalAlignment(SwingConstants.CENTER);
		sl_contentPane.putConstraint(SpringLayout.NORTH, largoVideoTxt, 6, SpringLayout.SOUTH, lblDuracin_1_1);
		largoVideoTxt.setColumns(10);
		contentPane.add(largoVideoTxt);

		JLabel lblDuracin_1_1_1 = new JLabel("Framerate");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDuracin_1_1_1, 12, SpringLayout.SOUTH, largoVideoTxt);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin_1_1_1, 0, SpringLayout.WEST, lblDuracin);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin_1_1_1, -5, SpringLayout.WEST, videoOutput);
		lblDuracin_1_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDuracin_1_1_1.setForeground(Color.WHITE);
		lblDuracin_1_1_1.setFont(new Font("Dialog", Font.PLAIN, 18));
		contentPane.add(lblDuracin_1_1_1);

		frameRateTxt = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblDuracin_1_1_1, -6, SpringLayout.NORTH, frameRateTxt);
		sl_contentPane.putConstraint(SpringLayout.WEST, frameRateTxt, 0, SpringLayout.WEST, duracionVideo);
		frameRateTxt.setHorizontalAlignment(SwingConstants.CENTER);
		frameRateTxt.setColumns(10);
		contentPane.add(frameRateTxt);

		JLabel lblDuracin_1_1_1_1 = new JLabel("Calidad");
		sl_contentPane.putConstraint(SpringLayout.NORTH, frameRateTxt, -46, SpringLayout.NORTH, lblDuracin_1_1_1_1);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, frameRateTxt, -18, SpringLayout.NORTH, lblDuracin_1_1_1_1);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin_1_1_1_1, 37, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin_1_1_1_1, 0, SpringLayout.EAST, duracionVideo);
		lblDuracin_1_1_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDuracin_1_1_1_1.setForeground(Color.WHITE);
		lblDuracin_1_1_1_1.setFont(new Font("Dialog", Font.PLAIN, 18));
		contentPane.add(lblDuracin_1_1_1_1);

		textField_6 = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, textField_6, 64, SpringLayout.WEST, btnNewButton_1_1_1);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, textField_6, -97, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, textField_6, -18, SpringLayout.EAST, contentPane);
		textField_6.setColumns(10);
		contentPane.add(textField_6);

		JLabel lblDuracin_1_1_1_1_1 = new JLabel("Desenfoque");
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin_1_1_1_1_1, -28, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, textField_6, 7, SpringLayout.SOUTH, lblDuracin_1_1_1_1_1);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblDuracin_1_1_1_1_1, -129, SpringLayout.SOUTH, contentPane);
		lblDuracin_1_1_1_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDuracin_1_1_1_1_1.setForeground(Color.WHITE);
		lblDuracin_1_1_1_1_1.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(lblDuracin_1_1_1_1_1);

		JCheckBox chckbxNewCheckBox = new JCheckBox("Optimizar");
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxNewCheckBox, 243, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxNewCheckBox, -111, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxNewCheckBox, -120, SpringLayout.WEST, textField_6);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin_1_1_1_1_1, 127, SpringLayout.EAST,
				chckbxNewCheckBox);
		chckbxNewCheckBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}
			}
		});
		chckbxNewCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxNewCheckBox.setFont(new Font("Dialog", Font.BOLD, 16));
		chckbxNewCheckBox.setBackground(Color.DARK_GRAY);
		chckbxNewCheckBox.setForeground(Color.WHITE);
		contentPane.add(chckbxNewCheckBox);

		JComboBox comboBox = new JComboBox();
		sl_contentPane.putConstraint(SpringLayout.NORTH, comboBox, 6, SpringLayout.SOUTH, lblDuracin_1_1_1_1);
		sl_contentPane.putConstraint(SpringLayout.WEST, comboBox, 0, SpringLayout.WEST, duracionVideo);
		sl_contentPane.putConstraint(SpringLayout.EAST, comboBox, 0, SpringLayout.EAST, duracionVideo);
		comboBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}
			}
		});
		contentPane.add(comboBox);

		JButton btnNewButton = new JButton("");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnNewButton, -26, SpringLayout.SOUTH, contentPane);
		btnNewButton.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/view.png")));
		btnNewButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}
			}
		});

		btnNewButton.addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {

				try {

					if (!inicio.getText().isEmpty() && !fin.getText().isEmpty() && segundosInicio < segundosFin) {

						DecimalFormat df = new DecimalFormat("#.000");

						duracionVideo.setText("" + df.format(segundosFin - segundosInicio));

						verVideo(true);

					}

				}

				catch (Exception e1) {

					e1.printStackTrace();

				}

			}

		});

		btnNewButton.setFont(new Font("Dialog", Font.BOLD, 14));
		contentPane.add(btnNewButton);

		JButton btnConvertir = new JButton("");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnConvertir, 23, SpringLayout.SOUTH, textField_6);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnConvertir, 572, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnConvertir, -21, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnConvertir, 0, SpringLayout.EAST, positionSlider);

		btnConvertir.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

			}

		});
		btnConvertir.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/video_2_frame.png")));
		btnConvertir.setFont(new Font("Dialog", Font.BOLD, 14));
		contentPane.add(btnConvertir);

		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("Limitación de tamaño");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnNewButton, 0, SpringLayout.NORTH, chckbxNewCheckBox_1);
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxNewCheckBox_1, 17, SpringLayout.SOUTH,
				chckbxNewCheckBox);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxNewCheckBox_1, 0, SpringLayout.WEST, duracionVideo);
		chckbxNewCheckBox_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}
			}
		});
		chckbxNewCheckBox_1.setBackground(Color.DARK_GRAY);
		chckbxNewCheckBox_1.setFont(new Font("Dialog", Font.BOLD, 16));
		chckbxNewCheckBox_1.setForeground(Color.WHITE);
		contentPane.add(chckbxNewCheckBox_1);

		JComboBox comboBox_1 = new JComboBox();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, comboBox_1, -26, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnNewButton, 93, SpringLayout.EAST, comboBox_1);
		sl_contentPane.putConstraint(SpringLayout.EAST, comboBox_1, -391, SpringLayout.EAST, contentPane);
		comboBox_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					play();
				}
			}
		});
		contentPane.add(comboBox_1);

		textField_8 = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, textField_8, -26, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, comboBox_1, 30, SpringLayout.EAST, textField_8);
		sl_contentPane.putConstraint(SpringLayout.WEST, textField_8, 0, SpringLayout.WEST, duracionVideo);
		textField_8.setHorizontalAlignment(SwingConstants.CENTER);
		textField_8.setColumns(10);
		contentPane.add(textField_8);

		tiempo = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.NORTH, tiempo, 346, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDuracin_1_1_1_1, -19, SpringLayout.SOUTH, tiempo);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblDuracin_1_1_1_1, 10, SpringLayout.SOUTH, tiempo);
		sl_contentPane.putConstraint(SpringLayout.WEST, tiempo, 215, SpringLayout.WEST, contentPane);
		tiempo.setForeground(Color.WHITE);
		contentPane.add(tiempo);

		duracion = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.NORTH, duracion, 37, SpringLayout.SOUTH, videoOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, duracion, 0, SpringLayout.EAST, playPauseButton_2);
		duracion.setForeground(Color.WHITE);
		contentPane.add(duracion);

		inicio = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxNewCheckBox, 20, SpringLayout.SOUTH, inicio);
		sl_contentPane.putConstraint(SpringLayout.WEST, inicio, 300, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnNewButton_1_1, -6, SpringLayout.WEST, inicio);
		sl_contentPane.putConstraint(SpringLayout.NORTH, inicio, 35, SpringLayout.SOUTH, positionSlider);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, inicio, -174, SpringLayout.SOUTH, contentPane);
		contentPane.add(inicio);
		inicio.setColumns(10);

		fin = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, fin, 520, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, fin, -23, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnNewButton_1_1_1, -6, SpringLayout.WEST, fin);
		sl_contentPane.putConstraint(SpringLayout.NORTH, fin, 29, SpringLayout.SOUTH, duracion);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, fin, -26, SpringLayout.NORTH, lblDuracin_1_1_1_1_1);
		contentPane.add(fin);
		fin.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.EAST, btnNewButton, 14, SpringLayout.EAST, lblNewLabel_1);
		sl_contentPane.putConstraint(SpringLayout.EAST, inicio, -25, SpringLayout.WEST, lblNewLabel_1);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblNewLabel_1, 0, SpringLayout.SOUTH, btnNewButton_1_1);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblNewLabel_1, -6, SpringLayout.WEST, btnNewButton_1_1_1);
		lblNewLabel_1.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/flag.png")));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setForeground(Color.WHITE);
		lblNewLabel_1.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(lblNewLabel_1);

		JLabel lblNewLabel = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblNewLabel, 37, SpringLayout.EAST, lblDuracin_1_1_1_1);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblNewLabel, 0, SpringLayout.SOUTH, btnNewButton_1_1);
		lblNewLabel.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/start.png")));
		contentPane.add(lblNewLabel);

		JLabel lblDuracin_1_1_1_2 = new JLabel("Nº Fotogramas");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDuracin_1_1_1_2, 20, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblDuracin_1_1_1_2, 10, SpringLayout.EAST, duracionVideo);
		lblDuracin_1_1_1_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblDuracin_1_1_1_2.setForeground(Color.WHITE);
		lblDuracin_1_1_1_2.setFont(new Font("Dialog", Font.PLAIN, 18));
		contentPane.add(lblDuracin_1_1_1_2);

		textField = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblDuracin_1_1_1_2, -13, SpringLayout.NORTH, textField);
		sl_contentPane.putConstraint(SpringLayout.NORTH, textField, 54, SpringLayout.SOUTH, comboBox);
		sl_contentPane.putConstraint(SpringLayout.WEST, textField, 0, SpringLayout.WEST, duracionVideo);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, textField, -16, SpringLayout.SOUTH, textField_6);
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setColumns(10);
		contentPane.add(textField);

		JLabel label = new JLabel("");

		label.addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {
				reproducir = false;

				play();
			}

		});

		sl_contentPane.putConstraint(SpringLayout.NORTH, label, 0, SpringLayout.NORTH, openFileButton);
		sl_contentPane.putConstraint(SpringLayout.EAST, label, -6, SpringLayout.WEST, positionSlider);
		label.setIcon(new ImageIcon(PlayerFrame.class.getResource("/imagenes/play-pause.png")));
		contentPane.add(label);

		playbin = new PlayBin("GstDumbPlayer");

		playbin.setVideoSink(videoOutput.getElement());

	}

	public void openFile(String file) {

		File f = new File(file);

		playbin.stop();

		playbin.setURI(f.toURI());

		playbin.play();

	}
}

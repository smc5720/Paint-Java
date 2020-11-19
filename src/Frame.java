import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEditSupport;

import java.io.*;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//전체 프레임
public class Frame extends JFrame {

	public static final int DEFAULT = 0;
	public static final int LINE = 1;
	public static final int RECT = 2;
	public static final int CIRCLE = 3;
	public static final int POLYLINE = 4;
	public static final int SKETCH = 5;
	public static final int ERASE = 6;
	public static final int ERASER = 7;
	public static final int UNDO = 8;
	public static final int REDO = 9;
	public static final int TEXT = 10;

	// 포인트 받아오는 곳
	Point start = null;
	Point end = null;
	int minx;
	int miny;
	int maxx;
	int maxy;
	int px, py;
	int width;
	int height;
	int move = 0;
	int changeSize = 0; // 크기 변경할 때 사용
	int copystate = 0; // 객체 복사에 사용
	Point mouse = new Point(0, 0);

	int tempx = 0;
	int tempy = 0;

	// 프레임 안에 있는 요소들
	JPanel optionPanel = new JPanel();
	Canvas canvas = new Canvas();
	JMenuBar menubar = new JMenuBar();
	JToolBar toolbar = new JToolBar("Options");
	SpinnerNumberModel sizemodel = new SpinnerNumberModel(8, 1, 50, 1);
	JSpinner spinner = new JSpinner(sizemodel);
	ExamplePanel examplepanel = new ExamplePanel();
	JLabel imagelabel = new JLabel();
	JPanel endbarpanel = new JPanel();
	JLabel xycoord;
	JLabel mode;

	JLabel shiftlabel = new JLabel();
	JLabel ctrllabel = new JLabel();

	// 작업선택용 옵션
	int option = 0;
	int mousepressed = 0;
	int psize = 0;

	// 텍스트 부분을 위한 변수들
	JTextField inputText;
	String string = "";
	int pointCount = 0;
	Point[] points = new Point[10000];
	String[] msg = new String[10000];

	// 각 도형들의 포인트를 저장해 놓는 벡터

	Vector<Point> sketSP = new Vector<Point>();
	ShapeRepository newshape;
	Stack<ShapeRepository> shape = new Stack<ShapeRepository>();
	Stack<ShapeRepository> loadingshape = new Stack<ShapeRepository>();
	Stack<ShapeRepository> redoshape = new Stack<ShapeRepository>();
	Stack<ShapeRepository> moveshape = new Stack<ShapeRepository>();

	// 폴리라인을 위한 벡터
	int[] tempX = new int[40];
	int[] tempY = new int[40];

	// 설정을 위한 변수들
	Color mypencolor = Color.black;
	Color myfillcolor = new Color(0, 0, 0, 0);

	Point hey1, hey2;
	int thick = 8;
	int eraserthick = 15;

	// 프레임 크기
	Dimension dim = new Dimension(1000, 700);
	// 툴바 크기
	Dimension dim1 = new Dimension(100, 610);
	// 캔버스 크기
	Dimension dim2 = new Dimension(900, 610);
	// 좌표 패널 크기
	Dimension dim3 = new Dimension(1000, 30);

	// 프레임 설정해주는 곳
	public Frame() {

		initFrame();

		setMenu();

		// 툴바 설정
		setToolbar();
	}

	class ChangeDetector implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
			thick = (Integer) spinner.getValue();
			examplepanel.repaint();
		}
	}

	// 메뉴바 Exit버튼을 위한 액션 리스너
	class ExitAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	// 그림판 설정
	class Canvas extends JPanel {
		// 리스너
		MyMouseListener ml = new MyMouseListener();
		MyKeyListener kl = new MyKeyListener();

		// 그림판 마우스 리스너 불러주기
		Canvas() {
			addMouseListener(ml);
			addMouseMotionListener(ml);

			addKeyListener(kl);

			setFocusable(true);
		}

		// 새로 그림 그리는 곳
		public void paintComponent(Graphics g) {

			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;
			if (option == ERASE) {
				while (shape.size() > 0) {
					shape.pop();
				}
			}

			int textcount = 0;

			for (int i = 0; i < shape.size(); i++) {
				g2.setPaint(shape.get(i).myfillcolor);
				g2.setStroke(new BasicStroke(shape.get(i).thick, BasicStroke.CAP_ROUND, 0));

				switch (shape.get(i).option) {
				case LINE:
					g2.setPaint(shape.get(i).mypencolor);
					g2.drawLine(shape.get(i).start.x, shape.get(i).start.y, shape.get(i).end.x, shape.get(i).end.y);
					break;
				case RECT:
					g2.fillRect(shape.get(i).minx, shape.get(i).miny, shape.get(i).width, shape.get(i).height);
					g2.setPaint(shape.get(i).mypencolor);
					g2.drawRect(shape.get(i).minx, shape.get(i).miny, shape.get(i).width, shape.get(i).height);
					break;
				case CIRCLE:
					g2.fillOval(shape.get(i).minx, shape.get(i).miny, shape.get(i).width, shape.get(i).height);
					g2.setColor(shape.get(i).mypencolor);
					g2.drawOval(shape.get(i).minx, shape.get(i).miny, shape.get(i).width, shape.get(i).height);
					break;
				case POLYLINE:
					g2.setPaint(shape.get(i).mypencolor);
					g2.drawPolyline(shape.get(i).array_x, shape.get(i).array_y, shape.get(i).size);
					break;
				case SKETCH:
				case ERASER:
					g2.setPaint(shape.get(i).mypencolor);
					for (int j = 1; j < shape.get(i).sketchSP.size(); j++) {
						g2.drawLine(shape.get(i).sketchSP.get(j - 1).x, shape.get(i).sketchSP.get(j - 1).y,
								shape.get(i).sketchSP.get(j).x, shape.get(i).sketchSP.get(j).y);
					}
					break;
				case ERASE:
					g2.setPaint(shape.get(i).myfillcolor);
					g2.fillRect(shape.get(i).start.x, shape.get(i).start.y,
							Math.abs(shape.get(i).end.x - shape.get(i).start.x),
							Math.abs(shape.get(i).end.y - shape.get(i).start.y));
				case TEXT:
					g2.setPaint(shape.get(i).mypencolor);
					g2.setFont(new Font("SANS_SERIF", Font.PLAIN, shape.get(i).thick));
					g2.drawString(msg[textcount], shape.get(i).minx, shape.get(i).miny);
					textcount++;
					break;
				}
			}

			// 그림자 그리기
			if (start != null) {

				if (option == ERASER) {
					g2.setColor(Color.white);
					g2.setStroke(new BasicStroke(eraserthick, BasicStroke.CAP_ROUND, 0));
				} else {
					g2.setPaint(myfillcolor);
					g2.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, 0));

				}
				if (option == LINE) {
					g2.setPaint(mypencolor);
					g2.drawLine(start.x, start.y, end.x, end.y);
				} else if (option == RECT) {
					g2.fillRect(minx, miny, width, height);
					g2.setPaint(mypencolor);
					g2.drawRect(minx, miny, width, height);
				} else if (option == CIRCLE) {
					g2.fillOval(minx, miny, width, height);
					g2.setPaint(mypencolor);
					g2.drawOval(minx, miny, width, height);
				} else if (option == POLYLINE) {
					g2.setPaint(mypencolor);
					if (move == 0)
						g2.drawPolyline(tempX, tempY, psize + 1);
					else
						g2.drawPolyline(tempX, tempY, psize);
				} else if (option == SKETCH) {
					g2.setPaint(mypencolor);
					for (int i = 1; i < sketSP.size(); i++) {

						g2.drawLine(sketSP.get(i - 1).x, sketSP.get(i - 1).y, sketSP.get(i).x, sketSP.get(i).y);
					}
				} else if (option == ERASER) {
					for (int i = 1; i < sketSP.size(); i++) {

						g2.drawLine(sketSP.get(i - 1).x, sketSP.get(i - 1).y, sketSP.get(i).x, sketSP.get(i).y);
					}

					g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, 0));
					g2.setColor(Color.black);
					if (end != null)
						g2.drawOval(end.x - eraserthick / 2, end.y - eraserthick / 2, eraserthick, eraserthick);

				}

			}

		}

		class MyKeyListener extends KeyAdapter implements KeyListener {

			public void keyPressed(KeyEvent e) {
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Shift")) {
					if (changeSize != 1) {
						changeSize = 1;
						shiftlabel.setIcon((new ImageIcon("images/shift_down.jpg")));
					}
				}

				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Ctrl")) {
					if (copystate != 1) {
						copystate = 1;
						ctrllabel.setIcon((new ImageIcon("images/ctrl_down.jpg")));
					}
				}
			}

			public void keyReleased(KeyEvent e) {
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Shift")) {
					changeSize = 0;
					shiftlabel.setIcon((new ImageIcon("images/shift_up.jpg")));
				}

				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Ctrl")) {
					copystate = 0;
					ctrllabel.setIcon((new ImageIcon("images/ctrl_up.jpg")));
				}
			}

			public void keyTyped(KeyEvent e) {

			}
		}

		// 마우스 리스너 클래스
		class MyMouseListener extends MouseAdapter implements MouseMotionListener {
			int temp = 0;
			int makeinstance = 0;
			ShapeRepository newshape;
			ShapeRepository tempshape;
			int top = -1;

			int sx, sy;

			// 마우스 프레스 됐을 때
			public void mousePressed(MouseEvent e) {
				if (option == DEFAULT) {
					top = shape.size();
					sx = e.getX();
					sy = e.getY();
					while (shape.isEmpty() == false) {
						if (top == 0)
							break;
						tempshape = shape.get(--top);

						int maxx = tempshape.maxx;
						int maxy = tempshape.maxy;
						int minx = tempshape.minx;
						int miny = tempshape.miny;
						if (sx <= maxx && sx >= minx && sy <= maxy && sy >= miny) {
							if (copystate == 0) {
								moveshape.push(shape.remove(top));
							} else {
								moveshape.push(shape.get(top));
							}
							myfillcolor = tempshape.myfillcolor;
							mypencolor = tempshape.mypencolor;
							thick = tempshape.thick;
							move = 1;
							newshape = new ShapeRepository();
							hey1 = tempshape.start;
							hey2 = tempshape.end;
							break;
						}
					}
				}
				if (makeinstance == 0 && (option == LINE || option == RECT || option == SKETCH || option == POLYLINE
						|| option == CIRCLE || option == ERASER || option == TEXT)) {
					redoshape.removeAllElements();
					newshape = new ShapeRepository();
					if (option == ERASER) {
						newshape.mypencolor = Color.white;
						newshape.thick = eraserthick;
					}

					else {
						newshape.mypencolor = mypencolor;
						newshape.myfillcolor = myfillcolor;
						newshape.thick = thick;
					}
					newshape.option = option;
					makeinstance = 1;
				}

				if (mousepressed == 0 && option != 0) {

					mousepressed += 1;
				} else
					mousepressed = 0;

				if (option == POLYLINE) {
					if (mousepressed == 1) {
						tempX[psize] = e.getX();
						tempY[psize] = e.getY();
					}
					psize++;
					tempX[psize] = e.getX();
					tempY[psize] = e.getY();

					if (e.getButton() == MouseEvent.BUTTON3) {
						newshape.size = psize;
						for (int i = 0; i < psize; i++) {

							newshape.maxx = Math.max(newshape.maxx, tempX[i]);
							newshape.minx = Math.min(newshape.minx, tempX[i]);
							newshape.maxy = Math.max(newshape.maxy, tempY[i]);
							newshape.miny = Math.min(newshape.miny, tempY[i]);
							newshape.array_x[i] = tempX[i];
							newshape.array_y[i] = tempY[i];
						}

						shape.add(newshape);
						makeinstance = 0;
						psize = 0;
						option = DEFAULT;
						repaint();
					}
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					option = DEFAULT;
				}

				else if (option == SKETCH || option == ERASER) {
					newshape.sketchSP.add(e.getPoint());
					sketSP.add(e.getPoint());
				}
				String t = "";
				if (option == 0)
					t = "DEFAULT";
				else if (option == 1)
					t = "LINE";
				else if (option == 2)
					t = "RECTANGLE";
				else if (option == 3)
					t = "CIRCLE";
				else if (option == 4)
					t = "POLYLINE";
				else if (option == 5)
					t = "SKETCH";
				else if (option == 6)
					t = "ERASE";
				else if (option == 7)
					t = "ERASER";
				else if (option == 8)
					t = "UNDO";
				else if (option == 9)
					t = "REDO";
				else if (option == 10)
					t = "TEXT";

				mode.setText("      [Mode] = [" + t + "]");
				start = e.getPoint();
			}

			// 마우스 릴리즈 됐을 때
			public void mouseReleased(MouseEvent e) {
				if (option != DEFAULT && move == 0)
					shape.add(newshape);
				if (move == 1) {
					newshape.option = option;
					newshape.minx = minx;
					newshape.miny = miny;
					newshape.maxx = maxx;
					newshape.maxy = maxy;
					newshape.width = width;
					newshape.height = height;
					newshape.start = start;
					newshape.end = end;
					newshape.size = psize;
					newshape.thick = thick;
					newshape.moved = 1;
					newshape.myfillcolor = myfillcolor;
					newshape.mypencolor = mypencolor;
					for (int i = 0; i < psize; i++) {
						newshape.array_x[i] = tempX[i];
						newshape.array_y[i] = tempY[i];
					}
					newshape.sketchSP.addAll(sketSP);
					shape.push(newshape);
					option = DEFAULT;
				}

				end = e.getPoint();

				if (option == ERASER)
					end = null;

				makeinstance = 0;

				if ((option == RECT || option == CIRCLE) && move == 0) {
					minx = (int) Math.min(start.getX(), end.getX());
					miny = (int) Math.min(start.getY(), end.getY());
					width = (int) Math.abs(start.getX() - end.getX());
					height = (int) Math.abs(start.getY() - end.getY());

					newshape.minx = minx;
					newshape.miny = miny;
					newshape.maxx = minx + width;
					newshape.maxy = miny + height;

					newshape.width = width;
					newshape.height = height;
				}

				else if ((option == TEXT) && move == 0) {
					minx = (int) Math.min(start.getX(), end.getX());
					miny = (int) Math.min(start.getY(), end.getY());

					newshape.minx = minx;
					newshape.miny = miny;
					newshape.thick = thick;

					if (pointCount < points.length) {
						points[pointCount] = e.getPoint();
						msg[pointCount] = String.format("%s", inputText.getText());
						pointCount++;
					}
				}

				else if (option == LINE && move == 0) {
					minx = (int) Math.min(start.getX(), end.getX());
					miny = (int) Math.min(start.getY(), end.getY());
					width = (int) Math.abs(start.getX() - end.getX());
					height = (int) Math.abs(start.getY() - end.getY());

					newshape.minx = minx;
					newshape.miny = miny;
					newshape.maxx = minx + width;
					newshape.maxy = miny + height;

					newshape.start = start;
					newshape.end = end;

				}

				else if (option == SKETCH || option == ERASER || move == 0) {

					sketSP.removeAllElements();
				}

				move = 0;
				repaint();
			}

			@Override
			// 마우스 드레그 됐을 때
			public void mouseDragged(MouseEvent e) {

				px = e.getX() - sx;
				py = e.getY() - sy;
				// TODO Auto-generated method stub
				if (option == SKETCH || option == ERASER) {
					newshape.sketchSP.add(e.getPoint());
					sketSP.add(e.getPoint());
					newshape.maxx = (int) Math.max(e.getPoint().getX(), newshape.maxx);
					newshape.minx = (int) Math.min(e.getPoint().getX(), newshape.minx);
					newshape.maxy = (int) Math.max(e.getPoint().getY(), newshape.maxy);
					newshape.miny = (int) Math.max(e.getPoint().getY(), newshape.maxy);
				}
				end = e.getPoint();
				if (option == RECT || option == CIRCLE) {
					if (changeSize != 1) {
						minx = (int) Math.min(start.getX(), end.getX());
						miny = (int) Math.min(start.getY(), end.getY());
						width = (int) Math.abs(start.getX() - end.getX());
						height = (int) Math.abs(start.getY() - end.getY());
					}

					else {
						minx = (int) Math.min(tempx, e.getPoint().getX());
						miny = (int) Math.min(tempy, e.getPoint().getY());
						width = (int) Math.abs(tempx - e.getPoint().getX());
						height = (int) Math.abs(tempy - e.getPoint().getY());
					}
				}

				if (move == 1) {
					if (changeSize != 1) {
						option = tempshape.option;
						minx = tempshape.minx + px;
						miny = tempshape.miny + py;
						maxx = tempshape.maxx + px;
						maxy = tempshape.maxy + py;
						width = tempshape.width;
						height = tempshape.height;
						start = new Point(hey1.x + px, hey1.y + py);
						end = new Point(hey2.x + px, hey2.y + py);
						psize = tempshape.size;

						for (int i = 0; i < tempshape.size; i++) {
							tempX[i] = tempshape.array_x[i] + px;
							tempY[i] = tempshape.array_y[i] + py;
						}

						for (int i = 0; i < tempshape.sketchSP.size(); i++) {
							Point pt;
							pt = tempshape.sketchSP.get(i);
							pt.move(pt.x + px, pt.y + py);
							sketSP.add(pt);
						}

						tempx = minx;
						tempy = miny;
					}
				}

				repaint();
			}

			@Override
			// 마우스 움직일 때
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				mouse = e.getPoint();

				xycoord.setText("[x] = [" + mouse.getX() + "]" + "  [y] = [" + mouse.getY() + "]");
				if (option == POLYLINE) {
					if (psize > 0) {
						tempX[psize] = e.getX();
						tempY[psize] = e.getY();

						repaint();
					}
				}

			}
		}
	}

	// 버튼 액션 리스너 클래스
	class ButtonAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton myButton = (JButton) e.getSource();
			mousepressed = 0;
			newshape = new ShapeRepository();
			String temp = myButton.getText();

			if (temp.equals("Line")) {
				if (option == LINE)
					option = DEFAULT;
				else
					option = LINE;
				new Canvas();
			} else if (temp.equals("Rect")) {
				if (option == RECT)
					option = DEFAULT;
				else
					option = RECT;
				new Canvas();
			} else if (temp.equals("Circ")) {
				if (option == CIRCLE)
					option = DEFAULT;
				else
					option = CIRCLE;
				new Canvas();
			} else if (temp.equals("Zig")) {
				if (option == POLYLINE)
					option = DEFAULT;
				else
					option = POLYLINE;
				new Canvas();

			} else if (temp.equals("Pen")) {
				if (option == SKETCH)
					option = DEFAULT;
				option = SKETCH;
				new Canvas();
			} else if (temp.equals("Text")) {
				if (option == TEXT)
					option = DEFAULT;
				else {
					option = TEXT;
					// 텍스트 프레임창 생성
					JFrame text = new JFrame("텍스트 입력기");
					text.setVisible(true);
					text.setSize(350, 120);
					text.setLocation(400, 400);

					// 텍스트 입력부분을 텍스트 프레임창에 추가
					inputText = new JTextField(10);
					inputText.setToolTipText("텍스트를 입력해주세요.");
					text.add(inputText);

					// 입력한 문자를 받아온다. (텍스트필드 리스너 등록)
					inputText.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (e.getSource() == inputText) {
								string = String.format("%s", e.getActionCommand());
							}
						}
					});
				}
				new Canvas();
			} else if (temp.equals("Draw")) {
				mypencolor = JColorChooser.showDialog(null, "색선정", Color.blue);
				examplepanel.repaint();
			}

			else if (temp.equals("Fill")) {
				myfillcolor = JColorChooser.showDialog(null, "색선정", Color.blue);
				examplepanel.repaint();
			}

			else if (temp.equals("Eras")) {
				if (option == ERASER)
					option = DEFAULT;
				else {
					option = ERASER;
					JFrame eraser = new JFrame("지우개");
					eraser.setVisible(true);
					eraser.setSize(250, 100);
					eraser.setLocation(200, 200);

					JSlider eraserSize = new JSlider(JSlider.HORIZONTAL, 0, 50, 15);
					eraserSize.setMajorTickSpacing(10);
					eraserSize.setMinorTickSpacing(1);
					eraserSize.setPaintTicks(true);
					eraserSize.setPaintLabels(true);

					eraser.add(eraserSize);

					eraserSize.addChangeListener(new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent e) {
							// TODO Auto-generated method stub
							JSlider source = (JSlider) e.getSource();
							if (!source.getValueIsAdjusting()) {
								eraserthick = (int) source.getValue();
							}
						}
					});
				}
				new Canvas();
			}

			else if (temp.equals("New")) {
				option = ERASE;
				canvas.repaint();
			}

			else if (temp.equals("Undo")) {
				option = UNDO;

				if (shape.isEmpty() == false) {
					if (shape.get(shape.size() - 1).moved == 1) {
						redoshape.push(shape.pop());
						shape.push(moveshape.pop());
					} else
						redoshape.push(shape.pop());
				}
				canvas.repaint();
			}

			else if (temp.equals("Redo")) {
				option = REDO;
				if (redoshape.isEmpty() == false) {
					if (redoshape.get(redoshape.size() - 1).moved == 1) {
						moveshape.push(shape.pop());
						shape.push(redoshape.pop());
					} else
						shape.push(redoshape.pop());
				}

				canvas.repaint();
			}

			else if (temp.equals("Redo")) {
				option = REDO;
				if (redoshape.isEmpty() == false)
					shape.push(redoshape.pop());

				canvas.repaint();
			}

			String t = "";

			if (option == 0)
				t = "DEFAULT";
			else if (option == 1)
				t = "LINE";
			else if (option == 2)
				t = "RECTANGLE";
			else if (option == 3)
				t = "CIRCLE";
			else if (option == 4)
				t = "POLYLINE";
			else if (option == 5)
				t = "SKETCH";
			else if (option == 6)
				t = "ERASE";
			else if (option == 7)
				t = "ERASER";
			else if (option == 8)
				t = "UNDO";
			else if (option == 9)
				t = "REDO";
			else if (option == 10)
				t = "TEXT";

			mode.setText("      [Mode] = [" + t + "]");

			canvas.requestFocus();
		}
	}

	class ExamplePanel extends JPanel {
		ExamplePanel() {
			setPreferredSize(new Dimension(40, 40));
			setLayout(new FlowLayout());
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			g2.setPaint(myfillcolor);
			g2.fillRect(28, 5, 25, 25);
			g2.setPaint(mypencolor);
			g2.drawRect(28, 5, 25, 25);
			g2.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, 0));
		}
	}

	void initFrame() {
		// 프레임 설정
		setLocation(800, 100);
		setTitle("PaintPanel");
		setSize(dim);
		setLayout(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 아이템 추가
		add(canvas);
		add(toolbar);
		add(endbarpanel);

		// 캔버스 설정
		canvas.setSize(dim2);
		canvas.setBackground(Color.white);
		canvas.setLocation(0, 0);

		// 좌표 패널 설정
		endbarpanel.setSize(dim3);
		endbarpanel.setBackground(Color.black);
		endbarpanel.setLocation(0, 610);
		endbarpanel.setLayout(new BoxLayout(endbarpanel, BoxLayout.X_AXIS));

		xycoord = new JLabel("[x] = [" + mouse.getX() + "]" + "  [y] = [" + mouse.getY() + "]");
		xycoord.setPreferredSize(new Dimension(200, 40));
		xycoord.setForeground(Color.WHITE);

		mode = new JLabel("      [Mode] = [DEFAULT]");
		mode.setForeground(Color.WHITE);

		endbarpanel.add(xycoord);
		endbarpanel.add(mode);

		ctrllabel.setIcon((new ImageIcon("images/ctrl_up.jpg")));
		shiftlabel.setIcon((new ImageIcon("images/shift_up.jpg")));
		canvas.add(ctrllabel);
		canvas.add(shiftlabel);
	}

	void setMenu() {
		setJMenuBar(menubar);

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		menubar.add(file);

		JMenuItem openfile = new JMenuItem("Open File...");
		openfile.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
		file.add(openfile);
		openfile.addActionListener(new LoadPanel());

		JMenuItem savebtn = new JMenuItem("Save As...");
		savebtn.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
		file.add(savebtn);
		savebtn.addActionListener(new SavePanel());

		file.addSeparator();

		JMenuItem loadimage = new JMenuItem("Load Image");
		loadimage.setAccelerator(KeyStroke.getKeyStroke('L', Event.CTRL_MASK));
		file.add(loadimage);
		loadimage.addActionListener(new LoadImage());

		JMenuItem deleteimage = new JMenuItem("Delete Image");
		deleteimage.setAccelerator(KeyStroke.getKeyStroke('D', Event.CTRL_MASK));
		file.add(deleteimage);
		deleteimage.addActionListener(new DeleteImage());

		file.addSeparator();

		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK));
		file.add(exit);
		exit.addActionListener(new ExitAction());

		// ----------------------------------------------------------

		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		menubar.add(help);
		JMenuItem about = new JMenuItem("About");
		about.setAccelerator(KeyStroke.getKeyStroke('A', Event.CTRL_MASK));
		help.add(about);
		about.addActionListener(new OpenReadTXT());
	}

	void setToolbar() {
		toolbar.setLocation(885, 0);

		toolbar.setBackground(Color.BLACK);
		toolbar.setSize(dim1);
		toolbar.setLayout(new GridLayout(0, 1));

		JLabel preview = new JLabel("Preview");
		preview.setForeground(Color.WHITE);
		preview.setHorizontalAlignment(SwingConstants.CENTER);
		preview.setPreferredSize(new Dimension(60, 40));
		toolbar.add(preview);

		toolbar.add(examplepanel);

		JLabel drawtool = new JLabel("Drawing");
		drawtool.setForeground(Color.WHITE);
		drawtool.setHorizontalAlignment(SwingConstants.CENTER);
		drawtool.setPreferredSize(new Dimension(60, 40));
		toolbar.add(drawtool);

		JButton[] optionbtn = new JButton[6];
		ImageIcon[] iconimage = new ImageIcon[6];

		iconimage[0] = new ImageIcon("images/line.png");
		iconimage[1] = new ImageIcon("images/rectangle.png");
		iconimage[2] = new ImageIcon("images/circle.png");
		iconimage[3] = new ImageIcon("images/zigzag.png");
		iconimage[4] = new ImageIcon("images/pen.png");
		iconimage[5] = new ImageIcon("images/text.png");

		optionbtn[0] = new JButton("Line", iconimage[0]);
		optionbtn[1] = new JButton("Rect", iconimage[1]);
		optionbtn[2] = new JButton("Circ", iconimage[2]);
		optionbtn[3] = new JButton("Zig", iconimage[3]);
		optionbtn[4] = new JButton("Pen", iconimage[4]);
		optionbtn[5] = new JButton("Text", iconimage[5]);

		for (int i = 0; i < optionbtn.length; i++) {
			toolbar.add(optionbtn[i]);
			optionbtn[i].setPreferredSize(new Dimension(60, 40));
			optionbtn[i].addActionListener(new ButtonAction());
		}

		JLabel otherbtn = new JLabel("Other");
		otherbtn.setForeground(Color.WHITE);
		otherbtn.setHorizontalAlignment(SwingConstants.CENTER);
		otherbtn.setPreferredSize(new Dimension(60, 40));
		toolbar.add(otherbtn);

		JButton[] setbtn = new JButton[6];
		ImageIcon[] iconimage2 = new ImageIcon[6];

		iconimage2[0] = new ImageIcon("images/undo.png");
		iconimage2[1] = new ImageIcon("images/redo.png");
		iconimage2[2] = new ImageIcon("images/eraser.png");
		iconimage2[3] = new ImageIcon("images/new.png");
		iconimage2[4] = new ImageIcon("images/draw.png");
		iconimage2[5] = new ImageIcon("images/fill.png");

		setbtn[0] = new JButton("Undo", iconimage2[0]);
		setbtn[1] = new JButton("Redo", iconimage2[1]);
		setbtn[2] = new JButton("Eras", iconimage2[2]);
		setbtn[3] = new JButton("New", iconimage2[3]);
		setbtn[4] = new JButton("Draw", iconimage2[4]);
		setbtn[5] = new JButton("Fill", iconimage2[5]);

		for (int i = 0; i < setbtn.length; i++) {
			toolbar.add(setbtn[i]);
			setbtn[i].setPreferredSize(new Dimension(80, 40));
			setbtn[i].addActionListener(new ButtonAction());
		}

		JLabel thickLabel = new JLabel("두께");
		thickLabel.setForeground(Color.WHITE);
		thickLabel.setHorizontalAlignment(SwingConstants.CENTER);
		thickLabel.setPreferredSize(new Dimension(40, 40));
		toolbar.add(thickLabel);
		toolbar.add(spinner);

		// 스피너에 리스너 설
		spinner.addChangeListener(new ChangeDetector());
	}

	class OpenReadTXT implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFrame helpme = new JFrame("About");

			try {
				// 바이트 단위로 파일읽기
				String filePath = "Readme.txt"; // 대상 파일
				FileInputStream fileStream = null; // 파일 스트림

				fileStream = new FileInputStream(filePath);// 파일 스트림 생성
				// 버퍼 선언
				byte[] readBuffer = new byte[fileStream.available()];

				while (fileStream.read(readBuffer) != -1) {
				}

				JLabel txtlabel = new JLabel(new String(readBuffer));
				txtlabel.setVerticalAlignment(SwingConstants.TOP);
				txtlabel.setHorizontalAlignment(SwingConstants.LEFT);
				txtlabel.setForeground(Color.BLACK);

				helpme.add(txtlabel);

				fileStream.close(); // 스트림 닫기
			}

			catch (Exception exception) {
				exception.getStackTrace();
			}

			helpme.setVisible(true);
			helpme.setSize(500, 500);
			helpme.setLocation(200, 200);
		}
	}

	class LoadImage implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			File src = imgOpenDlg();
			BufferedImage m;

			try {
				m = ImageIO.read(src);
				imagelabel.setIcon(new ImageIcon(m));
				canvas.add(imagelabel);
			}

			catch (Exception err) {
			}
		}
	}

	class DeleteImage implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (imagelabel.getIcon() != null) {
				imagelabel.setIcon(null);
			}
		}
	}

	class SavePanel implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ObjectOutputStream output;
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File("savefile"));

			int returnVal = jfc.showSaveDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					output = new ObjectOutputStream(new FileOutputStream(jfc.getSelectedFile()));

					try {
						int len = shape.size();
						for (int i = 0; i < len; i++) {
							ShapeRepository shapefile = new ShapeRepository();
							shapefile = shape.pop();
							shapefile.SerialNum = len - 1 - i;
							output.writeObject(shapefile);
						}
						output.close();
					}

					catch (IOException ioException) {
						System.err.println("Error writing to file.");
					}
				}

				catch (IOException ioException) {
					System.err.println("Error opening file.");
				}
			}
		}
	}

	class LoadPanel implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ObjectInputStream input;
			ShapeRepository loadShape;
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File("savefile"));

			int returnVal = jfc.showSaveDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					input = new ObjectInputStream(new FileInputStream(jfc.getSelectedFile()));

					while (shape.isEmpty() != true) {
						shape.pop();
					}

					try {

						do {
							loadShape = (ShapeRepository) input.readObject();
							loadingshape.push(loadShape);
						} while (loadShape.SerialNum > 0);
						input.close();

						while (loadingshape.size() > 0) {
							shape.push(loadingshape.pop());
						}

						repaint();
					}

					catch (EOFException eofException) {
						return;
					}

					catch (ClassNotFoundException classNotFoundException) {
						System.err.println("Unable to create object.");
					}

					catch (IOException ioException) {
						System.err.println("Error during read from file.");
					}
				}

				catch (IOException ioException) {
					System.err.println("Error opening file.");
				}
			}
		}
	}

	private static File imgOpenDlg() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("background"));
		int returnVal = chooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			return f;
		}
		return null;
	}
}
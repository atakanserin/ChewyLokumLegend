package gameboys.chewylokumlegend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * @author Gameboys
 *
 */
@SuppressWarnings("serial")
public class GameBoard extends JPanel {

	private GameMouseListener mouseListener;
	private LokumMatrix matrix;
	private int levelNum;
	private Timer refresh;
	private boolean mouseActive;
	private boolean mode;
	private boolean specialSwapMode;

	/**
	 * @param level The current Level object that the game state
	 * will be based on
	 * 
	 */
	public GameBoard(Level level){
		super();
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(new Color(255,230,230));
		setVisible(true);
		
		levelNum = level.getLevelNum();

		refresh = new Timer(Constants.REFRESH_RATE,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				repaint();
			}
		});   

		matrix = new LokumMatrix(10,10);
		mouseListener = new GameMouseListener();
	}

	/**
	 * @param mode set to true if the game is running, false otherwise
	 */
	public void setMode(boolean mode){
		this.mode = mode;
		if(mode){
			addMouseListener(mouseListener);
			mouseActive = true;
			refresh.start();
			Main.kanunMusic.loop(Clip.LOOP_CONTINUOUSLY);
		}else{
			removeMouseListener(mouseListener);
			mouseActive = false;
			refresh.stop();
			Main.kanunMusic.stop();
		}
	}

	private void makeMove(){
		setMouseActive(false);
		Timer timer = new Timer(Constants.TIMER_RATE,new ActionListener(){
			int multiplier = 1;
			int step = 1;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(step==1){
					GameWindow.gameBoard.setMouseActive(false);
					matrix.dropLokums();
					step++;
				}else if(step==2){
					matrix.fillInTheBlanks();
					step++;
				}else{
					boolean patternFound = matrix.scanForPatterns(multiplier);
					if(patternFound){
						Main.cukcukSound.setFramePosition(0);
						Main.cukcukSound.loop(1);
						multiplier++;
						step=1;
					}else{
						if(multiplier>8){
							Main.headshotSound.setFramePosition(0);
							Main.headshotSound.loop(1);
						}else if(multiplier>6){
							Main.divineSound.setFramePosition(0);
							Main.divineSound.loop(1);
						}else if(multiplier>4){
							Main.deliciousSound.setFramePosition(0);
							Main.deliciousSound.loop(1);
						}else if(multiplier>2){
							if(new Random().nextBoolean()){
								Main.tastySound.setFramePosition(0);
								Main.tastySound.loop(1);
							}else{
								Main.sweetSound.setFramePosition(0);
								Main.sweetSound.loop(1);
							}
						}
						ScoreBoard sb = GameWindow.scoreBoard;
						if(sb.getCurrentScore()>=sb.getTargetScore()){
							GameWindow.gameBoard.youWin();
						}else if(sb.getResourceLeft()<=0)GameWindow.gameBoard.gameOver();
						GameWindow.gameBoard.setMouseActive(true);
						((Timer)e.getSource()).stop();
					}
				}
			}
		});
		timer.setInitialDelay(Constants.TIMER_RATE);
		timer.start();
	}

	/**
	 * @param setActive true if mouse actions should be activated
	 * after method call, false if they should be deactivated
	 */
	public void setMouseActive(boolean setActive){
		if(setActive&&!mouseActive){
			addMouseListener(mouseListener);
			mouseActive = true;
		}else if(!setActive&&mouseActive){
			removeMouseListener(mouseListener);
			mouseActive = false;
		}
	}

	/**
	 * 
	 */
	public void youWin(){
		setMouseActive(false);
		GameWindow.setMode(false);
		Main.winSound.setFramePosition(0);
		Main.winSound.loop(1);
		removeAll();
		add(new LevelCompletedScreen());
		validate();
		repaint();
	}

	/**
	 * 
	 */
	public void gameOver(){
		setMouseActive(false);
		GameWindow.setMode(false);
		Main.aahSound.setFramePosition(0);
		Main.aahSound.loop(1);
		removeAll();
		add(new GameOverScreen());
		validate();
		repaint();
	}

	/**
	 * @param type the type of the lokums to be destroyed
	 * @param multiplier the current multiplier to be used
	 * while calculating point rewards. The multiplier starts
	 * from 1 when a swap occurs and is incremented every time
	 * a series of patterns are formed because of the same swap.
	 */
	public void delayedDestroyAllOfType(final int type, final int multiplier){
		Timer timer = new Timer(100000,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				matrix.destroyAllOfType(type,multiplier);
				((Timer)e.getSource()).stop();
			}
		});
		timer.setInitialDelay(Constants.TIMER_RATE);
		timer.start();
	}
	
	/**
	 * @param x the x index of the lokum that is destroyed
	 * @param y the y index of the lokum that is destroyed
	 * 
	 */
	public void paintExplodeImage(int x, int y){
		final JLabel gif = new JLabel(Main.explodeImage,JLabel.CENTER);
		gif.setVerticalAlignment(JLabel.CENTER);
		gif.setLocation(x*Constants.LOKUM_SIZE, y*Constants.LOKUM_SIZE);
		gif.setBounds(x*Constants.LOKUM_SIZE,y*Constants.LOKUM_SIZE,Constants.LOKUM_SIZE,Constants.LOKUM_SIZE);
		add(gif);
		final Timer end = new Timer(10000,new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				remove(gif);
				((Timer)arg0.getSource()).stop();
			}
		});
		end.setInitialDelay(300);
		end.start();
	}

	/**
	 * Swaps the lokum at the given index coordinates with
	 * the adjacent one in the given direction.
	 * 
	 * @param x1 the x index of the lokum in the lokumMatrix
	 * @param y1 the y index of the lokum in the lokumMatrix
	 * @param direction the direction of the swap (i.e. NORTH)
	 * enumerated as an integer value
	 */
	public void swapDirection(int x1, int y1, int direction){
		int xOffset = -5;
		int yOffset = -5;
		if(direction==Constants.NORTH || direction == Constants.SOUTH)xOffset=0;
		else if(direction>Constants.NORTH && direction < Constants.SOUTH)xOffset=1;
		else if(direction>Constants.SOUTH && direction <= Constants.NORTHWEST)xOffset=-1;

		if(direction==Constants.EAST || direction == Constants.WEST)yOffset=0;
		else if(direction>Constants.EAST && direction < Constants.WEST)yOffset=1;
		else if(direction>=Constants.NORTH && direction<=Constants.NORTHWEST)yOffset=-1;

		int x2 = Math.max(Math.min(x1+xOffset, matrix.getWidth()-1), 0);
		int y2 = Math.max(Math.min(y1+yOffset, matrix.getHeight()-1), 0);

		boolean validMove = matrix.swapLokums(x1,y1,x2,y2);
		if(validMove){
			makeMove();
			GameWindow.scoreBoard.makeMove();
			Main.cukcukSound.setFramePosition(0);
			Main.cukcukSound.loop(1);
		}
	}

	/**
	 * @param x1 the x index of the first lokum in the lokumMatrix
	 * @param y1 the y index of the first lokum in the lokumMatrix
	 * @param x2 the x index of the second lokum in the lokumMatrix
	 * @param y2 the y index of the second lokum in the lokumMatrix
	 */
	public void specialSwap(int x1, int y1, int x2, int y2){
		boolean validMove = matrix.swapLokums(x1,y1,x2,y2);
		if(validMove){
			makeMove();
			setSpecialSwapMode(false);
			GameWindow.scoreBoard.makeMove();
			Main.cukcukSound.setFramePosition(0);
			Main.cukcukSound.loop(1);
		}
	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		if(mode)matrix.paint(g);
	}

	/**
	 * @return the specialSwapMode
	 */
	public boolean isSpecialSwapMode() {
		return specialSwapMode;
	}

	/**
	 * @param specialSwapMode the specialSwapMode to set
	 */
	public void setSpecialSwapMode(boolean specialSwapMode) {
		this.specialSwapMode = specialSwapMode;
	}

	/**
	 * @author Gameboys
	 *
	 */
	class LevelCompletedScreen extends JPanel{

		public int WIDTH = (int)(Constants.WINDOW_WIDTH*(1-Constants.DIVIDER_RATIO));
		public int HEIGHT = Constants.WINDOW_HEIGHT;

		/**
		 * 
		 */
		private LevelCompletedScreen(){
			super();
			setLayout(null);
			addLabels();
		}

		/**
		 * 
		 */
		private void addLabels(){
			JLabel levelCompleted = new JLabel("Level Completed!");
			levelCompleted.setBounds(0,HEIGHT/4,WIDTH,HEIGHT/10);
			levelCompleted.setHorizontalAlignment(SwingConstants.CENTER);
			levelCompleted.setFont(new Font("Comic Sans MS", Font.BOLD, WIDTH/21));
			add(levelCompleted);
			

			if(levelNum<LevelFactory.numLevels){
				JButton nextLevel = new JButton("Next Level");
				nextLevel.setBounds(WIDTH/4,HEIGHT/2,WIDTH/2,HEIGHT/10);
				nextLevel.setHorizontalAlignment(SwingConstants.CENTER);
				nextLevel.setFont(new Font("Comic Sans MS", Font.BOLD, WIDTH/21));
				nextLevel.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						JPanel contentPane = (JPanel) ApplicationWindow.getInstance().getContentPane();
						contentPane.removeAll();
						contentPane.add(new GameWindow(LevelFactory.getLevel(levelNum+1)));
//						GameWindow.setMode(true);
						contentPane.validate();
						contentPane.repaint();
					}
				});
				add(nextLevel);
			}
		}


	}

	/**
	 * @author Gameboys
	 *
	 */
	class GameOverScreen extends JPanel{

		public int WIDTH = (int)(Constants.WINDOW_WIDTH*(1-Constants.DIVIDER_RATIO));
		public int HEIGHT = Constants.WINDOW_HEIGHT;

		/**
		 * 
		 */
		private GameOverScreen(){
			super();
			setLayout(null);
			addButtons();
		}

		/**
		 * 
		 */
		private void addButtons() {
			JButton tryAgain = new JButton("Try again");
			tryAgain.setBounds(WIDTH/2-WIDTH/4,HEIGHT/4,WIDTH/2,HEIGHT/10);
			tryAgain.setFont(new Font("Comic Sans MS", Font.BOLD, WIDTH/42));
			tryAgain.setBorderPainted(false);
			tryAgain.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JPanel contentPane = (JPanel) ApplicationWindow.getInstance().getContentPane();
					contentPane.removeAll();
					contentPane.add(new GameWindow(LevelFactory.getLevel(levelNum)));
					contentPane.validate();
					contentPane.repaint();
				}
			});
			add(tryAgain);

		}

		//		private void addLabels() {
		//			JLabel
		//		}
	}

	/**
	 * @author Gameboys
	 *
	 */
	class GameMouseListener implements MouseListener{

		private int initialLokumXIndex;
		private int initialLokumYIndex;
		private int finalLokumXIndex;
		private int finalLokumYIndex;

		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			initialLokumXIndex = e.getX()/Constants.LOKUM_SIZE;
			initialLokumYIndex = e.getY()/Constants.LOKUM_SIZE;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			finalLokumXIndex = e.getX()/Constants.LOKUM_SIZE;
			finalLokumYIndex = e.getY()/Constants.LOKUM_SIZE;
			int dispX = finalLokumXIndex - initialLokumXIndex;
			int dispY = finalLokumYIndex - initialLokumYIndex;
			int direction = -5;
			if(dispX==0 && dispY<0)direction = Constants.NORTH;
			else if(dispX>0 && dispY<0)direction = Constants.NORTHEAST;
			else if(dispX>0 && dispY==0)direction = Constants.EAST;
			else if(dispX>0 && dispY>0)direction = Constants.SOUTHEAST;
			else if(dispX==0 && dispY>0)direction = Constants.SOUTH;
			else if(dispX<0 && dispY>0)direction = Constants.SOUTHWEST;
			else if(dispX<0 && dispY==0)direction = Constants.WEST;
			else if(dispX<0 && dispY<0)direction = Constants.NORTHWEST;
			else return;
			if(specialSwapMode)specialSwap(initialLokumXIndex, initialLokumYIndex, finalLokumXIndex, finalLokumYIndex);
			else swapDirection(initialLokumXIndex, initialLokumYIndex, direction);
		}

	}

}

package mainGame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.net.MalformedURLException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

/**
 * Main game class. This class is the driver class and it follows the Holder
 * pattern. It houses references to ALL of the components of the game
 * 
 * @author Brandon Loehle 5/30/16
 */

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 1920, HEIGHT = 1080;
	public static final int drawWidth = 1280, drawHeight = 720;
	public static double scaleFactor;
	private Thread thread;
	private boolean running = false;
	private Handler handler;
	private HUD hud;
	private SpawnHard spawnerH;
	private Spawn1to10 spawner;
	private Spawn10to20 spawner2;
	private Menu menu;
	private GameOver gameOver;
	private GameWon gameWon;
	private UpgradeScreen upgradeScreen;
	private MouseListener mouseListener;
	private Upgrades upgrades;
	private Player player;
	private Pause pause;
	private Leaderboard leaderboard;
	private HardLeaderboard hardLeaderboard;
	public STATE gameState = STATE.Menu;
	private Midi menuMIDIPlayer;
	private String menuMIDIMusic = "Friends.mid";
	private Midi gameMIDIPlayer;
	private String gameMIDIMusic = "Danza_Kuduro_synth.mid";
	private Midi bossMIDIPlayer;
	private String bossMIDIMusic = "sm1castl_synth.mid";
	private String ShopMIDIMusic = "Fresh-Prince-Belair.mid";
	private Midi ShopMIDIPlayer;
	float originalTempoGAME;
	private Game game;
	
	private File _inFile;
	private Scanner _fileInput;
	
	private Boolean isGameSaved;
	private GameSave savedGame;	
	public SocketIO socket;
	private Midi upgradeMidiPlayer;
	private String upgradeMIDIMusic = "Bandit_Radio_synth.midi";
	private String winMIDIMusic = "Super_Mario_Bros._-_Flag_synth.mid";
	/**
	 * Used to switch between each of the screens shown to the user
	 */
	public enum STATE {
		Menu, Help, Help2, Help3, Game, GameOver, GameWon, Upgrade, Boss, Pause, PauseH1, PauseH2, PauseH3, PauseShop, Leaderboard, GameWonHard, GameHard, GameOverHard
	};

	/**
	 * Initialize the core mechanics of the game
	 * @throws MalformedURLException 
	 */

	public Game() throws IOException {

		scaleFactor = (double) drawWidth / (double) WIDTH;
		
		this.readFromSavedGameFile("gameSavesFile.txt");

		handler = new Handler();
		hud = new HUD();
		spawnerH = new SpawnHard(this.handler, this.hud, this.spawner, this);
		spawner = new Spawn1to10(this.handler, this.hud, this);
		spawner2 = new Spawn10to20(this.handler, this.hud, this.spawner, this);
		menu = new Menu(this, this.handler, this.hud, this.spawner);
		upgradeScreen = new UpgradeScreen(this, this.handler, this.hud);
		player = new Player(WIDTH / 2 - 32, HEIGHT / 2 - 32, ID.Player, handler, this.hud, this);
		upgrades = new Upgrades(this, this.handler, this.hud, this.upgradeScreen, this.player, this.spawnerH, this.spawner,
				this.spawner2);
		gameOver = new GameOver(this, this.handler, this.hud);
		gameWon = new GameWon(this, this.handler, this.hud);
		pause = new Pause(this.hud, this, this.handler, false, this.spawner, this.spawner2,this.spawnerH, upgrades,player);
		leaderboard = new Leaderboard(this,this.handler,this.hud, this.gameOver);
		hardLeaderboard = new HardLeaderboard(this, this.handler, this.hud, this.gameOver);
		mouseListener = new MouseListener(this, this.handler, this.hud, this.spawnerH, this.spawner, this.spawner2, this.upgradeScreen,
				this.player, this.upgrades, pause);
		this.addKeyListener(new KeyInput(this.pause, this.handler, this, this.hud, this.player, this.spawner, this.upgrades));
		this.addMouseListener(mouseListener);
		gameMIDIPlayer = new Midi();
		menuMIDIPlayer = new Midi();
		bossMIDIPlayer = new Midi();
		upgradeMidiPlayer = new Midi();
		ShopMIDIPlayer = new Midi();
		new Window((int) drawWidth, (int) drawHeight, "Wave Game ", this);
	}


	/**
	 * The thread is simply a programs path of execution. This method ensures
	 * that this thread starts properly.
	 */
	
	//This sets the game stats when the user goes into the wave game mode, takes stuff from the saved game
	public void setGameStats(){
		
		hud.setLevel(savedGame.getLevel());
		hud.setHealthValueMax(savedGame.getHealth());
		hud.setScore(savedGame.getScore());
		
		
		if(savedGame.getLevel() <= 10){
			spawner.setLevelsRemaining(savedGame.getLevelsRem());
			spawner.setLevelNumber(savedGame.getEnemy());
			spawner.resetTempCounter();
		} else {
			Spawn1to10.LEVEL_SET = 2;
			spawner2.setLevelNumber(savedGame.getEnemy());
			spawner2.setRandomMax(savedGame.getLevelsRem());
			spawner2.resetTempCounter();
			upgrades.setAbility(savedGame.getAbility());
			hud.setAbilityUses(savedGame.getAbilityUses());

		}
	}
	
	
	public void setIsGameSaved(Boolean b){
		isGameSaved = b;
	}
	
	
	public Boolean getIsGameSaved(){	
		return isGameSaved;
	}
	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	//reading from the saved game file to see if there are saved games or not.
	public void readFromSavedGameFile(String inputFile){
		try {
			_inFile =  new File(inputFile); 
			_fileInput = new Scanner(_inFile);
			
			//checks if there 
			
			if(_fileInput.nextInt() == 0){
				isGameSaved = false;
			} else { //takes all the elements from the file
				isGameSaved = true;
				do{
					_fileInput.nextLine();
					String name = _fileInput.next();
					int score = _fileInput.nextInt();
					double health = _fileInput.nextDouble();
					int level = _fileInput.nextInt();
					int enemy = _fileInput.nextInt();
					int lvlRem = _fileInput.nextInt();
					String ability = _fileInput.next();
					int abilityUses = _fileInput.nextInt();
					
					savedGame = new GameSave(name, score, health, level, enemy, lvlRem, ability, abilityUses);
					
					
				
				} while( _fileInput.hasNextLine());
				
				}
				
			} catch (FileNotFoundException e){
			System.out.println(e);
			System.exit(1);		// IO error; exit program
		}
		

	}
	
	
	
	
	
	
	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Best Java game loop out there (used by Notch!)
	 */
	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				try {
					tick();// 60 times a second, objects are being updated
				} catch (IOException | JSONException e) {
					e.printStackTrace();
				}
				delta--;
			}
			if (running)
				render();// 60 times a second, objects are being drawn
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println("FPS: " + frames);
				System.out.println(gameState);
				System.out.println(Spawn1to10.LEVEL_SET+"level");
				frames = 0;
			}
		}
		stop();

	}

	 /* Constantly ticking (60 times per second, used for updating smoothly). Used
	 * for updating the instance variables (DATA) of each entity (location, health,
	 * appearance, etc).
	 * @throws IOException 
	 * @throws JSONException 
	 */
	private void tick() throws IOException, JSONException {
		if(gameState == STATE.Pause || gameState == STATE.PauseShop || gameState == STATE.PauseH1 || gameState == STATE.PauseH2 || gameState == STATE.PauseH3 || gameState == STATE.Leaderboard){
			//do nothing when paused
			
			bossMIDIPlayer.StopMidi();
			upgradeMidiPlayer.StopMidi();
			menuMIDIPlayer.StopMidi();
			gameMIDIPlayer.StopMidi();
			
			
			try {
				ShopMIDIPlayer.PlayMidi(ShopMIDIMusic);
			} catch (IOException | InvalidMidiDataException | MidiUnavailableException e) {
				e.printStackTrace();
			}
			
		} else {
		
		
		handler.tick();// ALWAYS TICK HANDLER, NO MATTER IF MENU OR GAME SCREEN
		if (gameState == STATE.Game || gameState == STATE.Boss && Spawn1to10.LEVEL_SET != 3) {// game is running
			hud.tick();
			if (Spawn1to10.LEVEL_SET == 1) {// user is on levels 1 thru 10, update them
				spawner.tick();
			} else if (Spawn1to10.LEVEL_SET == 2) {// user is on levels 10 thru 20, update them
				spawner2.tick();
			}
		} else if (gameState == STATE.GameHard || gameState == STATE.Boss) {// user is on menu, update the menu items
			hud.tick();
			if (SpawnHard.LEVEL_SET == 1) {
				spawnerH.tick();
			} else {
				spawner.tick();
			}
		} else if (gameState == STATE.Menu || gameState == STATE.Help || gameState == STATE.Help2 || gameState == STATE.Help3) {// user is on menu, update the menu items
			menu.tick();
		} else if (gameState == STATE.Upgrade) {// user is on upgrade screen, update the upgrade screen
			upgradeScreen.tick();
		} else if (gameState == STATE.GameOver || gameState == STATE.GameOverHard) {// game is over, update the game over screen
			gameOver.tick();
		}else if (gameState == STATE.GameWon){
			gameWon.highscore = true;
			gameWon.tick();
		} else if (gameState == STATE.GameWonHard){
			gameWon.highscore = false;
			gameWon.tick();
		}
		
		//working with midi
		
		if (gameState == STATE.Game) {
			bossMIDIPlayer.StopMidi();
			upgradeMidiPlayer.StopMidi();
			menuMIDIPlayer.StopMidi();
			ShopMIDIPlayer.StopMidi();
			try {
				gameMIDIPlayer.PlayMidi(gameMIDIMusic);
				originalTempoGAME = gameMIDIPlayer.getTempo();
			} catch (IOException | InvalidMidiDataException | MidiUnavailableException e) {
				e.printStackTrace();
			}
			if (Spawn1to10.LEVEL_SET == 2) {// user is on levels 10 thru 20, update them
				gameMIDIPlayer.setTempo(160);
			}
			else if (Spawn1to10.LEVEL_SET == 1) {// user is on levels 1 thru 10, update them
				gameMIDIPlayer.setTempo(originalTempoGAME);
			}
		} else if (gameState == STATE.Menu || gameState == STATE.Help || gameState == STATE.Help2 || gameState == STATE.Help3) {
			gameMIDIPlayer.StopMidi();
			bossMIDIPlayer.StopMidi();
			upgradeMidiPlayer.StopMidi();
			ShopMIDIPlayer.StopMidi();
			try {
				menuMIDIPlayer.PlayMidi(menuMIDIMusic);
			} catch (IOException | InvalidMidiDataException | MidiUnavailableException e) {
				e.printStackTrace();
			}
		}else if (gameState == STATE.Boss) {
			gameMIDIPlayer.StopMidi();
			menuMIDIPlayer.StopMidi();
			ShopMIDIPlayer.StopMidi();
			try {
				bossMIDIPlayer.PlayMidi(bossMIDIMusic);
			} catch (IOException | InvalidMidiDataException | MidiUnavailableException e) {
				e.printStackTrace();
			}
		}  else if (gameState == STATE.Upgrade) {
			bossMIDIPlayer.StopMidi();
			ShopMIDIPlayer.StopMidi();
			try {
				upgradeMidiPlayer.PlayMidi(upgradeMIDIMusic);
			} catch (IOException | InvalidMidiDataException | MidiUnavailableException e) {
				e.printStackTrace();
			}
		} else {
			gameMIDIPlayer.StopMidi();
			menuMIDIPlayer.StopMidi();
			bossMIDIPlayer.StopMidi();
			upgradeMidiPlayer.StopMidi();
			ShopMIDIPlayer.StopMidi();
		}
		
		}
	}

	/**
	 * Constantly drawing to the many buffer screens of each entity requiring
	 * the Graphics objects (entities, screens, HUD's, etc).
	 */
	private void render() {
		/*
		 * BufferStrategies are used to prevent screen tearing. In other words,
		 * this allows for all objects to be redrawn at the same time, and not
		 * individually
		 */
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();

		///////// Draw things bellow this/////////////
		g.scale(scaleFactor, scaleFactor);

		g.setColor(Color.black);
		g.fillRect(0, 0, (int) WIDTH, (int) HEIGHT);

		handler.render(g); // ALWAYS RENDER HANDLER, NO MATTER IF MENU OR GAME
							// SCREEN

		if (gameState == STATE.Pause || gameState == STATE.PauseH1 || gameState == STATE.PauseH2
				|| gameState == STATE.PauseH3 || gameState == STATE.PauseShop) {
			hud.render(g);
			pause.render(g);
		} else {
			if (gameState == STATE.Game || gameState == STATE.Boss || gameState == STATE.GameHard) {// user is
																		// playing
																		// game,
																		// draw
																		// game
																		// objects
				hud.render(g);
			} else if (gameState == STATE.Menu || gameState == STATE.Help || gameState == STATE.Help2
					|| gameState == STATE.Help3) {// user is in help or the
													// menu, draw the menu and
													// help objects
				menu.render(g);
			} else if (gameState == STATE.Upgrade) {// user is on the upgrade
													// screen, draw the upgrade
													// screen
				upgradeScreen.render(g);
			} else if (gameState == STATE.GameOver || gameState == STATE.GameOverHard) {// game is over, draw the
														// game over screen
				gameOver.render(g);

			} else if (gameState == STATE.GameWon || gameState == STATE.GameWonHard) {
				gameWon.render(g);
			} else if (gameState == STATE.Leaderboard){
				leaderboard.render(g);
				hardLeaderboard.render(g);
			}
		}
		///////// Draw things above this//////////////
		g.dispose();
		bs.show();
	}

	/**
	 * 
	 * Constantly checks bounds, makes sure players, enemies, and info doesn't
	 * leave screen
	 * 
	 * @param var
	 *            x or y location of entity
	 * @param min
	 *            minimum value still on the screen
	 * @param max
	 *            maximum value still on the screen
	 * @return value of the new position (x or y)
	 */
	public static double clamp(double var, double min, double max) {
		if (var >= max)
			return var = max;
		else if (var <= min)
			return var = min;
		else
			return var;
	}

	public int getPlayerXInt() {
		return (int) player.getX();
	}

	public int getPlayerYInt() {
		return (int) player.getY();
	}

	public static void fuckItUpBrah() {
		Spawn1to10.LEVEL_SET = 3;
	}
	
	public GameOver getGameOver(){
		return gameOver;
	}
	
	public static void main(String[] args) throws IOException {
		new Game();
	}
}

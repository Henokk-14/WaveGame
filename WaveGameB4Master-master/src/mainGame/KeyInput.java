package mainGame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import mainGame.Game.STATE;

/**
 * Handles key input from the user
 * 
 * @author Brandon Loehle 5/30/16
 *
 */

public class KeyInput extends KeyAdapter {

	private Handler handler;
	private boolean[] keyDown = new boolean[5];
	private int speed;
	private int diagonalSpeed;
	private Game game;
	private HUD hud;
	private Player player;
	private Spawn1to10 spawner;
	private Upgrades upgrades;
	private String ability;
	private Pause pause;

	// uses current handler created in Game as parameter
	public KeyInput(Pause pause, Handler handler, Game game, HUD hud, Player player, Spawn1to10 spawner, Upgrades upgrades) {
		this.handler = handler;
		//this.speed = player.getPlayerSpeed();// if this causes issues, roll back change
		//this.diagonalSpeed = player.getDiagonalPlayerSpeed();// if this causes issues, roll back change
		this.game = game;
		this.player = player;
		this.hud = hud;
		this.spawner = spawner;
		this.upgrades = upgrades;
		this.pause = pause;
		keyDown[0] = false;
		keyDown[1] = false;
		keyDown[2] = false;
		keyDown[3] = false;
		keyDown[4] = false;
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		this.speed = player.getPlayerSpeed();
		this.diagonalSpeed = player.getDiagonalPlayerSpeed();//previously speed would not increase on diagonal
		
		//100 points are added when "." is pressed
		if(key == 46){
			if(e.isShiftDown()){
				hud.setScore(10000);//points added when "." is pressed
			}
			hud.setScore(100);
		}
		
		//pause functionality
		if(key == 80){
			if(game.gameState == STATE.Game){
				pause.setGameSaved(false);
				game.gameState = STATE.Pause;
				
				
			} else if (game.gameState == STATE.Pause || game.gameState == STATE.PauseH1 || game.gameState == STATE.PauseH2 || game.gameState == STATE.PauseH3 || game.gameState ==STATE.PauseShop){
				//game.socket.emit("getBoard");
				game.gameState = STATE.Game;
				
			}
			
			
			
			
		}
		//Debug menu for skipping level press shift "X"
		if (key == 88 && e.isShiftDown()) {
			upgrades.levelSkipAbility();
			
		}
		
		//Leader board is toggled when "L" key is pressed in menu
		if (key == 76){
			if (game.gameState == STATE.Menu){
				game.gameState = STATE.Leaderboard;
			} else if (game.gameState == STATE.Leaderboard){
				game.gameState = STATE.Menu;
			}
		}
		
		// finds what key strokes associate with Player
		for (int i = 0; i < handler.object.size(); i++) {
			GameObject tempObject = handler.object.get(i);

			// using only if's allows multiple keys to be triggered at once //nullpointer detected at line 97
			if (tempObject.getId() == ID.Player) {// find the player object, as he is the only one the user can control
				// key events for player 1
				//Goes UP
				if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
					tempObject.setVelY(-(this.speed));
					keyDown[0] = true;
				}
				//Goes Left
				if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
					tempObject.setVelX(-(this.speed));
					keyDown[1] = true;
				}
				//Goes Down
				if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
					tempObject.setVelY(this.speed);
					keyDown[2] = true;
				}
				//Goes Right
				if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
					tempObject.setVelX(this.speed);
					keyDown[3] = true;
				}
				
				//Up and Left
				if (keyDown[0] == true && keyDown[1] == true) {
					tempObject.setVelY(-(this.diagonalSpeed));
					tempObject.setVelX(-(this.diagonalSpeed));
				}
				//Up and Right
				if (keyDown[0] == true && keyDown[3] == true) {
					tempObject.setVelY(-(this.diagonalSpeed));
					tempObject.setVelX(this.diagonalSpeed);
				}
				//Down and Left
				if (keyDown[2] == true && keyDown[1] == true) {
					tempObject.setVelY(this.diagonalSpeed);
					tempObject.setVelX(-(this.diagonalSpeed));
				}
				//Down and Right
				if (keyDown[2] == true && keyDown[3] == true) {
					tempObject.setVelY(this.diagonalSpeed);
					tempObject.setVelX(this.diagonalSpeed);
				}
				/*
				if ((key == KeyEvent.VK_W || key == KeyEvent.VK_UP) && keyDown[2] == true) {
					tempObject.setVelY(this.speed);
				}
				if ((key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) && keyDown[3] == true) {
					tempObject.setVelX(this.speed);
				}
				if ((key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) && keyDown[0] == true) {
					tempObject.setVelY(-(this.speed));
				}
				if ((key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) && keyDown[1] == true) {
					tempObject.setVelX(-(this.speed));
				}
				*/
				
				//Possible space bar Level Skipper but is not used 
				if (key == KeyEvent.VK_SPACE) {
					//upgrades.levelSkipAbility();
				}
				//when having an Ability press enter to activate it 
				if (key == KeyEvent.VK_ENTER) {
					ability = upgrades.getAbility();
					if (ability.equals("clearScreen")) {
						upgrades.clearScreenAbility();
					} else if (ability.equals("levelSkip")) {
						upgrades.levelSkipAbility();
					} else if (ability.equals("freezeTime")) {
						upgrades.freezeTimeAbility();
					}
				}
			}
			
		}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		for (int i = 0; i < handler.object.size(); i++) {
			GameObject tempObject = handler.object.get(i);
			if (tempObject.getId() == ID.Player) {
				// key events for player 1
				if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
					keyDown[0] = false;// tempObject.setVelY(0);
				if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
					keyDown[1] = false;// tempObject.setVelX(0);
				if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
					keyDown[2] = false;// tempObject.setVelY(0);
				if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
					keyDown[3] = false;// tempObject.setVelX(0);
					keyDown[4] = false;
				}
				// vertical movement
				if (!keyDown[0] && !keyDown[2])
					tempObject.setVelY(0);
				// horizontal movement
				if (!keyDown[1] && !keyDown[3])
					tempObject.setVelX(0);
				
				if ((key == KeyEvent.VK_W || key == KeyEvent.VK_UP) && keyDown[2] == true){
					tempObject.setVelY(this.speed);
				}
				if ((key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) && keyDown[3] == true){
					tempObject.setVelX(this.speed);
				}
				if ((key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) && keyDown[0] == true){
					tempObject.setVelY(-(this.speed));
				}
				if ((key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) && keyDown[1] == true){
					tempObject.setVelX(-(this.speed));
				}
			}
		}
		// if (key == KeyEvent.VK_ESCAPE) System.exit(1);
	}
}

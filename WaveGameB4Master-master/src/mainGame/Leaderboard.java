package mainGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

public class Leaderboard {
	
	private Game game;
	private Handler handler;
	private HUD hud;
	private int timer;
	private Color retryColor;
	private String text, title;
	private GameOver gameOver;

	public Leaderboard(Game game, Handler handler, HUD hud, GameOver gameOver) throws MalformedURLException {
		this.game = game;
		this.handler = handler;
		this.hud = hud;
		this.retryColor = Color.white;
		this.gameOver = gameOver;
	}

	public void tick(){
		handler.clearPlayer();
	}

	public void render(Graphics g) {
		Font font = new Font("Amoebic", 1, 85);
		Font font2 = new Font("Amoebic", 1, 40);
		Font font3 = new Font("Amoebic", 1, 160);
		title = "Leaderboard";
		text = "Waves Mode";
		g.setFont(font3);
		g.setColor(Color.GREEN);
		g.drawString(title, Game.WIDTH / 2 - getTextWidth(font2,title) - 295, Game.HEIGHT/6);
		g.setFont(font);
		g.setColor(Color.WHITE);
		g.drawString(text, Game.WIDTH / 4 - getTextWidth(font2,text) - 15, Game.HEIGHT/3 - 50);
		g.setFont(font2);

		ArrayList<String> leaderboard = gameOver.getFileList();

		for (int i = 0; i < leaderboard.size(); i++){
			text = leaderboard.get(i);
			g.drawString(text,Game.WIDTH / 4 - getTextWidth(font2,text)/2, Game.HEIGHT/3 + (50*i));
		}
	}

	/**
	 * Function for getting the pixel width of text
	 * 
	 * @param font
	 *            the Font of the test
	 * @param text
	 *            the String of text
	 * @return width in pixels of text
	 */
	public int getTextWidth(Font font, String text) {
		AffineTransform at = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(at, true, true);
		int textWidth = (int) (font.getStringBounds(text, frc).getWidth());
		return textWidth;
	}

}

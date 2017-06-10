package myGameEngine;

import a3.Game;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;


public class QuitAction extends AbstractInputAction{
	private Game game;
	
	public QuitAction(Game g) {
		game = g;
	}
	public void performAction(float time, Event e) {
		game.shutdown();
		System.exit(0);
	}
}

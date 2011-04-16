import java.awt.*;

/**
 * <code>Player</code> keeps track of each player.
 * This class is used by both the server and client.
 * Written by Dr. Slattery.
 * Modified by Andrew & Paul.
 * 
 * @author Andrew Johnson
 * @author Paul Schrauder
 */
public class Player{
	/**
	 * Location of this player in cell coordinates.
	 */
	int x,y;
	
	/**
	 * Direction this player is facing (UP, DOWN, LEFT, RIGHT).
	 */
	int dir;
	
	/**
	 * Up direction.
	 */
	public static final int UP = 0;
	
	/**
	 * Right direction.
	 */
	public static final int RIGHT = 1;
	
	/**
	 * Down direction.
	 */
	public static final int DOWN = 2;
	
	/**
	 * Left direction.
	 */
	public static final int LEFT = 3;
	
	/**
	 * Color of this player.
	 */
	Color color;
	
	/**
	 * Number of sticks of dynamite player has remaining.
	 */
	private int sticksOfDynamite;
	
	/**
	 * Number of coins player collected.
	 */
	private int coinsCollected;
	
	/**
	 * Instantiates a new <code>Player</code> object with four sticks of dynamite.
	 * @param x1	X cell coordinate
	 * @param y1	Y cell coordinate
	 * @param d		Direction
	 * @param c		Color
	 */
	Player(int x1, int y1, int d, Color c){
		x = x1;
		y = y1;
		dir = d;
		color = c;
		sticksOfDynamite = 4;
		coinsCollected = 0;
	}
	
	/**
	 * Turns player left.
	 */
	public void turnLeft(){
		dir--;
		if (dir < UP){
			dir = LEFT;
		}
	}
	
	/**
	 * Turns player right.
	 */
	public void turnRight(){
		dir++;
		if (dir > LEFT){
			dir = UP;
		}
	}
	
	/**
	 * Paints this player to the screen.
	 * @param g
	 */
	public void paint(Graphics g){
		int px = Grab.CELLSIZE*x;
		int py = Grab.CELLSIZE*y;
		
		g.setColor(color);
		g.fillOval(px,py,Grab.CELLSIZE-1,Grab.CELLSIZE-1);
		
		//	Draw oval indicating which direction player is facing.
		g.setColor(Color.black);
		switch(dir){
			case UP:
				g.fillOval(px+Grab.CELLSIZE/4, py, Grab.CELLSIZE/2, Grab.CELLSIZE/2);
				break;
			case RIGHT:
				g.fillOval(px+Grab.CELLSIZE/2, py+Grab.CELLSIZE/4, Grab.CELLSIZE/2, Grab.CELLSIZE/2);
				break;
			case DOWN:
				g.fillOval(px+Grab.CELLSIZE/4, py+Grab.CELLSIZE/2, Grab.CELLSIZE/2, Grab.CELLSIZE/2);
				break;
			case LEFT:
				g.fillOval(px, py+Grab.CELLSIZE/4, Grab.CELLSIZE/2, Grab.CELLSIZE/2);
				break;
		}
	}
	
	/**
	 * Blasts a stick of dynamite.
	 */
	public void blastStickOfDynamite() {
		sticksOfDynamite--;
	}

	/**
	 * @return the sticksOfDynamite
	 */
	public int getSticksOfDynamite() {
		return sticksOfDynamite;
	}

	/**
	 * Adds a coin to this player's coin collection.
	 */
	public void addCoin() {
		coinsCollected++;
	}

	/**
	 * @return the coinsCollected
	 */
	public int getCoinsCollected() {
		return coinsCollected;
	}
}
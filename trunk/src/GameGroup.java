import java.net.*;
import java.util.*;
import java.awt.*;

/**
 * <code>GameGroup</code> handles the game logic on the server side.
 * This class was written by Dr. Slattery and was modified by Andrew & Paul.
 * 
 * @author Andrew Johnson
 * @author Paul Schrauder
 */
public class GameGroup extends Thread {

	GameClientThread arr[];
	final int SIZE=4;

	int config;  // Simple game "state"
	int grid[][];  //map of the board
	public static final int GWD = 25; // width
	public static final int GHT = 20; // and height of board
	Player red, blue, green, yellow;  //The two players
	public static final int NUM_BLOCKS = 80;
	public static final int NUM_MONEY = 15;

	GameGroup ( Socket s ) {
		arr = new GameClientThread[SIZE];		
		addClient( s );
	}

	public void addClient( Socket s ) {
		int x;

		for( x=0; x<SIZE; x++) 
			if( arr[x] == null || !arr[x].isAlive() ) {
				arr[x] = new GameClientThread(s,this);
				arr[x].start();
				return ;
				}
	}

	public void run() {
		Point p;

		System.out.println("GameGroup begun");
		//Get a random starting board
		String board = fillGrid();

		//Position the two players - Note, we never use	the colors here
		p = emptySpot();
		blue = new Player(p.x, p.y, (int)(4*Math.random()), null);

		// We also need to mark each player's spot in the grid, so we'll
		// know it's not empty
		grid[p.x][p.y] = Grab.PLAYER;
		p = emptySpot();
		red = new Player(p.x, p.y, (int)(4*Math.random()), null);
		grid[p.x][p.y] = Grab.PLAYER;
                
                p = emptySpot();
		green = new Player(p.x, p.y, (int)(4*Math.random()), null);
		grid[p.x][p.y] = Grab.PLAYER;
                
                p = emptySpot();
		yellow = new Player(p.x, p.y, (int)(4*Math.random()), null);
		grid[p.x][p.y] = Grab.PLAYER;

		//Send each player the config.
		output("start,"+board);
		//and player info (including which they are)
		output("blue,"+blue.x+","+blue.y+","+blue.dir);
		output("red,"+red.x+","+red.y+","+red.dir);
                output("green,"+green.x+","+green.y+","+green.dir);
		output("yellow,"+yellow.x+","+yellow.y+","+yellow.dir);
		// We don't use output() here, because we need to send
		// different messages to each player
		arr[0].message("who,blue");
		arr[1].message("who,red");
                arr[2].message("who,green");
		arr[3].message("who,yellow");
	}
	
	/**
	 * Fills in the board at random and returns a String representing the board in row-major order.
	 * Coords are used like screen coords - 0,0 in top-left, first coord is to right, second is down.
	 * @return
	 */
	public String fillGrid(){
		int x,y,i;
		Point p;

		grid = new int[GWD][GHT];
		// Clear grid
		for (x = 0; x < GWD; x++)
		 for (y = 0; y < GHT; y++)
			grid[x][y] = Grab.EMPTY;
		
		// Place blocks
		for (i = 0; i < NUM_BLOCKS; i++){
			p = emptySpot();
			grid[p.x][p.y] = Grab.BLOCK;
		}
		
		// Place money
		for (i = 0; i < NUM_MONEY; i++){
			p = emptySpot();
			grid[p.x][p.y] = Grab.COIN;
		}
		
		//Now, make the string
		StringBuffer sb = new StringBuffer(GHT*GWD);
		for (y = 0; y < GHT; y++)
		 for (x = 0; x < GWD; x++)
			sb.append(grid[x][y]);
		return new String(sb);
	}
	
	public Point emptySpot(){
		int x, y;
		
		// Find an empty square in the grid
		do{
			x = (int)(GWD*Math.random());
			y = (int)(GHT*Math.random());
		} while (grid[x][y] != Grab.EMPTY);
		
		return new Point(x,y);
	}

	public synchronized void processMessage(String msg){
		Player p;

		//System.out.println("pM got:"+msg);

		//Chop up the message, adjust the state, and tell the clients
		StringTokenizer st = new StringTokenizer(msg,",");
		String cmd = st.nextToken();

		// get the player name and find the correct
		// Player object
		// NOTE: This depends on all of the messages having the
		//   same "command,name" structure.
		String pname = st.nextToken();
		if (pname.equals("blue"))
			p = blue;
                else if (pname.equals("red"))
			p = red;
                else if (pname.equals("green"))
			p = green;
                else
			p = yellow;
                
		
		if (cmd.equals("turnleft"))
		{
			p.turnLeft();
			output(pname+","+p.x+","+p.y+","+p.dir);
		}
		else if (cmd.equals("turnright"))
		{
			p.turnRight();
			output(pname+","+p.x+","+p.y+","+p.dir);
		}
		else if (cmd.equals("step"))
		{
			int newx=-1, newy=-1;	//set to illegal subscripts in case the
									//logic below ever fails (at least we'll
									// get a message).

			//Compute new location
			switch(p.dir){
				case Player.UP: newx = p.x; newy = p.y-1;
					if (newy < 0) return;
					break;
				case Player.RIGHT: newx = p.x+1; newy = p.y;
					if (newx >= GameGroup.GWD) return;
					break;
				case Player.DOWN: newx = p.x; newy = p.y+1;
					if (newy >= GameGroup.GHT) return;
					break;
				case Player.LEFT: newx = p.x-1; newy = p.y;
					if (newx < 0) return;
					break;
			}
			if (grid[newx][newy] != Grab.EMPTY)
				return;
			// Clear mark in grid first
			grid[p.x][p.y] = Grab.EMPTY;
			p.x = newx; p.y = newy;
			// Then, mark the new spot
			grid[p.x][p.y] = Grab.PLAYER;
			output(pname+","+p.x+","+p.y+","+p.dir);
		}
		else if(cmd.equals("grab")){
			/*
			 * takes a coin which you are standing next to and facing 
			 * (and does nothing if there is no such coin)
			 */
			//	TODO
			
			int newx=-1, newy=-1;	//set to illegal subscripts in case the
			//logic below ever fails (at least we'll
			// get a message).

			//Compute new location
			switch(p.dir){
				case Player.UP:
					newx = p.x; 
					newy = p.y-1;
					if (newy < 0) return;
					break;
				case Player.RIGHT:
					newx = p.x+1;
					newy = p.y;
					if (newx >= GameGroup.GWD) return;
					break;
				case Player.DOWN:
					newx = p.x;
					newy = p.y+1;
					if (newy >= GameGroup.GHT) return;
					break;
				case Player.LEFT:
					newx = p.x-1;
					newy = p.y;
					if (newx < 0) return;
					break;
			}
			if (grid[newx][newy] == Grab.COIN){
				//	remove coin
				grid[newx][newy] = Grab.EMPTY;
				
				output(pname+"Action,"+"coin"+","+newx+","+newy);
			}
			else{
				return;
			}
		}
		else if(cmd.equals("blast")){
			/*
			 * takes a block which you are standing next to and facing and removes the block from the board. 
			 * Each player starts the game with 4 sticks of dynamite and uses one for each blast, 
			 * so there should be a counter for each player making sure they don't blast more than 4 blocks.
			 */
			//	TODO
			
			int dynamiteRemaining = Integer.parseInt(st.nextToken());
			
			if(dynamiteRemaining > 0){
				int newx=-1, newy=-1;	//set to illegal subscripts in case the
				//logic below ever fails (at least we'll
				// get a message).

				//Compute new location
				switch(p.dir){
					case Player.UP:
						newx = p.x; 
						newy = p.y-1;
						if (newy < 0) return;
						break;
					case Player.RIGHT:
						newx = p.x+1;
						newy = p.y;
						if (newx >= GameGroup.GWD) return;
						break;
					case Player.DOWN:
						newx = p.x;
						newy = p.y+1;
						if (newy >= GameGroup.GHT) return;
						break;
					case Player.LEFT:
						newx = p.x-1;
						newy = p.y;
						if (newx < 0) return;
						break;
				}
				if (grid[newx][newy] == Grab.BLOCK){
					//	remove block
					grid[newx][newy] = Grab.EMPTY;
					
					output(pname+"Action,"+"blast"+","+newx+","+newy);
				}
				else{
					return;
				}
			}
		}
	}

	public void finalize() {
		int x;

		output("bye");
	}

	public void output(String str) {
	// Send a message to each client
		int x;

		for(x=0;x<SIZE;x++) 
			if(arr[x] != null)
				arr[x].message(str);
	}

	public boolean full() {
	// Check if we have all our players
		int x;

		for(x=0;x<SIZE;x++)
			if( arr[x] == null )
				return false;
		return true;
	}
}

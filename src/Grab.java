import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * <code>Grab</code> is the client side of this game.
 * Two-player, networked game.  Run around and grab coins.
 *
 * Code modified from NetOthello (Black Art of Java Game Programming)
 *
 * modified by mike slattery - apr 2000
 * Modified by Andrew & Paul.
 * 
 * @author Andrew Johnson
 * @author Paul Schrauder
 */
public class Grab extends Applet implements Runnable {
	/* the Thread */
	Thread kicker;
	
	/**
	 * 0 - clear
	 * 1 - block (gray)
	 * 2 - money (orange)
	 * 3 - player
	 */
	int grid[][] = new int[GameGroup.GWD][GameGroup.GHT];	  // Game board
	
	/**
	 * Represents an empty spot.
	 */
	public static final int EMPTY = 0;
	
	/**
	 * Represents a block.
	 */
	public static final int BLOCK = 1;
	
	/**
	 * Represents a coin.
	 */
	public static final int COIN = 2;
	
	/**
	 * Represents a player.
	 */
	public static final int PLAYER = 3;
	
	public static final int CELLSIZE=30;
	boolean setup=false;  // record whether we've got the board yet
	Player blue=null, red=null;
	String my_name;

	/* the network stuff */
	PrintWriter pw;
	Socket s=null;
	BufferedReader br = null;
	String name, theHost="localhost";
	int thePort;

	public void init() {
		/* check applet parameters */
		try{
			thePort = Integer.valueOf(getParameter("port")).intValue();
		}
		catch(Exception e) {
			thePort = 2001;
		}

		addMouseListener(new mseL());
		addKeyListener(new keyL());
		
		/* start a new game */
		newGame();
	}

	public void newGame() {
		/* start the thread */
		kicker = new Thread(this);
		kicker.setPriority(Thread.MIN_PRIORITY);
		kicker.start();
	}

	/* the main Thread loop */
	public void run() {
		s = null;

		/* ok, now make the socket connection */
		while(s == null){
			try{
				theHost = getCodeBase().getHost();
				//theHost = "pascal.mscs.mu.edu";
				System.out.println("Attempting to make connection:"+theHost+", "+thePort);
				s = new Socket(theHost,thePort);
				br = new BufferedReader( new InputStreamReader( s.getInputStream() ));
				pw = new PrintWriter( s.getOutputStream() );
			}
			catch( Exception e) {
				System.out.println(e);
				try{
					kicker.sleep(7500);
				}
				catch(Exception ex){};
			}
		}
		
		System.out.println("Connection established");
		//display("Waiting for another player...");

		/* Here is the main network loop
		* Wait for messages from the server
		*/
		while( kicker != null) {
			String input=null;
			StringTokenizer st = null;

			while(input == null){
				try {
					kicker.sleep(100);
					input = br.readLine();
				}
				catch (Exception e) {
					input = null;
				}
			}
			
			System.out.println("Got input:"+input);
			/* if the other person disconnected for any reason... start over */
			if(input.equals("bye" )){
				//	display("Your partner has left the game... Restarting");
				newGame();
				repaint();
				
				return;
			}

			// Chop up the message and see what to do
			st = new StringTokenizer(input,",");
			String cmd = st.nextToken();

			/* if we are ready to start a game */
			if(cmd.equals("start")){
				String val = st.nextToken();
				fillGrid(val);
				setup = true;
				repaint();
			}
			else if (cmd.equals("who")){
				my_name = st.nextToken();
			}
			else if (cmd.equals("blue")){
				String bx = st.nextToken();
				String by = st.nextToken();
				String bd = st.nextToken();
				
				try{
					if (blue == null){
						blue = new Player(0,0,0,Color.blue);
					}
					
					blue.x = Integer.valueOf(bx).intValue();
					blue.y = Integer.valueOf(by).intValue();
					blue.dir = Integer.valueOf(bd).intValue();
				}
				catch(Exception e){}; //if nonsense message, just ignore it
				
				repaint();
			}
			else if (cmd.equals("red")){
				String rx = st.nextToken();
				String ry = st.nextToken();
				String rd = st.nextToken();
				
				try{
					if (red == null)
					red = new Player(0,0,0,Color.red);
					red.x = Integer.valueOf(rx).intValue();
					red.y = Integer.valueOf(ry).intValue();
					red.dir = Integer.valueOf(rd).intValue();
				}
				catch (Exception e) {}; //if nonsense message, just ignore it
				
				repaint();
			}
			else if(cmd.equals("blueAction")){
				String act = st.nextToken();	//	either "coin" or "blast"
				int x = Integer.parseInt(st.nextToken());	//	x
				int y = Integer.parseInt(st.nextToken());	//	y
				
				try{
					if (blue == null){
						blue = new Player(0,0,0,Color.blue);
					}
					
					if(act.equals("coin")){
						blue.addCoin();
						grid[x][y] = EMPTY;
					}
					else if(act.equals("blast") && (blue.getSticksOfDynamite() > 0)){
						blue.blastStickOfDynamite();
						grid[x][y] = EMPTY;
					}
				}
				catch(Exception e){}; //if nonsense message, just ignore it
				
				repaint();
			}
			else if(cmd.equals("redAction")){
				String act = st.nextToken();	//	either "coin" or "blast"
				int x = Integer.parseInt(st.nextToken());	//	x
				int y = Integer.parseInt(st.nextToken());	//	y
				
				try{
					if (red == null){
						red = new Player(0,0,0,Color.red);
					}
					
					if(act.equals("coin")){
						red.addCoin();
						grid[x][y] = EMPTY;
					}
					else if(act.equals("blast") && (red.getSticksOfDynamite() > 0)){
						red.blastStickOfDynamite();
						grid[x][y] = EMPTY;
					}
				}
				catch(Exception e){}; //if nonsense message, just ignore it
				
				repaint();
			}
		}
	}

	void fillGrid(String board){
		// Fill in the grid array with the values
		// in the String board.
		int x, y, i=0;
		char c;

		for (y = 0; y < GameGroup.GHT; y++){
			for (x = 0; x < GameGroup.GWD; x++){
				c = board.charAt(i);
				i++;
				
				switch (c){
					case '0':
						grid[x][y] = EMPTY;
						break;
					case '1':
						grid[x][y] = BLOCK;
						break;
					case '2':
						grid[x][y] = COIN;
						break;
				}
			}
		}
	}

	/* if the Thread stops, be sure to clean up! */
	public void stop() {
		try {
			pw.println("bye");
			pw.flush();
			br.close();
			pw.close();
			s.close();
		}
		catch(Exception e){};
	}

	public void paint(Graphics g) {
		int x,y;

		g.setColor(Color.white);
		g.fillRect(0,0,400,350);
		if (!setup){
			g.setColor(Color.black);
			g.drawString("Waiting...",50,50);
		}
		else{
			System.out.println("painting board");
			// Draw board
			for (x = 0; x < GameGroup.GWD; x++){
				for (y = 0; y < GameGroup.GHT; y++){
					if (grid[x][y] == BLOCK){
						g.setColor(Color.gray);
						g.fillRect(CELLSIZE*x,CELLSIZE*y,CELLSIZE-1,CELLSIZE-1);
					}
					else if (grid[x][y] == COIN){
						g.setColor(Color.orange);
						g.fillOval(CELLSIZE*x+2,CELLSIZE*y+2,CELLSIZE-4,CELLSIZE-4);
					}
				}
			}
			
			g.setColor(Color.black);
			g.drawRect(0,0,CELLSIZE*GameGroup.GWD,CELLSIZE*GameGroup.GHT);
			
			// Add the players if they're there
			if (blue != null){
				blue.paint(g);
				
				g.drawString("Blue Coins Collected: " + blue.getCoinsCollected(), 180, 10);
				g.drawString("Blue Dynamite remaining: " + blue.getSticksOfDynamite(), 180, 20);
			}
			
			if (red != null){
				red.paint(g);
				
				g.drawString("Red Coins Collected: " + red.getCoinsCollected(), 10, 10);
				g.drawString("Red Dynamite remaining: " + red.getSticksOfDynamite(), 10, 20);
			}
		}
	}

	/**
	* Sends message to the server.
	* @param msg
	*/
	public void tellServer(String msg){
		boolean flag = false;
		
		// we keep trying until it's sent
		while (!flag){
			try{
				pw.println(msg);
				pw.flush();
				flag = true;
			}
			catch(Exception e1){
				flag = false;
			}
		}
		
	}

	class mseL extends MouseAdapter{
		public void mousePressed(MouseEvent e){
			requestFocus();
		}
	}

	class keyL extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			int c = e.getKeyCode();
			switch(c){
				case KeyEvent.VK_J:
				case KeyEvent.VK_LEFT:
					tellServer("turnleft," + my_name);
					break;
				case KeyEvent.VK_L:
				case KeyEvent.VK_RIGHT:
					tellServer("turnright," + my_name);
					break;
				case KeyEvent.VK_K:
				case KeyEvent.VK_UP:
					tellServer("step," + my_name);
					break;
				case KeyEvent.VK_G:
					tellServer("grab," + my_name);
					break;
				case KeyEvent.VK_B:
					if(my_name.equals("blue")){
						tellServer("blast," + my_name+","+blue.getSticksOfDynamite());
					}
					else if(my_name.equals("red")){
						tellServer("blast," + my_name + ","+red.getSticksOfDynamite());
					}
					
					break;
			}
		}
	}
}
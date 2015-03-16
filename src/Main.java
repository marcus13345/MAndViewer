import MAndEngine.Engine;

/**
 * creates an engine instance and run the main class on it.
 * 
 * @author mgosselin
 *
 */
public class Main {
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		
		Engine engine = new Engine(new String[] {"Viewer"}, false);
		engine.run();
	}
}
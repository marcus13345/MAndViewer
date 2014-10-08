import MAndApps.Engine;


public class Main {
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		
		Engine engine = new Engine(new String[] {"Settings"}, false);
		engine.run();
	}
}

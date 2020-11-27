package valoeghese.amongusirl;

public class Sabotage {
	public Sabotage(Type type) {
		this.type = type;
	}

	boolean fixed = false;
	final Type type;

	public enum Type {
		OXYGEN
	}
}

package spacewar;

public class Score {

	private int score;
	private String name;
	
	public Score(String name, int score) {
		this.score = score;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	//MÉTODO OVERRIDE PARA HASHCODE?
	
	//MÉTODO PARA COMPARAR DOS PUNTUACIONES? OVERRIDE A EQUALS
}

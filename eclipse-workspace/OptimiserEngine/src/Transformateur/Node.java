package Transformateur;

public class Node implements Cloneable  {
	private String nom;
	private String condition;
	private String phOperator;
	private Node filsGauche;
	private Node filsDroit;

	public Node()
	{
		
	}
	public Node(String nom) {
		this.nom=nom;
		this.filsGauche = null;
		this.filsDroit = null;

	}
	public Node(String nom,String condition) {
		this.nom=nom;
		this.condition=condition;
	
		this.filsGauche = null;
		this.filsDroit = null;
	}
	public Node(String nom, String condition, String phOperator) {
		super();
		this.nom = nom;
		this.condition = condition;
		this.phOperator = phOperator;

		this.filsGauche = null;
		this.filsDroit = null;
	}
	
	public String getPhOperator() {
		return phOperator;
	}
	public void setPhOperator(String phOperator) {
		this.phOperator = phOperator;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public Node getFilsGauche() {
		return filsGauche;
	}
	public void setFilsGauche(Node filsGauche) {
		this.filsGauche = filsGauche;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public Node getFilsDroit() {
		return filsDroit;
	}
	public void setFilsDroit(Node filsDroit) {
		this.filsDroit = filsDroit;
	}
	public void showTree()
	{
		if(this.filsGauche!=null)
			this.filsGauche.showTree();
		
		if(this.getCondition()!=null && this.getPhOperator()==null)
		{
			System.out.print(this.nom);
			System.out.println(this.condition);
		}
		else if(this.getPhOperator()!=null && this.getCondition()!=null)
		{
			System.out.print(this.nom);
			System.out.print(this.condition);
			System.out.println(this.getPhOperator());
		}
		else
			System.out.println(this.nom);
		if(this.filsDroit!=null)
			this.filsDroit.showTree();
	}
	
	
	@Override
    public Node clone() throws CloneNotSupportedException {
        //return (Node) super.clone();
		Node clonedNode = (Node) super.clone();
        if (filsGauche != null) {
            clonedNode.setFilsGauche(filsGauche.clone());
        }
        if (filsDroit != null) {
            clonedNode.setFilsDroit(filsDroit.clone());
        }
        return clonedNode;
    }
}

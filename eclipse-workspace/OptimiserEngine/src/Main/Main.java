package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Transformateur.Node;

import Transformateur.Tree;
import Transformateur.Tree.AliasRepeated;
import Transformateur.Tree.ColumnNotExist;
import Transformateur.Tree.TableNotFoundException;
import Optimizer.Optimate;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Main extends Application {
	public static int counter=0;
	public static Node treeRoot;
	private static String query;
	public static Tree q;
	public static class LexicalIncorrect extends Exception {
		public LexicalIncorrect(String message) {
            super(message);
        }
	}
	public static HashMap<String,String> Explanation = new HashMap<>();
	public static void ReadExplanation()
	{
		Explanation.put("pipeline", "Les données sont traitées de manière continue à travers le pipeline, sans être stockées sous une forme intermédiaire. Cette approche convient mieux aux requêtes qui impliquent de gros volumes de données, où le stockage des résultats intermédiaires serait excessivement coûteux en termes de surcharge de stockage et de traitement.");
		Explanation.put("materialisation","des requêtes qui impliquent des opérations de jointure complexes, car elle réduit la quantité de calculs nécessaires pour exécuter la requête.");
		Explanation.put("Index Scan", "L'attribut est indexé et c'est la meilleur facon pour scaner la table");
		Explanation.put("Balayage", "L'attribut n'est pas indexé donc il faut passer par le balayage");
		Explanation.put("BII", "Joindre deux tables est utile car l'une des tables est petite et l'autre est grande, car la plus petite table peut être analysée plusieurs fois pour se joindre à la plus grande table");
		Explanation.put("BIB", "Joindre deux tables en BIB est util car il y a un petit nombre de lignes correspondantes dans la table interne le prédicat de jointure est sélectif");
		Explanation.put("JTF", "Joindre deux tables en utilisant des données triées est utile car les deux tables sont triées sur les colonnes de jointure. En triant les données, la jointure peut être effectuée efficacement ");
	}
	public static HashMap<String, String[][]> CataShema = new HashMap<>();
	public static HashMap<String, String[][]> ReadMetaData()
    {
   	 	File file = new File("C:\\Users\\Dell\\eclipse-workspace\\OptimiserEngine\\src\\Transformateur\\MetaData.txt");
        HashMap<String, String[][]> map = new HashMap<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                String tableName = parts[0];
                int numRows = parts.length - 1;
                String[][] table = new String[numRows][5];

                for (int i = 0; i < numRows; i++) {
                    String[] rowParts = parts[i + 1].split(",");
                    for (int j = 0; j < 5; j++) {
                        table[i][j] = rowParts[j];
                    }
                }
                map.put(tableName, table);
                	
                }
            
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }
	
	
	class BTView extends Pane {
		  private Node root;
		  private Color color;
		  private Image image;
		  private String es;
		  private double radius = 15; // Tree node radius
		  private double vGap = 90; // Gap between two
		 
		  private Label labeli;
		 private Label Es ;
		  BTView(Node tree) 
		  {
			    this.root = tree;
			    this.color=Color.LIGHTSKYBLUE;
			    this.image = new Image("C:\\Users\\Dell\\eclipse-workspace\\OptimiserEngine\\src\\Main\\7835.png");
			    this.es=("1.1ms");
		  }
		  
		  public void setColor(Color color) {
			this.color = color;
		}
		  public void setImage(Image img)
		  {
			  this.image=img;
		  }
		  public void setEs(String es)
		  {
			  this.es=es;
		  }
	

		

		public void displayTree()
		  {
//			  System.out.println(getWidth());
			    if (root != null)
			      displayTree(root, getWidth() / 2, vGap, getWidth()/5);
		  }
		  
		  private void displayTree(Node root, double x, double y, double hGap) {
			//  System.out.println("New Tree");
			  double localHGap=hGap;
			Line line = null;
		    if (root.getFilsGauche() != null) { //getChildren().add( new Line(x - localHGap, y + vGap, x, y));
		    	if(root.getFilsDroit()==null)
		    		localHGap=0;
		    line=new Line(x - localHGap, y + vGap, x, y);
		    labeli = new Label("");
		    Es = new Label("");
	    	labeli.setAlignment(Pos.CENTER); // align the text of the label to the center
	    	double midpointX = (line.getStartX() + line.getEndX()) / 2; // calculate the x-coordinate of the midpoint
	    	double midpointY = (line.getStartY() + line.getEndY()) / 2; // calculate the y-coordinate of the midpoint

	    	labeli.setLayoutX((midpointX - labeli.getWidth() / 2)-15); // position the label horizontally at the midpoint of the line
	    	labeli.setLayoutY((midpointY - labeli.getHeight() / 2)-20);
	    	Es.setLayoutX(labeli.getLayoutX()+30);
	    	Es.setLayoutY(labeli.getLayoutY()+20);
	    	
	    	 if(root.getPhOperator()!=null)
		     {
		    		//System.out.println("3iw");
	    		 if(((root.getFilsGauche()).getFilsGauche()!=null )
		    				|| ((root.getFilsGauche()).getFilsDroit()!=null))
		    		{
		    			
					    ImageView imageView = new ImageView(image);
					   /// Random rand = new Random();
				        //int randomNumber = rand.nextInt(100);
					    labeli.setGraphic(imageView);
					    Es.setText(es);
					    Es.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 9));
		    		}
		     }
		     Pane pane = new Pane(line, labeli,Es);  
		     getChildren().add(pane);
		     displayTree(root.getFilsGauche(), x - localHGap, y + vGap, hGap / 1.5);
		    }

		    if (root.getFilsDroit() != null) {
		    	if(root.getFilsGauche()==null)  //  getChildren().add(new Line(x + localHGap, y + vGap, x, y));
		    		localHGap=0;
		    	line=new Line(x + localHGap, y + vGap, x, y);
		    	labeli = new Label("");
		    	Es = new Label("");
		    	labeli.setAlignment(Pos.CENTER); // align the text of the label to the center
		    	double midpointX = (line.getStartX() + line.getEndX()) / 2; // calculate the x-coordinate of the midpoint
		    	double midpointY = (line.getStartY() + line.getEndY()) / 2; // calculate the y-coordinate of the midpoint

		    	labeli.setLayoutX((midpointX - labeli.getWidth() / 2)-15); // position the label horizontally at the midpoint of the line
		    	labeli.setLayoutY((midpointY - labeli.getHeight() / 2)-20);
		    	Es.setLayoutX(labeli.getLayoutX()+30);
		    	Es.setLayoutY(labeli.getLayoutY()+20);
		    	 if(root.getPhOperator()!=null)
			     {
			    		//System.out.println("3iw");
			    		if( ((root.getFilsDroit()).getFilsDroit()!=null)
			    				|| ((root.getFilsDroit()).getFilsGauche()!=null))
			    		{
			    			
						    ImageView imageView = new ImageView(image);
						   /// Random rand = new Random();
					        //int randomNumber = rand.nextInt(100);
						    labeli.setGraphic(imageView);
						    Es.setText(es);
						    Es.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 9));
			    		}
			     }
		    	
		    	 Pane pane = new Pane(line, labeli,Es);  
			     getChildren().add(pane);
			     
		      displayTree(root.getFilsDroit(), x + localHGap, y + vGap, hGap / 1.5);
		    }
		    // Display a node
		    Circle circle = new Circle(x, y, radius);
		    Ellipse ellipse = new Ellipse(x,y,58,22);
		    ellipse.setFill(color);
		   // ellipse.setStroke(Color.BLACK);
		   // ellipse.setOpacity(0.3);
		    double widthLab= ellipse.getCenterX();
		    double heightLab=ellipse.getCenterY();
		    String txtVal= new String();
		    String txtCnd= new String();
		    
		    Label Ph=new Label("");
		    Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, 
		    	    CornerRadii.EMPTY, BorderWidths.DEFAULT));
		    BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSALMON, CornerRadii.EMPTY, Insets.EMPTY);
		    Background background = new Background(backgroundFill);
		    Ph.setBackground(background);
		    Ph.setBorder(border);
		    Label subCost=new Label("");
	    	Label cnd=new Label("");
	    	
		    if(root.getCondition()!=null && root.getPhOperator()!=null)
		    {
		    	//txtVal=root.getNom()+""+root.getCondition();
		    	txtVal=root.getNom();
		    	txtCnd=root.getCondition();
		    	//labeli.setVisible(true);
		    	Ph.setText(root.getPhOperator());
		    	Ph.setLayoutX(widthLab-155);
		    	Ph.setLayoutY(heightLab+25);
		    	ellipse.setRadiusX(83);
		    	DecimalFormat df = new DecimalFormat("#.###");
		    	subCost.setText(""+df.format(Tree.getSubCost(root))+" ms");
		    	
		    }
		    else if(root.getCondition()!=null)
		    {
		    	txtVal=root.getNom();
		    	txtCnd=root.getCondition();
		    	ellipse.setRadiusX(83);
		    	subCost.setText("");
		    	
		    }
		    else
		    {
		    	txtVal=root.getNom();
		    }
		   
		    Text txt =new Text(x-45 , y+5, txtVal);
		    Text txtc =new Text(x-40 , y+8, txtCnd);
		    //txt.setFont(new Font(24));
		    double width = txt.getLayoutBounds().getWidth();
		    
		    subCost.setLayoutX(widthLab+60);
		    subCost.setLayoutY(heightLab+15);
		    subCost.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
		    Ph.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13)); 
		    txt.setX(x-width/2);
		    txt.setY(y-5);
		  //  txtc.setX((x-width/2));
		    if(txt.getText().equals("\u03C0") || txt.getText().equals("\u03C3"))
		    {
		    	txt.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 18));
		    }
		    else  if (txt.getText().equals("\u222A")
		    		|| txt.getText().equals("\u2A2F")
		    		|| txt.getText().equals("\u22C8")
		    		)
		    {
		    	if(txt.getText().equals("\u2A2F") 
		    			|| txt.getText().equals("\u22C8"))
    			{
		    		 txtc.setX((x-73));
    			}
		    	txt.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 28));
		    }
		    else
		    {
		    	txt.setY(y+5);
		    }
		   
		    getChildren().addAll(ellipse, txt,txtc,subCost,Ph);
		    
		  }
		}
	public static int getNodeIndex(Node nodeToFind) {
	    int index = 0;
	    for (Map.Entry<Node, Set<Node>> entry : Tree.AllTrees.entrySet()) {
	        if (entry.getKey().equals(nodeToFind)) {
	            return index;
	        }
	        index++;
	    }
	    return -1; // Node not found in map
	}
	int rangL=0;
	int rangP=0;
	String Explain="";
	public void OptimalStage(Stage myStage)
	{
		Explain="";
		myStage.close();
		ReadExplanation();
	    Stage OptimalStage = new Stage();
	    Map<Integer,Node> OptimalTree = Optimizer.Optimate.GetOptimalTree();
	    Map.Entry<Integer, Node> entry = OptimalTree.entrySet().iterator().next();
	    int keyType = entry.getKey();
	    String type="";
	    if(keyType==0) type="Matérialisation";
	    else type="Pipeline";
	    Node Optimal  = entry.getValue();
	    String OptimalQuery = Optimizer.Optimate.getOptimalQuery(Optimal);
		int rl=1;
		int rp=1;
		for(Node n:Tree.Trees)
		{
			if(Tree.compareTrees(n, Optimal)==true)
			{
				rangL=rl;
				Set<Node> currentPhysical = new HashSet<Node>();
				currentPhysical=Tree.AllTrees.get(n);
				rp=1;
				for(Node nn:currentPhysical)
				{
					if(keyType==0)
					{
						if(Tree.compareTrees(nn, Optimizer.Optimate.GetOptimalPhysicalTree(Tree.NodesCost.get(Optimal)))==true
								&& Tree.equalsTreePh(nn, Optimizer.Optimate.GetOptimalPhysicalTree(Tree.NodesCost.get(Optimal)))==true)
						{
							rangP=rp;
						}
					}
					else
					{
						if(Tree.compareTrees(nn, Optimizer.Optimate.GetOptimalPhysicalTree(Tree.NodesCostPipeline.get(Optimal)))==true
								&& Tree.equalsTreePh(nn, Optimizer.Optimate.GetOptimalPhysicalTree(Tree.NodesCostPipeline.get(Optimal)))==true)
						{
							rangP=rp;
						}
					}
					
					rp++;
				}
			}
			rl++;	
		}
	    BTView view = new BTView(Optimal);
		view.setFocusTraversable(false);

	    view.setColor(Color.LIGHTGOLDENRODYELLOW);
	    BorderPane pane = new BorderPane();
	    view.setPrefSize(1400, 600); 
	    pane.setCenter(view);
		AnchorPane top=new AnchorPane();
	    pane.setTop(top);
	    AnchorPane bottom=new AnchorPane();
	    pane.setBottom(bottom);
	    Label Desc = new Label("Optimal Tree From Result of Logical Tree N : "+rangL+" and Physical Tree N : "+rangP + "("+type+")");
	    Desc.setStyle("-fx-font-family: Verdana;"
	    		+ " -fx-font-weight: bold;"
	    		+ " -fx-font-size: 15px;"
	    		);
		Desc.setLayoutX(440);
	    Desc.setLayoutY(30);
	    top.getChildren().add(Desc);
	    
	    Label Adv = new Label("Query Adviser");
	    Adv.setStyle("-fx-font-family: Verdana;"
	    		+ " -fx-font-weight: bold;"
	    		+ " -fx-font-size: 14px;"
	    		);
	    Adv.setLayoutX(350);
		Adv.setLayoutY(-110);
	    bottom.getChildren().add(Adv);
	    TextArea queryOpt = new TextArea();
	    queryOpt.setText(OptimalQuery);
	    queryOpt.setEditable(false);
		queryOpt.setPrefSize(650, 120);
		queryOpt.setLayoutX(400);
		queryOpt.setLayoutY(-75);
		queryOpt.setFocusTraversable(false);
		queryOpt.setStyle("-fx-font-size: 10pt; " +
		        "-fx-font-family: Verdana; " +
		        "-fx-text-fill: black; " +
		        "-fx-background-color: transparent; " +
		        "-fx-border-color: white; " +
		        "-fx-border-width: 0.5px;");
		bottom.getChildren().add(queryOpt);
		int t=1;
		Explain="";
		if(keyType==0) Explain+="1- Matérialisation : "+Explanation.get("materialisation");
		else Explain+="1- Pipeline : "+Explanation.get("pipeline");
		for (Map.Entry<List<String>,String> entryExplain : Optimizer.Optimate.explain.entrySet()) {
			t++;
			String s= Explanation.get(entryExplain.getValue());
			
			if(entryExplain.getKey().size()!=3)
			{
				Explain+="\n\n"+t+"- "+entryExplain.getKey().get(0)+"("+entryExplain.getKey().get(1)+")"+"("+entryExplain.getValue()+") : "+s;
			}
			else
			{
				Explain+="\n\n"+t+"- "+entryExplain.getKey().get(0)+"("+entryExplain.getKey().get(1)+", "+entryExplain.getKey().get(2)+")"+"("+entryExplain.getValue()+") : "+s;
			}
	    }
		Button tips = new Button("Explain WHY ?");
		tips.setStyle("-fx-font-family: Verdana; " +
    	        "-fx-font-weight: bold; " +
    	        "-fx-font-size: 10pt; " +
    	        "-fx-text-fill: black; " +
    	        "-fx-background-color: lightyellow; " +
    	        "-fx-background-radius: 5; " +
    	        "-fx-padding: 10px 20px 10px 20px; " +
    	        "-fx-border-color: transparent; " +
    	        "-fx-border-width: 2px; " +
    	        "-fx-border-radius: 5;" +
    	        "-fx-opacity: 1");
		tips.setLayoutX(1200);
		tips.setLayoutY(-40);
		bottom.getChildren().add(tips);
		tips.setOnAction(event -> {
			
			Alert alert = new Alert(AlertType.INFORMATION, "Why it's the optimal one ?");
			alert.setHeaderText("Explanation");
			alert.setContentText(""+Explain); 
			alert.getDialogPane().setPrefWidth(800);
			alert.getDialogPane().setPrefHeight(500);
			alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
			alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
			alert.showAndWait();
			alert.setTitle("Explanation");
			
        });
		
		Button returnB = new Button("Return back to Physical trees");
		returnB.setStyle("-fx-font-family: Verdana; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 10pt; " +
                "-fx-text-fill: black; " +
                "-fx-background-color: lightblue; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 10px 10px 10px 10px; " +
                "-fx-border-color: none; " +
                "-fx-border-width: 0px; " +
                "-fx-border-radius: 3;");
		
		//returnB.setLayoutX(340);
		returnB.setLayoutX(0);
		returnB.setLayoutY(110);
		bottom.getChildren().add(returnB);
		returnB.setOnAction(event -> {
            UpdateStage(OptimalStage,Tree.Trees.get(0));
           
        });
		ScrollPane scrollPane = new ScrollPane();
	    scrollPane.setContent(pane);
	    
	    OptimalStage.setOnShown(e -> {
	        view.displayTree();
	    });
	    OptimalStage.setResizable(false);
	    Scene scene = new Scene(scrollPane, 1530, 800);
	    OptimalStage.setTitle("Optimiser");
	    OptimalStage.setScene(scene);
	    OptimalStage.show();
	}
	int whichOne=0;
	Button C = null;
	public void UpdateStage(Stage myStage,Node currentTree)
	{
		String type=Tree.Indicators.get(currentTree);
		Label title;
		if(type==null)
		{
			title=new Label("First Logical Tree : ");
		}
		else 
		{
			title=new Label("Logical Tree using  : "+type);
		}
		title.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13)); 
		title.setLayoutX(50);
	    title.setLayoutY(50);
		counter=0;
		myStage.close();
		
		Label mat=new Label("Model Matérialisation");
		Label pipe=new Label("Model Pipeline");
		pipe.setStyle("-fx-font-family: Verdana; " +
    	        "-fx-font-weight: bold; " +
    	        "-fx-font-size: 11pt; " +
    	        "-fx-underline: true;" +
    	        "-fx-text-fill: black; ");
		mat.setStyle("-fx-font-family: Verdana; " +
    	        "-fx-font-weight: bold; " +
    	        "-fx-font-size: 11pt; " +
    	        "-fx-underline: true;" +
    	        "-fx-text-fill: black; ");
		mat.setLayoutY(710);
		pipe.setLayoutY(710);
		mat.setLayoutX(300);
		pipe.setLayoutX(1080);
		Button[] Btn=new Button[Tree.Trees.size()+1];
		System.out.println(Btn.length);
		Stage secondStage = new Stage();
	 	BorderPane pane = new BorderPane();
	 	Set<Node> currentPhysical = new HashSet<Node>();
		currentPhysical=Tree.AllTrees.get(currentTree);
		List<BTView> Physicaltrees = new ArrayList<BTView>();
		TilePane r2 = new TilePane();
	    pane.setTop(r2);
	   
	    for (counter = 0; counter < Tree.Trees.size(); counter++) {
	    	Btn[counter]=new Button("Logical N° : "+Integer.toString(counter+1));
	    	/*Customize Button*/
	    	Btn[counter].setStyle("-fx-font-family: Verdana; " +
	    	        "-fx-font-weight: bold; " +
	    	        "-fx-font-size: 10pt; " +
	    	        "-fx-text-fill: black; " +
	    	        "-fx-background-color: lightblue; " +
	    	        "-fx-background-radius: 5; " +
	    	        "-fx-padding: 10px 20px 10px 20px; " +
	    	        "-fx-border-color: transparent; " +
	    	        "-fx-border-width: 2px; " +
	    			"-fx-opacity: 0.5;" +
	    	        "-fx-border-radius: 5;");
	    	r2.getChildren().add(Btn[counter]);
	    	int s=counter;
	    	if(s==whichOne)
	    	{
	    		Btn[s].setDisable(true);
	    		Btn[s].setStyle("-fx-font-family: Verdana; " +
		    	        "-fx-font-weight: bold; " +
		    	        "-fx-font-size: 10pt; " +
		    	        "-fx-text-fill: black; " +
		    	        "-fx-background-color: lightblue; " +
		    	        "-fx-background-radius: 5; " +
		    	        "-fx-padding: 10px 20px 10px 20px; " +
		    	        "-fx-border-color: transparent; " +
		    	        "-fx-border-width: 2px; " +
		    	        "-fx-border-radius: 5;" +
		    	        "-fx-opacity: 1");
	    		C=Btn[s];
	    	}
	    	Btn[counter].setOnAction(event2 -> {
	    		whichOne=s;
	    		UpdateStage(secondStage,Tree.Trees.get(s));	
            });
	    }
	    
	    r2.setAlignment(Pos.TOP_CENTER);

	    
	    int n = currentPhysical.size()+1;
		Pane[] panes = new Pane[n];
		panes[0]=new Pane();
		//panes[0].getChildren().addAll(title,mat,pipe);
		panes[0].getChildren().addAll(title);
		title.setAlignment(Pos.CENTER);
		BTView viewLogical = new BTView(currentTree);
		
		viewLogical.setLayoutX(0);
		viewLogical.setPrefSize(1400, 1000);
		Physicaltrees.add(viewLogical);
		panes[0].getChildren().add(viewLogical);
		Iterator<Node> It=currentPhysical.iterator();
		int i=1;
		
//		while(It.hasNext())
//		{
//			Node inCurrent=It.next();
//			panes[i] = new Pane();
//		    SplitPane splitPane = new SplitPane();
//		    splitPane.setOrientation(Orientation.HORIZONTAL);
//		    
//		    /*Materialisation*/
//		    Label labelMat = new Label("Physical Tree N° : "+ (i)+" (Matérialisation) ");
//		    labelMat.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13)); 
//		    labelMat.setLayoutX(50);
//		    labelMat.setLayoutY(50);
//		 
//		    BTView viewMat = new BTView(inCurrent);
//		    viewMat.setLayoutX(-70);
//		    viewMat.setPrefSize(900, 1000);
//		    viewMat.setColor(Color.LIGHTYELLOW);
//		    Physicaltrees.add(viewMat);
//		    DecimalFormat df = new DecimalFormat("#.###");
//		    /*COST MATERIALISATION*/
//		    String getC=df.format(Tree.getCost(inCurrent));
//	    	Label costMate = new Label("Cout Total Materialisation = "+getC+" ms");
//	    	costMate.setStyle("-fx-font-family: Verdana; " +
//	                   "-fx-font-weight: bold; " +
//	                   "-fx-font-size: 10pt; " +
//	                   "-fx-background-color: lightpink; " +
//	                   "-fx-border-style: dotted; " +
//	                   "-fx-border-color: black; " +
//	                   "-fx-border-width: 1px; " +
//	                   "-fx-padding: 5px;");
//		    Pane leftPane = new Pane();
//		    leftPane.setPrefWidth(765);
//		    leftPane.getChildren().add(labelMat);
//		    leftPane.getChildren().add(viewMat);
//		    leftPane.getChildren().add(costMate);
//		    costMate.setLayoutX(230);
//		    costMate.setLayoutY(550);
//		    
//		    
//		    
//		    /*PIPELINE*/
//		    Pane rightPane = new Pane();
//		    rightPane.setPrefWidth(765);
//		    if(!Tree.existJTF(inCurrent))
//		    {
//		    	 Label labelPipe = new Label("Physical Tree N° : "+ (i)+" (Pipeline)");
//				    labelPipe.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13)); 
//				    labelPipe.setLayoutX(50);
//				    labelPipe.setLayoutY(50);
//				    BTView viewPipe = new BTView(inCurrent);
//				    viewPipe.setLayoutX(-70);
//				    viewPipe.setPrefSize(900, 1200);
//				    viewPipe.setColor(Color.BISQUE);
//				    viewPipe.setEs("");
//				    Physicaltrees.add(viewPipe);
//				    String getCP=df.format(Tree.getCostPipeLine(inCurrent));
//			    	Label costPipe = new Label("Cout Total Pipeline = "+getCP+" ms");
//			    	costPipe.setStyle("-fx-font-family: Verdana; " +
//			                   "-fx-font-weight: bold; " +
//			                   "-fx-font-size: 10pt; " +
//			                   "-fx-background-color: lightpink; " +
//			                   "-fx-border-style: dotted; " +
//			                   "-fx-border-color: black; " +
//			                   "-fx-border-width: 1px; " +
//			                   "-fx-padding: 5px;");
//			    	costPipe.setLayoutX(230);
//				    costPipe.setLayoutY(550);
//			    	Image img=new Image("C:\\Users\\Dell\\eclipse-workspace\\OptimiserEngine\\src\\Main\\pipe.png");
//			    	viewPipe.setImage(img);
//			    	rightPane.getChildren().add(labelPipe);
//				    rightPane.getChildren().add(viewPipe);
//				    rightPane.getChildren().add(costPipe);
//		    }
//		   
//		  
//		    
//		    
//		    splitPane.getItems().addAll(leftPane, rightPane);
//		    panes[i].getChildren().add(splitPane);
//		  
//		    panes[i].setPrefWidth(1510);
//		    panes[i].setPrefHeight(700);
//		    i++;
//			
//		}
		while(It.hasNext())
		{
			Node inCurrent=It.next();
			panes[i] = new Pane();
		    SplitPane splitPane = new SplitPane();
		    splitPane.setOrientation(Orientation.HORIZONTAL);
		    
		    /*Materialisation*/
		    Label labelMat = new Label("Physical Tree N° : "+ (i)+" (Matérialisation) ");
		    labelMat.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13)); 
		    labelMat.setLayoutX(50);
		    labelMat.setLayoutY(50);
		 
		    BTView viewMat = new BTView(inCurrent);
		    viewMat.setLayoutX(-70);
		    viewMat.setPrefSize(900, 1000);
		    viewMat.setColor(Color.LIGHTYELLOW);
		    Physicaltrees.add(viewMat);
		    DecimalFormat df = new DecimalFormat("#.###");
		    /*COST MATERIALISATION*/
		    String getC=df.format(Tree.getCost(inCurrent));
	    	Label costMate = new Label("Cout Total Materialisation = "+getC+" ms");
	    	costMate.setStyle("-fx-font-family: Verdana; " +
	                   "-fx-font-weight: bold; " +
	                   "-fx-font-size: 10pt; " +
	                   "-fx-background-color: lightpink; " +
	                   "-fx-border-style: dotted; " +
	                   "-fx-border-color: black; " +
	                   "-fx-border-width: 1px; " +
	                   "-fx-padding: 5px;");
	    	costMate.setLayoutX(230);
		    costMate.setLayoutY(550);
	    	/* ******************************** */
	    	Label labelPipe = new Label("Physical Tree N° : "+ (i)+" (Pipeline)");
		    labelPipe.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13)); 
		    labelPipe.setLayoutX(50);
		    labelPipe.setLayoutY(50);
		    BTView viewPipe = new BTView(inCurrent);
		    viewPipe.setLayoutX(-70);
		    viewPipe.setPrefSize(900, 1200);
		    viewPipe.setColor(Color.BISQUE);
		    viewPipe.setEs("");
		    Physicaltrees.add(viewPipe);
		    String getCP=df.format(Tree.getCostPipeLineFinal(inCurrent));
	    	Label costPipe = new Label("Cout Total Pipeline = "+getCP+" ms");
	    	costPipe.setStyle("-fx-font-family: Verdana; " +
	                   "-fx-font-weight: bold; " +
	                   "-fx-font-size: 10pt; " +
	                   "-fx-background-color: lightpink; " +
	                   "-fx-border-style: dotted; " +
	                   "-fx-border-color: black; " +
	                   "-fx-border-width: 1px; " +
	                   "-fx-padding: 5px;");
	    	costPipe.setLayoutX(230);
		    costPipe.setLayoutY(550);
	    	Image img=new Image("C:\\Users\\Dell\\eclipse-workspace\\OptimiserEngine\\src\\Main\\pipe.png");
	    	viewPipe.setImage(img);
		    Pane leftPane = new Pane();
		    Pane rightPane = new Pane();
		   
		    if(!Tree.existJTF(inCurrent))
		    {
		    	 leftPane.setPrefWidth(765);
		    	 rightPane.setPrefWidth(765);
		    	
		    	 leftPane.getChildren().add(labelMat);
				 leftPane.getChildren().add(viewMat);
				 leftPane.getChildren().add(costMate);
		    	 rightPane.getChildren().add(labelPipe);
			     rightPane.getChildren().add(viewPipe);
			     rightPane.getChildren().add(costPipe);
			     splitPane.getItems().addAll(leftPane, rightPane);
		    }
		    else
		    {
			    labelMat.setLayoutX(250);
			    viewMat.setLayoutX(50);
			    viewMat.setPrefSize(1400, 1000);
			    costMate.setLayoutX(530);
		    	leftPane.setPrefWidth(1510);
		    	leftPane.getChildren().add(labelMat);
				leftPane.getChildren().add(viewMat);
				leftPane.getChildren().add(costMate);
				splitPane.getItems().addAll(leftPane);
		    }
		  
		    panes[i].getChildren().add(splitPane);
		    panes[i].setPrefWidth(1510);
		    panes[i].setPrefHeight(700);
		    i++;
			
		}
		
		Button Optimal= new Button("Show Optimal Tree");
		Optimal.setStyle("-fx-font-family: Verdana; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12pt; " +
                "-fx-text-fill: black; " +
                "-fx-background-color: lightblue; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 5px 5px 5px 5px; " +
                "-fx-border-color: black; " +
                "-fx-border-width: 0.5px; " +
                "-fx-border-radius: 3;");
		Button returnBack = new Button("Return Back");
		returnBack.setStyle("-fx-font-family: Verdana; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12pt; " +
                "-fx-text-fill: black; " +
                "-fx-background-color: lightblue; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 5px 5px 5px 5px; " +
                "-fx-border-color: black; " +
                "-fx-border-width: 0.5px; " +
                "-fx-border-radius: 3;");
		

		AnchorPane r3 = new AnchorPane();
		r3.setPrefHeight(50);
		returnBack.setLayoutX(820);
		returnBack.setLayoutY(10);
		Optimal.setLayoutY(10);
		Optimal.setLayoutX(550);
		r3.getChildren().addAll(Optimal, returnBack);
	    Optimal.setOnAction(event -> {
	       OptimalStage(secondStage);
	       whichOne=0;
	    });
		returnBack.setOnAction(event -> {
	        try {
	            start(new Stage());
	            Tree.Trees.clear();
	            Tree.AllTrees.clear();
	            Tree.NodesCost.clear();
	            Tree.NodesCostPipeline.clear();
	          
	           secondStage.close();
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        }
	    });
	    pane.setBottom(r3);
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(panes);

		double[] dividerPositions = new double[n - 1];
		for (int z = 0; z < n - 1; z++) {
		    dividerPositions[z] = (z + 1.0) / n;
		}
		splitPane.setDividerPositions(dividerPositions);
		
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(splitPane);
		//scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		pane.setCenter(scrollPane);
		scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
			int visibleIndex = (int) (newValue.doubleValue() * n);
			 if (visibleIndex == 1) {
				 C.setText("Physical Trees "+(whichOne+1));
				 C.setStyle("-fx-font-family: Verdana; " +
			    	        "-fx-font-weight: bold; " +
			    	        "-fx-font-size: 10pt; " +
			    	        "-fx-text-fill: black; " +
			    	        "-fx-background-color: grey; " +
			    	        "-fx-background-radius: 5; " +
			    	        "-fx-padding: 10px 20px 10px 20px; " +
			    	        "-fx-border-color: transparent; " +
			    	        "-fx-border-width: 2px; " +
			    	        "-fx-border-radius: 5;" +
			    	        "-fx-opacity: 1");
			 }
			
		});
		scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
			int visibleIndex = (int) (newValue.doubleValue() * n);
			 if (visibleIndex == 0) {
				 C.setText("Logical N° :  "+(whichOne+1));
				 C.setStyle("-fx-font-family: Verdana; " +
			    	        "-fx-font-weight: bold; " +
			    	        "-fx-font-size: 10pt; " +
			    	        "-fx-text-fill: black; " +
			    	        "-fx-background-color: lightblue; " +
			    	        "-fx-background-radius: 5; " +
			    	        "-fx-padding: 10px 20px 10px 20px; " +
			    	        "-fx-border-color: transparent; " +
			    	        "-fx-border-width: 2px; " +
			    	        "-fx-border-radius: 5;" +
			    	        "-fx-opacity: 1");
			 }
			
		});
	    /*ScrollPane scrollPane = new ScrollPane();
	    scrollPane.setContent(hbox);
	    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
	    pane.setCenter(scrollPane);*/
		
	    secondStage.setOnShown(e -> {
	    	for (BTView tree : Physicaltrees) {
	            BTView view = (BTView) tree;
	            view.displayTree();
	        }
	    });
	    secondStage.setResizable(false);
	    Scene scene = new Scene(pane, 1530, 800);
	    secondStage.setTitle("Optimiser Engine");
	    secondStage.setScene(scene);
	    secondStage.show();
	}

	public void LogicalFirst(Stage P) {
	    P.close();
	    Stage FirstStage = new Stage();
	    BTView view = new BTView(Tree.Trees.get(0));
	    AnchorPane pane = new AnchorPane();
	    pane.setPrefSize(1330, 740);

	    AnchorPane.setTopAnchor(view, 0.0);
	    AnchorPane.setBottomAnchor(view, 0.0);
	    AnchorPane.setLeftAnchor(view, 0.0);
	    AnchorPane.setRightAnchor(view, 0.0);
	    AnchorPane.setTopAnchor(view, 0.1 * (pane.getPrefHeight() - view.getPrefHeight()));

	    Label title = new Label("First Logical Tree Generated According To The Query");
	    title.setStyle("-fx-font-family: Verdana; " +
	            "-fx-font-weight: bold; " +
	            "-fx-font-size: 13pt;");
	    title.setLayoutX(500);
	    title.setLayoutY(10);
	   
	    
	    /*Button View Logical Trees*/
	    Button viewAll = new Button("View All Logical Trees");
	    /*Customize Button*/
	    viewAll.setStyle("-fx-font-weight: bold; " +
	            "-fx-font-size: 12pt; " +
	            "-fx-text-fill: black; " +
	            "-fx-background-color: lightblue; " +
	            "-fx-background-radius: 5; " +
	            "-fx-padding: 10px 20px 10px 20px; " +
	            "-fx-border-color: transparent; " +
	            "-fx-border-width: 2px;" +
	            "-fx-border-radius: 5;");
	    viewAll.setLayoutX(0);
	    viewAll.setLayoutY(pane.getPrefHeight());
	    
	    viewAll.setOnAction(event -> {
	        UpdateStage(FirstStage, Tree.Trees.get(0));
	    });
	    /*Button Return Back*/
	    Button returnBack = new Button("Return back to query");
	    /*Customize Button*/
	    returnBack.setStyle("-fx-font-weight: bold; " +
	            "-fx-font-size: 12pt; " +
	            "-fx-text-fill: black; " +
	            "-fx-background-color: lightblue; " +
	            "-fx-background-radius: 5; " +
	            "-fx-padding: 10px 20px 10px 20px; " +
	            "-fx-border-color: transparent; " +
	            "-fx-border-width: 2px;" +
	            "-fx-border-radius: 5;");
	    returnBack.setLayoutX(pane.getPrefWidth());
	    returnBack.setLayoutY(pane.getPrefHeight());
	    
	    returnBack.setOnAction(event -> {
	        try {
	            start(new Stage());
	            Tree.Trees.clear();
	            Tree.AllTrees.clear();
	            Tree.NodesCost.clear();
	            Tree.NodesCostPipeline.clear();
	            FirstStage.close();
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        }
	    });
	    pane.getChildren().addAll(title, view,viewAll,returnBack);
	    
	    FirstStage.setOnShown(e -> {
	        view.displayTree();
	    });
	    FirstStage.setResizable(false);
	    Scene scene = new Scene(pane);
	    FirstStage.setTitle("Optimiser");
	    FirstStage.setScene(scene);
	    FirstStage.show();
	}

	public void start(Stage primaryStage) throws Exception {
		AnchorPane pane = new AnchorPane();
		/*TITLE*/
		Label title=new Label("OPTIMISER ENGINE");
		title.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 27)); 
		pane.getChildren().add(title);
		title.setLayoutX(600);
		title.setLayoutY(10);
		
		/*DATABASE SCHEMA*/
		/*Title*/
		Label database= new Label("DATABASE SCHEMA : ");
		pane.getChildren().add(database);
		database.setStyle("-fx-font-size: 12pt; -fx-font-family: Verdana; -fx-fill: black; -fx-font-weight: bold"); 
		database.setLayoutX(70);
		database.setLayoutY(170);
		database.setUnderline(true);
		/*Tables*/
		VBox vbox = new VBox();
		vbox.setLayoutX(130);
		vbox.setLayoutY(200);
		CataShema = ReadMetaData();
		int nbr = 2;
		for (String key : CataShema.keySet()) {
		    HBox hbox = new HBox();
		    String all = (nbr-1) + " -  " + key + "  ";
		    Text allText = new Text(all);
		    allText.setStyle("-fx-font-size: 11pt; -fx-font-family: Verdana; -fx-fill: grey; -fx-font-weight: bold;");
		    TextFlow textFlow = new TextFlow(allText);
		   
		    for (String[] row : CataShema.get(key)) {
		    	Text part1 = new Text(" " + row[0] + " -");
		        part1.setStyle("-fx-font-size: 11pt; -fx-font-family: Verdana; -fx-fill: grey;");
		        if (row[1].equals("1")) {
		            part1.setUnderline(true);
		        }
		        textFlow.getChildren().add(part1);
		    }
		    hbox.getChildren().add(textFlow);
		    vbox.getChildren().add(hbox);
		    nbr++;
		}
		pane.getChildren().add(vbox);
		
		/*TEXT AREA Query*/
		TextArea queryArea = new TextArea();
		if(query!=null)
		{
			queryArea.setText(query);
		}
		queryArea.setPromptText("Enter your query here ... ");
		queryArea.setPrefSize(700, 130);
		pane.getChildren().add(queryArea);
		queryArea.setLayoutX(410);
		queryArea.setLayoutY(350);
		queryArea.setFocusTraversable(false);
		queryArea.setStyle("-fx-font-size: 10pt; " +
		        "-fx-font-family: Verdana; " +
		        "-fx-text-fill: black; " +
		        "-fx-background-color: transparent; " +
		        "-fx-border-color: white; " +
		        "-fx-border-width: 0.5px;");
		/*Button Generate*/
		Button button = new Button("GENERATE TREES");
		button.setStyle("-fx-font-weight: bold; " +
		                "-fx-font-size: 12pt; " +
		                "-fx-text-fill: white; " +
		                "-fx-background-color: lightblue; " +
		                "-fx-padding: 10 10 10 10; " +
		                "-fx-border-color: transparent; " +
		                "-fx-border-style: none none none solid; " +
		                "-fx-border-width: 2;");
		pane.getChildren().add(button);
		button.setLayoutX(690);
		button.setLayoutY(490);
		
		Scene scene = new Scene(pane, 1530, 800);
		primaryStage.setTitle("Optimiser Engine");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
		
		button.setOnAction(event -> {
			query=queryArea.getText().replaceAll("\\r\\n|\\r|\\n", " ");
			
			if(queryArea.getText().isEmpty()) {
				Alert alert = new Alert(AlertType.WARNING, "Please enter a query.");
				alert.setHeaderText("Missing Query");
				alert.setContentText("You forgot to enter a query. Please enter a query and try again.");
				alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
				alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
				alert.showAndWait();
		    } 
			else {
				
				try {
					q = new Tree(query.toUpperCase());
					String x1="(\\s*(\\w+\\.)?\\w+\\s*("+String.join("|", Tree.relational_operators)+")\\s*('[^']+'|[0-9]+(.[0-9]+)?)\\s*)";
					String x2="(\\s*(\\w+\\.)?\\w+\\s*=\\s*(\\w+\\.)?[a-zA-Z][a-zA-Z0-9]*\\s*)";
					        String x3="(AND|OR)";
					String regexWhere = "(\\s*(("+ x1 +")|(" + x2 +")))"+
					        		"(\\s+"+x3+"\\s+(("+ x1 +")|(" + x2 +")))*";
							
					String regex = "SELECT "+"((\\s*(\\w+\\.)?\\w+\\s*)(,\\s*(\\w+\\.)?\\w+\\s*)*|\\*)"+"\\s+FROM "+"(\\s*\\w+(\\s+\\w+)?)(,\\s*\\w+(\\s+\\w+)?)*"+"\\s*(\\s+WHERE "+regexWhere+")?"+"(\\s*;\\s*)?";
					Pattern pattern = Pattern.compile(regex);
					 
					Matcher matcher = pattern.matcher(query.toUpperCase());
					if(matcher.matches())
					{
						System.out.println("ok");
						q.Tokenizing();
						LogicalFirst(primaryStage);
						
					}
			       else
			        {
			        	System.out.println("no");
			        	Alert alert = new Alert(AlertType.ERROR, "Syntaxe Error");
					    alert.setHeaderText("Query Error");
					    alert.setContentText("There was an error executing the query, the sytaxe is incorrect ");
					    alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
						alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
					    alert.showAndWait();
					    try {
					    	Tree.Trees.clear();
					        Tree.AllTrees.clear();
					        Tree.NodesCost.clear();
				            Tree.NodesCostPipeline.clear();
				            primaryStage.close();
							start(new Stage());
							
						} catch (Exception e) {
						 
						}
			        }
					
					
						
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				catch(TableNotFoundException T)
				{
					Alert alert = new Alert(AlertType.ERROR, "Semantique Error");
				    alert.setHeaderText("Query Error");
				    alert.setContentText("There was an error executing the query, "+T.getMessage());
				    alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
					alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
				    alert.showAndWait();
				    try {
				    	Tree.Trees.clear();
				        Tree.AllTrees.clear();
				        Tree.NodesCost.clear();
			            Tree.NodesCostPipeline.clear();
			            primaryStage.close();
						start(new Stage());
						
					} catch (Exception e) {
					 //e.printStackTrace();
					}
				}
				catch(AliasRepeated A)
				{
					Alert alert = new Alert(AlertType.ERROR, "Syntax Error");
				    alert.setHeaderText("Query Error");
				    alert.setContentText("There was an error executing the query, "+A.getMessage());
				    alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
					alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
				    alert.showAndWait();
				    try {
				    	Tree.Trees.clear();
				        Tree.AllTrees.clear();
				        Tree.NodesCost.clear();
			            Tree.NodesCostPipeline.clear();
						start(new Stage());
						
					} catch (Exception e) {
					 //e.printStackTrace();
					}
				}
				catch(ColumnNotExist C)
				{
					Alert alert = new Alert(AlertType.ERROR, "Semantique Error");
				    alert.setHeaderText("Query Error");
				    alert.setContentText("There was an error executing the query, "+C.getMessage());
				    alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
					alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
				    alert.showAndWait();
				    try {
				    	Tree.Trees.clear();
				        Tree.AllTrees.clear();
				        Tree.NodesCost.clear();
			            Tree.NodesCostPipeline.clear();
			            primaryStage.close();
						start(new Stage());
						
					} catch (Exception e) {
					 //e.printStackTrace();
					}
				}
				catch(IOException C)
				{
					Alert alert = new Alert(AlertType.ERROR, "File Error");
				    alert.setHeaderText("File Error");
				    alert.setContentText("File Not Found");
				    alert.getDialogPane().setStyle("-fx-background-color: #f2f2f2; -fx-font-size: 14px; -fx-font-weight: bold;");
					alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
				    alert.showAndWait();
				    try {
				    	Tree.Trees.clear();
				        Tree.AllTrees.clear();
				        Tree.NodesCost.clear();
			            Tree.NodesCostPipeline.clear();
			            primaryStage.close();
						start(new Stage());
						
					} catch (Exception e) {
					 //e.printStackTrace();
					}
				} 
			}
        });    
	}

	public static void main(String[] args) throws CloneNotSupportedException
	{
		launch(args);
	}
}

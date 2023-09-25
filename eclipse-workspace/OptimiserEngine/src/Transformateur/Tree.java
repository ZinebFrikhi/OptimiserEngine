package Transformateur;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Estimateur.Estimate;

public class Tree {
	public static HashMap<String, String[][]> CataRelations = new HashMap<>();
	public static HashMap<String, String[]> CataStatisRelations = new HashMap<>();
	public static List<Node> Trees = new ArrayList<Node>();
	public static Map<Node,String> Indicators= new HashMap<Node,String>();
	private static final String ExpressionReguliere = "";
	private static final Set<String> keywords = new HashSet<>(Arrays.asList("SELECT", "FROM", "WHERE","GROUP BY","HAVING","ORDER BY"));
	private static final Set<String> Select_Predicat = new HashSet<>(Arrays.asList("DISTINCT","ALL"));
	public static final Set<String> relational_operators = new HashSet<>(Arrays.asList("=",">","<",">=","<=","!=","<>"));
	private static final Set<String> logical_operators = new HashSet<>(Arrays.asList("AND","OR"));
	public static Map<String, String> relationAliasMap = new HashMap<>();
	public static ArrayList<Node>  relations=new ArrayList<Node>();
	private String query;
	public static Map<Node, Map<Node,Double>> NodesCost = new HashMap<Node, Map<Node,Double>>();
	public static Map<Node, Map<Node,Double>> NodesCostPipeline = new HashMap<Node, Map<Node,Double>>();
	public static Map<Node, Set<Node>> AllTrees = new HashMap<Node, Set<Node>>();
	public Tree(String query)
	{
		//matcher l expression reguliere sinon erreur syntaxique
		this.query=query;
	}
	public static boolean existJTF(Node w)
	{
		if(w==null)
			return false;
		//System.out.println("hna lmushkil 0"+w.getPhOperator());
		if((w.getPhOperator()!=null))
		{
			if(((w.getPhOperator()).equals("JTF")))
			{
				//System.out.println("YES contains a JTF ");
				return true;
			}
		}
		return (existJTF(w.getFilsGauche()) || existJTF(w.getFilsDroit()));
	}
	public boolean ifExistTable(String table) {
	    boolean tableExists = false;
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader("C:\\\\Users\\\\Dell\\\\eclipse-workspace\\\\OptimiserEngine\\\\src\\\\Transformateur\\\\MetaData.txt"));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            String[] parts = line.split(":");
	            if (parts.length > 0 && parts[0].equals(table)) {
	                tableExists = true;
	                break;
	            }
	        }
	        reader.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return tableExists;
	}
	public static boolean isColumnExist(String columnName) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("C:\\\\Users\\\\Dell\\\\eclipse-workspace\\\\OptimiserEngine\\\\src\\\\Transformateur\\\\MetaData.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
        String[] tableDefs = line.split("\\s+(?=\\w+:)");
        for (String tableDef : tableDefs) {
            // Split the table definition into individual column definitions
            String[] columnDefs = tableDef.substring(tableDef.indexOf(":") + 1).split(":");

            // Loop through each column definition to find the specified column
            for (String columnDef : columnDefs) {
                String[] columnDetails = columnDef.split(",");
                String column = columnDetails[0];

                if (column.equals(columnName)) {
                    // Found the specified column in a table
                    return true;
                }
            }
            }
        }
		return false;
		
	}
	
	public static class TableNotFoundException extends Exception {
        public TableNotFoundException(String message) {
            super(message);
        }
    }
	public static class AliasRepeated extends Exception {
        public AliasRepeated(String message) {
            super(message);
        }
    }
	public static class ColumnNotExist extends Exception {
		public ColumnNotExist(String message) {
            super(message);
        }
	}

	/*LECTURE DE METADATA*/
	public static HashMap<String, String[][]> ReadMetaData(ArrayList<Node> relations)
    {
   	 	File file = new File("C:\\Users\\Dell\\eclipse-workspace\\OptimiserEngine\\src\\Transformateur\\MetaData.txt");
        HashMap<String, String[][]> map = new HashMap<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                String tableName = parts[0];
                Iterator <Node> It = relations.iterator();
                while(It.hasNext())
                {
                	if((It.next().getNom()).equals(tableName))
                	{
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
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }
	public static boolean getInfoCol(String relation , String colonne)//indexed or not
    {
    	 for (String r: CataRelations.keySet())
    	 {
    		 if(r.compareToIgnoreCase(relation)==0)
    		 {
    			 for(String[] st : Arrays.asList(CataRelations.get(r)))
    			 {
    				 if(st[0].compareToIgnoreCase(colonne.toUpperCase())==0)
    				 {
    					 if(st[1].compareToIgnoreCase("1")==0)
    						 return true;
    					 else
    						 return false;
    				 }
    				 	
    			 }
    		 }
    	 }
    	 return false;
     }
    public static Node isColumn(String s, ArrayList<Node> relations2)
     {
    	 for (String r: CataRelations.keySet())
    		 for(String[] st : Arrays.asList(CataRelations.get(r)))
  			{
  				if( Arrays.asList(st).contains(s.toUpperCase()))
  					for(Node n:relations2)
  			    		if(n.getNom().toUpperCase().equals(r))
  			    			return  n;
  			}
    	 return null;
     }
    public static Node isColumn(String s, ArrayList<Node> relations2,String rel)
     {
    	 for (String r: CataRelations.keySet())
    		 for(String[] st : Arrays.asList(CataRelations.get(r)))
  			{
  				if( Arrays.asList(st).contains(s.toUpperCase()))
  					for(Node n:relations2)
  			    		if(n.getNom().toUpperCase().equals(r) && n.getNom().toUpperCase().compareToIgnoreCase(rel)==0)
  			    			return  n;
  			}
    	 return null;
     }
    public static boolean isColumnOfRelation(String c,Node r)
 	{
 		String[][] columns = CataRelations.get(r.getNom());
 		for(String[] st : columns)
 			if( Arrays.asList(st).contains(c.toUpperCase()))
 				return true;
 		return false;
 	}
	/*LECTURE DE STATISDATA*/
    public static HashMap<String,String[]> ReadStatisData(ArrayList<Node> relations)
	{
		HashMap<String, String[]> map = new HashMap<>();
		try {
            File file = new File("C:\\Users\\Dell\\eclipse-workspace\\OptimiserEngine\\src\\Transformateur\\StatisData.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                String tableName = parts[0];
                Iterator <Node> It = relations.iterator();
                while(It.hasNext())
                {
                	if((It.next().getNom()).equals(tableName))
                	{
                		 String[] data = new String[4];
                         for (int i = 0; i < 4; i++) {
                             data[i] = parts[i+1];
                         }
                         map.put(tableName, data);
                	}
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

		return map;
	}
 
    public void Tokenizing() throws CloneNotSupportedException, TableNotFoundException, AliasRepeated, ColumnNotExist, IOException
	{
		Node root=null;
		HashMap<String, String> tokens = new HashMap<String, String>();
		
		for(String s:keywords)
		{
			String start ="("+s+")";
	        String end = "(("+String.join("|", keywords)+")|$|;)";
	        
	        String regex = start+"(.+?)"+ end;
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(query);
	        
	        if (matcher.find()) {
	            String result = matcher.group(2);
	            tokens.put(s, result.trim());
	        }
		}
		
	
		//where clause split
		ArrayList<String> WhereOps = null;
		if(tokens.containsKey("WHERE"))
		{
			WhereOps=new ArrayList<String>();
			String x1="(\\s*(\\w+\\.)?\\w+\\s*("+String.join("|", relational_operators)+")\\s*('[^']+'|[0-9]+(.[0-9]+)?)\\s*)";
			String x11="((\\w+\\.)?\\w+\\s+(LIKE)\\s+'\\w+%')";
			String x12="((\\w+\\.)?\\w+\\s+(IN)\\s+\\(('\\w+'(,'\\w+')+)\\))";
			String x13="((\\w+\\.)?\\w+\\s+(BETWEEN)\\s+([0-9]+)\\s+(AND)\\s+([0-9]+))";
			String x14="((\\w+\\.)?\\w+\\s+(IS NULL)\\s*)";
	        String x2="(\\s*(\\w+\\.)?\\w+\\s*=\\s*(\\w+\\.)?[a-zA-Z][a-zA-Z0-9]*\\s*)";
	        String x3="(AND|OR)";
	        String regex = "\\s*((" + x13 +")|(" + x3 +")|(" + x1 +")|(" + x11 + ")|(" + x12 +")|(" + x14 +")|(" + x2 +"))\\s*";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(tokens.get("WHERE"));

	        while (matcher.find()) {
	        	WhereOps.add(matcher.group().trim());
	        }
		}
//		for(String s:WhereOps)
//			System.out.println(s);
//		System.exit(0);
		
		//PROJECTION
		String[] columns = tokens.get("SELECT").split("\\s*,\\s*");
		for(String c : columns)
		{
			if(!isColumnExist(c) && (!c.equals("*"))) throw new ColumnNotExist("Column Not Found: " + c);
		}
		Node projection = new Node("\u03C0","("+String.join(",", columns)+")");
		
		String[] relationsA = tokens.get("FROM").split("\\s*,\\s*");
		Pattern relationAlias = Pattern.compile("\\s*\\w+\\s+\\w+");
		for(String ra:relationsA)
		{
			if((relationAlias.matcher(ra).matches()))
			{
				String[] relAli = ra.split("\\s+");
				String rela = relAli[0];
				String ali = relAli[1];
				if(relationAliasMap.containsValue(ali))
				{
					System.err.println("erreur alias repete");
					throw new AliasRepeated("Alias Repeated: " + ali);
				}
				relationAliasMap.put(rela, ali);
				//relations.add(new Relation(rela));
				if (ifExistTable(rela)) {
				    relations.add(new Node(rela));
				} else {
				    throw new TableNotFoundException("Table Not Found: " + rela);
				}
			}
			else
			{
				if(ifExistTable(ra))
				{
					relations.add(new Node(ra));
				}
				else {
				    throw new TableNotFoundException("Table Not Found: " + ra);
				}
			}
		}
		
		 CataRelations=ReadMetaData(relations);
		 CataStatisRelations=ReadStatisData(relations);
		
//		 for (Map.Entry<String, String[]> entry : CataStatisRelations.entrySet()) {
//	            String tableName = entry.getKey();
//	            String[] data = entry.getValue();
//
//	            System.out.print(tableName + ": ");
//	            for (String s : data) {
//	                System.out.print(s + " ");
//	            }
//	            System.out.println();
//	        }
		//System.exit(0);
//		for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
//		    System.out.println(entry.getKey() + ": " + entry.getValue());
//		}
//		for(Node rl:relations)
//			System.out.println(rl.getNom());
//		
//		System.exit(0);
		//WHERE CLAUSE
		Node whereNode,workOn=null;
		if(tokens.containsKey("WHERE"))
			{
				whereNode = new Node("Where");
				workOn = whereNode;
			}
		else 
			whereNode=null;
		String op = null;
		if(WhereOps!=null)
		for(String s:WhereOps)
		{	
			String x1="(\\s*(\\w+\\.)?\\w+\\s*("+String.join("|", relational_operators)+")\\s*('[^']+'|[0-9]+(.[0-9]+)?)\\s*)";
			String x11="((\\w+\\.)?\\w+\\s+(LIKE)\\s+'\\w+%')";
			String x12="((\\w+\\.)?\\w+\\s+(IN)\\s+\\(('\\w+'(,'\\w+')+)\\))";
			String x13="((\\w+\\.)?\\w+\\s+(BETWEEN)\\s+([0-9]+)\\s+(AND)\\s+([0-9]+))";
			String x14="((\\w+\\.)?\\w+\\s+(IS NULL)\\s*)";
			String regex = "\\s*((" + x13 +")|(" + x1 +")|(" + x11 + ")|(" + x12 +")|(" + x14 +"))\\s*";
			//Pattern pattern1 = Pattern.compile("(\s*(\\w+.)?\\w+\s*("+String.join("|", relational_operators)+")\s*('[^']+'|[0-9]+(.[0-9]+)?)\s*)");
			Pattern pattern1 = Pattern.compile(regex);
			Pattern pattern2 = Pattern.compile("\\s*(\\w+\\.)?\\w+\\s*=\\s*(\\w+\\.)?\\w+\\s*");
			Pattern pattern3 = Pattern.compile("(AND|OR)");
			if((pattern1.matcher(s)).matches())
			{
				String[] col = s.split("\\s*("+String.join("|", relational_operators)+"|LIKE|BETWEEN|IS NULL|IN)\\s*");
				Node r;
				Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
				Matcher m = columnP.matcher(col[0]);
				if((columnP.matcher(col[0]).matches()))
				{
					String[] spl = col[0].split("\\.");
					String rel = spl[0];
					if(relationAliasMap.containsValue(rel))
					{
						for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
						    if(entry.getValue().compareToIgnoreCase(rel)==0)
						    	rel=entry.getKey();
						}
					}
					//System.out.println(col[0]);
					String co = spl[1];
					//System.exit(0);
					r = isColumn(co, relations, rel);
				}
				else
				{
					r = isColumn(col[0], relations);
				}	
				if(r!=null)
				{
					//Node sigma = new Selection(s);
					Node sigma = new Node("\u03C3","("+s+")");
//					System.out.println("here sigma with nom ="+sigma.getNom() + " and condition "+sigma.getCondition());
					if(isFeuille(workOn,r)== true)
					{
						if(op!=null)
						{
							switch(op)
							{
								case "AND":
									Node parentR = getRelationParent(workOn,r);
									if(parentR.getFilsGauche()!=null)
										if(isSame(parentR.getFilsGauche(),r)==true)
										{
											sigma.setFilsGauche(parentR.getFilsGauche());
											parentR.setFilsGauche(sigma);
										}
										else if(parentR.getFilsDroit()!=null)
											if(isSame(parentR.getFilsDroit(),r)==true)
											{
												sigma.setFilsGauche(parentR.getFilsDroit());
												parentR.setFilsDroit(sigma);
											}
									break;
								case "OR" :
									Node union = new Node("\u222A");
									union.setFilsGauche(whereNode.getFilsGauche());
									sigma.setFilsGauche(r);
									union.setFilsDroit(sigma);
									whereNode.setFilsGauche(union);
									workOn=union.getFilsDroit();
									break;
							}
						}
					}
					else
					{
						if(op==null)
						{
							sigma.setFilsGauche(r);
							workOn.setFilsGauche(sigma);
						}
						else
						{
							switch(op)
							{
								case "AND":
									Node cartesianPr = new Node("\u2A2F");
									cartesianPr.setFilsGauche(workOn.getFilsGauche());
									sigma.setFilsGauche(r);
									cartesianPr.setFilsDroit(sigma);
									workOn.setFilsGauche(cartesianPr);
									addNewRelationtoLeftOr(whereNode,r);
									break;
								case "OR" :
									Node union = new Node("\u222A");
									union.setFilsGauche(whereNode.getFilsGauche());
									addNewRelationtoLeftOr(union,r);
									sigma.setFilsGauche(r);
									union.setFilsDroit(sigma);
									ArrayList<Node>  leftRelations= new ArrayList<Node>() ;
									getRelations(leftRelations,union.getFilsGauche());
									for(Node lr:leftRelations)
										if(isFeuille(union.getFilsDroit(),lr)==false)
											addNewRelationtoRightOr(union,lr);
									
									whereNode.setFilsGauche(union);
									workOn=union.getFilsDroit();
									break;
							}
						}
					}
				}
				else
				{
					//throw new No_Relation_Exception(col[0]);
					System.out.println("erreur");
				}
			}
			else if((pattern2.matcher(s)).matches())//jointure
			{
				Pattern equiJoinP = Pattern.compile("\\s*(\\w+\\.)?\\w+\\s*=\\s*(\\w+\\.)?\\w+\\s*");
				Pattern naturalJoinP = Pattern.compile("\\s*\\w+\\.\\w+\\s*=\\s*\\w+\\.\\w+\\s*");
				
				if((naturalJoinP.matcher(s)).matches() && areSameColumns(s)==true)
				{
					Node naturalJoin = new Node("\u22C8","("+s+")");
					ArrayList<Node> joinRelations = getRelations(s,relations);
					if(op==null)//first one
					{
						naturalJoin.setFilsGauche(joinRelations.get(0));
						naturalJoin.setFilsDroit(joinRelations.get(1));
						whereNode.setFilsGauche(naturalJoin);
					}
					else if(op!=null)
						if(op.equals("OR"))
						{
							Node union = new Node("\u222A");
							union.setFilsGauche(whereNode.getFilsGauche());
							naturalJoin.setFilsGauche(joinRelations.get(0));
							naturalJoin.setFilsDroit(joinRelations.get(1));
							union.setFilsDroit(naturalJoin);
							workOn=union.getFilsDroit();
							whereNode.setFilsGauche(union);
						}
					else //AND
					{
						//si les deux relations ne sont pas des feuilles
						if(isFeuille(whereNode,joinRelations.get(0))==false && isFeuille(whereNode,joinRelations.get(1))==false )
						{
							naturalJoin.setFilsGauche(joinRelations.get(0));
							naturalJoin.setFilsDroit(joinRelations.get(1));
							if(whereNode.getFilsGauche()!=null)
							{
								Node cartesianPr = new Node("\u2A2F");
								cartesianPr.setFilsGauche(workOn.getFilsGauche());
								cartesianPr.setFilsDroit(naturalJoin);
								workOn.setFilsGauche(cartesianPr);
							}
							else
								whereNode.setFilsGauche(naturalJoin);
						}
						//si une des deux relations n'est pas feuille
						else if(isFeuille(workOn,joinRelations.get(1))==false && isFeuille(workOn,joinRelations.get(0))==true)
						{
							Node parentR = getRelationParent(workOn,joinRelations.get(0));
							naturalJoin.setFilsGauche(joinRelations.get(0));
							naturalJoin.setFilsDroit(joinRelations.get(1));
							if(isSame(parentR.getFilsGauche(),joinRelations.get(0))==true)
								parentR.setFilsGauche(naturalJoin);
							else if(isSame(parentR.getFilsDroit(),joinRelations.get(0))==true)
								parentR.setFilsDroit(naturalJoin);
						}
						else if(isFeuille(workOn,joinRelations.get(1))==true && isFeuille(workOn,joinRelations.get(0))==false)
						{
							Node parentR = getRelationParent(workOn,joinRelations.get(1));
							naturalJoin.setFilsGauche(joinRelations.get(0));
							naturalJoin.setFilsDroit(joinRelations.get(1));
							if(isSame(parentR.getFilsGauche(),joinRelations.get(1))==true)
								parentR.setFilsGauche(naturalJoin);
							else if(isSame(parentR.getFilsDroit(),joinRelations.get(1))==true)
								parentR.setFilsDroit(naturalJoin);
						}
						//si les des deux relations sont des feuilles
						else if(isFeuille(workOn,joinRelations.get(1))==true && isFeuille(workOn,joinRelations.get(0))==true)
						{
							//on chercher le noeud qui combine les deux
							Node cmb = combineLeafs(workOn,joinRelations.get(1),joinRelations.get(0));
							naturalJoin.setFilsGauche(cmb.getFilsGauche());
							naturalJoin.setFilsDroit(cmb.getFilsDroit());
							if(cmb==workOn && workOn==whereNode)
								workOn=naturalJoin;
							else
								workOn.setFilsGauche(naturalJoin);
						}
					}
					setjoinRelations(whereNode,joinRelations);
				}
				else if((equiJoinP.matcher(s)).matches()|| ((naturalJoinP.matcher(s)).matches() && areSameColumns(s)==false))
				{
					Node sigmaJ = new Node("\u03C3","("+s+")");
					Node pdCtJ = new Node("\u2A2F");
					ArrayList<Node> joinRelations = getRelations(s,relations);
					pdCtJ.setFilsGauche(joinRelations.get(0));
					pdCtJ.setFilsDroit(joinRelations.get(1));
					sigmaJ.setFilsGauche(pdCtJ);
					if(op==null)//first op
					{
						whereNode.setFilsGauche(sigmaJ);
					}
					else if(op.equals("OR"))
					{
						Node union = new Node("\u222A");
						union.setFilsGauche(whereNode.getFilsGauche());
						union.setFilsDroit(sigmaJ);
						workOn=union.getFilsDroit();
						whereNode.setFilsGauche(union);
					}
					else if(op.equals("AND"))
					{
						//les deux relations ne sont pas des feuilles
						if(isFeuille(whereNode,joinRelations.get(0))==false && isFeuille(whereNode,joinRelations.get(1))==false )
						{
							Node cartesianPr = new Node("\u2A2F");
							cartesianPr.setFilsGauche(workOn.getFilsGauche());
							cartesianPr.setFilsDroit(sigmaJ);
							workOn.setFilsGauche(cartesianPr);
						}
						//si une des deux relations n'est pas feuille
						else if(isFeuille(workOn,joinRelations.get(1))==false && isFeuille(workOn,joinRelations.get(0))==true)
						{
							Node parentR = getRelationParent(workOn,joinRelations.get(0));
							if(isSame(parentR.getFilsGauche(),joinRelations.get(0))==true)
								parentR.setFilsGauche(sigmaJ);
							else if(isSame(parentR.getFilsDroit(),joinRelations.get(0))==true)
								parentR.setFilsDroit(sigmaJ);
						}
						else if(isFeuille(workOn,joinRelations.get(1))==true && isFeuille(workOn,joinRelations.get(0))==false)
						{
							Node parentR = getRelationParent(workOn,joinRelations.get(1));
							if(isSame(parentR.getFilsGauche(),joinRelations.get(1))==true)
								parentR.setFilsGauche(sigmaJ);
							else if(isSame(parentR.getFilsDroit(),joinRelations.get(1))==true)
								parentR.setFilsDroit(sigmaJ);
						}
						//si les des deux relations sont des feuilles
						else if(isFeuille(workOn,joinRelations.get(1))==true && isFeuille(workOn,joinRelations.get(0))==true)
						{
							//on chercher le noeud qui combine les deux
							Node cmb = combineLeafs(workOn,joinRelations.get(1),joinRelations.get(0));
							pdCtJ.setFilsGauche(cmb.getFilsGauche());
							pdCtJ.setFilsDroit(cmb.getFilsDroit());
							if(cmb==workOn)
								workOn=sigmaJ;
							else
								workOn.setFilsGauche(sigmaJ);
						}
					}
					setjoinRelations(whereNode,joinRelations);
				}
				else
				{
					System.out.println("err");
				}
			}
			else if((pattern3.matcher(s)).matches())//AND-OR
			{
				op=s;
			}
		}
		//matcher le nb tables avec nb feuilles
		for(Node r:relations)
		{
			if(whereNode!=null)
			{
				if(isFeuille(whereNode,r)==false)
				{
					System.out.println(r.getNom());
					Node cartesianPr = new Node("\u2A2F");
					cartesianPr.setFilsGauche(whereNode.getFilsGauche());
					cartesianPr.setFilsDroit(r);
					whereNode.setFilsGauche(cartesianPr);
				}
			}
			else//no where clause
			{	
				whereNode = new Node("Where");
				whereNode.setFilsGauche(r);
			}
		}
		
		equiRElations(whereNode,relations);
		for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
		    renameRelation(whereNode, entry.getKey(),entry.getValue());
		}
		//ajouter le order by
//		if(tokens.containsKey("ORDER BY"))
//		{
//			Node orderby = new Node("Order by","( "+tokens.get("ORDER BY")+" )");
			
			//System.out.println(orderby.getNom()+" "+orderby.getCondition());
			//System.exit(0);
//		}
		
		Node finalWhereNode = whereNode.clone();
		
		root=getRoot(finalWhereNode,projection);
		Trees.add(root);
		List<Node> ListLogicalNodes = new ArrayList<Node>();
		
		
		
		//Logical trees
		Node whereNodeJA = setNodeJA(finalWhereNode.clone());//R1
		if(compareTrees(finalWhereNode,whereNodeJA)==false)
		{
			ListLogicalNodes.add(whereNodeJA.clone());
			Node RootNd=getRoot(whereNodeJA,projection.clone());
			Trees.add(RootNd);
			Indicators.put(RootNd,"Associativité de la jointure");
		}
		Node whereNodeJC = setNodeJC(finalWhereNode.clone());//R2 
		if(compareTrees(finalWhereNode,whereNodeJC)==false)
		{
			ListLogicalNodes.add(whereNodeJC.clone());
			Node RootNd=getRoot(whereNodeJC,projection.clone());
			Trees.add(RootNd);
			Indicators.put(RootNd,"Commutativité de la jointure");
		}		
		Node whereNodeCSJ = setNodeCSJ(finalWhereNode.clone());//R3
		if(compareTrees(finalWhereNode,whereNodeCSJ)==false)
		{
			ListLogicalNodes.add(whereNodeCSJ.clone());
			Node RootNd=getRoot(whereNodeCSJ,projection.clone());
			Trees.add(RootNd);
			Indicators.put(RootNd,"Commutativité restreinte de la sélection et de la jointure");
		}
		Node whereNodeSE = setNodeSE(finalWhereNode.clone());//R4
		if(compareTrees(finalWhereNode,whereNodeSE)==false)
		{
			ListLogicalNodes.add(whereNodeSE.clone());
			Node RootNd=getRoot(whereNodeSE,projection.clone());
			Trees.add(RootNd);
			Indicators.put(RootNd,"Eclatement d'une sélection conjonctive");
		}
		Node whereNodeSC = setNodeSC(finalWhereNode.clone());//R5
		if(compareTrees(finalWhereNode,whereNodeSC)==false)
		{
			ListLogicalNodes.add(whereNodeSC.clone());
			Node RootNd=getRoot(whereNodeSC,projection.clone());
			Trees.add(RootNd);
			Indicators.put(RootNd,"Commutativité de la sélection");
		}
		List<Node> ListLogicalNodestmp =new ArrayList<Node>();
		for(Node l:ListLogicalNodes)
			ListLogicalNodestmp.add(l);
		
		for(Node lgN:ListLogicalNodes)
		{
			Node whereNodeJA2 = setNodeJA(lgN.clone());//R1
			if(compareTrees(lgN,whereNodeJA2)==false && compareTrees(finalWhereNode,whereNodeJA2)==false && compareNodetoList(whereNodeJA2,ListLogicalNodestmp)==false)
			{
				ListLogicalNodestmp.add(whereNodeJA2);
				Node RootNd=getRoot(whereNodeJA2,projection.clone());
				Trees.add(RootNd);
				Indicators.put(RootNd,"Associativité de la jointure");
			}
			Node whereNodeJC2 = setNodeJC(lgN.clone());//R2 
			if(compareTrees(lgN,whereNodeJC2)==false && compareTrees(finalWhereNode,whereNodeJC2)==false && compareNodetoList(whereNodeJC2,ListLogicalNodestmp)==false)
			{
				ListLogicalNodestmp.add(whereNodeJC2);
				Node RootNd=getRoot(whereNodeJC2,projection.clone());
				Trees.add(RootNd);
				Indicators.put(RootNd,"Commutativité de la jointure");
			}		
			Node whereNodeCSJ2 = setNodeCSJ(lgN.clone());//R3
			if(compareTrees(lgN,whereNodeCSJ2)==false && compareTrees(finalWhereNode,whereNodeCSJ2)==false && compareNodetoList(whereNodeCSJ2,ListLogicalNodestmp)==false)
			{
				ListLogicalNodestmp.add(whereNodeCSJ2);
				Node RootNd=getRoot(whereNodeCSJ2,projection.clone());
				Trees.add(RootNd);
				Indicators.put(RootNd,"Commutativité restreinte de la sélection et de la jointure");
			}
			Node whereNodeSE2 = setNodeSE(lgN.clone());//R4
			if(compareTrees(lgN,whereNodeSE2)==false &&compareTrees(finalWhereNode,whereNodeSE2)==false && compareNodetoList(whereNodeSE2,ListLogicalNodestmp)==false)
			{
				ListLogicalNodestmp.add(whereNodeSE2);
				Node RootNd=getRoot(whereNodeSE2,projection.clone());
				Trees.add(RootNd);
				Indicators.put(RootNd,"Eclatement d'une sélection conjonctive");
			}
			Node whereNodeSC2 = setNodeSC(lgN.clone());//R5
			if(compareTrees(lgN,whereNodeSC2)==false &&compareTrees(finalWhereNode,whereNodeSC2)==false && compareNodetoList(whereNodeSC2,ListLogicalNodestmp)==false)
			{
				ListLogicalNodestmp.add(whereNodeSC2);
				Node RootNd=getRoot(whereNodeSC2,projection.clone());
				Trees.add(RootNd);
				Indicators.put(RootNd,"Commutativité de la sélection");
			}
		}
		//BUILD PHYSICAL TREE
		for(Node n:Trees)
		{
			AllTrees.put(n, getPhysicalTrees(n.clone()));
		}
		int i=0;
		for (Map.Entry<Node, Set<Node>> entry : AllTrees.entrySet())
		{
			Map<Node,Double> map = new HashMap<Node,Double>();
			Map<Node,Double> mappipeline = new HashMap<Node,Double>();
			for(Node s:entry.getValue())
			{
				Double cost = getCost(s);
				map.put(s.clone(), cost);
				Double costPipeline = getCostPipeLineFinal(s);
				if(!existJTF(s))
				{
					//System.out.println("NO JTF");
					mappipeline.put(s.clone(),costPipeline);
				}	
			}
			NodesCost.put(entry.getKey(), map);
			//System.out.println("SIZE PIPELINE = "+mappipeline.size());
			//System.out.println("SIZE MATER = "+map.size());
			NodesCostPipeline.put(entry.getKey(), mappipeline);
		}
		
		int a=0;
		for(Node n:AllTrees.keySet())
			a=a+1+AllTrees.get(n).size();
		System.out.println("total trees = "+a);
	}
    
    
	public Set<Node> getPhysicalTrees(Node logicalTree) throws CloneNotSupportedException {
	    if (logicalTree == null) {
	        return null;
	    }
	    Set<Node> physicalTrees = new HashSet<Node>();
	    Set<Node> leftPhysicalTrees = getPhysicalTrees(logicalTree.getFilsGauche());
	    Set<Node> rightPhysicalTrees = getPhysicalTrees(logicalTree.getFilsDroit());
	    String op = logicalTree.getNom();
	    if (op.compareToIgnoreCase("\u03C3") == 0) { // selection
	        physicalTrees.addAll(selectionPH(logicalTree.clone()));
	    } else if (op.compareToIgnoreCase("\u03C0") == 0) { // projection
	        physicalTrees.addAll(projectionPH(logicalTree.clone()));
	    } else if (op.compareToIgnoreCase("\u22C8") == 0) { // join
	        physicalTrees.addAll(joinPH(logicalTree.clone()));
	    } else if (op.compareToIgnoreCase("tri") == 0) { // sort
	        physicalTrees.addAll(triPH(logicalTree.clone()));
	    } else { // leaf node
	        physicalTrees.add(logicalTree);
	    }
	    Set<Node> newPhysicalTrees = new HashSet<Node>();
	    for (Node root : physicalTrees) {
	        if (leftPhysicalTrees == null && rightPhysicalTrees == null) {
	            newPhysicalTrees.add(root);
	        } else if (leftPhysicalTrees == null) {
	            for (Node right : rightPhysicalTrees) {
	                Node rootClone = root.clone();
	                rootClone.setFilsDroit(right);
	                newPhysicalTrees.add(rootClone);
	            }
	        } else if (rightPhysicalTrees == null) {
	            for (Node left : leftPhysicalTrees) {
	                Node rootClone = root.clone();
	                rootClone.setFilsGauche(left);
	                newPhysicalTrees.add(rootClone);
	            }
	        } else {
	            for (Node left : leftPhysicalTrees) {
	                for (Node right : rightPhysicalTrees) {
	                    Node rootClone = root.clone();
	                    rootClone.setFilsGauche(left);
	                    rootClone.setFilsDroit(right);
	                    newPhysicalTrees.add(rootClone);
	                }
	            }
	        }
	    }
	    return newPhysicalTrees;
	}
	public Set<Node> projectionPH(Node w)
	{
		Set<Node> nodes =  new HashSet<Node>();
		String[] col =(w.getCondition().replaceAll("[()]", "")).split(",");
		boolean isIndexed=false;
		
		for(String c :col)
		{
			Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
			String rel;
			String co;
			if((columnP.matcher(c).matches()))
			{
				String[] spl = c.split("\\.");
				rel = spl[0];
				co = spl[1];
			}
			else
			{
				Node r = isColumn(c, relations);
				rel=r.getNom();
				co=c;
			}
			if(getInfoCol(rel,co)==true)
				isIndexed=true;
		}
		//if(isIndexed==true)
			//nodes.add(new Node(w.getNom(),w.getCondition(),"Index Scan"));
			nodes.add (new Node(w.getNom(),w.getCondition(),"Balayage"));
		return nodes;
	}
	public Set<Node> selectionPH(Node w)
	{
		Set<Node> nodes =  new HashSet<Node>();
		String[] col =(w.getCondition().replaceAll("[()]", "")).split("\\s*("+String.join("|", relational_operators)+"|LIKE|BETWEEN|IS NULL|IN)\\s*");
		Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
		String rel;
		String co;
		if((columnP.matcher(col[0]).matches()))
		{
			String[] spl = col[0].split("\\.");
			rel = spl[0];
			co = spl[1];
		}
		else
		{
			Node r = isColumn(col[0], relations);
			rel=r.getNom();
			co=col[0];
		}
		if(getInfoCol(rel,co)==true)//index scan
		{
			nodes.add(new Node(w.getNom(),w.getCondition(),"Index Scan"));
		}
			nodes.add(new Node(w.getNom(),w.getCondition(),"Balayage"));
			
		return nodes;
	}
	public Set<Node> joinPH(Node w)
	{
		Set<Node> nodes =  new HashSet<Node>();
		String[] eq = w.getCondition().replaceAll("[()]", "").split("\\s*=\\s*");
		for(String st:eq)
			st=st.trim();
		String[] tab1 = eq[0].split("\\.");
		String[] tab2 = eq[1].split("\\.");
		if(relationAliasMap.containsValue(tab1[0]))
		{
			for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
			    if(entry.getValue().compareToIgnoreCase(tab1[0])==0)
			    	tab1[0]=entry.getKey();
			}
		}
		if(relationAliasMap.containsValue(tab2[0]))
		{
			for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
			    if(entry.getValue().compareToIgnoreCase(tab2[0])==0)
			    	tab2[0]=entry.getKey();
			}
		}
		if((getInfoCol(tab1[0],tab1[1])==true)&&(getInfoCol(tab2[0],tab2[1])==true))
			nodes.add(new Node(w.getNom(),w.getCondition(),"BII"));
		nodes.add(new Node(w.getNom(),w.getCondition(),"JTF"));
		nodes.add(new Node(w.getNom(),w.getCondition(),"BIB"));
//		System.out.println("join return "+nodes.size());
		return nodes;
	}
	public Set<Node> triPH(Node w)
	{
		Set<Node> nodes =  new HashSet<Node>();
		nodes.add(w);
		return nodes;
	}
	public Node getRoot(Node w,Node p)
	{
		if(w!=null)
		{
				if(p.getCondition().compareToIgnoreCase("(*)")==0)
					p=w.getFilsGauche();
				else
				{
					p.setFilsGauche(w.getFilsGauche());
				}
		}	
		return p;
	}
	public static boolean compareTrees(Node tree1, Node tree2) {
	    if (tree1 == null && tree2 == null) {
	        return true;
	    }
	    if (tree1 == null || tree2 == null) {
	        return false;
	    }
	    if(tree1.getNom().compareToIgnoreCase(tree2.getNom())!=0){
	    	
	        return false;
	    }
	    if((tree1.getCondition()==null && tree2.getCondition()!=null)||(tree1.getCondition()!=null && tree2.getCondition()==null))
	    {
	    	return false;
	    }
	    if(tree1.getCondition()!=null && tree2.getCondition()!=null)
		    if(tree1.getCondition().compareToIgnoreCase(tree2.getCondition())!=0){
		        return false;
		    }
	    return compareTrees(tree1.getFilsGauche(), tree2.getFilsGauche()) 
	           && compareTrees(tree1.getFilsDroit(), tree2.getFilsDroit());
	}
	public static boolean equalsTreePh(Node node1, Node node2) {
	    // If both nodes are null, they are equal
	    if (node1 == null && node2 == null) {
	        return true;
	    }
	    if(compareTrees(node1,node2)==false)
	    	return false;
	    if (node1.getPhOperator() == null && node2.getPhOperator() != null ||
	        node1.getPhOperator() != null && !node1.getPhOperator().equals(node2.getPhOperator())) {
	        return false;
	    }
	    
	    // Recursively check the left and right subtrees
	    if (!equalsTreePh(node1.getFilsGauche(), node2.getFilsGauche())) {
	        return false;
	    }
	    if (!equalsTreePh(node1.getFilsDroit(), node2.getFilsDroit())) {
	        return false;
	    }
	    
	    // If we've made it this far, the trees are equal
	    return true;
	}

	
	public static boolean compareNodetoList(Node w, List<Node> list)
	{
		for(Node l:list)
		{
			if(compareTrees(w,l)==true)
				return true;
		}
		return false;
	}

	public Node setNodeSC(Node w)
	{
		if(w.getFilsGauche()==null)
			return null;
		if(w.getFilsGauche()!=null &&
		   w.getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0 && 
		   w.getFilsGauche().getFilsGauche()!=null &&
		   w.getFilsGauche().getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0)
		{
			w.setFilsGauche(SC(w.getFilsGauche()));
		}
		if(w.getFilsDroit()!=null&&
		   w.getFilsDroit().getNom().compareToIgnoreCase("\u03C3")==0 && 
		   w.getFilsDroit().getFilsGauche()!=null &&
		   w.getFilsDroit().getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0)
		{
			w.setFilsDroit(SC(w.getFilsDroit()));
		}
		if(w.getFilsGauche()!=null) 
			setNodeSC(w.getFilsGauche());
		if(w.getFilsDroit()!=null) 
			setNodeSC(w.getFilsDroit());
		return w;
	}
	public Node SC(Node w)
	{
		Node r1=getR(w);
		Node r2=getR(w.getFilsGauche());
		if(r1.getNom().compareToIgnoreCase(r2.getNom())==0)
		{
			Node tmp1 = new Node(w.getFilsGauche().getNom(),w.getFilsGauche().getCondition());
			Node tmp2 = new Node(w.getNom(),w.getCondition());
			tmp2.setFilsGauche(w.getFilsGauche().getFilsGauche());
			tmp1.setFilsGauche(tmp2);
			w=tmp1;
		}
		return w;
	}
	public Node setNodeSE(Node w)
	{
		if(w.getFilsGauche()==null)
			return null;
		if(w.getFilsGauche()!=null &&
		   w.getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0 && 
		   w.getFilsGauche().getFilsGauche()!=null &&
		   w.getFilsGauche().getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0)
		{
			w.setFilsGauche(SE(w.getFilsGauche()));
		}
		if(w.getFilsDroit()!=null&&
		   w.getFilsDroit().getNom().compareToIgnoreCase("\u03C3")==0 && 
		   w.getFilsDroit().getFilsGauche()!=null &&
		   w.getFilsDroit().getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0)
		{
			w.setFilsDroit(SE(w.getFilsDroit()));
		}
		if(w.getFilsGauche()!=null) 
			setNodeSE(w.getFilsGauche());
		if(w.getFilsDroit()!=null) 
			setNodeSE(w.getFilsDroit());
		return w;
	}
	public Node SE(Node w)
	{
		Node r1=getR(w);
		Node r2=getR(w.getFilsGauche());
		if(r1.getNom().compareToIgnoreCase(r2.getNom())==0)
		{
			Node sigma = new Node(w.getNom(),"("+w.getCondition().replaceAll("[()]", "")+" and "+w.getFilsGauche().getCondition().replaceAll("[()]", "")+")");
			sigma.setFilsGauche(w.getFilsGauche().getFilsGauche());
			w=sigma;
		}
		return w;
		
	}
	public Node getR(Node s)
	{
		Node r;
		String[] col =(s.getCondition().replaceAll("[()]", "")).split("\\s*("+String.join("|", relational_operators)+"|LIKE|BETWEEN|IS NULL|IN)\\s*");
		Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
		if((columnP.matcher(col[0]).matches()))
		{
			String[] spl = col[0].split("\\.");
			String rel = spl[0];
			if(relationAliasMap.containsValue(rel))
			{
				for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
				    if(entry.getValue().compareToIgnoreCase(rel)==0)
				    	rel=entry.getKey();
				}
			}
			String co = spl[1];
			r = isColumn(co, relations, rel);
		}
		else
		{
			r = isColumn(col[0], relations);
		}	
//		System.out.println("r = "+r.getNom());
		return r;
	}
	public Node setNodeCSJ(Node w)
	{
		if(w.getFilsGauche()==null)
			return null;
		if(w.getFilsGauche().getNom().compareToIgnoreCase("\u03C3")==0 && w.getFilsGauche().getFilsGauche().getNom().compareToIgnoreCase("\u22C8")==0)
			w.setFilsGauche(CSJ(w.getFilsGauche()));
		if(w.getFilsGauche()!=null) 
			setNodeCSJ(w.getFilsGauche());
		if(w.getFilsDroit()!=null) 
			setNodeCSJ(w.getFilsDroit());
		return w;
	}
	public Node CSJ(Node w)
	{
		Node tmp = new Node("\u22C8",w.getFilsGauche().getCondition());
		Node r=getR(w);
		Node sigma = new Node(w.getNom(),w.getCondition());	
		Node parentR = getRelationParent(w,r);
		if(parentR.getFilsGauche()!=null)
			if(isSame(parentR.getFilsGauche(),r)==true)
			{
				sigma.setFilsGauche(parentR.getFilsGauche());
				parentR.setFilsGauche(sigma);
			}
			else if(parentR.getFilsDroit()!=null)
				if(isSame(parentR.getFilsDroit(),r)==true)
				{
					sigma.setFilsGauche(parentR.getFilsDroit());
					parentR.setFilsDroit(sigma);
				}
		if(parentR==w.getFilsGauche())
		{
			tmp.setFilsGauche(parentR.getFilsGauche());
			tmp.setFilsDroit(parentR.getFilsDroit());
		}
		else
		{
			if(w.getFilsGauche().getFilsGauche()==parentR)
			{
				tmp.setFilsGauche(parentR);
				tmp.setFilsDroit(w.getFilsGauche().getFilsDroit());
			}
			else
			{
				tmp.setFilsGauche(w.getFilsGauche().getFilsGauche());
				tmp.setFilsDroit(parentR);
			}
		}
		
		return tmp;
	}
	public Node setNodeJC(Node w)
	{
		if(w.getFilsGauche()==null)
			return null;
		if(w.getFilsGauche().getNom().compareToIgnoreCase("\u22C8")==0)
			w.setFilsGauche(JC(w.getFilsGauche()));
		if(w.getFilsGauche()!=null) 
			setNodeJC(w.getFilsGauche());
		if(w.getFilsDroit()!=null) 
			setNodeJC(w.getFilsDroit());
		return w;
	}
	public Node JC(Node w)
	{
		Node tmp = w.getFilsGauche();
		w.setFilsGauche(w.getFilsDroit());
		w.setFilsDroit(tmp);
		return w;
	}
	public Node setNodeJA(Node w)
	{
		if(w.getFilsGauche()==null)
			return null;
		if(w.getFilsGauche().getNom().compareToIgnoreCase("\u22C8")==0)
			w.setFilsGauche(JA(w.getFilsGauche()));
		if(w.getFilsGauche()!=null) 
			setNodeJA(w.getFilsGauche());
		if(w.getFilsDroit()!=null) 
			setNodeJA(w.getFilsDroit());
		return w;
	}
	public Node JA(Node w)
	{
		if(w.getFilsGauche()!=null && w.getFilsGauche().getNom().compareToIgnoreCase("\u22C8")==0)
		{
			Node tmp = new Node("\u22C8",w.getFilsGauche().getCondition());
			Node tmp2 = new Node("\u22C8",w.getCondition());
			tmp2.setFilsDroit(w.getFilsDroit());
			if(inJoinCondition(tmp2.getCondition(),getFeuilleRelation(w.getFilsGauche().getFilsDroit()))==true)
			{
				tmp2.setFilsGauche(w.getFilsGauche().getFilsDroit());
				tmp.setFilsGauche(w.getFilsGauche().getFilsGauche());
			}
			else
			{
				tmp2.setFilsGauche(w.getFilsGauche().getFilsGauche());
				tmp.setFilsGauche(w.getFilsGauche().getFilsDroit());
			}
			tmp.setFilsDroit(tmp2);
			w=tmp;
		}
		if(w.getFilsDroit()!=null && w.getFilsDroit().getNom().compareToIgnoreCase("\u22C8")==0)
		{
			Node tmp = new Node("\u22C8",w.getFilsDroit().getCondition());
			Node tmp2 = new Node("\u22C8",w.getCondition());
			tmp2.setFilsGauche(w.getFilsGauche());
			if(inJoinCondition(tmp2.getCondition(),getFeuilleRelation(w.getFilsDroit().getFilsDroit()))==true)
			{
				tmp2.setFilsDroit(w.getFilsDroit().getFilsDroit());
				tmp.setFilsDroit(w.getFilsDroit().getFilsGauche());
			}
			else
			{
				tmp2.setFilsDroit(w.getFilsDroit().getFilsGauche());
				tmp.setFilsDroit(w.getFilsDroit().getFilsDroit());
			}
			tmp.setFilsGauche(tmp2);
			w=tmp;
		}
		return w;
	}
	public Node getFeuilleRelation(Node w)
	{
		if(w==null)
			return null;
		if(w.getFilsDroit()==null && w.getFilsGauche()==null)
			return w;
		if(w.getFilsGauche()!=null)
			return getFeuilleRelation(w.getFilsGauche());
		if(w.getFilsDroit()!=null)
			return getFeuilleRelation(w.getFilsDroit());
		return null;
	}
	public boolean inJoinCondition(String s,Node w)
	{
		String[] eq = s.replaceAll("[()]", "").split("\\s*=\\s*");
		for(String st:eq)
			st=st.trim();
		String[] tab1 = eq[0].split("\\.");
		String[] tab2 = eq[1].split("\\.");
		if(w.getNom().compareToIgnoreCase(tab1[0])==0 || w.getNom().compareToIgnoreCase(tab2[0])==0)
			return true;
		return false;
	}
	
	public void renameRelation(Node current, String key, String value) {
		if(current==null || current.getNom().compareToIgnoreCase("\u03C1")==0)
			return;
		if((current.getFilsGauche()!=null && current.getFilsGauche().getNom().compareToIgnoreCase(key)==0))
		{
			Node ren = new Node("\u03C1","("+value+")");
			ren.setFilsGauche(current.getFilsGauche());
			current.setFilsGauche(ren);
		}
		if((current.getFilsDroit()!=null && current.getFilsDroit().getNom().compareToIgnoreCase(key)==0))
		{
			Node ren = new Node("\u03C1","("+value+")");
			ren.setFilsGauche(current.getFilsDroit());
			current.setFilsDroit(ren);
		}
		renameRelation(current.getFilsGauche(), key,  value);
		renameRelation(current.getFilsDroit(), key,  value);		
	}
	public boolean isSame(Node a, Node b) {
		if(a.getNom().compareToIgnoreCase(b.getNom())==0)
			return true;
		return false;
	}
	public boolean isFeuille(Node n,Node f)
	{
		
		if(n==null)
			return false;
		else if(n.getNom().toUpperCase().compareToIgnoreCase(f.getNom())==0)
			return true;
		else
		{
			return (false || isFeuille(n.getFilsGauche(),f)||isFeuille(n.getFilsDroit(),f));
		}
	}
	public Node getRelationParent(Node w,Node r)
	{
		if(w == null)
			return null;
		if((w.getFilsGauche()!= null && w.getFilsGauche().getNom().toUpperCase().compareToIgnoreCase(r.getNom())==0)||
				(w.getFilsDroit()!=null && w.getFilsDroit().getNom().toUpperCase().compareToIgnoreCase(r.getNom())==0))
		{
			return w;
		}
		if(w.getFilsGauche()!=null)
		{
			Node left = getRelationParent(w.getFilsGauche(), r);
			if (left != null) {
		        return left;
		    }
		}
	    if(w.getFilsDroit()!=null)
	    {
	    	Node right = getRelationParent(w.getFilsDroit(), r);
		    if (right != null) {
		        return right;
		    }
	    }
	    return null;
	}
	public void addNewRelationtoLeftOr(Node w,Node r)
	{
		if(w==null)
			return;
		if(w.getNom().compareToIgnoreCase("\u222A")==0 && isFeuille(w.getFilsGauche(),r)==false)
		{
			Node cartesianPr = new Node("\u2A2F");
			cartesianPr.setFilsGauche(w.getFilsGauche());
			cartesianPr.setFilsDroit(r);
			w.setFilsGauche(cartesianPr);
		}
		addNewRelationtoLeftOr(w.getFilsGauche(),r);
		addNewRelationtoLeftOr(w.getFilsDroit(), r);
	}
	public void addNewRelationtoRightOr(Node w,Node r)
	{
		if(w==null)
			return;
		if(w.getFilsDroit()!=null)
			{if(w.getNom().compareToIgnoreCase("\u222A")==0 && isFeuille(w.getFilsDroit(),r)==false)
			{
				Node cartesianPr = new Node("\u2A2F");
				cartesianPr.setFilsGauche(w.getFilsDroit());
				cartesianPr.setFilsDroit(r);
				w.setFilsDroit(cartesianPr);
			}
			addNewRelationtoRightOr(w.getFilsGauche(),r);
			addNewRelationtoRightOr(w.getFilsDroit(), r);}
	}
	public void getRelations( ArrayList<Node> a,Node n) {
		if(n==null)
			return;
		if(n.getFilsGauche()==null && n.getFilsDroit()==null)
		{
			a.add(n);
		}
		if(n.getFilsGauche()!=null)
			getRelations(a,n.getFilsGauche());
		if(n.getFilsDroit()!=null)
			getRelations(a,n.getFilsDroit());
	}
	public boolean areSameColumns(String s) {
		String[] eq = s.split("\\s*=\\s*");
		for(String st:eq)
			st=st.trim();
		String[] tab1 = eq[0].split("\\.");
		String[] tab2 = eq[1].split("\\.");
		if(tab1[1].equals(tab2[1]))
			return true;
		return false;
	}
	public ArrayList<Node> getRelations(String s,ArrayList<Node>  relations) {
		String[] eq = s.split("\\s*=\\s*");
		for(String st:eq)
			st=st.trim();
		String[] tab1= new String[2];
		String[] tab2=new String[2];
		Pattern equiJoinP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
		if((equiJoinP.matcher(eq[0])).matches())//avec nom de table
		{
			tab1 = eq[0].split("\\.");
			if(relationAliasMap.containsValue(tab1[0]))
			{
				for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
				    if(entry.getValue().compareToIgnoreCase(tab1[0])==0)
				    	tab1[0]=entry.getKey();
				}
			}
		}
		else
		{
			for(Node n:relations)
			{
				if(isColumnOfRelation(eq[0], n)==true)
				{
					tab1[0]=n.getNom();
					tab1[1]=eq[0];
				}
				else
				{
					System.err.print("erreur colonne");
				}
			}
					
		}
		if((equiJoinP.matcher(eq[1])).matches())
		{
			tab2= eq[1].split("\\.");
			if(relationAliasMap.containsValue(tab2[0]))
			{
				for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
				    if(entry.getValue().compareToIgnoreCase(tab2[0])==0)
				    	tab2[0]=entry.getKey();
				}
			}
		}
		else
		{
			for(Node n:relations)
				if(isColumnOfRelation(eq[1], n))
				{
					tab2[0]=n.getNom();
					tab2[1]=eq[1];
				}
		}
		Node r1 = new Node(tab1[0]);
		Node r2 = new Node(tab2[0]);
		ArrayList<Node> R = new ArrayList<Node>();
		R.add(r1);
		R.add(r2);
		return R;
	}
	public  void setjoinRelations(Node whereNode, ArrayList<Node> joinRelations) {
		if(whereNode==null)
			return;
		for(Node lr:joinRelations)
		{
			if(isFeuille(whereNode.getFilsGauche(),lr)==false)
				addNewRelationtoLeftOr(whereNode,lr);
			if(isFeuille(whereNode.getFilsDroit(),lr)==false)
				addNewRelationtoRightOr(whereNode,lr);
		}
		setjoinRelations(whereNode.getFilsGauche(), joinRelations);
		setjoinRelations(whereNode.getFilsDroit(), joinRelations);
	}
	public Node combineLeafs(Node w, Node n1, Node n2) {
		Node cmb = getRelationParent(w,n1);
		if(cmb==w)
			return cmb;
		else if(isFeuille(cmb,n1)==true && isFeuille(cmb,n2)==true)
			return cmb;
		return combineLeafs(w, cmb, n2);
	}
	public void equiRElations(Node n,ArrayList<Node>  relations)
	{
		if(n==null)
			return;
		for(Node r:relations)
		{
			if(n.getNom().matches("\u222A"))//union
			{
				if(isFeuille(n.getFilsGauche(),r)==false)
				{
					Node cartesianPr = new Node("\u2A2F");
					cartesianPr.setFilsGauche(n.getFilsGauche());
					cartesianPr.setFilsDroit(r);
					n.setFilsGauche(cartesianPr);
				}
				if(isFeuille(n.getFilsDroit(),r)==false)
				{
					Node cartesianPr = new Node("\u2A2F");
					cartesianPr.setFilsGauche(n.getFilsDroit());
					cartesianPr.setFilsDroit(r);
					n.setFilsDroit(cartesianPr);
				}
			}
		}
		equiRElations(n.getFilsGauche(),relations);
		equiRElations(n.getFilsDroit(),relations);
	}
	
	public static double getCostPipeLine(Node w)
	{
		if(w.getFilsGauche()==null && w.getFilsDroit()==null)
			return 0;
		if(((w.getFilsGauche().getFilsGauche()==null && w.getFilsGauche().getFilsDroit()==null) && w.getFilsDroit()==null) ||
			((w.getFilsGauche().getFilsGauche()==null && w.getFilsGauche().getFilsDroit()==null)&&(w.getFilsDroit().getFilsGauche()==null && w.getFilsDroit().getFilsDroit()==null)))
		{
			if(w.getNom().compareToIgnoreCase("\u22C8")==0)//jointure
				return getSubCost(w);
			else
				return 0;
		}
		
		double s=0,sd=0,sg=0;
		if(w.getFilsGauche()!=null)
			sg=getCostPipeLine(w.getFilsGauche());
		if(w.getFilsDroit()!=null)
			sd=getCostPipeLine(w.getFilsDroit());
		double c;//=getSubCost(w);
		if(w.getNom().compareToIgnoreCase("\u22C8")==0)//jointure
			c=getSubCost(w);
		else
			c=0;
		if(c>Math.max(sd, sg))
			s=c;
		else
			s=Math.max(sg, sd);
		return s;
	}
	
	public static double getCostPipeLineNoJoin(Node w)
	{
		if(w.getFilsGauche()==null && w.getFilsDroit()==null)
			return 0;
		if(((w.getFilsGauche().getFilsGauche()==null && w.getFilsGauche().getFilsDroit()==null) && w.getFilsDroit()==null) ||
			((w.getFilsGauche().getFilsGauche()==null && w.getFilsGauche().getFilsDroit()==null)&&(w.getFilsDroit().getFilsGauche()==null && w.getFilsDroit().getFilsDroit()==null)))
			return getSubCost(w);
		double s=0,sd=0,sg=0;
		if(w.getFilsGauche()!=null)
			sg=getCostPipeLine(w.getFilsGauche());
		if(w.getFilsDroit()!=null)
			sd=getCostPipeLine(w.getFilsDroit());
		double c=getSubCost(w);
		if(c>Math.max(sd, sg))
			s=c;
		else
			s=Math.max(sg, sd);
		return s;
	}
	
	public static double getCostPipeLineFinal(Node w)
	{
		if(w.getFilsGauche()==null && w.getFilsDroit()==null)
			return 0;
		if(existJoin(w)==false)
		{
			return getCostPipeLineNoJoin(w);
		}
		else
		{
			return getCostPipeLine(w);
		}
	}
	
	public static boolean existJoin(Node w)
	{
		if(w==null)
			return false;
		if(w.getNom().compareToIgnoreCase("\u22C8")==0)
			return true;
		return (existJoin(w.getFilsGauche()) || existJoin(w.getFilsDroit()));
	}
	
	public static double getCost(Node w)
	{
		if(w==null)
			return 0;
		if(w.getFilsDroit()==null && w.getFilsGauche()==null)
			return 0;
		double cost = 0;
		cost=cost+getCost(w.getFilsGauche());
		cost=cost+getCost(w.getFilsDroit());
		
		cost=cost+getSubCost(w);
//		System.out.println("getCost "+w.getNom()+" "+w.getCondition()+" "+w.getPhOperator()+" cost ="+cost);
		return cost;
	}
	public static double getSubCost(Node w)
	{
		if(w.getNom().compareToIgnoreCase("\u03C3")==0)//selection
		{
//			System.out.print("selection ");
			String[] col = w.getCondition().replaceAll("[()]", "").split("\\s*("+String.join("|", relational_operators)+"|LIKE|BETWEEN|IS NULL|IN)\\s*");
			String relation;
			String colonne;
			Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
			Matcher m = columnP.matcher(col[0]);
//			System.out.println("colonne = "+col[0]);
			if((columnP.matcher(col[0]).matches()))
			{
				String[] spl = col[0].split("\\.");
				String rel = spl[0];
				if(relationAliasMap.containsValue(rel))
				{
					for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
					    if(entry.getValue().compareToIgnoreCase(rel)==0)
					    	rel=entry.getKey();
					}
				}
				colonne = spl[1];
				relation=(isColumn(colonne, relations, rel)).getNom();
			}
			else
			{
				colonne = col[0];
				relation = (isColumn(col[0], relations)).getNom();
			}	
			Node n = null;
			if(w.getFilsGauche().getFilsGauche()!=null || w.getFilsGauche().getFilsDroit()!=null)//w.getFilsGauche() is not a leaf but an operator
				n = w.getFilsGauche();
			if(w.getPhOperator().compareToIgnoreCase("balayage")==0)
				return Estimate.Full_Table_Scan(relation, n);
			else
				return Estimate.Index_Scan(relation, colonne, n);
				
		}
		else if(w.getNom().compareToIgnoreCase("\u03C0")==0)//projection
		{
			Node n = null;
			int i=0;
			if(w.getFilsGauche().getFilsGauche()!=null || w.getFilsGauche().getFilsDroit()!=null)//w.getFilsGauche() is not a leaf but an operator
				n = w.getFilsGauche();
			String[] col =(w.getCondition().replaceAll("[()]", "")).split(",");
			String co;
			String rel;

			double[] sum= new double[col.length];
			if(w.getPhOperator().compareToIgnoreCase("balayage")==0)
			{
				for(String c :col)
				{
					Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
					if((columnP.matcher(c).matches()))
					{
						String[] spl = c.split("\\.");
						rel = spl[0];
						co = spl[1];
					}
					else
					{
						Node r = isColumn(c, relations);
						rel=r.getNom();
						co=c;
					}
					sum[i++]=Estimate.Full_Table_Scan(rel, n);
				}
			}
			else//index
			{
				i=0;
				for(String c :col)
				{
					Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
					if((columnP.matcher(c).matches()))
					{
						String[] spl = c.split("\\.");
						rel = spl[0];
						co = spl[1];
					}
					else
					{
						Node r = isColumn(c, relations);
						rel=r.getNom();
						co=c;
					}
					if(getInfoCol(rel,co)==true)
					{
						sum[i++]=Estimate.Index_Scan(rel, co, n);
					}
					else
					{
						sum[i++]=Estimate.Full_Table_Scan(rel, n);
					}
				}
			}
			double max = -1;
			for(double m:sum)
				max=Math.max(max, m);
			return max;
		}
		else if(w.getNom().compareToIgnoreCase("\u22C8")==0)//joiture
		{
			String[] eq = w.getCondition().replaceAll("[()]", "").split("\\s*=\\s*");
			for(String st:eq)
				st=st.trim();
			String[] tab1 = eq[0].split("\\.");
			String[] tab2 = eq[1].split("\\.");
			Node n1 = null,n2=null;
			if(w.getFilsGauche().getFilsGauche()!=null || w.getFilsGauche().getFilsDroit()!=null)//w.getFilsGauche() is not a leaf but an operator
				n1 = w.getFilsGauche();
			if(w.getFilsDroit().getFilsGauche()!=null || w.getFilsDroit().getFilsDroit()!=null)//w.getFilsDroit() is not a leaf but an operator
				n2 = w.getFilsDroit();
			if(w.getPhOperator().compareToIgnoreCase("BIB")==0)
				return Estimate.BIB(tab1[0], n1, tab2[0], n2);
			else if(w.getPhOperator().compareToIgnoreCase("BII")==0)
				return Estimate.BII(tab1[0], tab1[1], n1, tab2[0], tab2[1], n2);
			else if(w.getPhOperator().compareToIgnoreCase("JTF")==0)
				return Estimate.JTF(tab1[0], n1, tab2[0], n2);
				
		}
		return 0;
	}
	public static String[] getColumnRelationSelection(Node w)
	{
		String[] col = w.getCondition().replaceAll("[()]", "").split("\\s*("+String.join("|", relational_operators)+"|LIKE|BETWEEN|IS NULL|IN)\\s*");
		String relation;
		String colonne;
		Pattern columnP = Pattern.compile("\\s*\\w+\\.\\w+\\s*");
		Matcher m = columnP.matcher(col[0]);
		if((columnP.matcher(col[0]).matches()))
		{
			String[] spl = col[0].split("\\.");
			String rel = spl[0];
			if(relationAliasMap.containsValue(rel))
			{
				for (Map.Entry<String, String> entry : relationAliasMap.entrySet()) {
				    if(entry.getValue().compareToIgnoreCase(rel)==0)
				    	rel=entry.getKey();
				}
			}
			colonne = spl[1];
			relation=(isColumn(colonne, relations, rel)).getNom();
		}
		else
		{
			colonne = col[0];
			relation = (isColumn(col[0], relations)).getNom();
		}
		String[] ret = new String[2];
		ret[0]=relation;
		ret[1]=colonne;
		return ret;
	}
	
}







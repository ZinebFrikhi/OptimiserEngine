package Optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Transformateur.Node;
import Transformateur.Tree;

public class Optimate {
	
	public static Map<List<String>,String> explain = new HashMap<List<String>,String>();
	
	
	
	public static Map GetOptimalTree()
	{
		Map<List<String>,String> explainMaterialisation = new HashMap<List<String>,String>();
		Map<Node,Node> Logical_PhysicalOp = new HashMap<Node,Node>();
		int j=1;
		//optimal Materialisation
		for (Map.Entry<Node, Map<Node,Double>> entry : Tree.NodesCost.entrySet())
		{
			Map<Node,Double> map = entry.getValue();
			Logical_PhysicalOp.put(entry.getKey(), GetOptimalPhysicalTree(map));
		}
		double minValueMaterialisation = Double.MAX_VALUE;
		Node optimalMaterialisation = null;
		j=1;
		int min=-1;
		for (Map.Entry<Node, Node> en : Logical_PhysicalOp.entrySet())
		{
			double opCost = Tree.getCost(en.getValue());
			if (opCost < minValueMaterialisation) {
				explainMaterialisation.clear();
				ExplainFill(en.getValue(),explainMaterialisation);
		        minValueMaterialisation = opCost;
		        optimalMaterialisation=en.getKey();
		        min=j;
		    }
			j++;
		}
		//optimal Pipeline
		Map<List<String>,String> explainPipeline = new HashMap<List<String>,String>();
		Logical_PhysicalOp = new HashMap<Node,Node>();
		for (Map.Entry<Node, Map<Node,Double>> entry : Tree.NodesCostPipeline.entrySet())
		{
			Map<Node,Double> map2 = entry.getValue();
			Logical_PhysicalOp.put(entry.getKey(), GetOptimalPhysicalTree(map2));
		}
		double minValuePipeline = Double.MAX_VALUE;
		Node optimalPipeline = null;
		j=1;
		min=-1;
		for (Map.Entry<Node, Node> en : Logical_PhysicalOp.entrySet())
		{
			double opCost = Tree.getCostPipeLineFinal(en.getValue());
			if (opCost <= minValuePipeline) {
				explainPipeline.clear();
				ExplainFill(en.getValue(),explainPipeline);
		        minValuePipeline = opCost;
		        optimalPipeline=en.getKey();
		        min=j;
		    }
			j++;
		}
		Map<Integer,Node> OptimalTree = new HashMap<Integer,Node>();
		if(minValuePipeline<minValueMaterialisation)
		{
			explain=explainPipeline;
			OptimalTree.put(1, optimalPipeline);
		}
		else
		{
			explain=explainMaterialisation;
			OptimalTree.put(0, optimalMaterialisation);
		}
		return OptimalTree;
	}
	public static Node GetOptimalPhysicalTree(Map<Node,Double> m)
	{
//		System.out.println("***************");
		
		int j=1;
		int min=-1;
		double minValue = Double.MAX_VALUE;
		Node phOp = null;
		for (Map.Entry<Node, Double> en : m.entrySet())
		{
//			System.out.println("cout ="+en.getValue());
			if (en.getValue() < minValue) {
//				System.out.println("optimal oui "+en.getValue()+"  <  "+minValue);
		        minValue = en.getValue();
		        phOp=en.getKey();
		        min=j;
		    }
//			else
//				System.out.println("no");
			j++;
		}
		return phOp;
	}
	public static String getWhereClause(Node n,int f)
	{
		String where="";
		if(n==null)
			return where;
		if(n.getNom().compareToIgnoreCase("\u03C3")==0 || n.getNom().compareToIgnoreCase("\u22C8")==0)//selection || jointure
		{
			if(f!=0)
				if(where.trim().endsWith("AND")==false)
					if(where.trim().endsWith("OR")==false)
					where+=" AND ";
			where = where+n.getCondition().replaceAll("[()]", "");

			where = where + getWhereClause(n.getFilsGauche(),1);
			where = where + getWhereClause(n.getFilsDroit(),1);
		}
		else if(n.getNom().compareToIgnoreCase("\u222A")==0)
		{
			where = where + getWhereClause(n.getFilsGauche(),0);
			where=where+" OR ";
			where = where + getWhereClause(n.getFilsDroit(),0);
		}
		return where;
	}
	public static List<Node> getLeafNodes(Node node) {
	    List<Node> leafNodes = new ArrayList<>();
	    if (node == null) {
	        return leafNodes;
	    }
	    if (node.getFilsGauche() == null && node.getFilsDroit() == null) {
	        leafNodes.add(node);
	    } else {
	        leafNodes.addAll(getLeafNodes(node.getFilsGauche()));
	        leafNodes.addAll(getLeafNodes(node.getFilsDroit()));
	    }
	    return leafNodes;
	}
	public static String getOptimalQuery(Node n)
	{
		String select,from=" FROM ",where=" WHERE ";
		if(n.getNom().compareToIgnoreCase("\u03C0")==0)
		{
			select ="SELECT "+n.getCondition().replaceAll("[()]", "")+" ";
				where = where + getWhereClause(n.getFilsGauche(),0);
		}
		else
		{
			select ="SELECT * ";
				where = where+  getWhereClause(n,0);
		}
		List<Node> leafs = getLeafNodes(n);
		int i=0;
		for(Node l:leafs)
		{
			if(i!=0)
			{
				from=from + " , ";
			}
			from+=l.getNom();
			i=1;
		}
		if(where.compareToIgnoreCase(" WHERE ")==0)
			return ""+select+"\n"+from;
		else
			return ""+select+"\n"+from+"\n"+where;
	}

	
	public static void ExplainFill(Node w,Map<List<String>,String> explain)
	{
		if(w==null)
			return;
		List<String> l = new ArrayList<String>();
		if(w.getNom().compareToIgnoreCase("\u03C3")==0)//selection
		{
			l.add("Sélection");
			l.add(""+Estimateur.Estimate.getNt(w.getFilsGauche()));
			explain.put(l, w.getPhOperator());
		}
		else if(w.getNom().compareToIgnoreCase("\u03C0")==0)//projection
		{
			l.add("Projection");
			l.add(""+Estimateur.Estimate.getNt(w.getFilsGauche()));
			explain.put(l, w.getPhOperator());
		}
		else if(w.getNom().compareToIgnoreCase("\u22C8")==0)//jointure
		{
//			System.out.println(Estimateur.Estimate.getNt(w.getFilsGauche()));
//			System.out.println(Estimateur.Estimate.getNt(w.getFilsDroit()));
			l.add("Jointure");
			l.add(""+Estimateur.Estimate.getNt(w.getFilsGauche()));
			l.add(""+Estimateur.Estimate.getNt(w.getFilsDroit()));
			explain.put(l, w.getPhOperator());
		}
		ExplainFill(w.getFilsGauche(),explain);
		ExplainFill(w.getFilsDroit(),explain);			
	}
}

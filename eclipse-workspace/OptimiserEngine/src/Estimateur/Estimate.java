package Estimateur;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

import Transformateur.Node;
import Transformateur.Tree;

public class Estimate {
	
	private final static double TempsTrans=0.1;
	private final static double TempsPosDébut=1.0;
	private final static double TempsESBloc=TempsTrans+TempsPosDébut;
	private static int FB=20;
	private static int FBM=20;
	private static int ordreMoyen=66;
	
	 public static int getNbRows(String relation)
     {
    	 for (String r: Tree.CataStatisRelations.keySet())
	 	 {
	 		if(r.compareToIgnoreCase(relation)==0)
	 		{
	 			return Integer.parseInt((Arrays.asList(Tree.CataStatisRelations.get(r))).get(0));
	 		}
	 	 }
		return -1;
     }
	 public static int getCardinality(String relation,String colonne)
     {
    	 for (String r: Tree.CataRelations.keySet())
	 	 {
	 		if(r.compareToIgnoreCase(relation)==0)
	 		{
	 			for(String[] st : Arrays.asList(Tree.CataRelations.get(r)))
	 			{
	 				if(st[0].compareToIgnoreCase(colonne.toUpperCase())==0)
	 				{
	 					return Integer.parseInt(st[4]);
	 				}	
	 			}
	 		}
	 	 }
		return -1;
	 }
     public static boolean isUnique(String relation , String colonne)
	 {
    	 for (String r: Tree.CataRelations.keySet())
	 	 {
	 		if(r.compareToIgnoreCase(relation)==0)
	 		{
	 			for(String[] st : Arrays.asList(Tree.CataRelations.get(r)))
	 			{
	 				if(st[0].compareToIgnoreCase(colonne.toUpperCase())==0)
	 				{
	 					if(st[3].compareToIgnoreCase("1")==0)
	 						return true;
	 					else
	 						return false;
	 				}	
	 			}
	 		}
	 	 }
    	 return false;
	 }
     public static boolean isPrimaryKey(String relation , String colonne)
     {
    	 for (String r: Tree.CataRelations.keySet())
    	 {
    		 if(r.compareToIgnoreCase(relation)==0)
    		 {
    			 for(String[] st : Arrays.asList(Tree.CataRelations.get(r)))
    			 {
    				 if(st[0].compareToIgnoreCase(colonne.toUpperCase())==0)
    				 {
    					 if(st[2].compareToIgnoreCase("1")==0)
    						 return true;
    					 else
    						 return false;
    				 }	
    			 }
    		 }
    	 }
    	 return false;
     }
	//selection
	public static double facteurDeSelectivite(String relation,String colonne)
	{
		int cardt=getCardinality(relation,colonne);
		return ((double)1/cardt);
	}
	public static double Selectivitet(String relation,String colonne)
	{
		int Nt = getNbRows(relation);
		return ((double)facteurDeSelectivite(relation,colonne)*Nt);
	}
	//faux prendre en consideration les autre cas de selection < > != 
	public static int getNt(Node n)
	{
		if(n.getNom().compareToIgnoreCase("\u03C3")==0)//selection
		{
			String[] tab = Tree.getColumnRelationSelection(n);
			return (int) Selectivitet(tab[0],tab[1]);
		}
		else if(n.getNom().compareToIgnoreCase("\u22C8")==0)//joiture
		{
			String[] eq = n.getCondition().replaceAll("[()]", "").split("\\s*=\\s*");
			for(String st:eq)
				st=st.trim();
			String[] tab1 = eq[0].split("\\.");
			String[] tab2 = eq[1].split("\\.");
			if(Transformateur.Tree.relationAliasMap.containsValue(tab1[0]))
			{
				for (Map.Entry<String, String> entry : Transformateur.Tree.relationAliasMap.entrySet()) {
				    if(entry.getValue().compareToIgnoreCase(tab1[0])==0)
				    	tab1[0]=entry.getKey();
				}
			}
			if(Transformateur.Tree.relationAliasMap.containsValue(tab2[0]))
			{
				for (Map.Entry<String, String> entry : Transformateur.Tree.relationAliasMap.entrySet()) {
				    if(entry.getValue().compareToIgnoreCase(tab2[0])==0)
				    	tab2[0]=entry.getKey();
				}
			}
			double selectivity1 = Selectivitet(tab1[0],tab1[1]);
			double selectivity2 = Selectivitet(tab2[0],tab2[1]);
			int Nt1 = getNbRows(tab1[0]);
			int Nt2 = getNbRows(tab2[0]);
			return (int) ((double)(Math.min(Nt1, Nt2)*(selectivity1*selectivity2)));
		}
		else if(n.getNom().compareToIgnoreCase("\u03C1")==0)//rename
		{
			return getNbRows(n.getFilsGauche().getNom());
		}
		else if(n.getNom().compareToIgnoreCase("\u2A2F")==0)//produit cartesian
		{
			int ntG=0,ntD=0;
			if(n.getFilsGauche().getFilsGauche()==null && n.getFilsGauche().getFilsDroit()==null)
				ntG=getNbRows(n.getFilsGauche().getNom());
			else
				ntG=getNt(n.getFilsGauche());
			if(n.getFilsDroit().getFilsGauche()==null && n.getFilsDroit().getFilsDroit()==null)
				ntD=getNbRows(n.getFilsDroit().getNom());
			else
				ntD=getNt(n.getFilsDroit());
			return (ntG*ntD);
		}
		return getNbRows(n.getNom());
	}

	//balayage
	public static double Full_Table_Scan(String relation,Node n)
	{
		int Nt;
		if(n==null)
			Nt = getNbRows(relation);
		else
			Nt = getNt(n);
		int Bt = (int) Math.ceil((double)Nt/FB);
		if(Bt==1)
			return (double) Math.ceil(Bt*0.1);
		else
			return (double) (Bt*1.1);
	}
	//index primaire
	public static double primary_Index(String relation , String colonne,Node n)
	{
		int Nt;
		if(n==null)
			Nt=getNbRows(relation);
		else
			Nt = getNt(n);
		
		int hauteur = (int) Math.ceil((double)Math.log(Nt)/Math.log(ordreMoyen));
		if(isUnique(relation,colonne)==true)
		{
			return ((double)(hauteur*TempsESBloc));
		}
		else
		{
			double selt = Selectivitet(relation,colonne);
			return ((double)((hauteur-1+Math.ceil((double)selt/FB))*TempsESBloc));
		}
	}
	//index sexondaire
	public static double secondary_Index(String relation , String colonne,Node n)
	{
		int Nt;
		if(n==null)
			Nt = getNbRows(relation);
		else
			Nt = getNt(n);
		int hauteur = (int) Math.ceil(Math.log(Nt)/Math.log(ordreMoyen));
		if(isUnique(relation,colonne)==true)
		{
			return ((double)((hauteur+1)*TempsESBloc));
		}
		else
		{
			double selt = Selectivitet(relation,colonne);
			return ((double)((hauteur-1+selt+Math.ceil((double)selt/ordreMoyen))*TempsESBloc));
		}
	}
	public static double Index_Scan(String relation , String colonne,Node n)
	{
		if(isPrimaryKey(relation,colonne)==true)
			return primary_Index(relation,colonne,n);
		else
			return secondary_Index(relation,colonne,n);
	}
	
	//TRI
	public static double TRI(String relation,Node n)
	{
		int M=50;//le nombre de blocs en mï¿½moire centrale
		int Nt;
		if(n==null)
			Nt = getNbRows(relation);
		else
			Nt = getNt(n);
		int Bt = (int) Math.ceil((double)Nt/FB);
		double x1 = (2*((Bt/M)*TempsPosDébut+Bt*TempsTrans));
		double x2;
		if(((double)Bt/M)<1)
			x2=0;
		else
			x2 = ((double)(Bt *(2*(double)(Math.log((double)Bt/M)/Math.log(2.0)) -1 )* TempsESBloc));
		
		return ((double)(x1+x2));
	}
	
	//joiture
	public static double BIB(String relation1,Node n1,String relation2,Node n2)
	{
		int Nt1;
		if(n1==null)
			Nt1 = getNbRows(relation1);
		else
			Nt1 = getNt(n1);
		int Bt1 = (int) Math.ceil((double)Nt1/FB);
		
		int Nt2;
		if(n2==null)
			Nt2 = getNbRows(relation2);
		else
			Nt2 = getNt(n2);
		
		int Bt2 = (int) Math.ceil((double)Nt2/FB);
		return ((double)(Bt1*(TempsESBloc+Bt2*TempsTrans+TempsPosDébut)));
	}
	public static double BII(String relation1,String col1,Node n1,String relation2,String col2,Node n2)
	{
		int Nt1;//number of rows of relation 1
		if(n1==null)
			Nt1 = getNbRows(relation1);
		else
			Nt1 = getNt(n1);
		int Bt1 = (int) Math.ceil((double)Nt1/FB);
		int Nt2;//number of rows of relation 2
		if(n2==null)
			Nt2 = getNbRows(relation2);
		else
			Nt2 = getNt(n2);
		int Bt2 = (int) Math.ceil((double)Nt2/FB);
		return ((double)(Bt1*TempsESBloc + Nt1*Index_Scan(relation2,col2,n2))) ;
	}
	public static double JTF(String relation1,Node n1,String relation2,Node n2)
	{
		int Nt1;
		if(n1==null)
		{
			Nt1 = getNbRows(relation1);
		}
		else
		{
			Nt1 = getNt(n1);
			
		}
		int Bt1 = (int) Math.ceil((double)Nt1/FB);
		int Nt2;
		if(n2==null)
		{
			Nt2 = getNbRows(relation2);
		}
		else
		{
			Nt2 = getNt(n2);
		}
		
		int Bt2 = (int) Math.ceil((double)Nt2/FB);
		return ((double)(TRI(relation1,n1)+TRI(relation2,n2)+(2*(Bt1+Bt2)*TempsESBloc)));
	}

	//produit cartesian
	public static double cartisianProduct(String relation1,Node n1,String relation2,Node n2)
	{
		int Nt1;
		if(n1==null)
			Nt1 = getNbRows(relation1);
		else
			Nt1 = getNt(n1);
		int Bt1 = (int) Math.ceil((double)Nt1/FB);
		
		int Nt2;
		if(n2==null)
			Nt2 = getNbRows(relation2);
		else
			Nt2 = getNt(n2);
		int Bt2 = (int) Math.ceil((double)Nt2/FB);
		return ((double)(Bt1*Bt2));
	}
}


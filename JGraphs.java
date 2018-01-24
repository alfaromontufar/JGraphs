import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import java.lang.System;
import java.lang.Runtime;
import java.io.IOException;


public class JGraphs extends JPanel
    implements ActionListener, MouseListener, MouseMotionListener {
    // Variables Globales
    private java.util.List<Nodo> nodos;
    private int cx, cy;
    private int arrowH = 5, arrowW = 15; // altura y anchura de la flecha
    private final int nz=6; //node size
    
    // Declaración de Menu
    private JMenuBar barraMenu = new JMenuBar();
    private JMenu menuMenu = new JMenu("Menu");
    private JMenu menuGraphType = new JMenu("Graph type");
    private JMenuItem itemGraph = new JMenuItem("Graph");
    private JMenuItem itemDigraph = new JMenuItem("Digraph");
    private JMenu menuExportar = new JMenu("Export to");
    private JMenuItem itemLaTex = new JMenuItem("To LaTeX");
    private JMenuItem itemPython = new JMenuItem("To Python");
    private JMenu menuCalcular = new JMenu("Compute");
    private JMenuItem itemCriticalIdeals = new JMenuItem("Critical Ideals");
    private JMenuItem itemCriticalIdealsText = new JMenuItem("Critical Ideals (only text)");
    private JMenuItem itemCriticalGroup = new JMenuItem("Critical group");
    private JMenuItem itemForbDigraph = new JMenuItem("Forbidden Digraph");
    private JMenuItem itemForbDigraphText = new JMenuItem("Forbidden Digraph (only text)");
    
    // Declaración de botones
    private JButton buttonV = new JButton("Vertex");
    private JButton buttonE = new JButton("Edge");
    private JButton buttonM = new JButton("Move");
    private JButton buttonC = new JButton("Clear");
    private JButton buttonD = new JButton("Delete");
    
    // Declaración de texto
    private JTextField text = new JTextField("ProjectName",20);
    
    private int mainflag=0, colorvflag=-1, binvflag=0, nodeflag;
    private int graphtype = 0; // 0 -> Graph, 1 -> Digraph
    
    public JGraphs ( ) {
        barraMenu.add(menuMenu);
        menuMenu.add(menuGraphType);
        menuGraphType.add(itemGraph);
        itemGraph.addActionListener(this);
        itemGraph.setArmed(false);
        menuGraphType.add(itemDigraph);
        itemDigraph.addActionListener(this);
        itemDigraph.setArmed(true);
        menuMenu.add(menuExportar);
        menuExportar.add(itemLaTex);
        itemLaTex.addActionListener(this);
        menuExportar.add(itemPython);
        itemPython.addActionListener(this);
        menuMenu.add(menuCalcular);
        menuCalcular.add(itemCriticalIdeals);
        itemCriticalIdeals.addActionListener(this);
	menuCalcular.add(itemCriticalIdealsText);
	itemCriticalIdealsText.addActionListener(this);
	menuCalcular.add(itemCriticalGroup);
        itemCriticalGroup.addActionListener(this);
        menuCalcular.add(itemForbDigraph);
        itemForbDigraph.addActionListener(this);
	menuCalcular.add(itemForbDigraphText);
        itemForbDigraphText.addActionListener(this);
        add(barraMenu);
        
        buttonV.addActionListener(this);
        buttonE.addActionListener(this);
        buttonM.addActionListener(this);
        buttonC.addActionListener(this);
        buttonD.addActionListener(this);
        add(buttonV);
        add(buttonE);
        add(buttonM);
        add(buttonC);
        add(buttonD);
        add(text);
        text.setEditable(true);
        nodos = new LinkedList<Nodo>();
        addMouseListener (this);
        addMouseMotionListener (this);
        text.addActionListener(this);
    }
    
    public static int countOccurrencesOf(String haystack, char needle){
        int count = 0;
        for (int i=0; i < haystack.length(); i++)
	    {
		if (haystack.charAt(i) == needle)
		    {
			count++;
		    }
	    }
        return count;
    }
    
    public static void printmatrix(int [][] matriz){
        int n = matriz.length;
        int m = matriz[0].length;
        for(int i=0; i<n; i++)
            for(int j=0; j<m; j++)
                System.out.print( matriz[i][j] + (j==m-1? "\n" : " " ) );
    }
    
        public static int[][] string2matrix(String cadena){
            int ini = 1;
            int fin = cadena.indexOf('}',ini);
            
            String subcad = cadena.substring(cadena.indexOf('{',1)+1,fin);
            int n = countOccurrencesOf(cadena,'{') - 1; // filas
            int m = countOccurrencesOf(subcad,',') + 1; // columnas
            int[][] matriz = new int [n][m];
            
            for(int j=0; j<n; j++){
                int in = 0;
                int fi;
                for(int i=0; i<m; i++){
                    if(i== m-1)
                        fi = subcad.length();
                    else
                        fi = subcad.indexOf(',',in);
                    matriz[j][i] = Integer.parseInt(subcad.substring(in,fi).replaceAll(" ",""));
                    in = fi+1;
                }
                if(j<n-1){
                    ini = cadena.indexOf('{',fin);
                    fin = cadena.indexOf('}',ini);
                    subcad = cadena.substring(ini+1,fin);
                }
            }
            return matriz;
        }
        
    public static int[][] string2matrix2(String cadena){
        int ini = 1;
        int fin = cadena.indexOf(']',ini);
        
        String subcad = cadena.substring(cadena.indexOf('[',1)+1,fin);
        int n = countOccurrencesOf(cadena,'[') - 1; // filas
        int m = countOccurrencesOf(subcad,',') + 1; // columnas
        int[][] matriz = new int [n][m];
        
        for(int j=0; j<n; j++){
            int in = 0;
            int fi;
            for(int i=0; i<m; i++){
                if(i== m-1)
                    fi = subcad.length();
                else
                    fi = subcad.indexOf(',',in);
                matriz[j][i] = Integer.parseInt(subcad.substring(in,fi).replaceAll(" ",""));
                in = fi+1;
            }
            if(j<n-1){
                ini = cadena.indexOf('[',fin);
                fin = cadena.indexOf(']',ini);
                subcad = cadena.substring(ini+1,fin);
            }
        }
        return matriz;
    }
    
    public int[][] graph2Laplacian(){
        
        int deleted = 0;
        int[] dvector = new int [nodos.size()];
        
        for(int i=0; i<nodos.size(); i++){
            if(nodos.get(i).d == true)
                deleted++;
            dvector[i] = deleted;
        }
        
        int nnodos = nodos.size()-deleted;
        int[][] matrix = new int [nnodos][nnodos];
        
        for(int i=0; i<nodos.size(); i++)
            if(!nodos.get(i).d){
                int aux1 = i - dvector[i];
                
                for(int j=0; j<nodos.get(i).list.size(); j++)
                    if(!nodos.get(nodos.get(i).list.get(j)).d){
                        int aux2 = nodos.get(i).list.get(j) - dvector[nodos.get(i).list.get(j)];
                        matrix[aux1][aux2]=-1;
                        
                        // This is in the non-directed graph case
                        if(graphtype == 0)
                            matrix[aux2][aux1]=-1;
                    }
            }
        
        for(int i=0; i<nnodos; i++){
            int suma=0;
            for(int j=0; j<nnodos; j++)
                if(i!=j)
                    suma+=matrix[i][j];
            matrix[i][i] = (-suma);
        }
        
        return matrix;
    }
    
    public String vars2string(){
        
        String graph = "";
        
        int deleted = 0;
        
        for(int i=0; i<nodos.size(); i++)
            if(nodos.get(i).d == true)
                deleted++;
        
        int nnodos = nodos.size() - deleted;
        
        for(int i=0; i<nnodos; i++)
            graph += ( i == 0? "x"  : ",x" ) + i;
        
        return graph;
        
    }
    
    public String graph2string(){
        
        String graph = "{";
        
        int deleted = 0;
        int[] dvector = new int [nodos.size()];
        
        for(int i=0; i<nodos.size(); i++){
            if(nodos.get(i).d == true)
                deleted++;
            dvector[i] = deleted;
        }
        
        int nnodos = nodos.size()-deleted;
        boolean firstused = true;
        int[][] matrix = graph2Laplacian();
        
        for(int i=0; i<nnodos; i++){
            int flag = 0;
            String graph1 = (firstused? "" : ",") + Integer.toString(i) + ":[";
            
            if(graphtype == 0){
                for(int j=i+1; j<nnodos; j++)
                    if(matrix[i][j] != 0 ){
                        graph1 += (flag == 0? "" : ",") + Integer.toString(j);
                        if(flag == 0)
                            flag = 1;
                    }
            }else
                for(int j=0; j<nnodos; j++)
                    if( i != j && matrix[i][j] != 0 ){
                        graph1 += (flag == 0? "" : ",") + Integer.toString(j);
                        if(flag == 0)
                            flag = 1;
                    }
            
            graph1 += "]";
            if(flag != 0){
                graph += graph1;
                firstused = false;
            }
        }
        
        graph += "}";
        
        return graph;
        
    }
    
    public void toPython(){
        String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".py";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
        
        try{
            file = new FileWriter(archivo);
            fout = new PrintWriter(file);
            fout.println("import networkx as nx");
            fout.println("d = " + graph2string());
            fout.println("G = nx.Graph(d)");
            fout.println("nx.write_gml(G,\"" + cadena + "gml\")");
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        
    }
    
    
    public void toLaplacian(){
        String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".tex";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
        
        try{
            file = new FileWriter(archivo);
            fout = new PrintWriter(file);
            
            
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
    
    public void toLaTeX (){
        String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".tex";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
        try{
            file = new FileWriter(archivo);
            fout = new PrintWriter(file);
            fout.println("\\documentclass[11pt,twoside]{amsart}");
            fout.println( "\\usepackage{tikz}");
            fout.println("\\begin{document} ");
            fout.println("	\\begin{center}");
            fout.println("		\\begin{tikzpicture}[scale=2,thick]");
            fout.println("		\\tikzstyle{every node}=[minimum width=0pt, inner sep=2pt, circle]");
            
            int w=getSize ( ).width;
            int h=getSize ( ).height;
            cx = w/2;
            cy = h/2;
            
            int deleted = 0;
            int[] dvector = new int [nodos.size()];
            
            for(int i=0; i<nodos.size(); i++){
                if(nodos.get(i).d == true)
                    deleted++;
                dvector[i] = deleted;
            }
            
            for(int i=0; i<nodos.size(); i++){
                if(nodos.get(i).d == false)
                    fout.println("			\\draw (" + (float)  (nodos.get(i).x - cx)/100 + "," + (float) (cy - nodos.get(i).y)/100 + ") node[draw] (" + i + ") { \\tiny " + (i - dvector[i]) + "};");
            }
            
            for( int i=0; i<nodos.size(); i++ )
                for( int j=0; j<nodos.get(i).list.size(); j++ )
                    if(nodos.get(i).d == false)
                        if(nodos.get(nodos.get(i).list.get(j)).d == false)
                            if(graphtype == 1)
                                fout.println("			\\draw  (" + i + ") edge[->] (" + nodos.get(i).list.get(j) + ");");
                            else
                                if(nodos.get(i).list.get(j) > i)
                                    fout.println("			\\draw  (" + i + ") edge (" + nodos.get(i).list.get(j) + ");");
            
            fout.println("		\\end{tikzpicture}");
            fout.println("	\\end{center}");
            fout.println("\\end{document}");
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void computeForbMZ (){
        String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".sage";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
        try{
            file = new FileWriter(archivo);
            fout = new PrintWriter(file);
            fout.println("# Forbidden graphs form MZ <= 3");

	    fout.println("min_forbid5=[Graph('DBg')]");
	    fout.println("min_forbid6=[Graph('E?NG'),");
	    fout.println(" Graph('E@JW'),");
	    fout.println(" Graph('E?NW'),");
	    fout.println(" Graph('E?Nw'),");
	    fout.println(" Graph('E?]w'),");
	    fout.println(" Graph('E@`w'),");
	    fout.println(" Graph('E@YW'),");
	    fout.println(" Graph('E@UW'),");
	    fout.println(" Graph('EGNW'),");
	    fout.println(" Graph('EAMw'),");
	    fout.println(" Graph('E@Vw'),");
	    fout.println(" Graph('ECXw'),");
	    fout.println(" Graph('E@^W'),");
	    fout.println(" Graph('EJeg'),");
	    fout.println(" Graph('EK]w')]");
	    fout.println("min_forbid7=[Graph('F?Cfw'),");
	    fout.println(" Graph('F?D~o'),");
	    fout.println(" Graph('F?D~w')]");

            fout.println("");
            fout.println( "D = " + (graphtype == 0? "Graph(" : "DiGraph(") + graph2string() + ")" );
            fout.println("");
            fout.println("n = len(D)");
            //fout.println("R = PolynomialRing(ZZ,['x%s'%p for p in range(n)]);");
            fout.println("R = macaulay2.ring(\"ZZ\",\"[" + vars2string() + "]\").to_sage();");
            fout.println("R.inject_variables();");
            fout.println("Laplacian = diagonal_matrix(list(R.gens())) - D.adjacency_matrix()");
            fout.println("file = open('" + cadena + ".txt', 'w')");
            fout.println("");
            fout.println("def IsTrivial(L):");
            fout.println("	subLaplacian = Laplacian[L,L]");
            fout.println("	I = R.ideal(subLaplacian.minors(2))");
            fout.println("	return I.is_one()");
            fout.println("");
            fout.println("def ThreeVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		for F in F3:");
            fout.println("			if(subD.is_isomorphic(F)):");
            fout.println("				file.write('Forbidden graph since contains ' + F.name() + ', with indices ' + str(L) + '\\n')");
            fout.println("				F.show()");
            fout.println("				return True");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def FourVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		for F in F4:");
            fout.println("			if(subD.is_isomorphic(F)):");
            fout.println("				file.write('Forbidden graph since contains ' + F.name() + ', with indices ' + str(L) + '\\n')");
            fout.println("				F.show()");
            fout.println("				return True");
            fout.println("		for l in L:");
            fout.println("			L1 = list(L)");
            fout.println("			L1.remove(l)");
            fout.println("			if(ThreeVertices(L1)):");
            fout.println("				return True");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def MoreThanFourVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		if(len(L) == 5):");
            fout.println("			for l in L:");
            fout.println("				L1 = list(L)");
            fout.println("				L1.remove(l)");
            fout.println("				if(FourVertices(L1)):");
            fout.println("					return True");
            fout.println("		else:");
            fout.println("			for l in L:");
            fout.println("				L1 = list(L)");
            fout.println("				L1.remove(l)");
            fout.println("				if(MoreThanFourVertices(L1)):");
            fout.println("					return True");
            fout.println("		file.write('Something goes wrong! gamma > 1 but no graph in Forb is induced subgraph of D.\\n')");
            fout.println("		if(D.subgraph(L).is_connected()):");
            fout.println("			file.write('Vertices ' + str(L) + ' induce a connected graph\\n')");
            fout.println("		else:");
            fout.println("			file.write('Vertices ' + str(L) + ' induce a disconnected graph\\n')");
            fout.println("		return False");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def Forb():");
            fout.println("	if(n == 3):");
            fout.println("		if(ThreeVertices(range(n)) == False):");
            fout.println("			file.write('The graph has gamma < 1.\\n')");
            fout.println("	elif(n == 4):");
            fout.println("		if(FourVertices(range(n)) == False):");
            fout.println("			file.write('The graph has gamma < 1.\\n')");
            fout.println("	elif(n > 4):");
            fout.println("		if(MoreThanFourVertices(range(n)) == False):");
            fout.println("			file.write('The graph has gamma < 1.\\n')");
            fout.println("	else:");
            fout.println("		file.write('The graph has gamma < 1, because it is too small.\\n')");
            fout.println("");
            fout.println("Forb()");
            fout.println("file.close()");
            fout.println("");
            fout.println("from subprocess import call");
            fout.println("call(['emacs','" + cadena + ".txt'])");
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    
    public void computeForbD (){
        String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".sage";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
        try{
            file = new FileWriter(archivo);
            fout = new PrintWriter(file);
            fout.println("# Forbidden digraphs with 3 vertices");
            fout.println("F3 = [];");
            fout.println("F3.append(DiGraph({0:[2],2:[1]}, name='F31'));");
            fout.println("F3.append(DiGraph({0:[2],1:[2],2:[0]}, name='F32'));");
            fout.println("F3.append(DiGraph({0:[2],2:[0,1]}, name='F33'));");
            fout.println("F3.append(DiGraph({0:[2],1:[2],2:[0,1]}, name='F34'));");
            fout.println("F3.append(DiGraph({0:[1],1:[2],2:[0]}, name='F35'));");
            fout.println("F3.append(DiGraph({0:[1,2],1:[0],2:[1]}, name='F36'));");
            fout.println("F3.append(DiGraph({0:[1,2],1:[0,2],2:[0]}, name='F37'));");
            fout.println("");
            fout.println("# Forbidden digraphs with 4 vertices");
            fout.println("F4 = [];");
            fout.println("F4.append(DiGraph({0:[2,3],1:[3]}, name='F41'));");
            fout.println("F4.append(DiGraph({0:[2,3],1:[3],2:[3]}, name='F42'));");
            fout.println("F4.append(DiGraph({0:[2],3:[0,1,2]}, name='F43'));");
            fout.println("F4.append(DiGraph({0:[2,3],1:[3],2:[0,3]}, name='F44'));");
            fout.println("F4.append(DiGraph({0:[2],1:[2],3:[0,1,2]}, name='F45'));");
            fout.println("F4.append(DiGraph({0:[2],2:[0],3:[0,1,2]}, name='F46'));");
            fout.println("F4.append(DiGraph({0:[1,2,3],1:[2,3],2:[3]}, name='F47'));");
            fout.println("F4.append(DiGraph({0:[1,2,3],1:[0,2,3],2:[3]}, name='F48'));");
            fout.println("F4.append(DiGraph({0:[1,2,3],1:[0,2,3],2:[3],3:[2]}, name='F49'));");
            fout.println("F4.append(DiGraph({0:[1],1:[0],2:[0,1,3],3:[0,1]}, name='F410'));");
            fout.println("");
            fout.println( "D = " + (graphtype == 0? "Graph(" : "DiGraph(") + graph2string() + ")" );
            fout.println("");
            fout.println("n = len(D)");
            //fout.println("R = PolynomialRing(ZZ,['x%s'%p for p in range(n)]);");
            fout.println("R = macaulay2.ring(\"ZZ\",\"[" + vars2string() + "]\").to_sage();");
            fout.println("R.inject_variables();");
            fout.println("Laplacian = diagonal_matrix(list(R.gens())) - D.adjacency_matrix()");
            fout.println("file = open('" + cadena + ".txt', 'w')");
            fout.println("");
            fout.println("def IsTrivial(L):");
            fout.println("	subLaplacian = Laplacian[L,L]");
            fout.println("	I = R.ideal(subLaplacian.minors(2))");
            fout.println("	return I.is_one()");
            fout.println("");
            fout.println("def ThreeVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		for F in F3:");
            fout.println("			if(subD.is_isomorphic(F)):");
            fout.println("				file.write('Forbidden graph since contains ' + F.name() + ', with indices ' + str(L) + '\\n')");
            fout.println("				F.show()");
            fout.println("				return True");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def FourVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		for F in F4:");
            fout.println("			if(subD.is_isomorphic(F)):");
            fout.println("				file.write('Forbidden graph since contains ' + F.name() + ', with indices ' + str(L) + '\\n')");
            fout.println("				F.show()");
            fout.println("				return True");
            fout.println("		for l in L:");
            fout.println("			L1 = list(L)");
            fout.println("			L1.remove(l)");
            fout.println("			if(ThreeVertices(L1)):");
            fout.println("				return True");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def MoreThanFourVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		if(len(L) == 5):");
            fout.println("			for l in L:");
            fout.println("				L1 = list(L)");
            fout.println("				L1.remove(l)");
            fout.println("				if(FourVertices(L1)):");
            fout.println("					return True");
            fout.println("		else:");
            fout.println("			for l in L:");
            fout.println("				L1 = list(L)");
            fout.println("				L1.remove(l)");
            fout.println("				if(MoreThanFourVertices(L1)):");
            fout.println("					return True");
            fout.println("		file.write('Something goes wrong! gamma > 1 but no graph in Forb is induced subgraph of D.\\n')");
            fout.println("		if(D.subgraph(L).is_connected()):");
            fout.println("			file.write('Vertices ' + str(L) + ' induce a connected graph\\n')");
            fout.println("		else:");
            fout.println("			file.write('Vertices ' + str(L) + ' induce a disconnected graph\\n')");
            fout.println("		return False");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def Forb():");
            fout.println("	if(n == 3):");
            fout.println("		if(ThreeVertices(range(n)) == False):");
            fout.println("			file.write('The graph has gamma < 1.\\n')");
            fout.println("	elif(n == 4):");
            fout.println("		if(FourVertices(range(n)) == False):");
            fout.println("			file.write('The graph has gamma < 1.\\n')");
            fout.println("	elif(n > 4):");
            fout.println("		if(MoreThanFourVertices(range(n)) == False):");
            fout.println("			file.write('The graph has gamma < 1.\\n')");
            fout.println("	else:");
            fout.println("		file.write('The graph has gamma < 1, because it is too small.\\n')");
            fout.println("");
            fout.println("Forb()");
            fout.println("file.close()");
            fout.println("");
            fout.println("from subprocess import call");
            fout.println("call(['emacs','" + cadena + ".txt'])");
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

	public void computeForbDText (){
        String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".sage";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
        try{
            file = new FileWriter(archivo);
            fout = new PrintWriter(file);
            fout.println("# Forbidden digraphs with 3 vertices");
            fout.println("F3 = [];");
            fout.println("F3.append(DiGraph({0:[2],2:[1]}, name='F31'));");
            fout.println("F3.append(DiGraph({0:[2],1:[2],2:[0]}, name='F32'));");
            fout.println("F3.append(DiGraph({0:[2],2:[0,1]}, name='F33'));");
            fout.println("F3.append(DiGraph({0:[2],1:[2],2:[0,1]}, name='F34'));");
            fout.println("F3.append(DiGraph({0:[1],1:[2],2:[0]}, name='F35'));");
            fout.println("F3.append(DiGraph({0:[1,2],1:[0],2:[1]}, name='F36'));");
            fout.println("F3.append(DiGraph({0:[1,2],1:[0,2],2:[0]}, name='F37'));");
            fout.println("");
            fout.println("# Forbidden digraphs with 4 vertices");
            fout.println("F4 = [];");
            fout.println("F4.append(DiGraph({0:[2,3],1:[3]}, name='F41'));");
            fout.println("F4.append(DiGraph({0:[2,3],1:[3],2:[3]}, name='F42'));");
            fout.println("F4.append(DiGraph({0:[2],3:[0,1,2]}, name='F43'));");
            fout.println("F4.append(DiGraph({0:[2,3],1:[3],2:[0,3]}, name='F44'));");
            fout.println("F4.append(DiGraph({0:[2],1:[2],3:[0,1,2]}, name='F45'));");
            fout.println("F4.append(DiGraph({0:[2],2:[0],3:[0,1,2]}, name='F46'));");
            fout.println("F4.append(DiGraph({0:[1,2,3],1:[2,3],2:[3]}, name='F47'));");
            fout.println("F4.append(DiGraph({0:[1,2,3],1:[0,2,3],2:[3]}, name='F48'));");
            fout.println("F4.append(DiGraph({0:[1,2,3],1:[0,2,3],2:[3],3:[2]}, name='F49'));");
            fout.println("F4.append(DiGraph({0:[1],1:[0],2:[0,1,3],3:[0,1]}, name='F410'));");
            fout.println("");
            fout.println( "D = " + (graphtype == 0? "Graph(" : "DiGraph(") + graph2string() + ")" );
            fout.println("");
            fout.println("n = len(D)");
            //fout.println("R = PolynomialRing(ZZ,['x%s'%p for p in range(n)]);");
            fout.println("R = macaulay2.ring(\"ZZ\",\"[" + vars2string() + "]\").to_sage();");
            fout.println("R.inject_variables();");
            fout.println("Laplacian = diagonal_matrix(list(R.gens())) - D.adjacency_matrix()");
            fout.println("");
            fout.println("def IsTrivial(L):");
            fout.println("	subLaplacian = Laplacian[L,L]");
            fout.println("	I = R.ideal(subLaplacian.minors(2))");
            fout.println("	return I.is_one()");
            fout.println("");
            fout.println("def ThreeVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		for F in F3:");
            fout.println("			if(subD.is_isomorphic(F)):");
            fout.println("				print('Forbidden graph since contains ' + F.name() + ', with indices ' + str(L) + '\\n')");
            fout.println("				F.show()");
            fout.println("				return True");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def FourVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		for F in F4:");
            fout.println("			if(subD.is_isomorphic(F)):");
            fout.println("				print('Forbidden graph since contains ' + F.name() + ', with indices ' + str(L) + '\\n')");
            fout.println("				F.show()");
            fout.println("				return True");
            fout.println("		for l in L:");
            fout.println("			L1 = list(L)");
            fout.println("			L1.remove(l)");
            fout.println("			if(ThreeVertices(L1)):");
            fout.println("				return True");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def MoreThanFourVertices(L):");
            fout.println("	subD = D.subgraph(L)");
            fout.println("	if(IsTrivial(L)):");
            fout.println("		if(len(L) == 5):");
            fout.println("			for l in L:");
            fout.println("				L1 = list(L)");
            fout.println("				L1.remove(l)");
            fout.println("				if(FourVertices(L1)):");
            fout.println("					return True");
            fout.println("		else:");
            fout.println("			for l in L:");
            fout.println("				L1 = list(L)");
            fout.println("				L1.remove(l)");
            fout.println("				if(MoreThanFourVertices(L1)):");
            fout.println("					return True");
            fout.println("		print('Something goes wrong! gamma > 1 but no graph in Forb is induced subgraph of D.\\n')");
            fout.println("		if(D.subgraph(L).is_connected()):");
            fout.println("			print('Vertices ' + str(L) + ' induce a connected graph\\n')");
            fout.println("		else:");
            fout.println("			print('Vertices ' + str(L) + ' induce a disconnected graph\\n')");
            fout.println("		return False");
            fout.println("	else:");
            fout.println("		return False");
            fout.println("");
            fout.println("def Forb():");
            fout.println("	if(n == 3):");
            fout.println("		if(ThreeVertices(range(n)) == False):");
            fout.println("			print('The graph has gamma < 1.\\n')");
            fout.println("	elif(n == 4):");
            fout.println("		if(FourVertices(range(n)) == False):");
            fout.println("			print('The graph has gamma < 1.\\n')");
            fout.println("	elif(n > 4):");
            fout.println("		if(MoreThanFourVertices(range(n)) == False):");
            fout.println("			print('The graph has gamma < 1.\\n')");
            fout.println("	else:");
            fout.println("		print('The graph has gamma < 1, because it is too small.\\n')");
            fout.println("");
            fout.println("Forb()");
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
	
    public void computeCriticalGroup(){
	String dirName = "files";
        File dir = new File (dirName);
        String cadena = text.getText() + ".sage";
        File archivo = new File(dir,cadena);
        FileWriter file = null;
        PrintWriter fout = null;
	try{
	    file = new FileWriter(archivo);
	    fout = new PrintWriter(file);
	    fout.println( "D = " + (graphtype == 0? "Graph(" : "DiGraph(") + graph2string() + ")" );
	    fout.println("");
	    fout.println("n = len(D)");
	    fout.println("");
	    fout.println("def SNF():");
	    fout.println("	L = - D.adjacency_matrix()");
	    fout.println("	for i in range(n):");
	    fout.println("		L[i,i] = 0");
	    fout.println("		for j in range(n):");
	    fout.println("			if(j != i):");
	    fout.println("				L[i,i] = L[i,i] - L[i,j]");
	    fout.println("");
	    fout.println("	list = []");
	    fout.println("	S,L1,R = L.smith_form()");
	    fout.println("	for i in range(n):");
	    fout.println("		if( S[i,i] != 0 ):");
	    fout.println("			list.append(S[i,i])");
	    fout.println("	return list ");
	    fout.println("");
	    fout.println("List = SNF()");
	    fout.println("");
	     fout.println("file = open('" + cadena + ".txt', 'w')");
            fout.println("");
	    fout.println("file.write('Smith Normal Form is ' + str(List) + '\\n')");
	    fout.println("file.write('f_1 is ' + str(List.count(1)) + '\\n')");
	    fout.println("");
	    fout.println("file.close()");
	    fout.println("");
	    fout.println("from subprocess import call");
	    fout.println("call(['emacs','" + cadena + ".txt'])");
	    fout.println("call(['open','" + cadena + ".txt'])");
		
	    System.out.println("Created File: ./" + cadena + ".sage");			
	}catch (Exception e) {
	    e.printStackTrace();
	}finally {
	    try {
		if (null != file)
		    file.close();
	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	}
    }
    
    public void computeCriticalIdeals(){
	String dirName = "files";
	File dir = new File (dirName);
	String cadena = text.getText() + ".sage";
	File archivo = new File(dir,cadena);
	FileWriter file = null;
	PrintWriter fout = null;
	try{
	    file = new FileWriter(archivo);
	    fout = new PrintWriter(file);
	    fout.println( "D = " + (graphtype == 0? "Graph(" : "DiGraph(") + graph2string() + ")" );
	    fout.println("");
	    fout.println("n = len(D)");
	    //fout.println("R = PolynomialRing(ZZ,['x%s'%p for p in range(n)]);");
	    fout.println("R = macaulay2.ring(\"ZZ\",\"[" + vars2string() + "]\").to_sage();");
	    fout.println("R.inject_variables();");
	    fout.println("Laplacian = diagonal_matrix(list(R.gens())) - D.adjacency_matrix()");
	    fout.println("file = open('" + cadena + ".txt', 'w')");
	    fout.println("");
	    fout.println("file.write(str(Laplacian) + '\\n')");
	    fout.println("def Gamma():");
	    fout.println("	for i in range(n+1):");
	    fout.println("		I = R.ideal(Laplacian.minors(i)).groebner_basis()");
	    fout.println("		if( I[0] == True ):");
	    fout.println("			next");
	    fout.println("		else:");
	    fout.println("			return i-1");
	    fout.println("	return n");
	    fout.println("");
	    fout.println("def CriticalIdealsOnZZ():");
	    fout.println("    Gamma = 0");
	    fout.println("    for i in range(n+1):");
	    fout.println("        I = R.ideal(Laplacian.minors(i)).groebner_basis()");
	    fout.println("        file.write(\"Critical ideals of size \" + str(i) + '\\n')");
	    fout.println("        file.write(str(I) + '\\n')");
	    fout.println("        if I[0] == 1:");
	    fout.println("            Gamma = i");
	    fout.println("    file.write(\"gamma = \" + str(Gamma) + '\\n')");
	    fout.println("");
	    fout.println("CriticalIdealsOnZZ()");
	    fout.println("");
	    fout.println("def SNF():");
	    fout.println("	L = - D.adjacency_matrix()");
	    fout.println("	for i in range(n):");
	    fout.println("		L[i,i] = 0");
	    fout.println("		for j in range(n):");
	    fout.println("			if(j != i):");
	    fout.println("				L[i,i] = L[i,i] - L[i,j]");
	    fout.println("");
	    fout.println("	list = []");
	    fout.println("	S,L1,R = L.smith_form()");
	    fout.println("	for i in range(n):");
	    fout.println("		if( S[i,i] != 0 ):");
	    fout.println("			list.append(S[i,i])");
	    fout.println("	return list ");
	    fout.println("");
	    fout.println("List = SNF()");
	    fout.println("");
	    fout.println("file.write('Smith Normal Form is ' + str(List) + '\\n')");
	    fout.println("file.write('f_1 is ' + str(List.count(1)) + '\\n')");
	    fout.println("");
	    fout.println("file.close()");
	    fout.println("");
	    fout.println("from subprocess import call");
	    fout.println("call(['emacs','" + cadena + ".txt'])");
	    fout.println("call(['open','" + cadena + ".txt'])");
		
	    System.out.println("Created File: ./" + cadena + ".sage");			
	}catch (Exception e) {
	    e.printStackTrace();
	}finally {
	    try {
		if (null != file)
		    file.close();
	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	}
    }

    public void computeCriticalIdealsText(){
	String dirName = "files";
	File dir = new File (dirName);
	String cadena = text.getText() + ".sage";
	File archivo = new File(dir,cadena);
	FileWriter file = null;
	PrintWriter fout = null;
	try{
	    file = new FileWriter(archivo);
	    fout = new PrintWriter(file);
	    fout.println( "D = " + (graphtype == 0? "Graph(" : "DiGraph(") + graph2string() + ")" );
	    fout.println("");
	    fout.println("n = len(D)");
	    //fout.println("R = PolynomialRing(ZZ,['x%s'%p for p in range(n)]);");
	    fout.println("R = macaulay2.ring(\"ZZ\",\"[" + vars2string() + "]\").to_sage();");
	    fout.println("R.inject_variables();");
	    fout.println("Laplacian = diagonal_matrix(list(R.gens())) - D.adjacency_matrix()");
	    fout.println("");
	    fout.println("file.write(str(Laplacian) + '\\n')");
	    fout.println("def Gamma():");
	    fout.println("	for i in range(n+1):");
	    fout.println("		I = R.ideal(Laplacian.minors(i)).groebner_basis()");
	    fout.println("		if( I.is_one() == True ):");
	    fout.println("			next");
	    fout.println("		else:");
	    fout.println("			return i-1");
	    fout.println("	return n");
	    fout.println("");
	    fout.println("def CriticalIdealsOnZZ():");
	    fout.println("    Gamma = 0");
	    fout.println("    for i in range(n+1):");
	    fout.println("        I = R.ideal(Laplacian.minors(i)).groebner_basis()");
	    fout.println("        print(\"Critical ideals of size \" + str(i) + '\\n')");
	    fout.println("        print(str(I) + '\\n')");
	    fout.println("        if I[0] == 1:");
	    fout.println("            Gamma = i");
	    fout.println("    print(\"gamma = \" + str(Gamma) + '\\n')");
	    fout.println("");
	    fout.println("CriticalIdealsOnZZ()");
	    fout.println("");
	    fout.println("def SNF():");
	    fout.println("	L = - D.adjacency_matrix()");
	    fout.println("	for i in range(n):");
	    fout.println("		L[i,i] = 0");
	    fout.println("		for j in range(n):");
	    fout.println("			if(j != i):");
	    fout.println("				L[i,i] = L[i,i] - L[i,j]");
	    fout.println("");
	    fout.println("	list = []");
	    fout.println("	S,L1,R = L.smith_form()");
	    fout.println("	for i in range(n):");
	    fout.println("		if( S[i,i] != 0 ):");
	    fout.println("			list.append(S[i,i])");
	    fout.println("	return list ");
	    fout.println("");
	    fout.println("List = SNF()");
	    fout.println("");
	    fout.println("print('Smith Normal Form is ' + str(List) + '\\n')");
	    fout.println("print('f_1 is ' + str(List.count(1)) + '\\n')");

		
	    System.out.println("Created File: ./" + cadena + ".sage");			
	}catch (Exception e) {
	    e.printStackTrace();
	}finally {
	    try {
		if (null != file)
		    file.close();
	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	}
    }
    
    public void runForbD() {
        
        try{
            String cadena = text.getText();
            String osname = System.getProperty("os.name");
            System.out.println(osname);
            if( osname.toLowerCase().compareTo("linux") == 0 ){
                System.out.println("sage ./files/" + cadena + ".sage");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && sage " + cadena + ".sage"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            } else if ( osname.toLowerCase().indexOf("windows") != -1 ){
                System.out.println("Not implemented yet. In the future it will execute: sage ./files/" + cadena + ".sage");
            } else if(osname.toLowerCase().indexOf("mac")!=-1){
                System.out.println("sage ./files/" + cadena + ".sage");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && sage " + cadena + ".sage"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            }
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }

	public void runForbDText() {
        
        try{
            String cadena = text.getText();
            String osname = System.getProperty("os.name");
            System.out.println(osname);
            if( osname.toLowerCase().compareTo("linux") == 0 ){
                //System.out.println("sage ./files/" + cadena + ".sage");
                //String[] cmd = {"/bin/sh", "-c", "cd ./files/ && sage " + cadena + ".sage"};
                //Process p = Runtime.getRuntime().exec(cmd);
                //p.waitFor();
            } else if ( osname.toLowerCase().indexOf("windows") != -1 ){
                System.out.println("notepad ./files/" + cadena + ".sage");
                String[] cmd = {"notepad","./files/" + cadena + ".sage"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            } else if(osname.toLowerCase().indexOf("mac")!=-1){
                //System.out.println("sage ./files/" + cadena + ".sage");
                //String[] cmd = {"/bin/sh", "-c", "cd ./files/ && sage " + cadena + ".sage"};
                //Process p = Runtime.getRuntime().exec(cmd);
                //p.waitFor();
            }
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }
	
    public void runCriticalGroup() {
        
        try{
            String cadena = text.getText();
            String osname = System.getProperty("os.name");
            System.out.println(osname);
            if( osname.toLowerCase().compareTo("linux") == 0 ){
                System.out.println("sage ./files/" + cadena + ".sage");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && sage ./" + cadena + ".sage"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                System.out.println("emacs ./files/" + cadena + ".py");
                //Runtime.getRuntime().exec("emacs ./files/" + cadena + ".py");
            } else if ( osname.toLowerCase().indexOf("windows") != -1 ){
                System.out.println("Not implemented yet. In the future it will execute: python ./files/" + cadena + ".py");
            } else if(osname.toLowerCase().indexOf("mac")!=-1){
                System.out.println("sage ./files/" + cadena + ".sage");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && sage ./" + cadena + ".sage"};
                Process p = Runtime.getRuntime().exec(cmd);
                //p.waitFor();
                //System.out.println("emacs ./files/" + cadena + ".py");
                //Runtime.getRuntime().exec("emacs ./files/" + cadena + ".py");
            }
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    public void runPython() {
        
        try{
            String cadena = text.getText();
            String osname = System.getProperty("os.name");
            System.out.println(osname);
            if( osname.toLowerCase().compareTo("linux") == 0 ){
                System.out.println("python ./files/" + cadena + ".py");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && python ./" + cadena + ".py"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                System.out.println("emacs ./files/" + cadena + ".py");
                Runtime.getRuntime().exec("emacs ./files/" + cadena + ".py");
            } else if ( osname.toLowerCase().indexOf("windows") != -1 ){
                System.out.println("Not implemented yet. In the future it will execute: python ./files/" + cadena + ".py");
            } else if(osname.toLowerCase().indexOf("mac")!=-1){
                System.out.println("python ./files/" + cadena + ".py");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && python ./" + cadena + ".py"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                System.out.println("emacs ./files/" + cadena + ".py");
                Runtime.getRuntime().exec("emacs ./files/" + cadena + ".py");
            }
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    public void runLaTeX() {
        
        try{
            String cadena = text.getText();
            String osname = System.getProperty("os.name");
            System.out.println(osname);
            if( osname.toLowerCase().compareTo("linux") == 0 ){
                System.out.println("pdflatex ./files/" + cadena + ".tex");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && pdflatex ./" + cadena + ".tex"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                System.out.println("evince ./files/" + cadena + ".pdf");
                Runtime.getRuntime().exec("evince ./files/" + cadena + ".pdf");
            } else if ( osname.toLowerCase().indexOf("windows") != -1 ){
			
				File archivo = new File("RunLatex.bat");
				FileWriter file = null;
				PrintWriter fout = null;
				try{
					file = new FileWriter(archivo);
					fout = new PrintWriter(file);
					fout.println("@echo off");
					fout.println("cd /D files");
					fout.println("pdflatex " + cadena + ".tex");
					// fout.println("pdflatex -output-directory=C:\\Users\\Erdos\\Documents\\Projects\\JGraphs\\trunk\\files\\ C:\\Users\\Erdos\\Documents\\Projects\\JGraphs\\trunk\\files\\" + cadena + ".tex");
					fout.println("start \"C:\\Program Files (x86)\\Adobe\\Reader 11.0\\Reader\\AcroRd32.exe\" " + cadena + ".pdf");
					System.out.println("Created File: ./RunLatex.bat");			
				}catch (Exception e) {
					e.printStackTrace();
				}finally {
					try {
						if (null != file)
						file.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				System.out.println("Please execute RunLatex.bat");
				
                /*
				System.out.println("pdflatex ./files/" + cadena + ".tex");
				String[] cmd = {"cmd.exe", "RunLatex.bat"};
                Process p = Runtime.getRuntime().exec(cmd);
                int exitVal = p.waitFor();
				System.out.println("Error value" + exitVal);
				System.out.println("AcroRd32.exe ./files/" + cadena + ".pdf");
				*/
            } else if(osname.toLowerCase().indexOf("mac")!=-1){
                System.out.println("pdflatex ./files/" + cadena + ".tex");
                String[] cmd = {"/bin/sh", "-c", "cd ./files/ && pdflatex ./" + cadena + ".tex"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                System.out.println("open ./files/" + cadena + ".pdf");
                Runtime.getRuntime().exec("open ./files/" + cadena + ".pdf");
            }
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    public void runCriticalIdeals() {
			
		try{
			String cadena = text.getText();
			String osname = System.getProperty("os.name");
			//System.out.println(osname);
			if(osname.toLowerCase().indexOf("mac")!=-1){
			System.out.println("Usando mac");
			String command = "sage " + cadena + ".sage";
			//command = command.trim();
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec( command );
			}
			Runtime.getRuntime().exec("sage ./" + cadena + ".sage");
			//Runtime.getRuntime().exec("notepad " + cadena + ".txt");
		}
		catch (Exception err) {
			err.printStackTrace();
		}			
    }
    
    public void paintComponent (Graphics g) {
        super.paintComponent( g );
        
        int w=getSize ( ).width;
        int h=getSize ( ).height;
        cx = w/2;
        cy = h/2;
        
        g.setColor (Color.white);
        g.fillRect (0, 0, w, h);
        
        //if(!(mainflag == 2 && binvflag == 1))
        // Dibuja las aristas
        for( int i=0; i<nodos.size(); i++ )
            for( int j=0; j<nodos.get(i).list.size(); j++ )
                if(!(nodos.get(i).d || nodos.get(nodos.get(i).list.get(j)).d)){
                    g.setColor(Color.black);
                    g.drawLine(nodos.get(i).x,nodos.get(i).y,nodos.get(nodos.get(i).list.get(j)).x,nodos.get(nodos.get(i).list.get(j)).y);
                    if(graphtype == 1){
                        int x1 = nodos.get(i).x, y1 = nodos.get(i).y, x2 = nodos.get(nodos.get(i).list.get(j)).x, y2 = nodos.get(nodos.get(i).list.get(j)).y;
                        double D = Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) );
                        // u is the point at distance D-arrowW from x to y
                        double u1 = (D-arrowW)*x2/D + arrowW*x1/D;
                        double u2 = (D-arrowW)*y2/D + arrowW*y1/D;
                        // p is the perpendicular unit vector
                        double p1 = arrowH*(x2 - x1)/D;
                        double p2 = arrowH*(y1 - y2)/D;
                        // v is the point at distance D-nz from x to y
                        double v1 = (D-nz)*x2/D + nz*x1/D;
                        double v2 = (D-nz)*y2/D + nz*y1/D;
                        g.drawLine( (int) (u1+p2), (int) (u2+p1), (int)v1, (int)v2);
                        g.drawLine( (int) (u1-p2), (int) (u2-p1), (int)v1, (int)v2);
                    }
                    
                }
        
        // Dibuja los nodos
        int allowed = -1;
        for(int i=0; i<nodos.size(); i++){
            g.setColor(Color.white);
            if(nodos.get(i).d)
                g.setColor(Color.gray);
            else
                allowed++;
            if(mainflag == 1 && i == colorvflag){
                if(binvflag == 1)
                    g.setColor (Color.green);
                if(binvflag == 0)
                    g.setColor (Color.red);
                colorvflag=-1;
            }
            g.fillOval(nodos.get(i).x-nz,nodos.get(i).y-nz,2*nz,2*nz);
            g.setColor(Color.black);
            if(!nodos.get(i).d)
                g.drawString("" + (allowed),nodos.get(i).x-nz,nodos.get(i).y-2*nz);
            g.drawOval(nodos.get(i).x-nz,nodos.get(i).y-nz,2*nz,2*nz);
        }
        
    }
    
    public void mousePressed (MouseEvent event) {
        event.consume ();
        int x = event.getX ();
        int y = event.getY ();
        if(mainflag == 2)
            for(int i=0; i<nodos.size(); i++)
                if(Math.abs(nodos.get(i).x-x)<=nz && Math.abs(nodos.get(i).y-y)<=nz){
                    nodeflag = i;
                    binvflag =1;
                    return;
                }
    }
    public void mouseDragged (MouseEvent event) {
        event.consume ();
        if(mainflag == 2){
            int x = event.getX ();
            int y = event.getY ();
            nodos.get(nodeflag).x = x;
            nodos.get(nodeflag).y = y;
            repaint();
        }
        
    }
    public void mouseReleased (MouseEvent event) {
        event.consume ();
        if(mainflag == 2){
            binvflag=0;
            repaint();
        }
    }
    
    public void mouseClicked (MouseEvent event) {
        event.consume ();
        int x = event.getX ();
        int y = event.getY ();
        if(mainflag == 0){
            Nodo nodo = new Nodo();
            nodo.x = x;
            nodo.y = y;
            
            nodos.add(nodo);
            
            repaint();
        }
        if(mainflag == 1){
            int i;
            for(i=0; i<nodos.size(); i++)
                if(Math.abs(nodos.get(i).x-x)<=nz && Math.abs(nodos.get(i).y-y)<=nz){
                    if(binvflag == 0){
                        nodeflag = i;
                        binvflag = 1;
                    }
                    else{
                        if(nodeflag != i){
                            int j = 0, k = 0;
                            if(graphtype == 0)
                                for(; k<nodos.get(i).list.size(); k++)
                                    if(nodos.get(i).list.get(k) == nodeflag){
                                        nodos.get(i).list.remove(k);
                                        k = -1;
                                        break;
                                    }
                            if(k > -1)
                                k = 0;
                            
                            for(; j<nodos.get(nodeflag).list.size(); j++)
                                if(nodos.get(nodeflag).list.get(j) == i){
                                    nodos.get(nodeflag).list.remove(j);
                                    j = -1;
                                    break;
                                }
                            
                            j = j + k;
                            
                            if( j > -1 )
                                nodos.get(nodeflag).list.add(i);
                        }
                        nodeflag = -1;
                        binvflag = 0;
                    }
                    colorvflag = i;
                    repaint();
                    return;
                }
            if(i == nodos.size()){
                binvflag = 0;
                repaint();
            }
        }
        if(mainflag == 3)
            for(int i=0; i<nodos.size(); i++)
                if(Math.abs(nodos.get(i).x-x)<=nz && Math.abs(nodos.get(i).y-y)<=nz){
                    nodos.get(i).d = !nodos.get(i).d;
                    repaint();
                    return;
                }	    
    }
    public void mouseEntered (MouseEvent event) { }
    public void mouseExited (MouseEvent event) { }
    public void mouseMoved (MouseEvent event) { }
    
    public void actionPerformed (ActionEvent event) {
	Object s = event.getSource( );
	if (s == buttonV){
	    mainflag=0;
	}
	else if (s == buttonE){
	    mainflag=1;
	}
	else if (s == buttonM){
	    mainflag=2;
	}
	else if (s == buttonC){
	    nodos.clear();
	    repaint();
	}
	else if (s == buttonD){
	    mainflag=3;
	}
	else if (s == itemCriticalIdeals){			
	    computeCriticalIdeals();
	    runCriticalIdeals();
	}
	else if (s == itemCriticalIdealsText){			
	    computeCriticalIdealsText();
	    runForbDText();
	}
	else if (s == itemCriticalGroup){
	    computeCriticalGroup();
	    runCriticalGroup();
	}    
	else if (s == itemLaTex){
			
	    toLaTeX();
	    runLaTeX();

	}
	else if (s == itemForbDigraph){
	    computeForbMZ();
	    runForbD();
	}
	else if (s == itemForbDigraphText){
	    computeForbMZText();
	    runForbDText();
	}else if (s == itemPython){
			
	    toPython();

	}
        else if (s == itemGraph){
            itemGraph.setArmed(true);
            itemDigraph.setArmed(false);
            graphtype = 0;
            repaint();
        }
        else if (s == itemDigraph){
            itemGraph.setArmed(false);
            itemDigraph.setArmed(true);
            graphtype = 1;
            repaint();
        }
        else if (s == text) {
            String cadena = text.getText();
            int nodosize=0;
            if(cadena.charAt(0)== 'K'){ // Grafica Completa
                if(cadena.length() > 1)
                    nodosize = Integer.parseInt(cadena.substring(1,cadena.length()));
                
                int w=getSize ( ).width;
                int h=getSize ( ).height;
                cx = w/2;
                cy = h/2;
                nodos.clear();
                
                for(int j=0; j<nodosize; j++){
                    Nodo nodo = new Nodo();
                    nodo.x = (int) -(200* Math.cos( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cx;
                    nodo.y = (int) (200* Math.sin( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cy;
                    
                    nodos.add(nodo);
                    for(int k=0; k<nodosize; k++)
                        if(k != j)
                            nodo.list.add(k);
                }
                repaint();
            }else if(cadena.charAt(0)== 'C'){ // Ciclo
                if(cadena.length() > 1)
                    nodosize = Integer.parseInt(cadena.substring(1,cadena.length()));
                
                int w=getSize ( ).width;
                int h=getSize ( ).height;
                cx = w/2;
                cy = h/2;
                nodos.clear();
                
                for(int j=0; j<nodosize; j++){
                    Nodo nodo = new Nodo();
                    nodo.x = (int) -(200* Math.cos( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cx;
                    nodo.y = (int) (200* Math.sin( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cy;
                    
                    if(j < nodosize -1)
                        nodo.list.add(j+1);
                    else
                        nodo.list.add(0);
                    
                    nodos.add(nodo);
                }
                repaint();
            } else if(cadena.charAt(0)== 'P'){ // Camino
                if(cadena.length() > 1)
                    nodosize = Integer.parseInt(cadena.substring(1,cadena.length()));
                
                int w=getSize ( ).width;
                int h=getSize ( ).height;
                cx = w/2;
                cy = h/2;
                nodos.clear();
                
                for(int j=0; j<nodosize; j++){
                    Nodo nodo = new Nodo();
                    nodo.x = (int) -(200* Math.cos( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cx;
                    nodo.y = (int) (200* Math.sin( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cy;
                    
                    if(j < nodosize -1)
                        nodo.list.add(j+1);
                    
                    nodos.add(nodo);
                }
                repaint();
            }else if(cadena.charAt(0)== 'T'){ // Grafica Trivial
                if(cadena.length() > 1)
                    nodosize = Integer.parseInt(cadena.substring(1,cadena.length()));
                
                int w=getSize ( ).width;
                int h=getSize ( ).height;
                cx = w/2;
                cy = h/2;
                nodos.clear();
                
                for(int j=0; j<nodosize; j++){
                    Nodo nodo = new Nodo();
                    nodo.x = (int) -(200* Math.cos( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cx;
                    nodo.y = (int) (200* Math.sin( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cy;
                    
                    nodos.add(nodo);
                }
                repaint();
            }else if(cadena.charAt(0)== '['){ // Desde Matriz
                int [][] matriz = string2matrix2(cadena);
                nodosize = matriz.length;
                
                int w=getSize ( ).width;
                int h=getSize ( ).height;
                cx = w/2;
                cy = h/2;
                nodos.clear();
                
                for(int j=0; j<nodosize; j++){
                    Nodo nodo = new Nodo();
                    nodo.x = (int) -(200* Math.cos( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cx;
                    nodo.y = (int) (200* Math.sin( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cy;
                    
                    nodos.add(nodo);
                    for(int k=0; k<nodosize; k++)
                        if(matriz[j][k] != 0 && k != j)
                            nodo.list.add(k);
                }
                repaint();
            }else if(cadena.charAt(0)== '{'){ // Desde Matriz
                int [][] matriz = string2matrix(cadena);
                nodosize = matriz.length;
                
                int w=getSize ( ).width;
                int h=getSize ( ).height;
                cx = w/2;
                cy = h/2;
                nodos.clear();
                
                for(int j=0; j<nodosize; j++){
                    Nodo nodo = new Nodo();
                    nodo.x = (int) -(200* Math.cos( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cx;
                    nodo.y = (int) (200* Math.sin( ( (double) 2*Math.PI*j)/ (double) nodosize)) + cy;
                    
                    nodos.add(nodo);
                    for(int k=0; k<nodosize; k++)
                        if(matriz[j][k] != 0 && k != j)
                            nodo.list.add(k);
                }
                repaint();
            }
        }
    }    
    
    public static void main (String[ ] args) {
        JGraphs inter = new JGraphs ( );
        JFrame jf = new JFrame ( );
        jf.setTitle ("Graphs");
        jf.setSize (800, 800);
        jf.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        jf.setContentPane (inter);
        jf.setVisible(true);
    }
}

class Nodo {
    
    public int x;
    public int y;
    public boolean d = false;
    public java.util.List<Integer> list = new LinkedList<Integer>();
    
    public Nodo(){}    
}

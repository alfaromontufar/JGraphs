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
    private java.util.List<Nodo> nodos;
    private int cx, cy;
    private int arrowH = 5, arrowW = 15; // altura y anchura de la flecha
    private final int nz=6; //node size
    private JButton buttonV = new JButton("Vertex");
    private JButton buttonE = new JButton("Edge");
    private JButton buttonM = new JButton("Move");
    private JButton buttonC = new JButton("Clear");
    private JButton buttonD = new JButton("Delete");
    private JTextField text = new JTextField("ProjectName",20);
    private JButton buttonG = new JButton("Generate");
    private int mainflag=0, colorvflag=-1, binvflag=0, nodeflag;
		
    public JGraphs ( ) {
	buttonV.addActionListener(this);
	buttonE.addActionListener(this);
	buttonM.addActionListener(this);
	buttonC.addActionListener(this);
	buttonD.addActionListener(this);
	buttonG.addActionListener(this);
	add(buttonV);
	add(buttonE);
	add(buttonM);
	add(buttonC);
	add(buttonD);
	add(text);
	text.setEditable(true);
	add(buttonG);
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
    
    public String graph2string(){

	String graph = "DiGraph({";
	int deleted = 0;
	int[] dvector = new int [nodos.size()];

	for(int i=0; i<nodos.size(); i++){
	    if(nodos.get(i).d == true)
		deleted++;
	    dvector[i] = deleted;
	}

	int nnodos = nodos.size()-deleted;
	boolean firstused = true;

	for(int i=0; i<nodos.size(); i++)	
	    if(!nodos.get(i).d){
		int aux1 = i - dvector[i], flag = 0;
		String graph1 = (aux1 == 0 || firstused? "" : ",") + Integer.toString(aux1) + ":[";
		
		for(int j=0; j<nodos.get(i).list.size(); j++)
		    if(!nodos.get(nodos.get(i).list.get(j)).d){
			int aux2 = nodos.get(i).list.get(j) - dvector[nodos.get(i).list.get(j)];
			
			graph1 += (flag == 0? "" : ",") + Integer.toString(aux2);
			if(flag == 0)
			    flag = 1;
		    }
		graph1 += "]";
		if(flag != 0){
			graph += graph1;
			firstused = false;
		}
	    }

	graph += "});";


	//System.out.println(graph);

	return graph;

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
	int[][] matrix = new int [nodos.size()-deleted][nnodos];

	for(int i=0; i<nodos.size(); i++)	
	    if(!nodos.get(i).d){
		int aux1 = i - dvector[i];

		for(int j=0; j<nodos.get(i).list.size(); j++)
		    if(!nodos.get(nodos.get(i).list.get(j)).d){
			int aux2 = nodos.get(i).list.get(j) - dvector[nodos.get(i).list.get(j)];
			matrix[aux1][aux2]--;
			
			// This is in the non-directed graph case
			//	matrix[aux2][aux1]--;
		    }
	    }

	for(int i=0; i<nnodos; i++){
	    int suma=0;
	    for(int j=0; j<nnodos; j++)
		if(i!=j)
		    suma+=matrix[i][j];
	    matrix[i][i] = (-suma);
	}

	//printmatrix(matrix);

	return matrix;
    }

    public void createFile (){
	String cadena = text.getText();
	FileWriter file = null;
	PrintWriter fout = null;
	try{
	    file = new FileWriter(cadena + ".sage");
	    fout = new PrintWriter(file);
	    fout.println( "D = " + graph2string() );
	    fout.println("");
	    fout.println("n = len(D)");
	    fout.println("R = PolynomialRing(ZZ,['x%s'%p for p in range(n)]);");
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
		
    public void runcsp() {
			
	try{
	    String cadena = text.getText();
	    String osname = System.getProperty("os.name");
	    //System.out.println(osname);
	    if(osname.toLowerCase().indexOf("mac")!=-1){
		String command = "sage " + cadena + ".sage"; 
		//command = command.trim(); 
		Runtime rt = Runtime.getRuntime(); 
		Process proc = rt.exec( command );
	    }
	    Runtime.getRuntime().exec("sage " + cadena + ".sage");
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
	    for( int j=0; j<nodos.get(i).list.size(); j++ ){
		g.setColor(Color.black);
		g.drawLine(nodos.get(i).x,nodos.get(i).y,nodos.get(nodos.get(i).list.get(j)).x,nodos.get(nodos.get(i).list.get(j)).y);
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
			
	// Dibuja los nodos
	for(int i=0; i<nodos.size(); i++){
	    g.setColor(Color.white);
	    if(nodos.get(i).d)		   
		g.setColor(Color.gray);
	    if(mainflag == 1 && i == colorvflag){
		if(binvflag == 1)
		    g.setColor (Color.green);
		if(binvflag == 0)
		    g.setColor (Color.red);
		colorvflag=-1;
	    }
	    g.fillOval(nodos.get(i).x-nz,nodos.get(i).y-nz,2*nz,2*nz);	   
	    g.setColor(Color.black);
	    g.drawString("" + (i+1),nodos.get(i).x-nz,nodos.get(i).y-2*nz);
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
			    int j = 0;
			    for(; j<nodos.get(nodeflag).list.size(); j++)
				if(nodos.get(nodeflag).list.get(j) == i){
				    nodos.get(nodeflag).list.remove(j);
				    j = nodos.get(nodeflag).list.size()+1;
				    j = -1;
				    break;
				}
			    if( j != -1 )
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
	else if (s == buttonG){
	    
	    createFile();
	    runcsp();

	}
	else if (s == text) {
	    String cadena = text.getText();
	    int nodosize=0;
	    if(cadena.charAt(0)== 'K'){
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
	    }else if(cadena.charAt(0)== 'C'){
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
	    }else if(cadena.charAt(0)== 'P'){
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
	    }else if(cadena.charAt(0)== 'T'){
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
	    }else if(cadena.charAt(0)== '{'){
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
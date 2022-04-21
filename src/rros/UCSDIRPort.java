/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.util.ArrayList;
import java.util.PriorityQueue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *
 * @author PETER-PC
 */
public class UCSDIRPort implements Comparable{
//    public static PriorityQueue<UCSDIRPort> UCSPaths = new PriorityQueue<>();
    private UCSPath path;
    public Path animPath;
    
    public UCSDIRPort(Vertex rootNode, Vertex goalNode){
        path = new UCSPath();
        path.findGoal(rootNode, goalNode);
//        UCSPaths.add(this);
        animPath = ucsToAnimPath(path);
    }
    
    public UCSDIRPort(Vertex rootNode, int dirPortSkip){
        path = new UCSPath();
        path.findNearestDIRPort(rootNode, dirPortSkip);
        animPath = ucsToAnimPath(path);
        
    }
    
    public UCSPath getUCSPath(){
        return path;
    }
    
    public final Path ucsToAnimPath(UCSPath p){
        Path animPath = new Path();
        ObservableList<Vertex> array = p.operator.peek().path;
        for(Vertex v: array){
            if(array.indexOf(v) == 0){
                MoveTo m = new MoveTo(v.getCenterX(), v.getCenterY());
                animPath.getElements().add(m);
            }else if(array.indexOf(v) == (array.size()-1)){
                LineTo lT = new LineTo(v.getCenterX(), v.getCenterY());
                System.out.println("Final coord x: "+v.getCenterX()+" y: "+v.getCenterY());
                animPath.getElements().add(lT);
            }else{
                LineTo lT = new LineTo(v.getCenterX(), v.getCenterY());
                animPath.getElements().add(lT);
            }
        }
        animPath.setFill(Color.RED);
        animPath.setStroke(Color.RED);
        animPath.setStrokeWidth(15);
        return animPath;
    }
    
    @Override
    public int compareTo(Object o) {
        if (getUCSPath().operator.peek().getWeight() > ((UCSDIRPort) o).getUCSPath().operator.peek().getWeight()) {
            return 1;
        } else if (getUCSPath().operator.peek().getWeight() < ((UCSDIRPort) o).getUCSPath().operator.peek().getWeight()) {
            return -1;
        } else {
            return 0;
        }
    }
    
    class UCSPath implements Comparable, Cloneable{
        private ObservableList<Vertex> path = FXCollections.observableArrayList();
        public PriorityQueue<UCSPath> operator = new PriorityQueue<>();
        public ArrayList<Vertex> visitedNodes = new ArrayList<>();
        public ArrayList<Vertex> unVisitedNodes = new ArrayList<>();
        private boolean goalFound = false;
        private double weight = 0;
        
        @Override
        public UCSPath clone() throws CloneNotSupportedException{
            UCSDIRPort.UCSPath cPath = (UCSDIRPort.UCSPath)super.clone();
            ObservableList<Vertex> temp = this.path;
            cPath.path = FXCollections.observableArrayList();
            for(Vertex v: temp)
                cPath.path.add(v);
            return cPath;
        }
                
        public final Vertex getCurrentVertex(){
            Vertex v = operator.peek().getPath().get(operator.peek().getPath().size() - 1);
            return v;
        }
        
        //improve UCS to expand search and change goalNode if the path is an invalid station slot
        //after goal is found use a countdown variable to stop search
        public boolean goalFound(Vertex goalNode){
            int goalFoundFrequency = 0;
            if(operator.size() > 2)
                for(UCSPath u: operator){
                    if(goalNode.equals(u.getPath().get(u.getPath().size() - 1)))
                        ++goalFoundFrequency;
                }
            
            if(goalFoundFrequency == 2)
                return true;
            else 
                return false;
        }
        
        public final void findGoal(Vertex rootNode, Vertex goalNode){          
            //transfer vertices to unVisitedNodes
            for(Vertex v: Vertex.vertices){
                //DeMorgan Theorem 5, boolean algebra
                if(!v.equals(rootNode))
                    unVisitedNodes.add(v);
                else if(v.equals(rootNode))
                    visitedNodes.add(v);
            }
            
            //initialize operator
            for(Vertex v: rootNode.getNeighbours()){
                if(!visitedNodes.contains(v)){
                    UCSPath p = new UCSPath();
                    p.path.add(rootNode);
                    p.path.add(v);
                    setWeight(p);
                    operator.add(p);
                    unVisitedNodes.remove(v);
                    System.out.println("Initial Processing Vertex "+v);
                }
            }
            
            //find goal
            while(!goalFound){               
                Vertex v = getCurrentVertex();
                    System.out.println("Processing Vertex: "+v);
                if(!v.equals(goalNode)){
                    visitedNodes.add(v);

                    //traverse every neighbour of current vertex
                    for(Vertex vert: v.getNeighbours()){
                        //process neighbour vertex only if it has not being visited or in frontier
                        if(unVisitedNodes.contains(vert)){
                            System.out.println("Processing Neighbour ("+vert+") of Vertex "+v);

                            try{
                                System.out.println("cloned path");
                                //clone the currentPath and add new vertex for branching routes
                                UCSPath tempPath = getCurrentPath(v).clone();
                                //remove neighbor from unVisited node and add to frontier
                                unVisitedNodes.remove(vert);
                                tempPath.getPath().add(vert);
                                setWeight(tempPath);
                                operator.add(tempPath);
                                printFrontierFromOperator();
                            }catch(CloneNotSupportedException ex){}
                        }
                    }
                    System.out.print("Removing Path: ");
                    printPath(getCurrentPath(v));
                    operator.remove(getCurrentPath(v));
                }
                else{
                    goalFound = true;
                    System.out.println("Goalfound");
                    printPath(getCurrentPath(v));
                    break;
                }
            }
            
        }
        
        public final void findNearestDIRPort(Vertex rootNode, int dirPortSkip){  
            unVisitedNodes.clear();
            visitedNodes.clear();
            operator.clear();
            
            System.out.println("Method call DIRPORTSKIP: "+dirPortSkip);
            
            //transfer vertices to unVisitedNodes
            for(Vertex v: Vertex.vertices){
                //DeMorgan Theorem 5, boolean algebra
                if(!v.equals(rootNode))
                    unVisitedNodes.add(v);
                else if(v.equals(rootNode))
                    visitedNodes.add(v);
            }
            
            //initialize operator
            for(Vertex v: rootNode.getNeighbours()){
                if(!visitedNodes.contains(v)){
                    UCSPath p = new UCSPath();
                    p.path.add(rootNode);
                    p.path.add(v);
                    setWeight(p);
                    operator.add(p);
                    unVisitedNodes.remove(v);
                    System.out.println("Initial Processing Vertex "+v);
                }
            }
            
            int dirPortCounter = 0;
            //find goal
            while(!goalFound){               
                Vertex v = getCurrentVertex();
                System.out.println("Processing Vertex: "+v);
                if(!(v instanceof DirPort) || (v instanceof DirPort && dirPortCounter < dirPortSkip)){
                    visitedNodes.add(v);
                    
                    if(v instanceof DirPort){
                        dirPortCounter++;
                        System.out.println("Port found: dirPortCounter -> "+dirPortCounter+", dirPortSkip -> "+dirPortSkip);
                    }
//                    else{
////                        printPath(getCurrentPath(v));
//                    }

                    //traverse every neighbour of current vertex
                    for(Vertex vert: v.getNeighbours()){
                        //process neighbour vertex only if it has not being visited or in frontier
                        if(unVisitedNodes.contains(vert)){
                            System.out.println("Processing Neighbour ("+vert+") of Vertex "+v);

                            try{
                                System.out.println("cloned path");
                                //clone the currentPath and add new vertex for branching routes
                                UCSPath tempPath = getCurrentPath(v).clone();
                                //remove neighbor from unVisited node and add to frontier
                                unVisitedNodes.remove(vert);
                                tempPath.getPath().add(vert);
                                setWeight(tempPath);
                                operator.add(tempPath);
                                printFrontierFromOperator();
                            }catch(CloneNotSupportedException ex){}
                        }
                    }
                    System.out.print("Removing Path: ");
                    printPath(getCurrentPath(v));
                    operator.remove(getCurrentPath(v));
                }
                else if(dirPortCounter == dirPortSkip){
                        goalFound = true;
                        System.out.println("Goalfound");
                        break;
                }
            }
            
        }
        
        public final void printPath(UCSPath p){
            System.out.print("Printing Path : ");
            for(Vertex v: p.path)
                System.out.print(v+", ");
            System.out.print("; weight: "+p.weight+"\n");
        }
        
        public final void printFrontierFromOperator(){
            Object[] paths = operator.toArray();
            for(int i = 0; i < paths.length; i++){

                System.out.print("Path ("+i+"): ");
                for(Vertex v: ((UCSPath) paths[i]).path){
                    System.out.print(v+", ");
                }
                System.out.print("; weight: "+((UCSPath) paths[i]).weight);
                System.out.println("");
            }
        }
        
        public UCSPath getCurrentPath(Vertex v){
            for(UCSPath p: operator){
                //return the path whose top node is vertex v
                if(p.path.get(p.path.size() - 1).equals(v))
                    return p;
            }
            
            return null;
        }
        
        public void setWeight(UCSPath path){
            int size = path.path.size();
            WeightedEdge w = WeightedEdge.getWeightedEdge(path.path.get(--size),  path.path.get(--size));
            if (w != null) 
                path.weight += w.getWeight();
        }
        
        
        public double getWeight(){
            return weight;
        }

        public ObservableList<Vertex> getPath(){
            return path;
        }

        @Override
        public int compareTo(Object o) {
            if(getWeight() > ((UCSPath)o).getWeight())
                return 1;
            else if(getWeight() < ((UCSPath)o).getWeight())
                return -1;
            else
                return 0;
        }
    }
    
    
}

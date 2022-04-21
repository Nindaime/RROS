/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author PETER
 * @param <V>
 */
public abstract class AbstractGraph<V> implements Graph<V>{
    protected List<V> vertices = new ArrayList<>();
    protected List<List<Edge>> neighbours = new ArrayList<>();
    
    protected AbstractGraph(){}
    
    // Construct a graph from vertices and edges stored in arrays
    protected AbstractGraph(V[] vertices, int[][] edges){
        for(V v: vertices)
            addVertex(v);
        
        createAdjacencyLists(edges, vertices.length);
    }
    
    // Construct a graph from vertices and edges stored in List
    protected AbstractGraph(List<V> vertices, List<Edge> edges){
        for(V v: vertices)
            addVertex(v);
        
        createAdjacencyLists(edges, vertices.size());
    }
    
    // Construct a graph for integer vertices 0, 1, 2 and edge list
    protected AbstractGraph(List<Edge> edges, int numberOfVertices){
        for(Integer i = 0; i < numberOfVertices; i++)
            addVertex((V) i);
            
        createAdjacencyLists(edges, numberOfVertices);
    }
    
    // Construct a graph from integer vertices 0, 1, and edge array
    protected AbstractGraph(int[][] edges, int numberOfVertices){
        for(Integer i = 0; i < numberOfVertices; i++)
            addVertex((V)i);
            
        createAdjacencyLists(edges, numberOfVertices);
    }
    
    // Create adjacency lists for each vertex using edge array
    private void createAdjacencyLists(int[][] edges, int numberOfVertices){
        for(int i = 0; i < edges.length; i++)
            addEdge(edges[i][0], edges[i][1]);
    }
    
    private void createAdjacencyLists(List<Edge> edges, int numberOfVertices){
        for(Edge edge: edges)
            addEdge(edge.startV, edge.endV);
    }
        
    @Override
    public int getSize(){
        return vertices.size();
    }
    
    @Override
    public List<V> getVertices(){
        return vertices;
    }
    
    @Override
    public V getVertex(int index){
        return vertices.get(index);
    }
    
    @Override
    public int getIndex(V v){
        return vertices.indexOf(v);
    }
    
    @Override
    public List<Integer> getNeighbors(int index){
        List<Integer> result = new ArrayList<>();
        for(Edge edge: neighbours.get(index))
            result.add(edge.endV);
        
        return result;
    }
    
    @Override
    public int getDegree(int vertex){
        return neighbours.get(vertex).size();
    }
    
    @Override
    public void printEdges(){
        for(int u = 0; u < neighbours.size(); u++){
            System.out.print(getVertex(u)+" ("+ u +"): ");
            for(Edge edge: neighbours.get(u))
                System.out.print("("+getVertex(edge.startV)+", " +
                        getVertex(edge.endV)+") ");
            System.out.println();
        }
    }
    
    @Override
    public void clear(){
        vertices.clear();
        neighbours.clear();
    }
    
    @Override
    public final boolean addVertex(V vertex){
        if(!vertices.contains(vertex)){
            vertices.add(vertex);
            neighbours.add(new ArrayList<>());
            return true;
        }
        else
            return false;
    }
    
    protected boolean addEdge(Edge e){
        if(e.startV < 0 || e.startV > getSize() - 1)
            throw new IllegalArgumentException("No such index: "+e.startV);
        
        if(e.endV < 0 || e.endV > getSize() - 1)
            throw new IllegalArgumentException("No such index: "+e.endV);
        
        if(!neighbours.get(e.startV).contains(e)){
            neighbours.get(e.startV).add(e);
            return true;
        }
        else{return false;}
    }
    
    @Override
    public boolean addEdge(int startV, int endV){
        return addEdge(new Edge(startV, endV));
    }
    
    public static class Edge {
    int startV, endV;
    
    public Edge(int u, int v){
        u = startV;
        v = endV;
    }
    
    @Override 
    public boolean equals(Object o){
        if(o instanceof Edge)
            return startV == ((Edge)o).startV && endV == ((Edge)o).endV;
        else
            return false;
    }

}
    
    public class Tree {

        private int root;
        private int[] parent;
        private List<Integer> searchOrder;

        public Tree(int root, int[] parent, List<Integer> searchOrder) {
            this.root = root;
            this.parent = parent;
            this.searchOrder = searchOrder;
        }

        public int getRoot() {
            return root;
        }

        public int getParent(int v) {
            return parent[v];
        }

        public List<Integer> getSearchOrder() {
            return searchOrder;
        }

        public int getNumberOfVerticesFound() {
            return searchOrder.size();
        }

        public List<V> getPath(int index) {
            ArrayList<V> path = new ArrayList<>();

            do {
                path.add(vertices.get(index));
                index = parent[index];
            }
            while(index != -1);
            
            return path;
        }
        
        public void printPath(int index){
            List<V> path = getPath(index);
            System.out.print("A path from "+ vertices.get(root) + " to "+
                    vertices.get(index) +": ");
            for(int i = path.size() - 1; i >= 0; i--)
                System.out.print(path.get(i) + " ");
        }
        
        public void printTree(){
            System.out.println("Root is: "+vertices.get(root));
            System.out.print("Edges: ");
            for(int i = 0; i < parent.length; i++){
                if(parent[i] != -1)
                    System.out.print("(" + vertices.get(parent[i]) + ", " +
                            vertices.get(i) + ") ");
            }
            System.out.println();
        }

    }
}

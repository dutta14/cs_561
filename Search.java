import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

enum Search {
    BFS, UCS, ASTAR
}

class Vertex {
    String name; //name of the intersection.
    boolean visited;
    long est_cost; //f(x)
    long pathCost; //g(x)
    int queuePriority;
    Vertex parent = null;

    Vertex(String name) {
        this.name = name;
        visited = false;
        est_cost = pathCost = Integer.MAX_VALUE; //initially setting pathCost to max.
        queuePriority = Integer.MAX_VALUE; //max value means, that this has not gone into the queue yet.
    }
}

class Edge {
    int weight;
    int trafficPriority;

    Edge(int w, int p) {
        weight = w; //weight is the pathCost between v1 and v2.
        trafficPriority = p; //trafficPriority means when this edge came in the traffic input. lower value means came later. Goes from m to 1.
    }
}

public class Search {

    private static HashMap<String, Vertex> vertices;
    private static HashMap<Vertex, LinkedHashMap<Vertex, Edge>> adjList;
    private static HashMap<Vertex, Integer> heuristic;  //required for A*. Has vertex->distance mapping.

    private static FileWriter writer;

    private static Vertex bfs(Vertex s, Vertex g) {
        if (s == g) {
            s.est_cost = s.pathCost = 0;
            return s;
        }
        Queue<Vertex> q = new LinkedList<>();
        s.visited = true;
        q.add(s); // added first element to q.

        while (!q.isEmpty()) {
            Vertex v = q.remove(); // remove first vertex from queue.
            HashMap<Vertex, Edge> list = adjList.get(v); // get all its children.
            if (list == null) //leaf node
                continue;
            for (Vertex i : list.keySet()) {
                if (!i.visited) {
                    i.parent = v;
                    if (i == g) // check for each child if it is goal.
                        return i;
                    i.visited = true;
                    q.add(i); // if not, then add it to queue.
                }
            }
        }
        return null; // no path found.
    }

    private static Vertex dfs(Vertex s, Vertex g) {
        if (s == g) {
            s.est_cost = s.pathCost = 0;
            return s;
        }

        Stack<Vertex> stk = new Stack<>();
        Stack<Vertex> tempStk = new Stack<>(); //used to reverse the set.
        s.visited = true;
        stk.push(s); // added first element to q.

        while (!stk.isEmpty()) {
            Vertex v = stk.pop(); // remove first vertex from stack.

            HashMap<Vertex, Edge> list = adjList.get(v); // get all its children.
            if (list == null) //leaf node.
                continue;

            for (Vertex i : list.keySet()) {
                if (!i.visited) {
                    i.parent = v;
                    if (i == g) // check for each child if it is goal.
                        return i;
                    i.visited = true;
                    tempStk.push(i); // if not, then add it to queue.
                }
            }
            while (!tempStk.isEmpty()) {
                stk.push(tempStk.pop());
            }
        }
        return null; // no path found.
    }

    private static Vertex ucsAstar(Vertex s, Vertex g, Search type) { //type checks whether UCS or ASTAR
        int priority = 0; //order in which elements went inside trafficPriority queue.
        s.est_cost = s.pathCost = 0; //updating source from INT_MAX to 0.
        if (s == g) {
            return s;
        }

        PriorityQueue<Vertex> pq = new PriorityQueue<>(vertices.size(), new Comparator<Vertex>() {

            @Override
            public int compare(Vertex v1, Vertex v2) {
                return (v1.est_cost < v2.est_cost) ? -1 //if v1 has lesser pathCost
                        : v1.est_cost > v2.est_cost ? 1 //if v2 has lesser pathCost
                        : v1.parent == v2.parent //if both have same parent,
                        ? findEdge(v2.parent, v2).trafficPriority - findEdge(v1.parent, v1).trafficPriority //check who came first in traffic line.
                        : v1.queuePriority - v2.queuePriority; //else check who came first in queue. lower queuePriority is better.
            }
        });

        s.queuePriority = ++priority; //set new trafficPriority of element inserted into trafficPriority q.
        pq.add(s);

        ArrayList<Vertex> closed = new ArrayList<>();

        while (!pq.isEmpty()) {
            boolean addtoQ = false;
            Vertex v = pq.remove(); // remove first vertex from queue.
            if (v == g)
                return v;


            HashMap<Vertex, Edge> list = adjList.get(v); // get all its children.
            if (list == null) //leaf node
                continue;
            for (Vertex i : list.keySet()) {
                long pathcost = v.pathCost /*cost upto v1*/ + findEdge(v, i).weight /*edge length (v1,v2)*/;
                long est_cost = pathcost + (type == Search.ASTAR ? heuristic.get(i) : 0); //h(v2) for A*.

                if (!pq.contains(i) && !closed.contains(i)) {
                    addtoQ = true;
                } else if (pq.contains(i) && i.est_cost > est_cost) {
                    pq.remove(i);
                    addtoQ = true;
                } else if (closed.contains(i) && i.est_cost > est_cost) {
                    closed.remove(i);
                    addtoQ = true;
                }
                if (addtoQ) {
                    i.est_cost = est_cost;
                    i.pathCost = pathcost;
                    i.parent = v;
                    i.queuePriority = ++priority; //here we update it from INT_MAX to the proper correct value.
                    pq.add(i);
                    addtoQ = false;
                }
            }

            closed.add(v); //all outdegrees explored, put back in closed. (will open if has other indegrees).
        }
        return null;
    }

    private static Edge findEdge(Vertex s, Vertex d) {
        return adjList.get(s).get(d);
    }


    private static void print(Vertex goal, Search type) throws IOException {
        Stack<Vertex> stk = new Stack<>();
        while (goal != null) {
            stk.push(goal);

            goal = goal.parent;
        }

        int distcost = 0; // for bfs/dfs;
        while (!stk.isEmpty()) {
            Vertex v = stk.pop();
            writer.write(v.name + " " + (type == Search.BFS ? distcost++ : v.pathCost) + "\n");
        }
    }

    public static void main(String args[]) {
        String algo, s_state, g_state;
        int m, n;
        try {
            Scanner s = new Scanner(new File("input.txt"));
            writer = new FileWriter(new File("output.txt"));

            vertices = new HashMap<>();

            algo = s.nextLine();
            s_state = s.nextLine();
            vertices.put(s_state, new Vertex(s_state));
            g_state = s.nextLine();
            Vertex g = vertices.get(g_state);
            if (g == null) {
                vertices.put(g_state, new Vertex(g_state)); // adding vertex v2 to vertices.
            }
            m = Integer.parseInt(s.next());
            s.nextLine();// to parse end of line for number of edges line.

            String st1, st2;
            Vertex v1, v2; // to find corresponding vertices.
            int time;


            adjList = new HashMap<>();

            //create the adjacency list
            for (int i = m; i > 0; i--) {
                st1 = s.next();
                st2 = s.next();
                v1 = vertices.get(st1);
                if (v1 == null) {
                    vertices.put(st1, v1 = new Vertex(st1)); // adding vertex v1 to vertices.
                }
                v2 = vertices.get(st2);
                if (v2 == null) {
                    vertices.put(st2, v2 = new Vertex(st2)); // adding vertex v2 to vertices.
                }
                time = s.nextInt();
                s.nextLine(); // to finish line.
                LinkedHashMap<Vertex, Edge> list = adjList.get(v1);
                if (list == null)
                    list = new LinkedHashMap<>();
                list.put(v2, new Edge(time, i));
                adjList.put(v1, list); // adding edge to edge list.
            }

            // Input Sunday List
            n = s.nextInt(); //number of vertices
            s.nextLine();
            heuristic = new HashMap<>();
            for (int i = 0; i < n; i++) {
                String v = s.next();
                int dist = s.nextInt();
                if (s.hasNextLine())
                    s.nextLine();
                heuristic.put(vertices.get(v), dist);
            }

            Vertex src = vertices.get(s_state), dst = vertices.get(g_state);
            Vertex goal;
            switch (algo) {
                case "BFS":
                    print(bfs(src, dst), Search.BFS);
                    break;
                case "DFS":
                    print(dfs(src, dst), Search.BFS);
                    break;
                case "UCS":
                    print(ucsAstar(src, dst, Search.UCS), Search.UCS);
                    break;
                case "A*":
                    print(ucsAstar(src, dst, Search.ASTAR), Search.ASTAR);
                    break;
            }
            writer.flush();
            writer.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
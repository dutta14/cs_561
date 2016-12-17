import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class Node {
    int value;
    char[][] state;
    ArrayList<Node> actions = new ArrayList<>();
    boolean hasRaided;
    int x,y;

    Node(char[][] state) {
        this.state=state;
        actions = null;
        hasRaided = false;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        for (char[] aState : state) {
            for (int j = 0; j < state.length; j++)
                str.append(aState[j]);
            str.append('\n');
        }
        return new String(str);
    }

    ArrayList<Node> getActions(char c) {
        if(actions!=null)
            return actions;
        actions = new ArrayList<>(); //to store only stakes.
        ArrayList<Node> raids = new ArrayList<>(); //to store only raids.

        int n = state.length;
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                if(state[i][j]=='.') {
                    char[][] nv = new char[n][n];
                    char[][] nv2 = new char[n][n];
                    for (int k = 0; k < n; k++) {
                        nv[k] = Arrays.copyOf(state[k], n);
                        nv2[k] = Arrays.copyOf(state[k], n);
                    }
                    nv2[i][j] = nv[i][j] = c;

                    Node random = new Node(nv);
                    random.x = i; random.y=j;
                    actions.add(random);

                    Node tryRaid = new Node(nv2);
                    tryRaid.raid(i,j,c);
                    tryRaid.x = i; tryRaid.y = j;
                    if(tryRaid.hasRaided)
                        raids.add(tryRaid);
                }
        actions.addAll(raids);
        raids.clear();
        return actions;
    }

    private boolean canRaid(int i, int j, char p) {
        try {
            return state[i][j]==p;
        } catch (Exception e) {
            return false;
        }
    }

    private void tryConquer(int i, int j, char p) {
        try {
            if(state[i][j]!=p && state[i][j]!='.') {
                state[i][j] = p;
                hasRaided = true;
            }
        } catch (Exception ignored) {}
    }

    private void raid(int i, int j, char p) {
        if(canRaid(i,j+1,p) || canRaid(i,j-1,p) || canRaid(i+1,j,p) || canRaid(i-1,j,p)) {
            tryConquer(i,j-1,p);
            tryConquer(i,j+1,p);
            tryConquer(i+1,j,p);
            tryConquer(i-1,j,p);
        }
    }
}

public class homework {

    private int[][] mCellValues;
    private char youPlay, opponent;
    private int depth;

    private void utility(Node root) {
       char[][] s = root.state;
       int sum = 0;
       for (int i = 0; i < s.length; i++)
            for (int j = 0; j < s.length; j++)
                sum += s[i][j] == 'X' ? mCellValues[i][j] : s[i][j] == 'O' ? -mCellValues[i][j] : 0;

       root.value = (youPlay == 'X') ? sum : -sum;
    }

    private int max(Node node, int d, boolean ab, int alpha, int beta) {
        if(d==0) {
            utility(node);
            return node.value;
        }
        int v = Integer.MIN_VALUE;
        for(Node a: node.getActions(youPlay)) {
            int temp = ab? min(a,d-1, true, alpha, beta): min(a,d-1,false,0,0);
            if(v<temp)
                node.value = v = temp;
            if(ab) {
                if (v >= beta) {
                    if(d!=depth) {
                        node.actions.clear(); node.actions = null;
                    }
                    return v;
                }

                alpha = Math.max(alpha, v);
            }
        }
        if(d!=depth) {
            node.actions.clear(); node.actions = null;
        }
        return v;
    }

    private int min(Node node, int d, boolean ab, int alpha, int beta) {
        if(d==0) {
            utility(node);
            return node.value;
        }
        int v = Integer.MAX_VALUE;
        for(Node a: node.getActions(opponent)) {
            int temp = ab?max(a,d-1,true, alpha, beta): max(a,d-1,false,0,0);
            if(v>temp)
                node.value = v = temp;
            if(ab) {
                if (v <= alpha) {
                    node.actions.clear();
                    node.actions = null;
                    return v;
                }
                beta = Math.min(beta, v);
            }
        }
        node.actions.clear();
        node.actions = null;
        return v;
    }

    public static void main(String args[]) {
        try {
            Scanner s = new Scanner(new File("input.txt"));
            homework o = new homework(); //create an object.

            int n = s.nextInt(); /*board width*/ s.nextLine();
            String mode = s.nextLine();
            o.youPlay = s.nextLine().charAt(0);
            o.opponent = o.youPlay =='X'?'O':'X';
            o.depth = s.nextInt();
            o.mCellValues = new int[n][n];
            for(int i=0; i<n; i++)
                for(int j=0; j<n; j++)
                   o.mCellValues[i][j] = s.nextInt();
            s.nextLine();
            char[][] state = new char[n][n];
            int dots=0;
            for(int i=0; i<n; i++) {
                String inp = s.nextLine();
                for (int j = 0; j < n; j++) {
                    state[i][j] = inp.charAt(j);
                    if (state[i][j] == '.') dots++;
                }
            }
            if(o.depth > dots)
                o.depth = dots;

            Node root = new Node(state);
            int solution = 0;
            switch(mode) {
                case "MINIMAX":  solution = o.max(root,o.depth, false, 0, 0); break;
                case "ALPHABETA": solution = o.max(root,o.depth,true, Integer.MIN_VALUE, Integer.MAX_VALUE); break;
            }
            Node sol = null;
            for(Node n1: root.actions)
                if (solution == n1.value) {
                    sol = n1;  break;
                }
            FileWriter f = new FileWriter(new File("output.txt"));
            f.write((char) (sol.y + 65) + "" + (sol.x + 1) + " " + (sol.hasRaided ? "Raid" : "Stake") + "\n");
            f.write(sol.toString());
            f.close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
}

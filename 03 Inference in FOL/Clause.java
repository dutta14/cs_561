import java.util.ArrayList;
import java.util.Stack;

class Clause {
    ArrayList<Atom> atoms;

    Clause(String c) {
        atoms = new ArrayList<>();
        segregate(c);
    }

    Clause(Clause c) {
        atoms = new ArrayList<>();
        for(Atom i: c.atoms) {
            atoms.add(new Atom(i));
        }
    }

    void segregate(String s) {
        String[] atomArr = s.split("\\|");
        for(String i: atomArr)
            atoms.add(new Atom(removeExtraPar(i).trim()));
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        for(Atom i: atoms)
            str.append(i+" | ");
        str.deleteCharAt(str.lastIndexOf("|"));
        return new String(str);
    }

    private String removeExtraPar(String s) {
        Stack<Integer> openPar = new Stack<>(), closePar = new Stack<>();
        StringBuilder str = new StringBuilder(s);
        for(int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if(c=='(')
                openPar.push(i);
            else if(c==')') {
                if(openPar.isEmpty())
                    closePar.push(i);
                else
                    openPar.pop();
            }
        }
        while(!closePar.isEmpty())
            str.deleteCharAt(closePar.pop());
        while(!openPar.isEmpty())
            str.deleteCharAt(openPar.pop());
        return str.toString();
    }

    public static void main(String args[]) {
        System.out.println(new Clause("A(x)").removeExtraPar("((A(x)))"));
    }
}

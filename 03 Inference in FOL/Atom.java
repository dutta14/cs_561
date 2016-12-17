class Atom {
    String op;
    String[] args;
    boolean neg;

    Atom(String atom) {
        neg = atom.contains("~");
        atom = atom.substring(neg?1:0, atom.length()-1);
        int index =  atom.indexOf('(');
        op = atom.substring(0, index).trim();
        atom = atom.substring(index+1);
        args = atom.split(",");
        for(int i=0; i<args.length; i++)
            args[i] = args[i].trim();
    }

    Atom(Atom a) {
        op = a.op;
        neg = a.neg;
        args = new String[a.args.length];
        for(int i=0; i<args.length; i++)
            args[i] = a.args[i];
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(neg?"~":"");
        str.append(op);
        str.append("(");
        for(String i: args) {
            str.append(i).append(",");
        }
        str.deleteCharAt(str.length() - 1);
        str.append(")");
        return new String(str);
    }

    public static void main(String args[]) {
        System.out.println(new Atom("A(x)"));
    }
}

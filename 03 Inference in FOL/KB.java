import java.util.ArrayList;

class KB {
    ArrayList<Clause> mKb;
    ArrayList<Clause> sKb;
    ArrayList<String> input;

    KB(ArrayList<String> i) {
        input = i;
        mKb = new ArrayList<>();
        sKb = new ArrayList<>();
        makeKB();
    }

    void makeKB() {
        for(String i: input) {
            String[] clauses = i.split("&");
            for(String j: clauses) {
                Clause c = new Clause(j);
                if(c.atoms.size() == 1)
                    sKb.add(c);
                else
                    mKb.add(c);
            }
        }
    }
}

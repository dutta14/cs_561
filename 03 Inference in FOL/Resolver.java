import java.util.HashMap;
import java.util.HashSet;

class Resolver {

    KB kb;
    String mQuery;
    HashSet<String> s;

    Resolver(KB k, String q) {
        kb = k;
        s = new HashSet<>();
        mQuery = q;
        kb.sKb.add(new Clause(mQuery));
    }

    static boolean isConstant(String str) {
        return Character.isUpperCase(str.charAt(0));
    }

    static boolean isVariable(String str) {
        return !isConstant(str);
    }

    static Atom canUnify(Atom j, Clause c2) {
        for(Atom i: c2.atoms)
            if(i.op.equals(j.op) && i.neg != j.neg) {
                for(int k=0; k<j.args.length; k++)
                    if(isConstant(j.args[k]) && isConstant(i.args[k]) && !i.args[k].equals(j.args[k]))
                        return null;

                return i;
            }
        return null;
    }

    static void substitute(Clause c2, Atom clauseAtom, Atom singleAtom) {
       HashMap<String,String> theta = new HashMap<>();
       for(int i=0; i<clauseAtom.args.length; i++) {
            if(isConstant(clauseAtom.args[i]) && isConstant(singleAtom.args[i]) && !singleAtom.args[i].equals(clauseAtom.args[i])) {
                return;
            }
        }

        for(int i=0; i<clauseAtom.args.length; i++) {
            String aS = clauseAtom.args[i];
            String cS = singleAtom.args[i];

            if(isVariable(cS) && isConstant(aS) && !theta.containsValue(aS))
                theta.put(cS,aS);
            else if(isConstant(cS) && isVariable(aS) && !theta.containsValue(cS))
                theta.put(aS,cS);
            else if(isVariable(cS) && isVariable(aS))
                theta.put(aS,cS);
        }

        for(int k=0; k<c2.atoms.size(); k++) {
            Atom atom = c2.atoms.get(k);
            for(int i=0; i<atom.args.length; i++) {
                String v = theta.get(atom.args[i]);
                if(v!=null)
                    atom.args[i] =v;
            }
        }

        for(int k=0; k<c2.atoms.size(); k++) {
            Atom atom = c2.atoms.get(k);
            if(atom.op.equals(clauseAtom.op) && atom.neg==clauseAtom.neg) {
                c2.atoms.remove(atom);
                break;
            }
        }
    }

    boolean canResolve(Atom a, Atom b) {
        if(!a.op.equals(b.op))
            return false;
        if(a.neg == b.neg)
            return false;
        for(int i=0; i<a.args.length; i++) {
            if(isConstant(a.args[i]) && isConstant(b.args[i]) && !a.args[i].equals(b.args[i]))
                return false;
        }
        return true;
    }

    boolean resolution() {
        for(int i=0; i<kb.sKb.size(); i++) {
            Atom c1 = kb.sKb.get(i).atoms.get(0);
            for(int j=i+1; j<kb.sKb.size(); j++) {
                Atom c2 = kb.sKb.get(j).atoms.get(0);
                if(c1.op.equals(c2.op) && c1.neg!=c2.neg) {
                    int k=0;
                    for(; k<c1.args.length; k++) {
                        if(isConstant(c1.args[k]) && isConstant(c2.args[k]) && !c1.args[k].equals(c2.args[k]))
                            break;
                    }
                    if(k==c1.args.length)
                        return true;
                }
            }
        }

        for(int i=0; i< kb.sKb.size(); i++) {
            Clause c1 = kb.sKb.get(i);
            for(int j=0; j< kb.mKb.size(); j++) {
                Clause c2 = kb.mKb.get(j);
                if(s.contains(c1.toString()+","+c2.toString()))
                    continue;
                else
                    s.add(c1.toString() + "," + c2.toString());
                Atom a = canUnify(c1.atoms.get(0),c2);
                if(a == null)
                    s.remove(c1.toString() + "," + c2.toString());
                if(a != null) {
                    if(c2.atoms.size() == 1)
                        return true;
                    Clause t = new Clause(c2);
                    substitute(t,a,c1.atoms.get(0));
                    if(t.atoms.size()>1) {
                        kb.mKb.add(t);
                    }
                    else if(t.atoms.size()==1) {
                        for(int l=0; l<kb.sKb.size(); l++) {
                            Clause k = kb.sKb.get(l);
                            Atom atom = k.atoms.get(0);
                             if(canResolve(atom,t.atoms.get(0))) {
                                 return true;
                             }
                        }
                        kb.sKb.add(t);
                        break;
                    }
                }
            }
       }
       return false;
    }
}

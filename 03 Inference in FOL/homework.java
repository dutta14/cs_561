import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class homework {

    public static void main(String args[]) {

        ArrayList<String> list = new ArrayList<>(),
                qlist = new ArrayList<>();
        try (Scanner s = new Scanner(new File("input.txt"))) {
            int q = s.nextInt(); s.nextLine();
            for(int i=0; i<q; i++) {
                String query = s.nextLine();
                query = query.contains("~")?query.substring(1,query.length()):"(~"+query+")";
                qlist.add(query);
            }

            s.nextInt(); s.nextLine();
            while(s.hasNextLine()) {
                list.add(s.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<String> nlist = new ArrayList<>();
        for(String i:list) {
            ArrayList<String> t = new CNF(i).makeCNF();
            nlist.addAll(t);
        }
        try {
            FileWriter f = new FileWriter("output.txt");
            for(String q: qlist) {
                KB kb = new KB(nlist);
                String r = new CNF(q).makeCNF().get(0);
                Resolver resolver = new Resolver(kb, r);
                int s = kb.sKb.size();
                int m = kb.mKb.size();
                boolean result;
                while (true) {
                    result = resolver.resolution();
                    if (result) {
                        break;
                    }
                    if (kb.sKb.size() == s && kb.mKb.size() == m) {
                        break;
                    }
                    s = kb.sKb.size();
                    m = kb.mKb.size();
                }
                f.write(result ? "TRUE\n" : "FALSE\n");
            }
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

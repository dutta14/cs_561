import java.util.*;

class CNF {

    private class TreeNode {
        String operator;
        TreeNode left;
        TreeNode right;

        private TreeNode(String op, TreeNode left, TreeNode right) {
            this.operator = op;
            this.left = left;
            this.right = right;
        }

        private void inorder() {
            if(null != left)
                left.inorder();
            if(left == null && right == null){
                if(operator.contains("~~"))
                    operator = operator.substring(2);
                if(operator.contains("~"))
                    mCNF.append("~").append(mMap.get(operator.substring(1)));
                else
                    mCNF.append(mMap.get(operator));
            } else
                mCNF.append(operator);
            if(null != right)
              right.inorder();
        }

        private void removeImplies() {
            if(left != null)
               left.removeImplies();
            if(operator.equals(">")){
                operator ="|";
                if (left != null)
                    left.deMorgan();
            }
            if(right != null)
               right.removeImplies();
        }

        private void moveNegativeIn() {
            if(left !=null)
              left.moveNegativeIn();
            if(operator.equals("~")) {
                right.deMorgan();
                copy(right);
                moveNegativeIn();
            }
            if(right !=null)
              right.moveNegativeIn();
        }

        private void deMorgan() {
            boolean flag=false;
            if(left !=null)
              left.deMorgan();
            switch(operator) {
                case "&": operator ="|"; break;
                case "|": operator ="&"; break;
                case "~": flag=true; break;
                default: operator ="~"+ operator; break;
            }

            if (flag) {
                right.removeImplies();
                copy(right);
            }
            else if(right != null) {
                right.deMorgan();
            }
        }

        private void distAndOverOr() {
            if(left != null)
               left.distAndOverOr();
            if (operator.equals("|")) {
                if (right.operator.equals("&"))
                    orAnd();
                else if (left.operator.equals("&"))
                    andOr();
                else if (left.operator.equals("&") && right.operator.equals("&"))
                    andAnd();
                if(right != null)
                  right.distAndOverOr();
            }
        }

        private void andOr() {
            TreeNode temp = right;
            right = new TreeNode("|", left.right,temp);
            left.right = temp;
            operator ="&";
            left.operator ="|";
        }

        private void orAnd() {
            TreeNode temp = left;
            left = new TreeNode("|",temp, right.left);
            right.left = temp;
            operator ="&";
            right.operator ="|";
        }

        private void andAnd() {
            TreeNode w = left.left,
                 x = left.right,
                 y = right.left,
                 z = right.right;

            left = new TreeNode(left.operator, new TreeNode("|",w,y), new TreeNode("|",w,z));
            right = new TreeNode(right.operator, new TreeNode("|",x,y), new TreeNode("|",x,z));
            operator = "&";
        }

        private void copy(TreeNode t) {
            operator = t.operator;
            left = t.left;
            right = t.right;
        }
    }

    private TreeNode mExpression;
    private HashMap<String, String> mMap;
    private StringBuilder mCNF;

    CNF(String str) {
        mMap = new HashMap<>();
        mCNF = new StringBuilder();
        mExpression = evaluate(makeExpr(str));
    }

    private String makeExpr(String str) {
        char[] array = str.toCharArray();
        int i = 0, exprNo = 1;
        while(i < array.length){
            StringBuilder temp = new StringBuilder();
            if(Character.isUpperCase(array[i])){
                temp.append((i>0 && array[i-1]=='~')?'~':"");
                while(array[i] != ')')
                    temp.append(array[i++]);
                temp.append(')');
                String exprN = Integer.toString(exprNo), tempN = new String(temp);
                mMap.put(exprN, tempN);
                str = str.replace(tempN,exprN);
                exprNo++;
            }
            i++;
        }
        str = str.replace("=>", ">");
        return str;
    }

    private TreeNode evaluate(String eval) {
        char[] tokens = eval.toCharArray();

        Stack<TreeNode> operands = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < tokens.length; i++)
            if (Character.isDigit(tokens[i])) {  //atoms
                StringBuilder sbuf = new StringBuilder();
                while (i < tokens.length && Character.isDigit(tokens[i]))
                    sbuf.append(tokens[i++]);
                i--;
                operands.push(new TreeNode(sbuf.toString(), null, null));
            } else if (tokens[i] == ')') { //start popping to make new node.
                while (operators.peek() != '(') {
                    char op = operators.pop();
                    TreeNode b = operands.pop(), a = operands.pop();
                    operands.push(new TreeNode(op+"",a,b));
                }
                operators.pop();
                if (!operators.isEmpty() && operators.peek() == '~') {
                    char op = operators.pop();
                    TreeNode a = operands.pop();
                    operands.push(new TreeNode(op+"",null,a)); //only right child will contain element.
                }
            } else if ("|&>".contains(tokens[i] + "")) {
                while (!operators.empty() && notParen(operators.peek())) {
                    char op = operators.pop();
                    TreeNode b = operands.pop(), a = operands.pop();
                    operands.push(new TreeNode(op+"",a,b));
                }
                operators.push(tokens[i]);
            } else if (tokens[i] == '~' || tokens[i] == '(') {
                operators.push(tokens[i]);
            }

        while (!operators.empty()) {
            char op = operators.pop();
            TreeNode b = operands.pop(), a=operands.pop();
            operands.push(new TreeNode(op+"",a,b));
        }
        return operands.pop();
    }

    private boolean notParen(char op) {
        return !(op == '(' || op == ')');
    }

    ArrayList<String> makeCNF() {
        mExpression.removeImplies();
        mExpression.moveNegativeIn();
        mExpression.inorder();
        int l = mCNF.toString().split("&").length;
        mCNF = new StringBuilder();
        while(l-->=0)
            mExpression.distAndOverOr();
        mExpression.inorder();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(mCNF.toString().replace("~~", "").split("&")));
        return list;
    }
}
import java.util.LinkedList;

public class Tableaux {

    Branch start;
    LinkedList<Branch> branches;

    LinkedList<Branch> unfinishedBranches;

    public Tableaux(){        
        branches = new LinkedList<>();
        unfinishedBranches = new LinkedList<>();
    }

    public void doTableaux(Expression startExpression) {
        start = new Branch(new TableauxPart(startExpression, null), null);
        unfinishedBranches.add(start);

        while(!unfinishedBranches.isEmpty()){
            Branch current = unfinishedBranches.getFirst();

            LinkedList<Branch> result = current.nextStep();
            if(result == null){
                branches.add(current);
                unfinishedBranches.remove(current);
            }
            else{
                for(int i = 0; i < result.size();i++){
                    if(!unfinishedBranches.contains(result.get(i))){
                        unfinishedBranches.add(result.get(i));
                    }
                }
            }
        }
    }

    public Boolean isValid(){
        return false;
    }

    public String toString(){
        String s = "";
        for(int i = 0; i < branches.size();i++){
            s += "\n"+branches.get(i).toString();
        }
        return s;
    }

}

class Branch {

    LinkedList<TableauxPart> tableauxParts;
    LinkedList<EndCase> endcases;
    LinkedList<TableauxPart> upNext;
    Branch from;

    public Branch(TableauxPart startTab, Branch from){
        this.tableauxParts = new LinkedList<>();
        this.tableauxParts.add(startTab);

        this.upNext = new LinkedList<>();
        upNext.add(startTab);

        endcases = new LinkedList<>();
        this.from = from;
    }

    public LinkedList<Branch> nextStep(){

        LinkedList<Branch> returnBranches = new LinkedList<>();

        if(upNext.isEmpty()){
            return null;
        }

        TableauxPart current = upNext.getFirst();
        upNext.removeFirst();

        Expression expr = current.expr;
        ExpressionTypes type = expr.getType();

        if(isEndcase(expr)){
            endcases.add(new EndCase(expr));
            returnBranches.add(this);
            return returnBranches;
        }

        switch (type) {
            case AND:
                And and = (And)expr;
                TableauxPart andLeft = new TableauxPart(and.propositionLeft, current);
                TableauxPart andRight = new TableauxPart(and.propositionRight, current);
                upNext.add(andLeft);
                upNext.add(andRight);
                tableauxParts.add(andLeft);
                tableauxParts.add(andRight);
                returnBranches.add(this);
                break; 
            case OR:
                Or or = (Or)expr;
                TableauxPart orLeft = new TableauxPart(or.propositionLeft, current);
                TableauxPart orRight = new TableauxPart(or.propositionRight, current); 
                returnBranches.add(this);
                returnBranches.add(new Branch(orLeft, this));   
                returnBranches.add(new Branch(orRight, this));  
                break;
            default:
                System.err.println("Encountered an unexpeted ExpressionTypes: "+type);
                return null;
        }

        return returnBranches;
    }

    public Boolean isEndcase(Expression e){
        if(e.getType() == ExpressionTypes.NOT){
            e = ((Not)e).proposition;
        }
        Boolean isLoneNominal = e.getType() == ExpressionTypes.NOMINAL;
        Boolean isLoneProp = e.getType() == ExpressionTypes.PROPOSITIONAL_SYMBOL;
        return isLoneNominal || isLoneProp;
    }

    public String toString(){
        String s = "New branch:";
        for(int i = 0; i < tableauxParts.size();i++){
            s += "\n"+tableauxParts.get(i).toString();
        }
        return s;
    }
    
}

class TableauxPart {
    Expression expr;
    TableauxPart from;

    public TableauxPart(Expression e, TableauxPart from){
        this.expr = e;
        this.from = from;
    }

    public String toString(){
        return expr.toString();
    }
}

class EndCase {
    Expression expr;
    String id;
    Boolean isNot = false;

    public EndCase(Expression e){
        this.expr = e;
        if(e.getType() == ExpressionTypes.NOT){
            id = getId(((Not)e).proposition);
            isNot = true;
        } else id = getId(e);

    }

    public String getId(Expression e){
        switch (e.getType()) {
            case NOMINAL:
                return ((Nominal)e).identifier;
            case PROPOSITIONAL_SYMBOL:
                return ((Prop_symbol)e).identifier;
            default:
                return "";
        }
    }

    public Boolean contradicts(EndCase otherEndcase){
        if(id.equals(otherEndcase.id)){
            return isNot != otherEndcase.isNot;
        }
        return false;
    }

}
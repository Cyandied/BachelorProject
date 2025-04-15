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
        start = new Branch(new TableauxPart(startExpression, null,null), null);
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
        String s = "\n";
        for(int i = 0; i < branches.size();i++){
            s += "Branch nr: "+i;
            s += branches.get(i).toString();
            s += "\n";
        }
        return s;
    }

}

class Branch {

    LinkedList<TableauxPart> tableauxParts;
    LinkedList<EndCase> endcases;
    LinkedList<TableauxPart> upNext;
    TableauxPart currentStep;
    Branch from;

    public Branch(TableauxPart startTab, Branch from){
        this.tableauxParts = new LinkedList<>();
        this.tableauxParts.add(startTab);
        this.currentStep = startTab;

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
                TableauxPart andLeft = new TableauxPart(and.propositionLeft, this.currentStep,current);
                TableauxPart andRight = new TableauxPart(and.propositionRight, andLeft,current);
                this.currentStep = andRight;
                upNext.add(andLeft);
                upNext.add(andRight);
                tableauxParts.add(andLeft);
                tableauxParts.add(andRight);
                returnBranches.add(this);
                break; 
            case OR:
                Or or = (Or)expr;
                TableauxPart orLeft = new TableauxPart(or.propositionLeft, this.currentStep,current);
                TableauxPart orRight = new TableauxPart(or.propositionRight, this.currentStep,current); 
                returnBranches.add(this);
                returnBranches.add(new Branch(orLeft, this));   
                returnBranches.add(new Branch(orRight, this));  
                break;
            case SATISFIER:
                Expression satisInner = ((Satisfier)expr).proposition;
                Nominal satisNominal = (Nominal)((Satisfier)expr).referencePoint;
                switch (satisInner.getType()) {
                    case NOT:
                        Not statisNot = (Not)satisInner;
                        TableauxPart notSatis = new TableauxPart(new Not(new Satisfier(satisNominal, statisNot.proposition)), this.currentStep,current);
                        this.currentStep = notSatis;
                        returnBranches.add(this);
                        upNext.add(notSatis);
                        tableauxParts.add(notSatis);
                        break;
                    case AND:
                        And satisAnd = (And)satisInner;
                        TableauxPart andSatisLeft = new TableauxPart(new Satisfier(satisNominal, satisAnd.propositionLeft), this.currentStep,current);
                        TableauxPart andSatisRight = new TableauxPart(new Satisfier(satisNominal, satisAnd.propositionRight), andSatisLeft,current);
                        this.currentStep = andSatisLeft;
                        upNext.add(andSatisLeft);
                        tableauxParts.add(andSatisLeft);
                        upNext.add(andSatisRight);
                        tableauxParts.add(andSatisRight);
                        returnBranches.add(this);
                        break;
                    case OR:
                        Or satisOr = (Or)satisInner;
                        TableauxPart orSatisLeft = new TableauxPart(new Satisfier(satisNominal, satisOr.propositionLeft), this.currentStep,current);
                        TableauxPart orSatisRight = new TableauxPart(new Satisfier(satisNominal, satisOr.propositionRight), this.currentStep,current);
                        returnBranches.add(this);
                        returnBranches.add(new Branch(orSatisLeft, this));
                        returnBranches.add(new Branch(orSatisRight, this));
                        break;
                    case SATISFIER:
                        TableauxPart satis = new TableauxPart(satisInner, this.currentStep,current);
                        this.currentStep = satis;
                        upNext.add(satis);
                        tableauxParts.add(satis);
                        returnBranches.add(this);
                        break;
                    case DIAMOND:
                        Diamond diamond = (Diamond)satisInner;
                        boolean diamondHasLoneNominal = diamond.proposition.getType() == ExpressionTypes.NOMINAL;
                        boolean branchHasDiamondRuleExpression = this.currentStep.seekDiamondRuleExpression(diamond.proposition,satisNominal.identifier,false);
                        if(!diamondHasLoneNominal && !branchHasDiamondRuleExpression){
                        String id = ((Nominal)satisNominal).identifier;
                        TableauxPart newNominalSatis = new TableauxPart(new Satisfier(new Nominal(id+id),diamond.proposition), this.currentStep,current);
                        TableauxPart newDiamondNominal = new TableauxPart(new Satisfier(satisNominal,new Diamond(new Nominal(id+id))), newNominalSatis,current);
                        this.currentStep = newDiamondNominal;
                        upNext.add(newNominalSatis);
                        tableauxParts.add(newNominalSatis);
                        tableauxParts.add(newDiamondNominal);
                        }
                        returnBranches.add(this);
                        break;
                    default:
                        System.err.println("Attempted to break down: "+current.expr+" but encountered an unexpeted ExpressionType in Satisfier(...): "+satisInner.getType());
                        this.currentStep = current;
                        returnBranches.add(this);
                        break;
                }
            break;
            case NOT:
                Expression notInner = ((Not)expr).proposition;
                switch (notInner.getType()) {
                    case NOT:
                        Not not = (Not)notInner;
                        TableauxPart prop = new TableauxPart(not.proposition, this.currentStep,current);
                        this.currentStep = prop;
                        returnBranches.add(this);
                        upNext.add(prop);
                        tableauxParts.add(prop);
                        break;
                    case AND:
                        And and2 = (And)notInner;
                        TableauxPart notAndLeft = new TableauxPart(new Not(and2.propositionLeft), this.currentStep,current);
                        TableauxPart notAndRight = new TableauxPart(new Not(and2.propositionRight), this.currentStep,current);
                        returnBranches.add(this);
                        returnBranches.add(new Branch(notAndLeft, this));   
                        returnBranches.add(new Branch(notAndRight, this));  
                        break;
                    case OR:
                        Or or2 = (Or)notInner;
                        TableauxPart notOrLeft = new TableauxPart(new Not(or2.propositionLeft), this.currentStep,current);
                        TableauxPart notOrRight = new TableauxPart(new Not(or2.propositionRight), notOrLeft,current);
                        this.currentStep = notOrRight;
                        upNext.add(notOrLeft);
                        upNext.add(notOrRight);
                        tableauxParts.add(notOrLeft);
                        tableauxParts.add(notOrRight);
                        returnBranches.add(this);
                        break; 
                    case SATISFIER:
                        Satisfier satisfier2 = (Satisfier)notInner;
                        if (satisfier2.proposition.getType() == ExpressionTypes.NOT) {
                            Not notProp = (Not)satisfier2.proposition;
                            TableauxPart notNotSatis = new TableauxPart(new Satisfier(satisfier2.referencePoint, notProp.proposition), this.currentStep,current);
                            this.currentStep = notNotSatis;
                            upNext.add(notNotSatis);
                            tableauxParts.add(notNotSatis);
                            returnBranches.add(this);
                        }
                        else if (satisfier2.proposition.getType() == ExpressionTypes.AND){
                            And andProp = (And)satisfier2.proposition;
                            TableauxPart notSatisAndLeft = new TableauxPart(new Not(new Satisfier(satisfier2.referencePoint, andProp.propositionLeft)), this.currentStep,current);
                            TableauxPart notSatisAndRight = new TableauxPart(new Not(new Satisfier(satisfier2.referencePoint, andProp.propositionRight)), this.currentStep,current);
                            returnBranches.add(this);
                            returnBranches.add(new Branch(notSatisAndLeft, this));   
                            returnBranches.add(new Branch(notSatisAndRight, this));  
                        }
                        else if (satisfier2.proposition.getType() == ExpressionTypes.OR){
                            Or orProp = (Or)satisfier2.proposition;
                            TableauxPart notSatisOrLeft = new TableauxPart(new Not(new Satisfier(satisfier2.referencePoint, orProp.propositionLeft)), this.currentStep,current);
                            TableauxPart notSatisOrRight = new TableauxPart(new Not(new Satisfier(satisfier2.referencePoint, orProp.propositionRight)), notSatisOrLeft,current);
                            this.currentStep = notSatisOrRight;
                            upNext.add(notSatisOrLeft);
                            tableauxParts.add(notSatisOrLeft);
                            upNext.add(notSatisOrRight);
                            tableauxParts.add(notSatisOrRight);
                            returnBranches.add(this);
                        }
                        else if (satisfier2.proposition.getType() == ExpressionTypes.SATISFIER){
                            Satisfier satisProp = (Satisfier)satisfier2.proposition;
                            TableauxPart notSatis = new TableauxPart(new Not(satisProp), this.currentStep,current);
                            this.currentStep = notSatis;
                            upNext.add(notSatis);
                            tableauxParts.add(notSatis);
                            returnBranches.add(this);
                        }
                        else if(satisfier2.proposition.getType() == ExpressionTypes.DIAMOND){
                            Diamond notSatisDiamond = (Diamond) satisfier2.proposition;
                            Nominal desiredNominal = (Nominal)satisfier2.referencePoint;
                            Nominal result = this.currentStep.seekNotDiamondRule(desiredNominal.identifier);
                            if(result != null){
                                TableauxPart notSatisOldNominal = new TableauxPart(new Not(new Satisfier(result,notSatisDiamond.proposition)), this.currentStep, current);
                                this.currentStep = notSatisOldNominal;
                                upNext.add(notSatisOldNominal);
                                tableauxParts.add(notSatisOldNominal);
                            }
                            returnBranches.add(this);
                        }
                        else {
                            System.err.println("Attempted to break down: "+current.expr+" but encountered unexpected Expression type in Not(Satisfier(...)): "+satisfier2.proposition.getType());
                            this.currentStep = current;
                            returnBranches.add(this);
                            break;
                        }
                    break;
                    default:
                        System.err.println("Attempted to break down: "+current.expr+"but encountered an unexpeted ExpressionType in Not(...):" + notInner.getType());
                        this.currentStep = current;
                        returnBranches.add(this);
                        break;
                }
            break;
            default:
                System.err.println("Attempted to break down: "+current.expr+"but encountered an unexpeted ExpressionType: "+type);
                this.currentStep = current;
                returnBranches.add(this);
                break;
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
        LinkedList<String> s = new LinkedList<>();
        TableauxPart next = this.currentStep;
        while (next != null) {
            s.add(next.toString());
            if(tableauxParts.contains(next.from)){
                next = next.from;
            } else next = null;
        }
        String string = "";
        for(int i = s.size()-1;i>=0;i--){
            string += "\n "+s.get(i);
        }
        return string;
    }
    
}

class TableauxPart {
    Expression expr;
    TableauxPart from;
    TableauxPart source;

    public TableauxPart(Expression e, TableauxPart from,TableauxPart source){
        this.expr = e;
        // From is a reference to the "step" it is from, it has nothing to do with what expression the current tableauxPart is a subexpression of
        this.from = from;
        // Souce is the expression this tableaux part is made from
        this.source = source;
    }

    public String toString(){
        return expr.toString();
    }

    public boolean seekExpressionInBranch(Expression model){
        if(expr.compare(model)){
            return true;
        }
        if(from == null){
            return false;
        }
        return from.seekExpressionInBranch(model);
    }

    public boolean seekDiamondRuleExpression(Expression target,String selfId,Boolean rightTargetRightNominal){
        Boolean flag = rightTargetRightNominal;
        if(expr.getType() == ExpressionTypes.SATISFIER){
            Satisfier inner = (Satisfier)expr;
            Boolean isOwnNominal = ((Nominal)inner.referencePoint).identifier.equals(selfId);
            if(inner.proposition.compare(target) && !isOwnNominal && rightTargetRightNominal){
                return true;
            }
            else if(inner.proposition.compare(target) && isOwnNominal){
                flag = true;
            }
        }
        else if(expr.getType() == ExpressionTypes.NOT){
            Not inner = (Not)expr;
            if(inner.proposition.getType() == ExpressionTypes.SATISFIER){
                Satisfier notInner = (Satisfier)inner.proposition;
                Boolean isOwnNominal = ((Nominal)notInner.referencePoint).identifier.equals(selfId);
                if(notInner.proposition.compare(target) && !isOwnNominal && rightTargetRightNominal){
                    return true;
                }
                else if(notInner.proposition.compare(target) && isOwnNominal){
                    flag = true;
                }
            }
        }
        if(from == null){
            return false;
        }
        return from.seekDiamondRuleExpression(target,selfId,flag);
    }

    public Nominal seekNotDiamondRule(String nominalID){
        if(expr.getType() == ExpressionTypes.SATISFIER){
            Satisfier inner = (Satisfier)expr;
            Nominal nominal = (Nominal)inner.referencePoint;
            if(nominal.identifier.equals(nominalID) && inner.proposition.getType() == ExpressionTypes.DIAMOND){
                Diamond innerInner = (Diamond)inner.proposition;
                if(innerInner.proposition.getType() == ExpressionTypes.NOMINAL){
                    return (Nominal)innerInner.proposition;
                }
            }
        }
        if(from == null){
            return null;
        }
        return from.seekNotDiamondRule(nominalID);
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
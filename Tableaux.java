import java.util.HashMap;
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
        start = new Branch(new TableauxPart(startExpression, null,null,null), null);
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

    HashMap<Expression,LinkedList<TableauxRules>> exprRulesApplied;
    LinkedList<String> nomsInBranch;

    boolean notEInEffect = false;
    LinkedList<TableauxPart> origNotEParts;

    public Branch(TableauxPart startTab, Branch from){
        this.tableauxParts = new LinkedList<>();
        this.tableauxParts.add(startTab);
        this.currentStep = startTab;

        this.upNext = new LinkedList<>();
        upNext.add(startTab);

        endcases = new LinkedList<>();
        this.from = from;

        if(this.from == null){
            exprRulesApplied = new HashMap<Expression,LinkedList<TableauxRules>>() {};
            nomsInBranch = new LinkedList<>();
            origNotEParts = new LinkedList<>();
        } else {
            exprRulesApplied = this.from.exprRulesApplied;
            nomsInBranch = this.from.nomsInBranch;
            origNotEParts = this.from.origNotEParts;
        }
    }

    public void documentRule(Expression expr, TableauxRules ruleApplied){
        exprRulesApplied.get(expr).add(ruleApplied);
    }

    public void applyAnd(Expression parent, Expression left, Expression right, TableauxPart current,LinkedList<Branch> returnBranches,TableauxRules ruleApplied){
        documentRule(parent, ruleApplied);
        TableauxPart andLeft = new TableauxPart(left, this.currentStep,current,ruleApplied);
        TableauxPart andRight = new TableauxPart(right, andLeft,current,ruleApplied);
        this.currentStep = andRight;
        this.upNext.add(andLeft);
        this.upNext.add(andRight);
        this.tableauxParts.add(andLeft);
        this.tableauxParts.add(andRight);
        returnBranches.add(this);
    }

    public void applyOr(Expression parent, Expression left, Expression right, TableauxPart current,LinkedList<Branch> returnBranches,TableauxRules ruleApplied){
        documentRule(parent, ruleApplied);
        TableauxPart orLeft = new TableauxPart(left, this.currentStep,current,ruleApplied,this.currentStep.lineNumber+1);
        TableauxPart orRight = new TableauxPart(right, this.currentStep,current,ruleApplied,this.currentStep.lineNumber+2); 
        returnBranches.add(this);
        returnBranches.add(new Branch(orLeft, this));   
        returnBranches.add(new Branch(orRight, this));  
    }

    public void applyNotE(TableauxPart origNotEPart, Nominal nominal){
        Expression origNotEExpr = null;
        try {
            origNotEExpr = ((E)((Satisfier)((Not)origNotEPart.expr).proposition).proposition).proposition;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        TableauxPart newNotE = new TableauxPart(new Not(new Satisfier(nominal, origNotEExpr)), this.currentStep, origNotEPart,TableauxRules.NOTE);
        documentRule(origNotEPart.expr, TableauxRules.NOTE);
        this.currentStep = newNotE;
        this.tableauxParts.add(newNotE);
        upNext.add(newNotE);
    }

    public void newNominal(String id,TableauxPart from, TableauxPart source){
        nomsInBranch.add(id);
        TableauxPart ref = new TableauxPart(new Satisfier(new Nominal(id), new Nominal(id)), from, source, TableauxRules.REF);
        this.currentStep = ref;
        this.tableauxParts.add(ref);
        if(notEInEffect){
            for(int i = 0;i<origNotEParts.size();i++){
                applyNotE(origNotEParts.get(i), new Nominal(id));
            }
        }
    }

    public void applyNomRule(TableauxPart target,Expression innerPart,String targetId){
        TableauxRules rule = innerPart.getType() == ExpressionTypes.DIAMOND ? TableauxRules.NOM2 : TableauxRules.NOM1;
        LinkedList<String> foundNoms = this.currentStep.seekAllNominalRelations(targetId, null);
        if(foundNoms.size() > 0){
            for(int i = 0; i<foundNoms.size();i++){
                TableauxPart newRelation = new TableauxPart(new Satisfier(new Nominal(foundNoms.get(i)), innerPart), this.currentStep, target, rule);
                if(this.currentStep.seekExpressionInBranch(new Satisfier(new Nominal(foundNoms.get(i)), innerPart))){
                    return;
                }
                documentRule(target.expr, rule);
                this.currentStep = newRelation;
                this.tableauxParts.add(newRelation);
                this.upNext.add(newRelation);
            }
        }
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

        exprRulesApplied.put(expr, new LinkedList<>());

        if(isEndcase(expr)){
            endcases.add(new EndCase(expr));
            returnBranches.add(this);
            return returnBranches;
        }

        switch (type) {
            case AND:
                And and = (And)expr;
                applyAnd(expr,and.propositionLeft, and.propositionRight, current, returnBranches,TableauxRules.AND);
                break; 
            case OR:
                Or or = (Or)expr;
                applyOr(expr,or.propositionLeft, or.propositionRight, current, returnBranches,TableauxRules.OR);
                break;
            case SATISFIER:
                Expression satisProp = ((Satisfier)expr).proposition;
                Nominal satisNominal = (Nominal)((Satisfier)expr).referencePoint;
                if(!nomsInBranch.contains(satisNominal.identifier)){
                    newNominal(satisNominal.identifier, current, current);
                }
                switch (satisProp.getType()) {
                    case NOT:
                        Not statisNot = (Not)satisProp;
                        TableauxPart notSatis = new TableauxPart(new Not(new Satisfier(satisNominal, statisNot.proposition)), this.currentStep,current,TableauxRules.NOT);
                        documentRule(expr, TableauxRules.NOT);
                        this.currentStep = notSatis;
                        returnBranches.add(this);
                        upNext.add(notSatis);
                        tableauxParts.add(notSatis);
                        break;
                    case AND:
                        And satisAnd = (And)satisProp;
                        applyAnd(expr,new Satisfier(satisNominal, satisAnd.propositionLeft), new Satisfier(satisNominal, satisAnd.propositionRight), current, returnBranches,TableauxRules.AND);
                        break;
                    case OR:
                        Or satisOr = (Or)satisProp;
                        applyOr(expr,new Satisfier(satisNominal, satisOr.propositionLeft), new Satisfier(satisNominal, satisOr.propositionRight), current, returnBranches,TableauxRules.OR);
                        break;
                    case SATISFIER:
                        TableauxPart satisSatis = new TableauxPart(satisProp, this.currentStep,current,TableauxRules.SATIS);
                        documentRule(expr, TableauxRules.SATIS);
                        this.currentStep = satisSatis;
                        upNext.add(satisSatis);
                        tableauxParts.add(satisSatis);
                        returnBranches.add(this);
                        break;
                    case DIAMOND:
                        Diamond diamond = (Diamond)satisProp;
                        boolean diamondHasLoneNominal = diamond.proposition.getType() == ExpressionTypes.NOMINAL;
                        boolean branchHasDiamondRuleExpression = this.currentStep.seekLoopCondition(diamond.proposition,satisNominal.identifier,false);
                        if(!diamondHasLoneNominal && !branchHasDiamondRuleExpression){
                            String id = satisNominal.identifier;
                            TableauxPart newNominalSatis = new TableauxPart(new Satisfier(new Nominal("D"+id+id),diamond.proposition), this.currentStep,current,TableauxRules.DIAMOND,current.lineNumber+1);
                            TableauxPart newDiamondNominal = new TableauxPart(new Satisfier(satisNominal,new Diamond(new Nominal(id+id))), newNominalSatis,current,TableauxRules.DIAMOND,current.lineNumber+2);
                            documentRule(expr, TableauxRules.DIAMOND);
                            this.currentStep = newDiamondNominal;
                            upNext.add(newNominalSatis);
                            tableauxParts.add(newNominalSatis);
                            tableauxParts.add(newDiamondNominal);
                            newNominal("D"+id+id, this.currentStep, newNominalSatis);
                        }
                        if(diamondHasLoneNominal){
                            applyNomRule(current, diamond, satisNominal.identifier);
                        }
                        returnBranches.add(this);
                        break;
                    case E:
                        E e = (E)satisProp;
                        boolean eHasLoneNominal = e.proposition.getType() == ExpressionTypes.NOMINAL;
                        boolean branchHasERuleExpression = this.currentStep.seekLoopCondition(e.proposition, satisNominal.identifier, false);
                        if(!eHasLoneNominal && !branchHasERuleExpression){
                            String id = satisNominal.identifier;
                            TableauxPart newESatis = new TableauxPart(new Satisfier(new Nominal("E"+id+id), e.proposition), this.currentStep, current, TableauxRules.E);
                            documentRule(expr, TableauxRules.E);
                            this.currentStep = newESatis;
                            upNext.add(newESatis);
                            tableauxParts.add(newESatis);
                            newNominal("E"+id+id,this.currentStep, newESatis);
                        }
                        returnBranches.add(this);
                        break;
                    case NOMINAL:
                        Nominal satisLoneNominal = (Nominal)satisProp;
                        if(!nomsInBranch.contains(satisLoneNominal.identifier)){
                            newNominal(satisLoneNominal.identifier, current, current);
                        }
                        applyNomRule(current, satisLoneNominal, satisNominal.identifier);
                        break;
                    case PROPOSITIONAL_SYMBOL:
                        applyNomRule(current, satisProp, satisNominal.identifier);
                        break;
                    default:
                        System.err.println("Attempted to break down: "+current.expr+" but encountered an unexpeted ExpressionType in Satisfier(...): "+satisProp.getType());
                        returnBranches.add(this);
                        break;
                }
            break;
            case NOT:
                Expression notProp = ((Not)expr).proposition;
                switch (notProp.getType()) {
                    case NOT:
                        Not notNot = (Not)notProp;
                        TableauxPart prop = new TableauxPart(notNot.proposition, this.currentStep,current,TableauxRules.NOTNOT);
                        documentRule(expr, TableauxRules.NOTNOT);
                        this.currentStep = prop;
                        returnBranches.add(this);
                        upNext.add(prop);
                        tableauxParts.add(prop);
                        break;
                    case AND:
                        And notAnd = (And)notProp;
                        applyOr(expr,new Not(notAnd.propositionLeft), new Not(notAnd.propositionRight), current, returnBranches,TableauxRules.NOTAND);
                        break;
                    case OR:
                        Or notOr = (Or)notProp;
                        applyAnd(expr,new Not(notOr.propositionLeft), new Not(notOr.propositionRight), current, returnBranches,TableauxRules.NOTOR);
                        break; 
                    case SATISFIER:
                        Satisfier notSatis = (Satisfier)notProp;
                        Nominal notSatisNominal = notSatis.referencePoint;
                        if(!nomsInBranch.contains(notSatisNominal.identifier)){
                            newNominal(notSatisNominal.identifier, current, current);
                        }
                        if (notSatis.proposition.getType() == ExpressionTypes.NOT) {
                            Not notSatisNot = (Not)notSatis.proposition;
                            TableauxPart notNotSatis = new TableauxPart(new Satisfier(notSatis.referencePoint, notSatisNot.proposition), this.currentStep,current,TableauxRules.NOTNOT);
                            documentRule(expr, TableauxRules.NOTNOT);
                            this.currentStep = notNotSatis;
                            upNext.add(notNotSatis);
                            tableauxParts.add(notNotSatis);
                            returnBranches.add(this);
                        }
                        else if (notSatis.proposition.getType() == ExpressionTypes.AND){
                            And notSatisAnd = (And)notSatis.proposition;
                            applyOr(expr,new Not(new Satisfier(notSatis.referencePoint, notSatisAnd.propositionLeft)), new Not(new Satisfier(notSatis.referencePoint, notSatisAnd.propositionRight)), current, returnBranches,TableauxRules.NOTAND); 
                        }
                        else if (notSatis.proposition.getType() == ExpressionTypes.OR){
                            Or notSatisOr = (Or)notSatis.proposition;
                            applyAnd(expr,new Not(new Satisfier(notSatis.referencePoint, notSatisOr.propositionLeft)), new Not(new Satisfier(notSatis.referencePoint, notSatisOr.propositionRight)), current, returnBranches,TableauxRules.NOTOR);
                        }
                        else if (notSatis.proposition.getType() == ExpressionTypes.SATISFIER){
                            Satisfier notSatisSatis = (Satisfier)notSatis.proposition;
                            TableauxPart innerNotSatis = new TableauxPart(new Not(notSatisSatis), this.currentStep,current,TableauxRules.NOTSATIS);
                            documentRule(expr, TableauxRules.NOTSATIS);
                            this.currentStep = innerNotSatis;
                            upNext.add(innerNotSatis);
                            tableauxParts.add(innerNotSatis);
                            returnBranches.add(this);
                        }
                        else if(notSatis.proposition.getType() == ExpressionTypes.DIAMOND){
                            Diamond notSatisDiamond = (Diamond) notSatis.proposition;
                            Nominal desiredNominal = notSatis.referencePoint;
                            Nominal result = this.currentStep.seekNotDiamondRule(desiredNominal.identifier);
                            if(result != null){
                                TableauxPart notSatisOldNominal = new TableauxPart(new Not(new Satisfier(result,notSatisDiamond.proposition)), this.currentStep, current,TableauxRules.NOTDIAMOND);
                                documentRule(expr, TableauxRules.NOTDIAMOND);
                                this.currentStep = notSatisOldNominal;
                                upNext.add(notSatisOldNominal);
                                tableauxParts.add(notSatisOldNominal);
                            }
                            returnBranches.add(this);
                        }
                        else if(notSatis.proposition.getType() == ExpressionTypes.E){
                            applyNotE(current, notSatis.referencePoint);
                            this.notEInEffect = true;
                            this.origNotEParts.add(current);
                        }
                        else {
                            System.err.println("Attempted to break down: "+current.expr+" but encountered unexpected Expression type in Not(Satisfier(...)): "+notSatis.proposition.getType());

                            returnBranches.add(this);
                            break;
                        }
                        break;
                    case NOMINAL:
                        Nominal notNominal = (Nominal)notProp;
                        if(!nomsInBranch.contains(notNominal.identifier)){
                            newNominal(notNominal.identifier, current, current);
                        }
                        break;
                    default:
                        System.err.println("Attempted to break down: "+current.expr+"but encountered an unexpeted ExpressionType in Not(...):" + notProp.getType());
                        returnBranches.add(this);
                        break;
                }
            break;
            case NOMINAL:
                Nominal nominal = (Nominal)expr;
                if(!nomsInBranch.contains(nominal.identifier)){
                    newNominal(nominal.identifier, current, current);
                }
            break;
            default:
                System.err.println("Attempted to break down: "+current.expr+"but encountered an unexpeted ExpressionType: "+type);
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
    int lineNumber;
    TableauxRules ruleApplied;

    // Line number set explicidly
    public TableauxPart(Expression e, TableauxPart from,TableauxPart source, TableauxRules ruleApplied,int lineNumber){
        this.expr = e;
        // From is a reference to the "step" it is from, it has nothing to do with what expression the current tableauxPart is a subexpression of
        this.from = from;
        // Souce is the expression this tableaux part is made from
        this.source = source;
        // Set the line number
        this.lineNumber = lineNumber;
        // Rule applied to get this tableauxPart
        this.ruleApplied = ruleApplied;
    }

    // Line number gotten from from
    public TableauxPart(Expression e, TableauxPart from,TableauxPart source, TableauxRules ruleApplied){
        this.expr = e;
        // From is a reference to the "step" it is from, it has nothing to do with what expression the current tableauxPart is a subexpression of
        this.from = from;
        // Souce is the expression this tableaux part is made from
        this.source = source;
        // Set the line number
        if(from == null){
            this.lineNumber = 1;
        } else this.lineNumber = from.lineNumber +1;
        // Rule applied to get this tableauxPart
        this.ruleApplied = ruleApplied;
    }

    public String toString(){
        if(from != null && source != null){
            return expr.toString() + "\t" + "Line: "+lineNumber+", Rule: "+ruleApplied+" from: "+source.lineNumber;
        }
        return expr.toString() + "\t" + "Line: "+lineNumber;
    }

    public boolean seekExpressionInBranch(Expression model){
        if(expr.equals(model)){
            return true;
        }
        if(from == null){
            return false;
        }
        return from.seekExpressionInBranch(model);
    }

    public LinkedList<String> seekAllNominalRelations(String targetId,LinkedList<String> foundNoms){
        LinkedList<String> returnNoms = foundNoms;
        if(foundNoms == null){
            returnNoms = new LinkedList<>();
        }
        if(expr.getType() == ExpressionTypes.SATISFIER){
            Satisfier inner = (Satisfier)expr;
            Boolean isLoneNominal = inner.proposition.getType() == ExpressionTypes.NOMINAL;
            Boolean isTargetReference = inner.referencePoint.identifier.equals(targetId);
            if(isLoneNominal && isTargetReference){
                returnNoms.add(inner.referencePoint.identifier);
            }
        }
        if(from == null){
            return returnNoms;
        }
        return from.seekAllNominalRelations(targetId, returnNoms);
    }

    public boolean seekLoopCondition(Expression target,String selfId,Boolean rightTargetRightNominal){
        Boolean flag = rightTargetRightNominal;
        if(expr.getType() == ExpressionTypes.SATISFIER){
            Satisfier inner = (Satisfier)expr;
            Boolean isOwnNominal = ((Nominal)inner.referencePoint).identifier.equals(selfId);
            Boolean isTarget = inner.proposition.equals(target);
            if(isTarget && !isOwnNominal && rightTargetRightNominal){
                return true;
            }
            else if(isTarget && isOwnNominal){
                flag = true;
            }
        }
        else if(expr.getType() == ExpressionTypes.NOT){
            Not inner = (Not)expr;
            if(inner.proposition.getType() == ExpressionTypes.SATISFIER){
                Satisfier notInner = (Satisfier)inner.proposition;
                Boolean isOwnNominal = ((Nominal)notInner.referencePoint).identifier.equals(selfId);
                Boolean isTarget = notInner.proposition.equals(target);
                if(isTarget && !isOwnNominal && rightTargetRightNominal){
                    return true;
                }
                else if(isTarget && isOwnNominal){
                    flag = true;
                }
            }
        }
        if(from == null){
            return false;
        }
        return from.seekLoopCondition(target,selfId,flag);
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

enum TableauxRules {
    AND,
    OR,
    NOTNOT,
    NOTOR,
    NOTAND,
    NOT,
    SATIS,
    NOTSATIS,
    DIAMOND,
    NOTDIAMOND,
    E,
    NOTE,
    REF,
    NOM1,
    NOM2
}
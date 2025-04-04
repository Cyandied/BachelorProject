import java.util.ArrayList;

public abstract class Expression {
    // Method to return the type of the expression in terms of the enum ExpressionTypes
    // Ex. Not(p).getType() -> ExpressionTypes.NOT
    abstract ExpressionTypes getType();
    // Method to return the types of the expressions inside the current expression in terms of a list of enum ExpressionTypes
    // Ex. Nominal().getNextType() -> []
    // Ex. Not(new Or(p,q)).getNextType() -> [ExpressionTypes.OR]
    // Ex. Or(new Nominal(), new And(p,q)) -> [ExpressionTypes.NOMINAL, ExpressionTypes.AND]
    abstract ArrayList<ExpressionTypes> getNextType();
}

class Prop_symbol extends Expression {

    public String identifier;

    public Prop_symbol(String id){
        identifier = "p|"+id;
    }

    public String toString(){
        return "p|"+identifier;
    }

    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.PROPOSITIONAL_SYMBOL;
    }

    @Override
    ArrayList<ExpressionTypes> getNextType() {
        return new ArrayList<ExpressionTypes>();
    }
}

class Nominal extends Expression {

    public String identifier;

    public Nominal(String id){
        identifier = "n|"+id;
    }
    
    public String toString(){
        return "n|"+identifier;
    }

    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.NOMINAL;
    }

    @Override
    ArrayList<ExpressionTypes> getNextType() {
        return new ArrayList<ExpressionTypes>();
    }
}

class Satisfier extends Expression {
    public Expression referencePoint;
    public Expression proposition;

    public Satisfier(Expression nominal, Expression expr){
        referencePoint = nominal;
        proposition = expr;
    }

    public String toString(){
        return "@_("+referencePoint+":"+proposition+")";
    }

    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.SATISFIER;
    }

    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(proposition.getType());
        return list;
    }
}

class Box extends Expression {
    public Expression proposition;

    public Box(Expression expr){
        proposition = expr;
    }

    
    public String toString(){
        return "[]("+proposition+")";
    }


    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.BOX;
    }


    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(proposition.getType());
        return list;
    }
}

class Implies extends Expression {
    public Expression propositionLeft;
    public Expression propositionRight;

    public Implies(Expression exprLeft, Expression exprRight){
        propositionLeft = exprLeft;
        propositionRight = exprRight;
    }

    
    public String toString(){
        return "("+propositionLeft+"->"+propositionRight+")";
    }


    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.IMPLIES;
    }


    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(propositionLeft.getType());
        list.add(propositionRight.getType());
        return list;
    }
}

class Not extends Expression {
    public Expression proposition;

    public Not(Expression expr){
        proposition = expr;
    }

    
    public String toString(){
        return "!("+proposition+")";
    }


    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.NOT;
    }


    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(proposition.getType());
        return list;
    }
}

class And extends Expression {
    public Expression propositionLeft;
    public Expression propositionRight;

    public And(Expression exprLeft, Expression exprRight){
        propositionLeft = exprLeft;
        propositionRight = exprRight;
    }

    
    public String toString(){
        return "("+propositionLeft+"*"+propositionRight+")";
    }


    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.AND;
    }


    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(propositionLeft.getType());
        list.add(propositionRight.getType());
        return list;
    }
}

class Or extends Expression {
    public Expression propositionLeft;
    public Expression propositionRight;

    public Or(Expression exprLeft, Expression exprRight){
        propositionLeft = exprLeft;
        propositionRight = exprRight;
    }
    
    public String toString(){
        return "("+propositionLeft+"+"+propositionRight+")";
    }

    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.OR;
    }

    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(propositionLeft.getType());
        list.add(propositionRight.getType());
        return list;
    }
}
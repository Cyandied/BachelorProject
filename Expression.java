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
    // Compares the current Expression with a given model expression, returns true if they are perfect identical, otherwise it returns false.
    abstract Boolean compare(Expression model);
}

class Prop_symbol extends Expression {

    public String identifier;

    public Prop_symbol(String id){
        identifier = id;
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

    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            Prop_symbol cast = (Prop_symbol)model;
            return identifier.equals(cast.identifier);
        }
        return false;
    }
}

class Nominal extends Expression {

    public String identifier;

    public Nominal(String id){
        identifier = id;
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

    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            Nominal cast = (Nominal)model;
            return identifier.equals(cast.identifier);
        }
        return false;
    }
}

class Satisfier extends Expression {
    public Expression referencePoint;
    public Expression proposition;

    public Satisfier(Expression nominal, Expression expr){
        if(nominal.getType() != ExpressionTypes.NOMINAL){
            System.err.println("Invalid assingment! Non-nominal assinged to satistifer referencePoint, cannot interpret!");
            System.exit(0);
        }
        referencePoint = nominal;
        proposition = expr;
    }

    public String toString(){
        return "@("+referencePoint+":"+proposition+")";
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

    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            Satisfier cast = (Satisfier)model;
            return proposition.compare(cast.proposition) && referencePoint.compare(cast.referencePoint);
        }
        return false;
    }
}

class Diamond extends Expression {
    public Expression proposition;

    public Diamond(Expression expr){
        proposition = expr;
    }

    
    public String toString(){
        return "<>("+proposition+")";
    }


    @Override
    ExpressionTypes getType() {
        return ExpressionTypes.DIAMOND;
    }


    @Override
    ArrayList<ExpressionTypes> getNextType() {
        ArrayList<ExpressionTypes> list = new ArrayList<ExpressionTypes>();
        list.add(proposition.getType());
        return list;
    }


    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            Diamond cast = (Diamond)model;
            return proposition.compare(cast.proposition);
        }
        return false;
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

    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            Not cast = (Not)model;
            return proposition.compare(cast.proposition);
        }
        return false;
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

    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            And cast = (And)model;
            return propositionLeft.compare(cast.propositionLeft) && propositionRight.compare(cast.propositionRight);
        }
        return false;
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

    @Override
    Boolean compare(Expression model) {
        if(model.getType() == this.getType()){
            And cast = (And)model;
            return propositionLeft.compare(cast.propositionLeft) && propositionRight.compare(cast.propositionRight);
        }
        return false;
    }
}
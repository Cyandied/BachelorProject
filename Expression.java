public abstract class Expression {
    
}

class Prop_symbol extends Expression {

    public int identifier;

    public Prop_symbol(){
        identifier = (int)((Math.random()+1)*900);
    }

    public String toString(){
        return "p"+identifier;
    }
}

class Nominal extends Expression {

    public int identifier;

    public Nominal(){
        identifier = (int)((Math.random()+1)*900);
    }
    
    public String toString(){
        return "n"+identifier;
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
        return "@_"+referencePoint+":"+proposition;
    }
}

class Box extends Expression {
    public Expression proposition;

    public Box(Expression expr){
        proposition = expr;
    }

    
    public String toString(){
        return "[]"+proposition;
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
        return propositionLeft+"->"+propositionRight;
    }
}

class Not extends Expression {
    public Expression proposition;

    public Not(Expression expr){
        proposition = expr;
    }

    
    public String toString(){
        return "!"+proposition;
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
        return propositionLeft+"*"+propositionRight;
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
        return propositionLeft+"/"+propositionRight;
    }
}
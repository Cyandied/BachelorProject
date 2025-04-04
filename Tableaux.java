import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class Tableaux {

    public Expression startExpression;
    public LinkedList<Vertex> tree;
    public List<ExpressionTypes> endCases = Arrays.asList(ExpressionTypes.NOMINAL,ExpressionTypes.PROPOSITIONAL_SYMBOL);

    public Tableaux(Expression e){
        startExpression = e;
    }

    private LinkedList<Vertex> findNext(Vertex from){
        
        LinkedList<Vertex> nextVertexs = new LinkedList<>();
        LinkedList<Expression> expressions = from.exprs;

        for(int i=0;i<expressions.size();i++){
            switch (from.getType()) {
                case NOT:
                    return null;
            
                default:
                    return null;
            }
        }
    }

    private boolean EvaulteBranch(Edge edge){
        Expression endExpr = edge.end.expr;

        LinkedList<EndCase> toCompare = new LinkedList<>();

        if(IsEndcase(endExpr)){
            toCompare.add(new EndCase(endExpr));
            Vertex nextVertex = edge.start;
            while (nextVertex.expr != startExpression) {
                if(IsEndcase(nextVertex.expr)){
                    toCompare.add(new EndCase(nextVertex.expr));
                }
                nextVertex = nextVertex.from.start;
            }

            for(int i=0;i<toCompare.size();i++){
                for(int j=0;j<toCompare.size();j++){
                    if(toCompare.get(i).isContradiction(toCompare.get(j))){
                        return false;
                    }
                }
            }

        }

        return true;
    }

    private boolean IsEndcase(Expression expr){
        return endCases.contains(expr.getType()) || (expr.getType() == ExpressionTypes.NOT && endCases.contains(expr.getNextType()));
    }

    public boolean DoTableaux(){

        Vertex startVertex = new Vertex(startExpression, null);
        startVertex.SetTo(findNext(startVertex));
        tree.add(startVertex);

        LinkedList<Vertex> upNext = new LinkedList<>();
        for(int i=0;i<startVertex.to.size();i++){
            upNext.add(startVertex.to.get(i));
        }

        while (!upNext.isEmpty()) {
            Vertex current = upNext.getFirst();
            upNext.removeFirst();

            LinkedList<Vertex> nextVertexs = findNext(current);
            current.SetTo(nextVertexs);
            tree.add(current);

            if (!nextVertexs.isEmpty()) {
                for(int i=0;i<current.to.size();i++){
                    upNext.add(current.to.get(i));
                }
            }
            
        }
        

        return false;
    }

}

class EndCase {
    String id;
    boolean isNot;

    public EndCase(Expression e){
        switch (e.getType()) {
            case NOMINAL:
                id = getId(e);
                isNot = false;
                break;
            case PROPOSITIONAL_SYMBOL:
                id = getId(e);
                isNot = false;
                break;
            case NOT:
                id = getId(((Not)e).proposition);
                isNot = true;
            default:
                break;
        }
    }

    public boolean isContradiction(EndCase compare){
        if(id.equals(compare.id)){
            if(isNot != compare.isNot){
                return true;
            }
        }
        return false;
    }
}

    public String getId(Expression e){
        switch (e.getType()) {
            case NOMINAL:
                return ((Nominal)e).identifier;
            case PROPOSITIONAL_SYMBOL:
                return ((Prop_symbol)e).identifier;
            default:
                return null;
    }
}

class Vertex {

    public LinkedList<Expression> exprs = new LinkedList<>();
    public Vertex from;
    public LinkedList<Vertex> to = new LinkedList<>();

    public Vertex(Expression e,Vertex f){
        exprs.add(e);
        from = f;
    }

    public void SetTo(LinkedList<Vertex> t){
        to = t;
    }

    public void AddExpression(Expression e){
        exprs.add(e);
    }

}
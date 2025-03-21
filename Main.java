import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        
    /* Start the parser */
    try {
      Lexer l = new Lexer(new FileReader(args[0]));
      // SymbolFactory sf = new ComplexSymbolFactory();
      parser p = new parser(l /* , sf */);
      Object result = p.parse().value;
      if(result instanceof Expression){
        @SuppressWarnings("unchecked")
        Expression res = (Expression) result;
        System.out.print(res);
      }

    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
    }
}
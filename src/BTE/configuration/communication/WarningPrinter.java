package BTE.configuration.communication;

import BTE.configuration.communication.interfaces.IPrintStream;
import java.io.PrintStream;

/**
 * Jednoducha trieda na vypis upozorneni. Vypisuje varovania na System.err.
 * @author Milan
 */
public class WarningPrinter implements IPrintStream {
    /**
     * Priznak, ci sa maju vypisovat varovania, alebo ma byt vypis potlaceny.
     */
    public boolean print = true;

    /**
     * Vystupny prud, predvolene System.err.
     */
    private PrintStream errorStream = System.err;

    public WarningPrinter(boolean print, PrintStream errorStream){
        this.errorStream = errorStream;
        this.print = print;
    }

    public WarningPrinter(boolean print) {
        this.print = print;
    }

    public WarningPrinter() {
    }

    public void println(String line){
        if(print)
            System.err.println(line);
    }

    public PrintStream getErrorStream(){
        return this.errorStream;
    }
}

package BTE.configuration.parsing.model.xml;

/**
 * Messenger vzor pre dvojicu objektov.
 * @author Milan
 */
public class Twins<T> {
    // Prvy objekt
    public T first;
    // Druhy objekt
    public T second;

    /**
     * Konstruktor.
     * @param first
     * @param second
     */
    public Twins(T first, T second){
        this.first = first;
        this.second = second;
    }
}

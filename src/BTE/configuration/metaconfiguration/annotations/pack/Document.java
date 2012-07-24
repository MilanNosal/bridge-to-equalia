package BTE.configuration.metaconfiguration.annotations.pack;

/**
 * Anotacia definujuca zdrojovy dokument.
 * @author Milan
 */
public @interface Document {
    /**
     * Jeho lokacia.
     * @return
     */
    String location();

    /**
     * Ide o volanie getResource() (teda abstrakcia zdrojov jazyka Java),
     * alebo cesta v suborovom systeme?
     * @return
     */
    boolean resource() default true;
}

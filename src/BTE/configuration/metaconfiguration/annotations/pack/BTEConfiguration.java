package BTE.configuration.metaconfiguration.annotations.pack;

import BTE.configuration.communication.Priority;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hlavna metakonfiguracna anotacia, cielovym typom je balik, anotuje sa nou
 * balik, ktory obsahuje konf. anotacie.
 * @author Milan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE})
public @interface BTEConfiguration {
    /**
     * Zoznam vsetkych konfiguracnych anotacnych typov, ktore chceme spracovavat. Tie
     * musia byt v baliku, ktory je anotovany tymto anotacnym typom.
     * @return
     */
    String[] configurationAnnotations();

    /**
     * Informacia o generovanom dokumente a aj o vstupnych dokumentoch.
     * @return
     */
    DocumentConfiguration document();

    /**
     * Priznak, ci sa maju vypisovat varovania.
     * @return
     */
    boolean warningPrinting() default false;

    /**
     * Vyber priority pri spajani konfiguracie z viacerych zdrojov.
     * @return
     */
    Priority priority() default Priority.XML;

    /**
     * Nazov vystupneho dokumentu.
     * @return
     */
    String outputDocument() default "document.xml";

    /**
     * Nazov vystupneho priecinku (do neho sa maju generovat dokument aj
     * schema, ak sa pri nich neuvedu absolutne cesty).
     * @return
     */
    String outputDirectory() default "";

    /**
     * Lokacia k scheme. Moze byt relativna k dokumentom.
     * @return
     */
    String schemaLocation() default "document.xsd";

    /**
     * Zoznam vstupnych dokumentov.
     * @return
     */
    Document[] inputDocuments();

    /**
     * Nazov balika s triedami, ktore zodpovedaju scheme konfiguracie, pre 
     * potreby JAXB. Prazdny retazec znamena, ze ma nezaujima konfiguracia
     * spracovana JAXB, popr. ze sa o to postaram sam.
     * @return
     */
    String jaxbPackage() default "";
}

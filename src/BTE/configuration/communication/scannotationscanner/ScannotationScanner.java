package BTE.configuration.communication.scannotationscanner;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.communication.interfaces.AnnotationScanner;
import BTE.configuration.communication.interfaces.IPrintStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.scannotation.AnnotationDB;

/**
 * Implementacia anotacneho skenera pomocou projektu Scannotation. Pomocou
 * Scannotation viem vybrat iba triedy, ktore sa oplati prehladavat. Tie vsak
 * potom musim skenovat manualne pomocou reflexie.
 * @author Milan
 */
public class ScannotationScanner implements AnnotationScanner {
    /**
     * Prud na vypis upozorneni.
     */
    private IPrintStream errorStream;

    /**
     * Mapa anotacnych typov a vyskytov anotacii ich druhu. Pouzite pri
     * eager metode (ak je LAZY_SCANNING==false). Pri inicializacii sa v pripade
     * eager vyhladavania prehladaju vsetky vyskyty a ulozia sa do tabulky,
     * odkial sa pri dopyte uz iba vratia. Pri lazy scanning sa do mapy ukladaju
     * iba v pripade ze je zapnuta optimalizacia ukladanim a aj to az po tom,
     * co su anotacie nejakeho typu dopytovane.
     */
    private Map<String, Set<AnnotationTypeInstance>> annotations
            = new HashMap<String, Set<AnnotationTypeInstance>>();

    /**
     * Tabulka anotacnych typov a celych mien tried s vyskytmi anotacii daneho
     * typu. Toto mi vrati Scannotation, aby som vedel, ktore triedy sa mi
     * oplati skenovat.
     */
    private Map<String, Set<String>> scannotations;

    /**
     * Metakonfiguraciou by som vedel zefektivnit eager prehladavanie,
     * prehladavali by sa iba potrebne triedy. Tato mnozina nesie nazvy
     * anotacnych typov, ktore ma zaujimaju (Scannotation vrati totiz vsetky
     * vyskyty vsetkych anotacii, potrebujem vyradit nezaujimave).
     */
    private Set<String> annotationTypesForScan;

    /**
     * PropertiesReader mi poskytne potrebne nastavenia (Napr. optimalizaciu
     * lazy vyhladavania ukladanim).
     */
    private PropertiesReader properties;

    /**
     * Konstruktor si pripravi properties reader, vyuzije scannotation na ziskanie
     * nazvov tried, ktore maju anotacie a v pripade potreby ich hned aj
     * prehlada (tj ak ide o eager prehladavanie).
     * @param annotationTypesForScan
     * @param errorStream
     */
    public ScannotationScanner(Set<String> annotationTypesForScan, IPrintStream errorStream){
        // Inicializacia
        this.errorStream = errorStream;
        this.annotationTypesForScan = annotationTypesForScan;
        this.properties = new PropertiesReader();
        this.properties.setErrorStream(errorStream);
        // Pouzitie Scannotation
        AnnotationDB annotationDB = new AnnotationDB();
        try {
            annotationDB.scanArchives(properties.getUrlsToScan());
        } catch (IOException ex){
            throw new RuntimeException("ScannotationScanner encountered some" +
                    " troubles while trying to scan archives shown in URLs to scan.", ex);
        }
        this.scannotations = annotationDB.getAnnotationIndex();
        // Ak je eager prehladavanie, hned ho aj vykoname
        if(!properties.isLazyScanning()){
            scanAllInterestingClasses();
        }
    }

    /**
     * Konstruktor si pripravi properties reader, vyuzije scannotation na ziskanie
     * nazvov tried z url, ktore berie ako argument, ktore maju anotacie a v pripade potreby ich hned aj
     * prehlada (tj ak ide o eager prehladavanie).
     * @param urlsToScan
     * @param annotationTypesForScan
     * @param errorStream 
     */
    public ScannotationScanner(URL[] urlsToScan, Set<String> annotationTypesForScan, IPrintStream errorStream){
        // Inicializacia
        this.errorStream = errorStream;
        this.annotationTypesForScan = annotationTypesForScan;
        this.properties = new PropertiesReader();
        this.properties.setErrorStream(errorStream);
        // Pouzitie Scannotation
        AnnotationDB annotationDB = new AnnotationDB();
        try {
            annotationDB.scanArchives(urlsToScan);
        } catch (IOException ex){
            throw new RuntimeException("ScannotationScanner encountered some" +
                    " troubles while trying to scan archives shown in URLs to scan.", ex);
        }
        this.scannotations = annotationDB.getAnnotationIndex();
        // Ak je eager prehladavanie, hned ho aj vykoname
        if(!properties.isLazyScanning()){
            scanAllInterestingClasses();
        }
    }

    /**
     * Metoda ma vyplnit hesovaciu tabulku vyskytov anotacii. Pouziva sa iba
     * pri eager prehladavani.
     */
    private void scanAllInterestingClasses(){
        // Mnozina, aby sme neprehladavali triedu viackrat
        Set<String> classesToScan = new HashSet<String>();
        Class clazz;
        // Ak boli uvedene podstatne anotacne typy, tak skenujeme len triedy,
        // ktore ich obsahuju
        if(this.annotationTypesForScan!=null){
            for(String annotationType : this.annotationTypesForScan){
                if(this.scannotations.get(annotationType)!=null)
                    // Vyberame iba tie triedy na skenovanie, ktore maju na sebe
                    // anotacie hladaneho typu
                    classesToScan.addAll(this.scannotations.get(annotationType));
            }
            // Tie prehladame
            for(String classToScan : classesToScan){
                try {
                    clazz = Class.forName(classToScan);
                    // Samotne prehladanie triedy clazz, ziskane informacie sa
                    // ukladaju do kolekcii predavanych ako argumenty volania
                    (new ClassAnnotationScanner(clazz)).scanClass(annotationTypesForScan, annotations);
                } catch (ClassNotFoundException ex) {
                    errorStream.println("Scannotation scanner cannot find class "+classToScan+" that" +
                            " should contain tracked annotations.");
                }
            }
        }
        // Inak skenujeme vsetky triedy, ktore obsahuju anotacie
        else {
            for(String annotationType : this.scannotations.keySet()){
                classesToScan.addAll(this.scannotations.get(annotationType));
            }
            for(String classToScan : classesToScan){
                try {
                    clazz = Class.forName(classToScan);
                    (new ClassAnnotationScanner(clazz)).scanClass(annotations);
                } catch (ClassNotFoundException ex) {
                    errorStream.println("Scannotation scanner cannot find class "+classToScan+" that" +
                            " should contain tracked annotations.");
                }
            }
        }
        
    }

    /**
     * Konstruktor s jednym parametrom. Ak nemame vybrane anotacny typy k
     * dispozicii.
     */
    public ScannotationScanner(IPrintStream errorStream){
        this(null, errorStream);
    }

    /**
     * Vrati zoznam vyskytov anotacii ziadaneho typu. Ak ziadne nenasiel, vrati
     * prazdnu mnozinu.
     * @param qualifiedName
     * @return
     */
    public Set<AnnotationTypeInstance> getAnnotationsOfType(String qualifiedName) {
        if(this.properties.isLazyScanning()){
            // Ak je lazy scanning, tak prehladavame prislusne triedy
            if(this.annotations.get(qualifiedName)!=null){
                // Ak je v tabulke vyskytov hodnota s menom hladaneho typu,
                // mozeme zoznam rovno vratit
                return this.annotations.get(qualifiedName);
            }
            if(this.scannotations.get(qualifiedName)==null){
                // Ak Scannotation nenasiel ziadny vyskyt anotacie, vratime
                // prazdnu mnozinu
                return new HashSet<AnnotationTypeInstance>();
            }
            // Pripravime si mnozinu najdenych anotacii
            Set<AnnotationTypeInstance> foundAnnotations = new HashSet<AnnotationTypeInstance>();
            Class clazz;
            for(String classToScan : this.scannotations.get(qualifiedName)){
                try {
                    clazz = Class.forName(classToScan);
                    // A prehladame ju
                    (new ClassAnnotationScanner(clazz)).scanClass(qualifiedName, foundAnnotations);
                } catch (ClassNotFoundException ex) {
                    errorStream.println("Scannotation scanner cannot find class "+classToScan+" that" +
                            " should contain tracked annotations ("+qualifiedName+").");
                }
            }
            // Ak je zapnuta optimalizacia ukladanim, ulozime do tabulky
            if(properties.isArchiving()){
                this.annotations.put(qualifiedName, foundAnnotations);
            }
            return foundAnnotations;
        } else {
            // Ak bolo eager vyhladavanie, iba vratime vysledok
            if(this.annotations.get(qualifiedName)==null){
                return new HashSet<AnnotationTypeInstance>();
            }
            return this.annotations.get(qualifiedName);
        }
    }
}

package BTE.configuration.communication.scannotationscanner;

import BTE.configuration.communication.interfaces.IPrintStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.scannotation.ClasspathUrlFinder;

/**
 * Trieda, ktora nacita properties pre ScannotationScanner.
 * @author Milan
 */
public class PropertiesReader {
    /**
     * Prud na vypis upozorneni.
     */
    private IPrintStream errorStream;

    /**
     * Nastavenie, ci sa ma skenovanie vykonavat eager formou, alebo
     * lazy, az ked je dopyt po konkretnej anotacii
     */
    private boolean lazyScanning = true;

    /**
     * Optimalizacia, pri lazy prehladavani mozem informacie,
     * ktore som prave vyhladal hned ulozit, aby pri dalsom dopyte nebolo
     * potrebne skenovat znova.
     */
    private boolean archiving = true;

    /**
     * Nastavuje, ci sa ma skenovat classpath alebo iba pouzit zoznam suborov
     * na preskumanie.
     */
    private boolean classpathScan = true;

    /**
     * Hlbka skenovania jar suborov. Tie sa mozu odkazovat na kniznice (atd).
     * -1 znamena skenovanie pokial sa da.
     */
    private int scanningDepth = 0;

    /**
     * Zoznam url k suborom (ci uz jar, alebo priecinkom k triedam).
     */
    private Set<URL> urlsToScan = new HashSet<URL>();

    /**
     * Konstanty pre kluce v properties subore.
     */
    private final static String PROPERTIES_FILE = "scannotationscanner.properties";
    private final static String LAZY_SCANNING_KEY = "lazy_scanning";
    private final static String ARCHIVING_KEY = "archiving";
    private final static String CLASSPATH_SCAN_KEY = "classpath_scan";
    private final static String SCANNING_DEPTH_KEY = "scanning_depth";
    private final static String URLS_KEY = "urls";

    /**
     * Konstruktor nacita properties a ziska URL, ktore maju byt skenovane
     * nastrojom scannotation.
     */
    public PropertiesReader(){
        // Nacitanie properties suboru
        Properties props = new Properties();
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            populateURLsToScan(new HashSet<URL>());
            return;
        } catch (NullPointerException ex){
            populateURLsToScan(new HashSet<URL>());
            return;
        }
        
        String value = null;
        value = props.getProperty(LAZY_SCANNING_KEY);
        if("true".equals(value)){
            lazyScanning = true;
        } else if("false".equals(value)){
            lazyScanning = false;
        }

        value = props.getProperty(ARCHIVING_KEY);
        if("true".equals(value)){
            archiving = true;
        } else if("false".equals(value)){
            archiving = false;
        }

        value = props.getProperty(CLASSPATH_SCAN_KEY);
        if("true".equals(value)){
            classpathScan = true;
        } else if("false".equals(value)){
            classpathScan = false;
        }

        value = props.getProperty(SCANNING_DEPTH_KEY);
        try {
            int i = Integer.parseInt(value);
            scanningDepth = i;
        } catch ( Exception ex ){
        }

        value = props.getProperty(URLS_KEY);
        Set<URL> urlsFromProperties = new HashSet<URL>();
        if(value!=null){
            urlsFromProperties = parseUrls(value);
        }

        populateURLsToScan(urlsFromProperties);
    }

    /**
     * Metoda rozparsuje zoznam URL (oddeleny ;) na subory.
     * @param urls
     */
    private Set<URL> parseUrls(String urls){
        Set<URL> ret = new HashSet<URL>();
        StringTokenizer st = new StringTokenizer(urls, ";");
        String temp = null;
        while(st.hasMoreTokens()){
            try {
                temp = st.nextToken();
                ret.add(new URL("file", "", temp));
            } catch (MalformedURLException ex) {
                if(errorStream!=null)
                    errorStream.println("ScannotationScanner.PropertiesReader.parseUrls()::" +
                            "\n\tMalformed URL "+temp);
            }
        }
        return ret;
    }

    /**
     * Ulohou tejto metody je podla ziskanych informacii vygenerovat mnozinu
     * URL, ktore maju byt skenovane.
     */
    private void populateURLsToScan(Set<URL> urlsFromProperties){
        if(classpathScan){
            // Tu sa pokusim spojit tie URL, ktore mi dal pouzivatel s tymi z classpath
            // Predpokladam, ze aspon jedne z nich su neprazdne
            urlsFromProperties.addAll(Arrays.asList(ClasspathUrlFinder.findClassPaths()));
        }
        for(URL url : urlsFromProperties){
            // Okrem uvedenych URL sa pokusim prehladat aj odkazovane v .jar
            this.urlsToScan.addAll(getUrlsFromJar(url, 0));
        }
    }

    /**
     * Metoda ma ziskat URL .jar suborov odkazovanych z jar suboru, ktoreho URL
     * dostane ako parameter. Druhym parametrom je aktualna hlbka, v ktorej sa
     * prehladavanie prave nachadza.
     * @param jarUrl
     * @return
     * @throws IOException
     */
    private Set<URL> getUrlsFromJar(URL jarUrl, int actualDepth) {
        // Mnozina URL ziskanych z akt. jar suboru
        Set<URL> urls = new HashSet<URL>();
        
        File file;

        // Podla jedneho zdroja toto by mala byt najspolahlivejsia cesta k zisku
        // suboru z url
        try {
            file = new File(jarUrl.toURI());
        } catch(URISyntaxException e) {
            file = new File(jarUrl.getPath());
        }
        if(!file.exists()){
            if(errorStream!=null)
                errorStream.println("ScannotationScanner.PropertiesReader.getUrlsFromJar()::\n\t" +
                    "Cannot find file(folder) "+jarUrl.getFile()+".");
            return new HashSet<URL>();
        }
        // Pridam aktualnu URL
        urls.add(jarUrl);
        if(this.scanningDepth!=-1 && actualDepth>=this.scanningDepth){
            // Ak som dosiahol pozadovanu hlbku, koncim
            return urls;
        }
        // Ak som nedosiahol, idem hlbsie
        // Nasledujuce riadku su podstatne, iba ak chceme hlbsie skenovanie
        JarFile jarfile = null;
        Manifest jarManifest = null;
        try {
            jarfile = new JarFile(file);
            jarManifest = jarfile.getManifest();
        } catch (IOException ex) {
            // Toto mi vyfiltruje cesty, ktore nie su .jar
            return urls;
        }
        if(jarManifest==null){
            return urls;
        }
        String classPath = jarManifest.getMainAttributes().getValue(Name.CLASS_PATH);
        if(classPath==null){
            return urls;
        }
        Set<String> paths = getReferencedJarPaths(file.getParent(), classPath);
        
        for(String path : paths){
            // Malo by to upravit mena
            File fileTemp = new File(path);
            if(!fileTemp.exists()){
                if(errorStream!=null)
                    errorStream.println("ScannotationScanner.URLScanner.getUrlsFromJar()::\n\t" +
                        "Cannot find file "+path+", referenced from file "+jarUrl.getFile()+".");
                continue;
            }
            try {
                // Rekurzivne prehladavanie, pridam subory na ktore sa odkazuje
                // Plus zvysim hlbku o 1
                urls.addAll(getUrlsFromJar(fileTemp.toURI().toURL(), actualDepth+1));
            } catch (MalformedURLException ex) {
                if(errorStream!=null)
                    errorStream.println("ScannotationScanner.URLScanner.getUrlsFromJar()::\n\t" +
                        "Malformed URL exception thrown when getting URL of file "+path+", referenced from file "+jarUrl.getFile()+".");
            }
        }
        return urls;
    }

    /**
     * Metoda spracuje referencie na .jar subory.
     * @param path
     * @param referencedJars
     * @return
     */
    private Set<String> getReferencedJarPaths(String path, String referencedJars){
        Set<String> paths = new HashSet<String>();
        StringTokenizer tokenizer = new StringTokenizer(referencedJars, " ");
        while(tokenizer.hasMoreTokens()){
            String postfix = tokenizer.nextToken();
            paths.add(path+System.getProperty("file.separator")+postfix);
        }
        return paths;
    }

    /**
     * @return the urlsToScan
     */
    public URL[] getUrlsToScan() {
        return urlsToScan.toArray(new URL[]{});
    }

    /**
     * @return the lazyScanning
     */
    public boolean isLazyScanning() {
        return lazyScanning;
    }

    /**
     * @return the archiving
     */
    public boolean isArchiving() {
        return archiving;
    }

    /**
     * @param errorStream the errorStream to set
     */
    public void setErrorStream(IPrintStream errorStream) {
        this.errorStream = errorStream;
    }
}

package BTE.configuration.communication.gaastimpl.lang;

/**
 * Trieda predstavujuca konstruktor.
 * @author Milan
 */
public class Constructor extends ExecutableElement {
    // <editor-fold defaultstate="collapsed" desc="Modifiers">
    public boolean isPrivate() {
        return ((this.getModifiers() & Modifier.PRIVATE) > 0);
    }

    public boolean isProtected() {
        return ((this.getModifiers() & Modifier.PROTECTED) > 0);
    }

    public boolean isPublic() {
        return ((this.getModifiers() & Modifier.PUBLIC) > 0);
    }
    // </editor-fold>
}

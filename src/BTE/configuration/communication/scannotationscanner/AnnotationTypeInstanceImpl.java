package BTE.configuration.communication.scannotationscanner;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.model.utilities.Utilities;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.lang.model.element.ElementKind;

/**
 * Jednoducha implementacia rozhrania pre anotacie.
 * Kazdy z konstruktorov sa snazi overit, ci sa neinstancuje nespravna kombinacia.
 * @author Milan
 */
public class AnnotationTypeInstanceImpl implements AnnotationTypeInstance {
    private Package sourcePackage;

    private Class sourceClass;

    private Method sourceMethod;

    private Field sourceField;

    private Constructor sourceConstructor;

    private int parameterNumber=-1;

    private ElementKind javaElementKind;

    private Annotation annotation;

    public AnnotationTypeInstanceImpl(Package sourcePackage, ElementKind javaElementKind, Annotation annotation) {
        
        switch(javaElementKind){
            case PACKAGE:
                break;
            default:
                throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourcePackage object and "+javaElementKind);
        }
        this.sourcePackage = sourcePackage;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public AnnotationTypeInstanceImpl(Class sourceClass, ElementKind javaElementKind, Annotation annotation) {
        switch(javaElementKind){
            case CLASS:
            case ANNOTATION_TYPE:
            case ENUM:
            case INTERFACE:
                break;
            default:
                throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourceClass object and "+javaElementKind);
        }
        this.sourceClass = sourceClass;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public AnnotationTypeInstanceImpl(Method sourceMethod, ElementKind javaElementKind, Annotation annotation) {
        if(javaElementKind!=ElementKind.METHOD){
            throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourceMethod object and "+javaElementKind);
        }
        this.sourceMethod = sourceMethod;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public AnnotationTypeInstanceImpl(Method sourceMethod, int parameterNumber, ElementKind javaElementKind, Annotation annotation) {
        if(javaElementKind!=ElementKind.PARAMETER){
            throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourceMethod object and parameter order and "+javaElementKind);
        }
        this.parameterNumber = parameterNumber;
        this.sourceMethod = sourceMethod;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public AnnotationTypeInstanceImpl(Field sourceField, ElementKind javaElementKind, Annotation annotation) {
        if(javaElementKind!=ElementKind.FIELD){
            throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourceField object and "+javaElementKind);
        }
        this.sourceField = sourceField;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public AnnotationTypeInstanceImpl(Constructor sourceConstructor, ElementKind javaElementKind, Annotation annotation) {
        if(javaElementKind!=ElementKind.CONSTRUCTOR){
            throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourceConstructor object and "+javaElementKind);
        }
        this.sourceConstructor = sourceConstructor;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public AnnotationTypeInstanceImpl(Constructor sourceConstructor, int parameterNumber, ElementKind javaElementKind, Annotation annotation) {
        if(javaElementKind!=ElementKind.PARAMETER){
            throw new RuntimeException("Trying to instantiate AnnotationInstanceImpl with" +
                        " sourceConstructor object and parameter order and "+javaElementKind);
        }
        this.parameterNumber = parameterNumber;
        this.sourceConstructor = sourceConstructor;
        this.javaElementKind = javaElementKind;
        this.annotation = annotation;
    }

    public Class getSourceClass() {
        return sourceClass;
    }

    public Method getSourceMethod() {
        return sourceMethod;
    }

    public Field getSourceField() {
        return sourceField;
    }

    public Constructor getSourceConstructor() {
        return sourceConstructor;
    }

    public int getParameterNumber() {
        return parameterNumber;
    }

    public ElementKind getJavaElementKind() {
        return javaElementKind;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Package getSourcePackage(){
        return sourcePackage;
    }

    /**
     * Jednoducha toString metoda.
     * @return
     */
    @Override
    public String toString(){
        switch(javaElementKind){
            case ANNOTATION_TYPE:
                return "AI[sourceAnnType:"+getSourceClass().getCanonicalName()+";annotation:"+getAnnotation()+"]";
            case CLASS:
                return "AI[sourceClass:"+getSourceClass().getCanonicalName()+";annotation:"+getAnnotation()+"]";
            case CONSTRUCTOR:
                return "AI[sourceConstructor:"+getSourceConstructor().toGenericString()+";annotation:"+getAnnotation()+"]";
            case ENUM:
                return "AI[sourceEnum:"+getSourceClass().getCanonicalName()+";annotation:"+getAnnotation()+"]";
            case FIELD:
                return "AI[sourceField:"+getSourceField().toGenericString()+";annotation:"+getAnnotation()+"]";
            case INTERFACE:
                return "AI[sourceInterface:"+getSourceClass().getCanonicalName()+";annotation:"+getAnnotation()+"]";
            case METHOD:
                return "AI[sourceMethod:"+getSourceMethod().toGenericString()+";annotation:"+getAnnotation()+"]";
            case PACKAGE:
                return "AI[sourcePackage:"+getSourcePackage().getName()+";annotation:"+getAnnotation()+"]";
            case PARAMETER:
                return "AI[sourceParameter:"+((getSourceMethod()==null)?
                    (getSourceConstructor().toGenericString())
                    :(getSourceMethod().toGenericString()))+"."+getParameterNumber()+";annotation:"+getAnnotation()+"]";
            default:
                return "AI[bad]";
        }
    }

    public String getJavaElementCanonicalName() {
        switch(javaElementKind){
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return getSourceClass().getName();
            case CONSTRUCTOR:
                return Utilities.getMethodsCanonicalName(
                        getSourceConstructor().toGenericString(),
                        getSourceConstructor().getName());
            case METHOD:
                return Utilities.getMethodsCanonicalName(
                        getSourceMethod().toGenericString(),
                        getSourceMethod().getName());
            case FIELD:
                return getSourceField().getDeclaringClass().getName()+"."+getSourceField().getName();
            case PACKAGE:
                return getSourcePackage().getName();
            case PARAMETER:
                return
                        ((getSourceMethod()==null)?
                            Utilities.getMethodsCanonicalName(getSourceConstructor().toGenericString(), getSourceConstructor().getName())
                            :
                            Utilities.getMethodsCanonicalName(getSourceMethod().toGenericString(), getSourceMethod().getName()))+
                        "."+getParameterNumber();
            default:
                return null;
        }
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof AnnotationTypeInstanceImpl) || object == null)
            return false;
        AnnotationTypeInstanceImpl aii = (AnnotationTypeInstanceImpl)object;
        switch(this.javaElementKind){
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return sourceClass.equals(aii.sourceClass);
            case PACKAGE:
                return sourcePackage.equals(aii.sourcePackage);
            case CONSTRUCTOR:
                return sourceConstructor.equals(aii.sourceConstructor);
            case METHOD:
                return sourceMethod.equals(aii.sourceMethod);
            case FIELD:
                return sourceField.equals(aii.sourceField);
            case PARAMETER:
                return
                        ((sourceMethod==null)?
                            sourceConstructor.equals(aii.sourceConstructor)
                            :
                            sourceMethod.equals(aii.sourceMethod))
                        && parameterNumber == aii.parameterNumber;
            default:
                return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.annotation != null ? this.annotation.hashCode() : 0);
        hash = 17 * hash + (this.sourceClass != null ? this.sourceClass.hashCode() : 0);
        hash = 17 * hash + (this.sourceConstructor != null ? this.sourceConstructor.hashCode() : 0);
        hash = 17 * hash + (this.sourceField != null ? this.sourceField.hashCode() : 0);
        hash = 17 * hash + (this.sourceMethod != null ? this.sourceMethod.hashCode() : 0);
        hash = 17 * hash + (this.sourcePackage != null ? this.sourcePackage.hashCode() : 0);
        hash = 17 * hash + this.parameterNumber;
        return hash;
    }
}

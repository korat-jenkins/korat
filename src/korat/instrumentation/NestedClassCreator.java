package korat.instrumentation;

import javassist.CannotCompileException;
import javassist.CtClass;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class NestedClassCreator {

    public static CtClass createNestedClass(String className,
            CtClass declaringClass) {
        return declaringClass.makeNestedClass(className, true);
    }

    public static CtClass createNestedClass(String className,
            CtClass declaringClass, CtClass superClass) {
        CtClass clz = createNestedClass(className, declaringClass);
        try {
            clz.setSuperclass(superClass);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return clz;
    }

}

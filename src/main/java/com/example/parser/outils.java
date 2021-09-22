package com.example.parser;

import sun.misc.Unsafe;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

public class outils {

    public static String firstCharUpperCase(String s){
        return s.substring(0,1).toUpperCase()+ s.substring(1);
    }
    public static Object createObject(final String className, ArrayList<String> attributes) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        final String path ="xmlparser";
        final String fullClassName = path.replace('.', '/') + "/" + className;
        final StringBuilder source = new StringBuilder();
        source.append("package "+path+";");
        source.append("public class " + className + " {\n");
        attributes.forEach(att->{
            source.append("public String "+ att+" ;");
            source.append("public void set"+firstCharUpperCase(att)+"(String s) {\n");
            source.append("this."+att+"=s;");
            source.append("}\n");
            source.append("public String get"+firstCharUpperCase(att)+"() {\n");
            source.append("return "+att+";");
            source.append("}\n");
        });
        String ss="\"["+className+"]\"+";
        for(int i=0;i<attributes.size();i++){
            if(i!=attributes.size()-1)
                ss=ss+" \"  "+attributes.get(i)+": \" + get"+firstCharUpperCase(attributes.get(i))+"()+";
            else
                ss=ss+" \"  "+attributes.get(i)+": \" + get"+firstCharUpperCase(attributes.get(i))+"()";

        }
        //System.out.println(ss);
        source.append("@Override ");
        source.append("public String toString() {\n");
        source.append("return "+ss+";");
        source.append("}\n");
        source.append("}\n");

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final SimpleJavaFileObject simpleJavaFileObject
                = new SimpleJavaFileObject(URI.create(fullClassName + ".java"), JavaFileObject.Kind.SOURCE) {

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                return byteArrayOutputStream;
            }
        };

        final JavaFileManager javaFileManager = new ForwardingJavaFileManager(
                ToolProvider.getSystemJavaCompiler().
                        getStandardFileManager(null, null, null)) {

            @Override
            public JavaFileObject getJavaFileForOutput(
                    Location location,String className,
                    JavaFileObject.Kind kind,
                    FileObject sibling) {
                return simpleJavaFileObject;
            }
        };

        ToolProvider.getSystemJavaCompiler().getTask(null, javaFileManager, null, null, null, Collections.singletonList(simpleJavaFileObject)).call();

        // then the bytes that make up the class are loaded into the class loader :

        final byte[] bytes = byteArrayOutputStream.toByteArray();

        // use the unsafe class to load in the class bytes
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");

        f.setAccessible(true);
        final Unsafe unsafe = (Unsafe) f.get(null);
        Class aClass = null;
        try {
            aClass = unsafe.defineClass(fullClassName, bytes, 0, bytes.length,ClassLoader.getSystemClassLoader(),null);
        }catch (Exception e){
            return null;
        }

        final Object o = aClass.newInstance();
        //System.out.println(o);

        return o;
    }
    public static Object executerMethode(Object objet, String nomMethode,
                                         Object[] parametres) throws Exception {
        Object retour;
        Class[] typeParametres = null;

        if (parametres != null) {
            typeParametres = new Class[parametres.length];
            for (int i = 0; i < parametres.length; ++i) {
                typeParametres[i] = parametres[i].getClass();
            }
        }

        Method m = objet.getClass().getMethod(nomMethode, typeParametres);
        if (Modifier.isStatic(m.getModifiers())) {
            retour = m.invoke(null, parametres);
        } else {
            retour = m.invoke(objet, parametres);
        }
        return retour;
    }
}

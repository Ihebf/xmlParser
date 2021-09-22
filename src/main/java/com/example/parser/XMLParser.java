package com.example.parser;

import com.example.exception.EmptyFileException;
import com.example.model.Student;
import sun.misc.Unsafe;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class XMLParser {

    private String filePath;
    private static ArrayList<Object> pTab = new ArrayList<>();

    public XMLParser(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public XMLParser(){}

    public String getXMLFile(){
        String line="";
        StringBuilder content= new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            while((line=bufferedReader.readLine())!=null){
                content.append(line);
            }
            if(content.toString().equals("")) throw new EmptyFileException("Empty file");
            return content.toString();
        } catch (IOException | EmptyFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getXMLFile(String url){
        String line="";
        StringBuilder content= new StringBuilder();

        BufferedReader bufferedReader;
        try {
            XMLParser xmlParser = new XMLParser(url);
            bufferedReader = new BufferedReader(new FileReader(xmlParser.getFilePath()));
            while((line=bufferedReader.readLine())!=null){
                content.append(line);
            }
            bufferedReader.close();
            if(content.toString().equals("")) throw new EmptyFileException("Empty file");
            return content.toString();
        } catch (IOException | EmptyFileException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean update(Object obj) throws Exception {
        boolean b = false;
        int id =0;
        try {
            id= Integer.parseInt((String) executerMethode(obj,"getId",null));
        }catch (Exception e){
            id= (int) executerMethode(obj,"getId",null);

        }
        for(int i=0;i<pTab.size();i++){
            Object a = pTab.get(i);
            System.out.println("-----------");
            System.out.println(showFields(a));
            int idd=0;
            try {
                idd= Integer.parseInt((String) executerMethode(a,"getId",null));
            }catch (Exception e){
                idd= (int) executerMethode(a,"getId",null);

            }
            if(id==idd){

                for (String att:showFields(obj)) {
                    System.out.println(att);

                    if(executerMethode(obj,"get"+firstCharUpperCase(att),null) != null && att !="id"){
                        try{
                            String at = (String) executerMethode(obj,"get"+firstCharUpperCase(att),null);
                            executerMethode(a,"set"+firstCharUpperCase(att), new Object[]{at});
                        }catch (Exception e){
                            try{
                                Integer at = (Integer) executerMethode(obj,"get"+firstCharUpperCase(att),null);
                                System.out.println(at);
                                executerMethode(a,"set"+firstCharUpperCase(att), new Object[]{Integer.toString(at)});
                            }catch (Exception e1){
                                Integer at = (Integer) executerMethode(obj,"get"+firstCharUpperCase(att),null);
                                System.out.println(at);
                                executerMethode(a,"set"+firstCharUpperCase(att), new Object[]{at.intValue()});
                            }
                        }
                    }
                }

                b=true;
            }
        }
        writeXml(filePath,this.listToXml(pTab));
        return b;
    }
    public String getChildElementByTagName(String baliseName,String chaine){
        Pattern p = Pattern.compile("<"+baliseName+">(.+?)</"+baliseName+">", Pattern.DOTALL);
        Matcher m = p.matcher(chaine);
        m.find();
        return m.group(1);
    }
    public ArrayList<String> getParentElementByTagName(String baliseName, String chaine){
        Pattern p = Pattern.compile("<"+baliseName+">(.+?)</"+baliseName+">", Pattern.DOTALL);
        Matcher m = p.matcher(chaine);
        ArrayList<String> list = new ArrayList<>();
        while (m.find()){
            list.add(m.group(1));
        }
        return list;
    }

    public ArrayList<Object> fillTable(List<?> list){
        pTab.addAll(list);
        return pTab;
    }

    public List<?> getElementsByTagName(String baliseName) throws Exception {

        ArrayList<String> attribut = new ArrayList<>();

        Pattern p = Pattern.compile("<"+baliseName+">(.+?)</"+baliseName+">", Pattern.DOTALL);
        String content = getXMLFile();
        Matcher m = p.matcher(content);

        Pattern p1 = Pattern.compile("</(.+?)>", Pattern.DOTALL);
        List<Object> list = new ArrayList<>();

        while (m.find()){
            String g = m.group(1);
            Matcher m1 = p1.matcher(g);
            while (m1.find()){
                String m11=m1.group(1);
                attribut.add(m11);
            }
        }
        m.reset();


        ArrayList<String> a = (ArrayList<String>) attribut.stream().distinct().collect(Collectors.toList());

        Object obj= createObject(baliseName,a);
        while (m.find()){
            String g = m.group(1);
            Matcher m1 = p1.matcher(g);
            Object obj1 = obj.getClass().newInstance();
            while (m1.find()){
                String m11=m1.group(1);
                executerMethode(obj1,"set"+firstCharUpperCase(m11),new Object[]{getChildElementByTagName(m11,g)});
            }
            list.add(obj1);
        }

        return list;
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

    public static List<String> showFields(Object o) {
        Class<?> c = o.getClass();
        List<String> fields= new ArrayList<>();

        for(Field field : c.getDeclaredFields()) {
            fields.add(field.getName());
        }
        return fields;
    }

    public void add(Object obj) throws Exception {
        pTab.add(obj);
        writeXml(filePath,this.listToXml(pTab));
    }

    public boolean delete(int id) throws Exception {
        boolean b = false;
        for(int i=0;i<pTab.size();i++){
            Object a = pTab.get(i);
            int idd=0;
            try {
                 idd= Integer.parseInt((String) executerMethode(a,"getId",null));
            }catch (Exception e){
                idd= (int) executerMethode(a,"getId",null);
            }
            if(id==idd){
                pTab.remove(pTab.get(i));
                i--;
                b=true;
            }
        }
        writeXml(filePath,this.listToXml(pTab));
        return b;
    }
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
            public OutputStream openOutputStream() throws IOException{
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
    public String listToXml(List<Object> list) throws Exception {
        String s ="<Personne>\n";
        for (Object l:list) {
            ArrayList<String> arrayList = (ArrayList<String>) showFields(l);
            s+="<"+l.getClass().getSimpleName()+">\n";
            for (String ss:arrayList ){
                s+="<"+ss+">"+executerMethode(l,"get"+firstCharUpperCase(ss),null)+"</"+ss+">\n";
            }
            s+="</"+l.getClass().getSimpleName()+">\n";
        }
        s+="</Personne>";
        return s;
    }
    public void writeXml(String filePath,String chain) throws IOException {
        System.out.println("----"+filePath);
        BufferedWriter bf=null;
        bf=new BufferedWriter(new FileWriter(filePath));
        bf.write(chain);
        bf.close();
    }
    public static void main(String[] args) throws Exception {
        XMLParser xmlParser = new XMLParser("C:\\Users\\ihfarhat\\Desktop\\ex.txt");
        //System.out.println(xmlParser.getXMLFile());
        List<?> list1 = xmlParser.getElementsByTagName("Student");

        List<?> list2 = xmlParser.getElementsByTagName("Employee");
        List<?> list3 = xmlParser.getElementsByTagName("Guest");

        xmlParser.fillTable(list1);
        xmlParser.fillTable(list2);
        xmlParser.fillTable(list3);
        Student s = new Student(1,"name",4);
        System.out.println(pTab);

        xmlParser.add(s);

        System.out.println(xmlParser.getXMLFile());

       // xmlParser.delete(1);
        xmlParser.update(new Student(1,"name1",5));
    }

}

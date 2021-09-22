package com.example.parser;

import com.example.exception.EmptyFileException;
import com.example.model.Student;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.parser.outils.*;

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
                                executerMethode(a,"set"+firstCharUpperCase(att), new Object[]{at});
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

    public void fillTable(List<?> list){
        pTab.addAll(list);
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



    public String ArrayToString(){
        String result = "";
        for (int i = 0; i <pTab.size() ; i++) {
            result += pTab.get(i).toString();
        }
        return result;
    }

    public String listToXml(List<Object> list) throws Exception {
        String s ="<Personne>\n";
        for (Object l:list) {
            ArrayList<String> arrayList = (ArrayList<String>) showFields(l);
            s+="    <"+l.getClass().getSimpleName()+">\n";
            for (String ss:arrayList ){
                s+="        <"+ss+">"+executerMethode(l,"get"+firstCharUpperCase(ss),null)+"</"+ss+">\n";
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

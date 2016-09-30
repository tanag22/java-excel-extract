/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallyproject;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
/**
 *
 * @author Rahul Sinha
 */
public class TallyProject {

    /**
     * @param args the command line arguments
     */
    public static String path;
    public static Vector<DATA> data;
    public static String UrlLink="http://tallyproject1.hol.es/";
    public static boolean init=true;
    public static BGProcess bgp;
    static HideToSystemTray jf;
    public static JTextArea jta;
    public static JButton jvb;
    
    public TallyProject(){
        try{
            DataInputStream db=new DataInputStream(new FileInputStream("path.txt"));
            path=db.readLine().trim();
            db.close();
            db=new DataInputStream(new FileInputStream("init.txt"));
            String in=db.readLine().trim();
            if(in.equals("true"))
                init=true;
            else
                init=false;
        }catch(Exception ex){
            jprintln("Error1 :"+ex.getMessage());
        }
    }
    public void runextraction(){
        if(init){
            jvb.setText("Initiating the Server...");
            jvb.setEnabled(false);
            data = new Vector<DATA>(100,10);
            try{
                DataInputStream db = new DataInputStream(new FileInputStream("LedgersAndGroups.dat"));
                while(true){
                    DATA d=new DATA(db);
                    if(d.name!=null)
                        data.addElement(d);
                    else
                        break;
                }
                for(int i=0;i<data.size();i++)
                    data.get(i).printData();
                db.close();
                new File("LedgersAndGroups.dat").delete();
            }catch(FileNotFoundException ex){
                extractLedgerGroup();
                extractVoucher();
            }catch(Exception ex){
                jprintln("Error555 : "+ex.getMessage());
            }
            
            while(true){
                try{
                    //jprintln("Wait");
                    if(checkEmpty()){
                        DataOutputStream db = new DataOutputStream(new FileOutputStream("init.txt"));
                        db.flush();
                        db.writeChars("false");
                        init=false;
                        runextraction();
                        break;
                    }
                    jprintln("Uploading the Ledgers and Group Data ...");
                    for(int i=0;i<data.size();i++){
                        //data.get(i).uploadData();
                    }
                    Thread.sleep(1000);
                }catch(Exception ex){
                    jprintln("Error53 :"+ex.getMessage());
                }
            }
        }else{
            jvb.setText("Save Vouchers and Exit");
            jvb.setEnabled(true);
            bgp= new BGProcess("Upload Vouchers");
            try{
                DataInputStream db = new DataInputStream(new FileInputStream("Vouchers.dat"));
                while(true){
                    Voucher d=new Voucher(db);
                    if(d.name!=null)
                        BGProcess.vch.addElement(d);
                    else
                        break;
                }
                for(int i=0;i<BGProcess.vch.size();i++)
                    BGProcess.vch.get(i).printData();
                db.close();
                new File("Vouchers.dat").delete();
                jprintln("Waiting for Vouchers Entry...");
            }catch(FileNotFoundException ex){}
            catch(Exception ex){
                jprintln("Error655 : "+ex.getMessage());
            }
            
            bgp.start();
        }
        
    }
    public static void jprintln(String str){
        //jta.setText(jta.getText()+str+"\n");
        System.out.println(str);
        jta.append(str+"\n");
        jta.setCaretPosition(jta.getDocument().getLength());
    }
    public static void jprintln(){
        System.out.println();
        jta.append("\n");
        jta.setCaretPosition(jta.getDocument().getLength());
    }
    public static void jprint(String str){
        System.out.print(str);
        jta.append(str);
        //jta.setCaretPosition(jta.getDocument().getLength());
    }
    static boolean checkEmpty(){
        for(int i=0;i<data.size();i++){
            if(!data.get(i).isUpl)
                return false;
        }
        return true;
    }
    public static void main(String[] args) {
        // TODO code application logic here
        TallyProject obj=new TallyProject();
        jf=new HideToSystemTray();
        Display jp = new Display();
        jta=jp.jTextArea1;
        jvb=jp.getVBut();
        jf.add(jp);
        //jf.setVisible(true);
        jf.setSize(500, 500);
        jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jf.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowIconified(WindowEvent e)
            {
                // add to the system tray if not already done so
                e.getWindow().setVisible(false);
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                         jf, "Are you sure you want to exit?", 
                         "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
                         JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    saveData();
                    System.exit(0);
                }
            }
        });
        obj.runextraction();
        
    }
    public static void saveData(){
        if(TallyProject.init){
            try{
                if(!TallyProject.checkEmpty()){
                    DataOutputStream db=new DataOutputStream(new FileOutputStream("LedgersAndGroups.dat"));
                    for(int i=0;i<TallyProject.data.size();i++){
                       TallyProject.data.get(i).writeData(db);
                    }
                    db.close();
                }
            }catch(Exception ex){
                System.out.println("Error99 :"+ex.getMessage());
            }
        }else{
            try{
                if(!BGProcess.checkEmpty()){
                    DataOutputStream db=new DataOutputStream(new FileOutputStream("Vouchers.dat"));
                    for(int i=0;i<BGProcess.vch.size();i++){
                       BGProcess.vch.get(i).writeData(db);
                    }
                    db.close();
                }
            }catch(Exception ex){
                System.out.println("Error99 :"+ex.getMessage());
            }
        }
        TallyProject.jf.dispose();
        
    }
    public static double convStr(String s){
        String str="";
        int strt=0;
        boolean pos=true;
        if(s.length()>=3)
        if(s.substring(0,3).equals("(-)")){
            strt=3;
            pos=false;
        }
        for(int i=strt;i<s.length();i++){
            if(s.charAt(i)!=',')
                str=str+s.charAt(i);
        }
        double d=0.0;
        try{
            if(str.isEmpty() || str.equals("-"))
                d=0.0;
            else
                d=Double.parseDouble(str);
            if(!pos)
                d=-1*d;
        }catch(Exception ex){
            jprintln("Error2 :"+ex.getMessage());
        }
        return d;
    }
    
    public void extractLedgerGroup(){
        try{
            File myFile = new File(path+"\\LedgersAndGroups.xlsx"); 
            FileInputStream fis = new FileInputStream(myFile); 
            // Finds the workbook instance for XLSX file 
            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis); 
            // Return first sheet from the XLSX workbook 
            for(int kk=0;kk<2;kk++){
                
                XSSFSheet mySheet = myWorkBook.getSheetAt(kk);
                // Get iterator to all the rows in current sheet 
                //System.out.println("\n\n"+mySheet.getSheetName()+"\n\n");
                Iterator<Row> rowIterator = mySheet.iterator(); 
                // Traversing over each row of XLSX file 
                while (rowIterator.hasNext()) { 
                    Row row = rowIterator.next();
                    // For each row, iterate through each columns 
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int ci = 0;
                    DATA d=new DATA();
                    while (cellIterator.hasNext()) { 
                        ci++;
                        Cell cell = cellIterator.next();
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        String val=cell.getStringCellValue().trim();
                        
                        if(ci==1){
                            d.name=val;
                            if(kk==0)
                                d.isLedger=true;
                            else 
                                d.isLedger=false;
                        }else if(ci==2){
                            d.parent=val;
                        }else if(ci==3){
                            d.opBal=convStr(val);
                        }else if(ci==4){
                            d.clBal=convStr(val);
                            data.addElement(d);
                        }
                    }
                    //System.out.println();
                }
            }

        }catch(Exception ex){
            //System.out.println("Error3 :"+ex.getMessage());
            jprintln("Error3 :"+ex.getMessage());
        }
    }
    
    public void extractVoucher(){
        try{
            File myFile = new File(path+"\\AllVouchers.xlsx"); 
            FileInputStream fis = new FileInputStream(myFile); 
            // Finds the workbook instance for XLSX file 
            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis); 
            // Return first sheet from the XLSX workbook 
            for(int kk=0;kk<1;kk++){
                
                XSSFSheet mySheet = myWorkBook.getSheetAt(kk);
                //System.out.println("\n\n"+mySheet.getSheetName()+"\n\n");
                int mon=-1;
                Iterator<Row> rowIterator = mySheet.iterator(); 
                while (rowIterator.hasNext()) { 
                    try{
                        Row row = rowIterator.next();
                        // For each row, iterate through each columns 
                        Iterator<Cell> cellIterator = row.cellIterator();
                        int ci=0;
                        int ind=-1;
                        while (cellIterator.hasNext()) { 

                            Cell cell = cellIterator.next();
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            String val = cell.getStringCellValue().trim();

                            if(ci==0){
                                if(isDate(val)){
                                    mon=mtoi(val.substring(val.indexOf('-')+1, val.lastIndexOf('-')));
                                    break;
                                }else{
                                    ind=search(val);
                                    if(ind==-1) break;
                                }
                            }else if(ci==1){
                                data.get(ind).month[mon][0]+=convStr(val);
                            }else if(ci==2){
                                data.get(ind).month[mon][1]+=convStr(val);
                            }

                            ci++;
                        }
                    }catch(Exception ex){
                        //System.out.println("Error35 :"+ex.getMessage());
                        jprintln("Error35 :"+ex.getMessage());
                    }
                }
            }

        }catch(Exception ex){
            //System.out.println("Error4 :"+ex.getMessage());
            jprintln("Error4 :"+ex.getMessage());
        }
        for(int i=0;i<data.size();i++)
            data.get(i).printData();
        
        //System.out.println("\n\n\nAll Datas Which are INVALID\n");
        jprintln("\n\n\nAll Datas Which are INVALID\n");
        for(int i=0;i<data.size();i++)
            data.get(i).validateD();
        
        //System.out.println("\n\n\nLets Check Uploading\n");
        jprintln("\n\n\nLets Check Uploading\n");
//        for(int i=0;i<data.size();i++)
//            data.get(i).uploadData();
    }
    int search(String ln){
        for(int i=0;i<data.size();i++){
            if(ln.equals(data.get(i).name))
                return i;
        }
        return -1;
    }
    static boolean isDate(String s){
        if(s.substring(0, 3).equals("~#~"))
            return true;
        return false;
    }
    static int mtoi(String s){
        if(s.equals("Jan"))
            return 0;
        else if(s.equals("Feb"))
            return 1;
        else if(s.equals("Mar"))
            return 2;
        else if(s.equals("Apr"))
            return 3;
        else if(s.equals("May"))
            return 4;
        else if(s.equals("Jun"))
            return 5;
        else if(s.equals("Jul"))
            return 6;
        else if(s.equals("Aug"))
            return 7;
        else if(s.equals("Sep"))
            return 8;
        else if(s.equals("Oct"))
            return 9;
        else if(s.equals("Nov"))
            return 10;
        else if(s.equals("Dec"))
            return 11;
        
        //System.out.println("Error String is "+s);
        jprintln("Error String is "+s);
        return -1;
    }

}
class DATA{
    String name,parent;
    double opBal,clBal;
    boolean isLedger;
    double month[][];
    boolean isUpl=false;
    public DATA(){
        name="";
        parent="";
        opBal=0.0;
        clBal=0.0;
        isLedger=true;
        month=new double[12][2];
    }
    public DATA(DataInputStream db){
        month=new double[12][2];
        try{
            name=db.readUTF();
            if(name==null)
                return;
            parent=db.readUTF();
            opBal=db.readDouble();
            clBal=db.readDouble();
            isLedger=db.readBoolean();
            for(int i=0;i<12;i++){
                month[i][0]=db.readDouble();
                month[i][1]=db.readDouble();
            }
        }catch(Exception ex){
            //System.out.println("Error :"+ex.getMessage());
            //TallyProject.jprintln("Error :"+ex.getMessage());
        }
    }
    public void writeData(DataOutputStream db){
        try{
            if(!isUpl){
                db.writeUTF(name);
                db.writeUTF(parent);
                db.writeDouble(opBal);
                db.writeDouble(clBal);
                db.writeBoolean(isLedger);
                for(int i=0;i<12;i++){
                    db.writeDouble(month[i][0]);
                    db.writeDouble(month[i][1]);
                }
            }
        }catch(Exception ex){
            //System.out.println("Error5 :"+ex.getMessage());
            TallyProject.jprintln("Error5 :"+ex.getMessage());
        }
        
    }
    public void printData(){
        //System.out.print(name+"\t"+parent+"\t"+opBal+"\t"+clBal+"\t");
        TallyProject.jprint(name+"\t"+parent+"\t"+opBal+"\t"+clBal+"\t");
        for(int i=0;i<12;i++){
            //System.out.print("Month "+i+" : "+month[i][0]+" , "+month[i][1]+"\t");
            TallyProject.jprint("Month "+i+" : "+month[i][0]+" , "+month[i][1]+"\t");
        }
        //System.out.println();
        TallyProject.jprintln();
    }
    public void uploadData(){
        String urlL=TallyProject.UrlLink+"uploadInit.php";
        if(!isUpl){
            try{
                StringBuffer sb=new StringBuffer(urlL);
                sb.append(aStr(true,"name",name))
                        .append(aStr(false,"parent",parent))
                        .append(aStr(false, "opBal", String.valueOf(opBal)))
                        .append(aStr(false, "clBal", String.valueOf(clBal)))
                        .append(aStr(false, "isLedger", String.valueOf(isLedger)));
                for(int i=0;i<12;i++){
                    sb.append(aStr(false, itom(i)+"Dr", String.valueOf(month[i][0])))
                            .append(aStr(false, itom(i)+"Cr", String.valueOf(month[i][1])));
                }
                URL url = new URL(sb.toString());
                HttpURLConnection hup = (HttpURLConnection) url.openConnection();
                hup.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(hup.getInputStream()));
                if(!br.readLine().trim().equals("Success")){
                    //System.out.print(" Error  : ");
                    TallyProject.jprint(" Error  : ");
                    printData();
                }else{
                    //System.out.println(name+" --  Success");
                    TallyProject.jprintln(name+" --  Success");
                    isUpl=true;
                }

            }catch(Exception ex){
                //System.out.println("Error56: "+ex.getMessage());
                TallyProject.jprintln("Error56: "+ex.getMessage());
                //System.out.print("The error is ");
                TallyProject.jprint("The error is ");
                printData();
            }
        }
    }
    
    
    public String aStr(boolean first,String data,String val){
        String str="";
        try{
            if(first){
                str="?"+URLEncoder.encode(data,"UTF-8")+"="+URLEncoder.encode(val,"UTF-8");
            }else{
                str="&"+URLEncoder.encode(data,"UTF-8")+"="+URLEncoder.encode(val,"UTF-8");
            }
        }catch(Exception ex){
            //System.out.println("Error55 : "+ex.getMessage());
            TallyProject.jprintln("Error55 : "+ex.getMessage());
        }
        return str;
    }
    public boolean validateD(){
        double sum=opBal;
        for(int i=0;i<12;i++){
            sum+=(month[i][0]-month[i][1]);
        }
        if(checkEq(sum) || !isLedger)
            return true;
        //System.out.println(name+"\t"+parent+"\t"+opBal+"\t"+clBal+"\t"+sum);
        TallyProject.jprintln(name+"\t"+parent+"\t"+opBal+"\t"+clBal+"\t"+sum);
        return false;
    }
    boolean checkEq(double s){
        //long sum = Math.abs(Math.round(s));
        if(Math.round(s)==Math.round(clBal))
            return true;
        else return false; 
    }
    String itom(int i){
        switch(i){
            case 0:
                return "Jan";
            case 1:
                return "Feb";
            case 2:
                return "Mar";
            case 3:
                return "Apr";
            case 4:
                return "May";
            case 5:
                return "Jun";
            case 6:
                return "Jul";
            case 7:
                return "Aug";
            case 8:
                return "Sep";
            case 9:
                return "Oct";
            case 10:
                return "Nov";
            case 11:
                return "Dec";
        }
        return "";
    }
}
class UpProcess implements Runnable{
    Thread t;
    String Tname;
    public UpProcess(String n){
        Tname=n;
    }
    
    @Override
    public void run() {
        while(true){
            try{

                TallyProject.jprintln("Uploading Data ....");
                for(int i=0;i<BGProcess.vch.size();i++)
                    BGProcess.vch.get(i).uploadData();
                Thread.sleep(5000);

            }catch(InterruptedException ex){
                TallyProject.jprintln("Error997 : "+ex.getMessage());
            }
            catch(Exception ex){
                TallyProject.jprintln("Error998 : "+ex.getMessage());
            }
        }
    }
    public void start(){
        if(t==null){
            t=new Thread(this, Tname);
            t.start();
        }
    }
}

class BGProcess implements Runnable{
    Thread t;
    String Tname;
    public static Vector<Voucher> vch;
    
    public BGProcess(String n){
        Tname=n;
        vch=new Vector<Voucher>(100,10);
        try{
            UpProcess upl=new UpProcess("Upload Process");
            upl.start();
        }catch(Exception ex){
            TallyProject.jprintln("Error999 : "+ex.getMessage());
        }
        
    }
    @Override
    public void run() {
        try{
            while(true){
                extractVoucher();
                Thread.sleep(1000);
            }
        }catch(Exception ex){
            //System.out.println("Thread Err : "+ex.getMessage());
            TallyProject.jprintln("Thread Err : "+ex.getMessage());
        }
    }
    static boolean checkEmpty(){
        for(int i=0;i<vch.size();i++){
            if(!vch.get(i).isUpl)
                return false;
        }
        return true;
    }
    public void start(){
        if(t==null){
            t=new Thread(this, Tname);
            t.start();
        }
    }
    public void stop(){
        t.stop();
    }
    public void extractVoucher(){
        try{
            File myFile = new File(TallyProject.path+"\\Voucher.xlsx"); 
            FileInputStream fis = new FileInputStream(myFile); 
            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis); 

            for(int kk=0;kk<1;kk++){
                
                XSSFSheet mySheet = myWorkBook.getSheetAt(kk);
                //System.out.println("\n\n"+mySheet.getSheetName()+"\n\n");
                String mon="";
                Iterator<Row> rowIterator = mySheet.iterator(); 
                while (rowIterator.hasNext()) { 
                    try{
                        Row row = rowIterator.next();
                        // For each row, iterate through each columns 
                        Iterator<Cell> cellIterator = row.cellIterator();
                        int ci=0;
                        int ind=-1;
                        while (cellIterator.hasNext()) { 

                            Cell cell = cellIterator.next();
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            String val = cell.getStringCellValue().trim();

                            if(ci==0){
                                if(TallyProject.isDate(val)){
                                    mon=val.substring(val.indexOf('-')+1, val.lastIndexOf('-'));
                                    break;
                                }else{
                                    vch.addElement(new Voucher(mon,val));
                                }
                            }else if(ci==1){
                                vch.get(vch.size()-1).dr=TallyProject.convStr(val);
                            }else if(ci==2){
                                vch.get(vch.size()-1).cr=TallyProject.convStr(val);
                                vch.get(vch.size()-1).printData();
                            }

                            ci++;
                        }
                    }catch(Exception ex){
                        //System.out.println("Error35 :"+ex.getMessage());
                        TallyProject.jprintln("Error35 :"+ex.getMessage());
                    }
                }
            }
            myWorkBook.close();
            fis.close();
            if(!myFile.delete()){
                //System.out.println("Voucher File could not be deleted");
                TallyProject.jprintln("Voucher File could not be deleted");
            }
        }catch(FileNotFoundException ex){}
        catch(Exception ex){
            //System.out.println("Error8 :"+ex.getMessage());
            TallyProject.jprintln("Error8 :"+ex.getMessage());
        }
        
    }
    
}


class Voucher{
    String month,name;
    double dr,cr;
    boolean isUpl=false;
    public Voucher(String mon, String n){
        month=mon;
        name=n;
    }
    
    public Voucher(DataInputStream db){
        try{
            name=db.readUTF();
            if(name==null)
                return;
            month=db.readUTF();
            dr=db.readDouble();
            cr=db.readDouble();
        }catch(Exception ex){
            //System.out.println("Error90 : "+ex.getMessage());
            //TallyProject.jprintln("Error90 : "+ex.getMessage());
        }
        
    }
    public void writeData(DataOutputStream db){
        try{
            db.writeUTF(name);
            db.writeUTF(month);
            db.writeDouble(dr);
            db.writeDouble(cr);
        }catch(Exception ex){
            //System.out.println("Error9 : "+ex.getMessage());
            TallyProject.jprintln("Error9 : "+ex.getMessage());
        }
    }
    public void uploadData(){
        String urlL=TallyProject.UrlLink+"upload.php";
        if(!isUpl){
            try{
                StringBuffer sb=new StringBuffer(urlL);
                sb.append(aStr(true,"month",month))
                        .append(aStr(false,"name",name))
                        .append(aStr(false, "dr", String.valueOf(dr)))
                        .append(aStr(false, "cr", String.valueOf(cr)));
                URL url = new URL(sb.toString());
                HttpURLConnection hup = (HttpURLConnection) url.openConnection();
                hup.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(hup.getInputStream()));
                while(true){
                    String val =br.readLine();
                    if(val==null){
                        isUpl=true;
                        break;
                    }
                    //System.out.println(val);
                    TallyProject.jprintln(val);
                }

            }catch(Exception ex){
                //System.out.println("Error7: "+ex.getMessage());
                TallyProject.jprintln("Error7: "+ex.getMessage());
                //System.out.print("The error is ");
                TallyProject.jprint("The error is ");
            }
        }
    }
    
    public String aStr(boolean first,String data,String val){
        String str="";
        try{
            if(first){
                str="?"+URLEncoder.encode(data,"UTF-8")+"="+URLEncoder.encode(val,"UTF-8");
            }else{
                str="&"+URLEncoder.encode(data,"UTF-8")+"="+URLEncoder.encode(val,"UTF-8");
            }
        }catch(Exception ex){
            //System.out.println("Error55 : "+ex.getMessage());
            TallyProject.jprintln("Error55 : "+ex.getMessage());
        }
        return str;
    }
    public void printData(){
        if(!isUpl)
        //System.out.println(month+"\t"+name+"\t"+dr+"\t"+cr+"\t");
        TallyProject.jprintln(month+"\t"+name+"\t"+dr+"\t"+cr+"\t");
    }
    
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallyproject;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
        initiateMenu();
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
    
    public static void initiateMenu(){
        JMenuBar menuBar=new JMenuBar();
        JMenu menu=new JMenu("File");

        JMenuItem menuItem1=new JMenuItem("Initiate Server");
        JMenuItem menuItem2=new JMenuItem("Add Companies");
        JMenuItem menuItem3=new JMenuItem("View Companies");
        
        menu.add(menuItem1);
        menu.add(menuItem2);
        menu.add(menuItem3);
        
        menuBar.add(menu);
        jf.setJMenuBar(menuBar);
        
        menuItem1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        menuItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        menuItem3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallyproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Rahul Sinha
 * 
 * This Class searches for any recent uploads of vouchers
 */
public class BGProcess implements Runnable{
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
                String date="";
                String company="";
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
                                    String day=val.substring(3, val.indexOf('-'));
                                    String mon=val.substring(val.indexOf('-')+1, val.lastIndexOf('-'));
                                    String year=val.substring(val.lastIndexOf('-')+1, val.length());
                                    date=year+"/"+(TallyProject.mtoi(mon)+1)+"/"+day;
                                    Date d=new Date(Integer.parseInt(year)-1900,TallyProject.mtoi(mon),Integer.parseInt(day));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                    date = dateFormat.format(d);
                                    cell = cellIterator.next();
                                    cell.setCellType(Cell.CELL_TYPE_STRING);
                                    company = cell.getStringCellValue().trim();
                                    break;
                                }else{
                                    vch.addElement(new Voucher(date,val,company));
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

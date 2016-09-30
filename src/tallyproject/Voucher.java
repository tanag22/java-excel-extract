/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallyproject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author Rahul Sinha
 */
public class Voucher{
    String date,name,company;
    double dr,cr;
    boolean isUpl=false;
    public Voucher(String date, String n, String company){
        //month=mon;
        name=n;
    }
    
    public Voucher(DataInputStream db){
        try{
            name=db.readUTF();
            if(name==null)
                return;
            company=db.readUTF();
            date=db.readUTF();
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
            db.writeUTF(company);
            db.writeUTF(date);
            db.writeDouble(dr);
            db.writeDouble(cr);
        }catch(Exception ex){
            //System.out.println("Error9 : "+ex.getMessage());
            TallyProject.jprintln("Error9 : "+ex.getMessage());
        }
    }
    public void uploadData(){
        
        //Send Date
        
        String urlL=TallyProject.UrlLink+"upload.php";
        if(!isUpl){
            try{
                StringBuffer sb=new StringBuffer(urlL);
                sb.append(aStr(true,"date",date))
                        .append(aStr(false,"company",company))
                        .append(aStr(false,"name",name))
                        .append(aStr(false, "change", String.valueOf((dr-cr))));
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
        TallyProject.jprintln(date+"\t"+name+"\t"+dr+"\t"+cr+"\t");
    }
    
}
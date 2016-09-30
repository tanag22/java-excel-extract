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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Rahul Sinha
 */
public class DATA{
    String company;
    String name,parent;
    double clBal;
    boolean isLedger;
    boolean isUpl=false;
    public DATA(String comp){
        company=comp;
        name="";
        parent="";
        clBal=0.0;
        isLedger=true;
    }
    public DATA(DataInputStream db){
        try{
            company=db.readUTF();
            name=db.readUTF();
            if(name==null)
                return;
            parent=db.readUTF();
            clBal=db.readDouble();
            isLedger=db.readBoolean();
        }catch(Exception ex){
            //System.out.println("Error :"+ex.getMessage());
            //TallyProject.jprintln("Error :"+ex.getMessage());
        }
    }
    public void writeData(DataOutputStream db){
        try{
            if(!isUpl){
                db.writeUTF(company);
                db.writeUTF(name);
                db.writeUTF(parent);
                db.writeDouble(clBal);
                db.writeBoolean(isLedger);
            }
        }catch(Exception ex){
            //System.out.println("Error5 :"+ex.getMessage());
            TallyProject.jprintln("Error5 :"+ex.getMessage());
        }
        
    }
    public void printData(){
        //System.out.print(name+"\t"+parent+"\t"+opBal+"\t"+clBal+"\t");
        TallyProject.jprint(company+"\t"+name+"\t"+parent+"\t"+clBal+"\t");
    }
    public void uploadData(){
        
        String urlL=TallyProject.UrlLink+"php/uploadInit.php";
        if(!isUpl){
            String table=checkCompany();
            if(table!=null){
                try{
                    StringBuffer sb=new StringBuffer(urlL);
                    sb.append(aStr(true,"name",name))
                            .append(aStr(false,"company",table))
                            .append(aStr(false,"parent",parent))
                            .append(aStr(false, "clBal", String.valueOf(clBal)))
                            .append(aStr(false, "date", getDate()))
                            .append(aStr(false, "isLedger", String.valueOf(isLedger)));
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
        }else{
            TallyProject.jprint("The Company is not Registered in the software");
        }
    }
    private String checkCompany(){
        for(int i=0;i<TallyProject.company.size();i++){
            Company comp=TallyProject.company.get(i);
            if(comp.name.equals(company))
                return comp.uname;
        }
        return null;
    }
    
    public String getDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
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

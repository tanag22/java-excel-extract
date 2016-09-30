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
public class DATA{
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

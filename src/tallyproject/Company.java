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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 *
 * @author Rahul Sinha
 */
public class Company {
    String name, uname, owner;
    public Company(){
        name=null;
        uname=null;
        owner=null;
    }
    public Company(String n, String u, String o){
        name=n;
        uname=u;
        owner=o;
    }
    public Company(DataInputStream db){
        try{
            name=db.readUTF();
            uname=db.readUTF();
            owner=db.readUTF();
        }catch(Exception ex){
            TallyProject.jprintln(ex.getMessage());
        }
    }
    public static Company checkCompany(String user, String password){
//        JDialog jd=new JDialog(TallyProject.jf, "Loading",true);
//        JProgressBar jpb=new JProgressBar();
//        jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        jd.setSize(200,200);
//        jd.add(jpb);
//        jd.setVisible(true);
        
        String urlL=TallyProject.UrlLink+"php/validUser.php";
        try{
            StringBuffer sb=new StringBuffer(urlL);
            sb.append(aStr(true,"user",user))
                    .append(aStr(false,"pass",password));
            URL url = new URL(sb.toString());
            HttpURLConnection hup = (HttpURLConnection) url.openConnection();
            hup.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(hup.getInputStream()));
            Company comp=new Company();
            String val =br.readLine();
            //jd.dispose();
            if(val.equals("Success")){
                val =br.readLine();
                comp.name=val;
                val =br.readLine();
                comp.uname=val;
                val =br.readLine();
                comp.owner=val;
                return comp;
            }else{
                TallyProject.jprintln("ErrorX: "+val);
                return null;
            }

        }catch(Exception ex){
            TallyProject.jprintln("ErrorX: "+ex.getMessage());
        }
        //jd.dispose();
        return null;
    }
    
    public static String aStr(boolean first,String data,String val){
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

    public void writeLocal(DataOutputStream db){
        try{
            db.writeUTF(name);
            db.writeUTF(uname);
            db.writeUTF(owner);
        }catch(Exception ex){
            TallyProject.jprintln(ex.getMessage());
        }
    }
    public void readLocal(DataInputStream db){
        try{
            name=db.readUTF();
            uname=db.readUTF();
            owner=db.readUTF();
        }catch(Exception ex){
            TallyProject.jprintln(ex.getMessage());
        }
    }
}

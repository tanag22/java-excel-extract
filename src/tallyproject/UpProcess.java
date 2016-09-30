/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallyproject;

/**
 *
 * @author Rahul Sinha
 * 
 * This Class Keeps on Searching for net and uploading DATA
 * 
 */
public class UpProcess implements Runnable{
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

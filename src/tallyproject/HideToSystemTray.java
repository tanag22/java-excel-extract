/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tallyproject;

/**
 *
 * @author Rahul Sinha
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class HideToSystemTray extends JFrame{
    TrayIcon trayIcon;
    SystemTray tray;
    HideToSystemTray(){
        super("Sync Your Tally with your Mobile");
        System.out.println("creating instance");
        try{
            System.out.println("setting look and feel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            System.out.println("Unable to set LookAndFeel");
        }
        if(SystemTray.isSupported()){
            System.out.println("system tray supported");
            tray=SystemTray.getSystemTray();

            Image image=Toolkit.getDefaultToolkit().getImage("Icon.jpg");
            ActionListener exitListener=new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting....");
                    TallyProject.saveData();
                    System.exit(0);
                }
            };
            PopupMenu popup=new PopupMenu();
            MenuItem defaultItem=new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            defaultItem=new MenuItem("Open");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                }
            });
            popup.add(defaultItem);
            trayIcon=new TrayIcon(image, "Sync Tally", popup);
            trayIcon.setImageAutoSize(true);
        }else{
            System.out.println("system tray not supported");
        }
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState()==ICONIFIED){
                    hideInTray();
                }
                if(e.getNewState()==7){
                    hideInTray();
                }
                if(e.getNewState()==MAXIMIZED_BOTH){
                    removeFromTray();
                }
                if(e.getNewState()==NORMAL){
                    removeFromTray();
                }
            }
        });
        setIconImage(Toolkit.getDefaultToolkit().getImage("Icon.jpg"));

        exhideInTray();
    }
    public void hideInTray(){
        try {
            tray.add(trayIcon);
            setVisible(false);
            System.out.println("added to SystemTray");
        } catch (AWTException ex) {
            System.out.println("unable to add to tray");
        }        
    }
    public void exhideInTray(){
        try {
            tray.add(trayIcon);
            setVisible(false);
            System.out.println("added to SystemTray");
            setState(ICONIFIED);
        } catch (AWTException ex) {
            System.out.println("unable to add to tray");
        }        
    }
    public void removeFromTray(){
        tray.remove(trayIcon);
        setVisible(true);
        System.out.println("Tray icon removed");
    }
}
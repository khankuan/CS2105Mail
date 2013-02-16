package cs2105mail;

import java.io.FileNotFoundException;
import java.io.IOException;

//  Main class to start GUI
public class main {
    //  variables
    private static MailClient mc = new MailClient();
    private static SentMails sm = new SentMails();
    private static Inbox inb;
    private static boolean inboxStarted = false;
    private static String fileName = "sentMails.txt";   //  default file name
    
    @SuppressWarnings("static-access")
    public static void sendMail(String smtp, String username, String password, 
        String fr, String to, String sub, String bod) throws Exception{              
        try {       
            //  If different account is being used
            if (!mc.isAlreadyLoggedIn(smtp, username, password)){
                if (mc.isConnected())
                    mc.logout();
                //  Start connection with encoded username and password
                mc.login(smtp,Base64.encodeString(username),
                    Base64.encodeString(password));
            }
            
            //  Send mail then logout
            mc.send(fr, to, sub, bod);          
            
            //  Save in save file for sent mails
            sm = new SentMails();
            sm.save(fileName, fr, to, sub, bod, smtp);
            
        } catch (IOException ex){
            if (smtp.length() == 0)
                throw new Exception("Invalid SMTP server.");
            else
                throw new Exception("I/O error.");
        }
    }
    
    public static void loadSavedSentFile() throws Exception{
        sm.open(fileName);  //  open saved file to sm     
    }
    
    public static String[] getSentMailHeaders(int cur){ //  retrieve sent mail headers
        int i = sm.getMailCount()-cur-1;    //  reverse order, new mails at front
        return new String[]{sm.getFromEmail(i),sm.getToEmail(i),
            sm.getSubject(i),sm.getSMTP(i)};
    }
    
    public static String getSentMail(int cur){  //  retrieve ith sent item
        int i = sm.getMailCount()-cur-1;    //  reverse order, new mails at front
        return sm.getBody(i);
    }
    
    public static int getSentMailCount(){   //  returns number of sent items
        return sm.getMailCount();
    }
    
    public static void retrieveInbox(String pop, String user, String pass) throws Exception{
        //  Initialise Inbox object with headers
        inboxStarted = false;
        inb = new Inbox(user,pass,pop);
        inboxStarted = true;
    }
    
    public static int getInboxCount(){  //  returns number of mails in inbox
        return inb.getInboxCount();
    }
    
    public static int getInboxTotalSize(){  //  returns total inbox size
        return inb.getInboxSize();
    }
    
    public static boolean isInboxStarted(){ //  check is inbox started
        return inboxStarted;
    }
    
    public static Object[] getInboxMailHeaders(int cur){    //  retrieve headers for inbox
        int i = inb.getInboxCount()-cur-1;    //  reverse order, new mails at front
        return new Object[]{inb.getFromEmail(i),inb.getSubject(i),
            inb.getDate(i),inb.getSize(i)};
    }
    
    public static String readMail(int cur) throws Exception{  //  retrieve ith mail
        //  Update selection of inbox mails and retrieve
        try {
            int i = inb.getInboxCount()-cur;    //  reverse order, new mails at front
            return inb.getMail(i);
        } catch (IOException ex){
            throw new Exception("I/O error while retrieving mail!");
        }
    }
    
    private static void createAndShowGUI() {    //  setup gui
        //Create and set up the window.
        MailGUI frame = new MailGUI();
        frame.setTitle("CS2105Mail");
        frame.setDefaultCloseOperation(MailGUI.EXIT_ON_CLOSE);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
                public void run() {
                    //  Start GUI
                    createAndShowGUI();
                }
        });  
    }
}

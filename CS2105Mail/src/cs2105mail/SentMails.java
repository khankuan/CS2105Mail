package cs2105mail;

import java.io.*;
import java.util.*;

public class SentMails {
    // Variables of sent mails
    private static int mailCount;
    private static Vector<String> fromEmails;
    private static Vector<String> toEmails;
    private static Vector<String> subjects;
    private static Vector<String> bodys;
    private static Vector<String> smtps;
    
    //  get sent mails info
    public String getFromEmail(int i) {return fromEmails.get(i);}
    public String getToEmail(int i) {return toEmails.get(i);}
    public String getSubject(int i) {return subjects.get(i);}
    public String getBody(int i) {return bodys.get(i);}
    public String getSMTP(int i) {return smtps.get(i);}
    public int getMailCount(){ return mailCount;}
   
    /*  save newly sent items to sentMail.txt   */
    public void save(String fileName, String from, String to, String sub,
            String bod, String smtp) throws Exception{

        FileOutputStream file;   
        //  Opens file in append mode, make sure file is available
        try {
            file = new FileOutputStream(fileName,true);
        }catch (FileNotFoundException exp){
            throw new Exception("File not found!");
        }catch (SecurityException exp1){
            throw new Exception("Unable to open file.");
        }
        
        //  Appending message to file
        try {
            file.write(from.getBytes());
            file.write("\n".getBytes());
            file.write(to.getBytes());
            file.write("\n".getBytes());
            file.write(sub.getBytes());
            file.write("\n".getBytes());
            file.write(smtp.getBytes());
            file.write("\n".getBytes());   
            file.write(bod.getBytes());
            file.write("\n\0\n".getBytes());    //  Indicate end of body
            file.close();   //  close file after written
        } catch (IOException exp){
            throw new Exception("IO error.");
        }
    }
    
    /*  open sentMail.txt  */
    public void open(String fileName) throws Exception, IOException{
        //  Initialise variables
        fromEmails = new Vector<String>();
        toEmails = new Vector<String>();
        subjects = new Vector<String>();
        bodys = new Vector<String>();
        smtps = new Vector<String>();
        mailCount = 0;
        
        //  Open file to read sent mails, make sure file is available
        FileInputStream file;
        try {
            file = new FileInputStream(fileName);
        }catch (FileNotFoundException exp){
            throw new Exception("File not found!");
        }catch (SecurityException exp1){
            throw new Exception("Unable to open file!");
        }
        //  Start reading
        DataInputStream in = new DataInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = br.readLine();
        while (strLine != null){
            //  Add for every mail item
            fromEmails.add(strLine);
            toEmails.add(br.readLine());
            subjects.add(br.readLine());
            smtps.add(br.readLine());
            strLine = br.readLine();
            
            //  Adding of body
            bodys.add(mailCount,"");
            while(!strLine.equals("\0")){    // check for end of body
                if (bodys.get(mailCount).equals(""))
                    bodys.add(mailCount, strLine);
                else
                    bodys.add(mailCount, bodys.get(mailCount)+"\n"+strLine);
                strLine = br.readLine();
            }
            //  Add SMTP server used to send email
            strLine = br.readLine();
            mailCount++;
        }
        file.close();   //  close sent mails file
    }

}

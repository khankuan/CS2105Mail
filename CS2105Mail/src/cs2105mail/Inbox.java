package cs2105mail;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Inbox {
    //  Variables for inbox
    private static int mailCount;
    private static int totalSize;
    private static String username;
    private static String password;
    private static String pop3;
    private static Vector<Integer> sizes;
    private static Vector<String> fromEmails;
    private static Vector<String> subjects;
    private static Vector<Date> dates;
    private static TreeMap<Integer,String> mails;
    private static Socket soc;
    private static OutputStream os;
    private static DataOutputStream serverWriter; //to write string,otherwise, need to write byte array
    private static InputStreamReader isrServer;
    private static BufferedReader serverReader;
    
    
    //  get mail info
    public int getInboxCount(){return mailCount;}
    public int getInboxSize(){return totalSize;}
    public int getSize(int i){return sizes.get(i);}
    public String getFromEmail(int i){return fromEmails.get(i);}
    public String getSubject(int i){return subjects.get(i);}
    public Date getDate(int i){return dates.get(i);}
    
    /*  close connection    */
    private void quit() throws Exception{
        //  Function to quit from connected host
        try{
            if (soc == null || !soc.isConnected())  //  check if socket is open
                throw new Exception("Socket not connected!");
            
            //  Quit and close
            serverWriter.writeBytes("QUIT"+"\r\n");
            serverReader.close();
            serverWriter.close();
            soc.close();
        } catch(IOException exp) {} //  Exception for remaining InputBuffer
    }
    
    /*  start connection    */
    private void start() throws Exception{
        try {
            //  Open socket
            soc = new Socket(pop3,110);
            
            // Declare data stream object
            os = soc.getOutputStream();
            serverWriter = new DataOutputStream(os); //to write string,otherwise, need to write byte array
            
            // Open input stream to read from server
            isrServer = new InputStreamReader(soc.getInputStream());
            serverReader = new BufferedReader(isrServer);
            
            if(!serverReader.readLine().substring(0, 3).equals("+OK"))   // Connected message
                throw new Exception("Error while connecting.");
            
            //  Login with username and password
            serverWriter.writeBytes("USER "+username+"\r\n");
            serverReader.readLine();     // Reply from server to ask for password         
            serverWriter.writeBytes("PASS "+password+"\r\n");
            String buffer = serverReader.readLine();
            //  Check if login is successful
            if (!buffer.equals("+OK User successfully logged on.")){
                throw new Exception("Authentication unsuccessful.");              
            }
        } catch(UnknownHostException obj){ //  Exception to unknown host
            throw new Exception("Unknown host.");
        } catch(IOException obj){  //  Exception to invalid host
            throw new Exception("Invalid host.");
        } catch(SecurityException obj){  //  Exception to invalid host
            throw new Exception("Security error.");
        }        
    }
    
    /*  retr a single mail  */
    public String getMail(int i) throws IOException, Exception{
        //  Retrieve i-th mail that is not cached
        if (!mails.containsKey(i)){
            //  retrieve mail
            serverWriter.writeBytes("RETR "+(i)+"\r\n");
            if (!(isrServer.read() == '+' && isrServer.read() == 'O' && isrServer.read() == 'K'))    //  read +OK line
                throw new Exception("Error while retrieving mail.");
            
            //  Reading of reply from server
            StringBuilder thisMail = new StringBuilder();
            int buffer5 = isrServer.read();
            int buffer4 = isrServer.read();
            int buffer3 = isrServer.read();
            int buffer2 = isrServer.read();
            int buffer1 = isrServer.read();
            
            //  Breaks when break sequence found
            while (!(buffer1 == 10 && buffer2 == 13 && buffer3 == 46 && buffer4 == 10 && buffer5 == 13)){
                thisMail.append((char)buffer5);
                buffer5 = buffer4;
                buffer4 = buffer3;
                buffer3 = buffer2;
                buffer2 = buffer1;
                buffer1 = isrServer.read();
            }
            
            //  Remove headers
            thisMail.delete(0, thisMail.indexOf("MIME-Version:"));
            thisMail.delete(0, thisMail.indexOf("\n")+1);
            //thisMail = thisMail.replace(thisMail.indexOf("MIME-Version:"),thisMail.length());            //  Store mail to mails
            mails.put(i, new String(thisMail));   
        }
        
        //  Return mail in String
        return mails.get(i);
    }
       
    /*  login and list inbox    */
    public Inbox(String user, String pass, String pop) throws Exception{
        //  Initialise variables
        username = user;
        password = pass;
        pop3 = pop;
        sizes = new Vector<Integer>();
        fromEmails = new Vector<String>();
        subjects = new Vector<String>();
        dates = new Vector<Date>();
        mails = new TreeMap<Integer,String>();
        mailCount = 0;
        totalSize = 0;      
        
        //  Open socket
        start();
        
        //  Get number of emails and total size
        serverWriter.writeBytes("STAT\r\n");
        String buffer = serverReader.readLine();            
        mailCount = Integer.parseInt(buffer.substring(4,4+buffer.substring(4).indexOf(" ")));
        totalSize = Integer.parseInt(buffer.substring(buffer.lastIndexOf(" ")+1,buffer.length()));

        //  List each email index with its size
        serverWriter.writeBytes("LIST\r\n"); 
        serverReader.readLine();    //  reply from server
        for (int i = 0 ; i < mailCount; i++){
            String listed = serverReader.readLine();
            sizes.add(Integer.parseInt(listed.substring(listed.indexOf(" ")+1)));
        }

        //  Get top headers of each email
        int headGotten = 0;
        while (headGotten < mailCount){
            int limit = 500;    // Set a limit for reconnect
            int i;
            for (i = 0 ; i < limit; i++){   
                headGotten++;
                serverWriter.writeBytes("TOP "+(headGotten)+" 0\r\n");
                if (headGotten >= mailCount){
                    i++;
                    break;
                }
            }

            //  Store the useful mail headers
            int mailsProcessed = 0;
            while (mailsProcessed < i){
                buffer = serverReader.readLine(); //   read +OK line
                if (buffer.length() >= 5){
                    if (buffer.substring(0,5).equals("From:"))   // fromEmails
                        fromEmails.add(buffer.substring(5));
                    if (buffer.substring(0,5).equals("Date:")){   // dates
                        String d = buffer.substring(6);
                        SimpleDateFormat t = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"); 
                        dates.add(t.parse(d));
                    }
                }
                if (buffer.length() >= 8){
                    if (buffer.substring(0,8).equals("Subject:")){
                        subjects.add(buffer.substring(8));  // subjects
                        mailsProcessed++;
                    }
                }
            }         
            //quit();
            //start();    // restart connection
        } 
    }
}

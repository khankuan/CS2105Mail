package cs2105mail;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class MailClient {
        //  Variables of mail
        private String fromEmail;
        private String toEmail;
        private String subject;
        private String body;
        private String smtp;
        private String username;
        private String password;
        private static Socket s;
        private static DataOutputStream serverWriter;
        private static BufferedReader serverReader;
        
        //  Check if username, password and smtp server are the same
        public boolean isAlreadyLoggedIn(String smtp1, String user1, String pass1){
            if (s != null)
                return smtp1.equals(smtp)&&user1.equals(username)&&pass1.equals(password);
            else
                return false;
        }

        //  Check if MailClient is connected
        public boolean isConnected(){
            if (s != null)
                return s.isConnected();
            return false;
        }
        
        //  Send email from client
        public void send(String fr, String to, String sub, String bod) throws Exception {
            //  Initialise inputs
            fromEmail = fr;
            toEmail = to;
            subject = sub;
            body = bod;
            
            //  Start sending of mail
            serverWriter.writeBytes("MAIL FROM:"+fromEmail+"\r\n");
            String buffer = serverReader.readLine();

            //  Check if FROM address is valid
            if (buffer.equals("501 5.5.4 Invalid Address"))
                throw new Exception("Invalid sender address.");

            //  Sending receipent's email
            serverWriter.writeBytes("RCPT TO:"+toEmail+"\r\n");
            buffer = serverReader.readLine();

            //  Check if TO address is valid
            if (buffer.equals("501 5.5.4 Invalid Address"))
                throw new Exception("Invalid recipient address.");

            //  Sending DATA of mail
            serverWriter.writeBytes("DATA"+"\r\n");
            serverReader.readLine();        //  DATA response   
            serverWriter.writeBytes("From:"+fromEmail+"\r\n"+"To:"+toEmail+"\r\n"+"Subject:"+subject+"\r\n"+body+"\r\n"+"."+"\r\n");
            String response = serverReader.readLine();

            //  Check if mail is successfully queued
            if (!response.contains("Queued mail for delivery"))
                throw new Exception("Unknown sending error.");
        }

        //  Creating MailClient with login info
        public void login(String sm, String un, String pw) throws Exception {
            //  initialise variables
            smtp = sm;
            username = un;
            password = pw;

            //  Open socket
            try{
                s = new Socket(smtp,25);
            } catch (UnknownHostException ex){  //  Unknown host found
                throw new Exception("Unknown SMTP server!");
            } catch (SecurityException ex){  //  Unknown host found
                throw new Exception("Security error!");
            } catch (IOException ex){  //  I/O error
                throw new Exception("I/O error!");
            }

            // Open stream to write to server
            OutputStream os= s.getOutputStream();
            serverWriter = new DataOutputStream(os); //to write string,otherwise, need to write byte array

            // Open input stream to read from server
            InputStreamReader isrServer = new InputStreamReader(s.getInputStream());
            serverReader = new BufferedReader(isrServer);
            
            if(!serverReader.readLine().substring(0,3).equals("220"))    //  connected message
                throw new Exception("Error connecting.");
                
            //  Hello command to server      
            serverWriter.writeBytes("EHLO\r\n");
            while (true){   //  reading hello reply
                serverReader.readLine();    //  hello reply
                if (!serverReader.ready())
                    break; 
            }

            //  Logs in with username and password
            serverWriter.writeBytes("AUTH LOGIN "+username+"\r\n");
            if (!serverReader.readLine().substring(0,3).equals("334"))  //  334 ask for password
                throw new Exception("Error while logging in.");
            serverWriter.writeBytes(password+"\r\n");           
            String buffer = serverReader.readLine();

            //  Check if login is successful
            if (buffer.equals("535 5.7.3 Authentication unsuccessful"))
                throw new Exception("Authentication unsuccessful.");
        }

        public static void logout() throws Exception{
            if (!s.isConnected())   //  make sure s is connected
                throw new Exception("Socket not connected.");
            try{
                serverWriter.writeBytes("QUIT"+"\r\n");
                serverReader.readLine();    //  QUIT response

                // Close socket
                s.close();
            } catch (IOException ex){}  //  buffer remaining when close
        }
}

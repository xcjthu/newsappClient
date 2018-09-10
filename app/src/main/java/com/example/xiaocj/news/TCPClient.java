package com.example.xiaocj.news;

import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.TreeMap;

public class TCPClient implements Runnable{
    private String serverIP = "127.0.0.1";
    private int serverPort;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private boolean isRun = true;
    private Socket socket;
    byte[] rcvBytes = new byte[409600];
    private String receiveMsg;
    private int receiveLen;

    public static SearchActivity.MyHandler searchHandler;

    boolean isInit = false;
    MainActivity.MyHandler handler;

    static TCPClient instance;

    public TCPClient(String IP, int port){
        serverIP = IP;
        serverPort = port;
        // init();
    }

    public static TCPClient getInstance(){
        return instance;
    }

    public TCPClient(MainActivity.MyHandler handler){

        this.handler = handler;
        // init();
        instance = this;
    }

    public void init(){
        try {
            socket = new Socket(serverIP, serverPort);
            // socket.setSoTimeout(2000);
            pw = new PrintWriter(socket.getOutputStream(), true);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
        } catch (ConnectException e){
            Log.d("Tcp init", "failed");
            e.printStackTrace();
        }catch (IOException e) {
            Log.d("Tcp init", "failed");
            e.printStackTrace();
        }
        isInit = true;
    }

    static public void setSearchHandler(SearchActivity.MyHandler h){
        searchHandler = h;
    }

    static MoreActivity.MyHandler moreHandler;
    static public void setMoreHandler(MoreActivity.MyHandler h){
        moreHandler = h;
    }

    public void close(){
        isRun = false;
    }

    public void send(final String message){
        /*
        */
        while (!isInit){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isRun) return;
        Log.d("TCP", "send message " + message);
        pw.print(message + "$");
        pw.flush();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Log.d("sendMessage", message);
        // pw.println(message);
        // pw.flush();
    }

    @Override
    public void run(){
        init();
        while (isRun){
            try {
                receiveMsg = "";
                boolean end = false;
                byte signal = -1;

                while (!end) {
                    receiveLen = dis.read(rcvBytes);
                    if (receiveLen <= 0){
                        throw new NullPointerException();
                    }
                    if (rcvBytes[receiveLen - 1] == '$')
                        end = true;
                    if (signal == -1)
                        signal = rcvBytes[0];

                    String tmp =  new String(rcvBytes, 0, receiveLen, "utf-8");
                    //Log.d("tmpReceive", tmp);
                    receiveMsg += tmp;

                }
                Log.d("TCPclient", "收到消息" + receiveMsg);

                Message msg = new Message();
                msg.obj = receiveMsg;


                switch (signal) {
                    case'0': //发送给主界面
                        Log.d("SendMessage:", "give the message to MainActivity");
                        msg.what = 1;
                        handler.sendMessage(msg);
                        // MainActivity.myHandler.sendMessage(msg);
                        // MainActivity.context.sendBroadcast(intent); //发送消息给主界面
                        break;
                    case '1':
                        Log.d("SendMessage:", "give the message to MainActivity");
                        msg.what = 2;
                        handler.sendMessage(msg);
                        break;
                    case '2':
                        Log.d("SendMessage:", "give the message to MainActivity");
                        msg.what = 4;
                        handler.sendMessage(msg);
                        break;

                    case '3':
                        Log.d("receive", "recommend news");
                        msg.what = 2;
                        moreHandler.sendMessage(msg);
                        break;
                }
                System.out.println(System.currentTimeMillis());


            } catch (IOException e) {
                Log.d("TCPClient", "error for receive message");
                e.printStackTrace();
            } catch (NullPointerException e){
                Log.d("connect internet failed", "unable to connect the server");
                isRun = false;
                Message msg = new Message();
                msg.what = 6;
                handler.sendMessage(msg);
                // Toast.makeText(MainActivity.context, "unable to connect the server", Toast.LENGTH_LONG);
                e.printStackTrace();
            }
        }

        try {
            pw.close();
            dis.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

}


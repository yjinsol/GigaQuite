package com.example.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketTest extends Activity {
    Button btn;
    TextView tv;

    //  TCP연결 관련
//    private java.net.Socket clientSocket;
    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 5000;
    private final String ip = "192.168.123.105";
    //    private final String ip = "112.186.113.108";
    private MyHandler myHandler;
    private MyThread myThread;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        // StrictMode는 개발자가 실수하는 것을 감지하고 해결할 수 있도록 돕는 일종의 개발 툴
        // - 메인 스레드에서 디스크 접근, 네트워크 접근 등 비효율적 작업을 하려는 것을 감지하여
        //   프로그램이 부드럽게 작동하도록 돕고 빠른 응답을 갖도록 함, 즉  Android Not Responding 방지에 도움
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
//            clientSocket = new java.net.Socket(ip, port);
            clientSocket = new Socket(ip, port);
            saveFile(clientSocket);
//            socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        myHandler = new MyHandler();
        myThread = new MyThread();
        myThread.start();

        final EditText input = (EditText)findViewById(R.id.input);
        btn = (Button) findViewById(R.id.btn);
        tv = (TextView) findViewById(R.id.tv);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketOut.println(input.getText());
            }
        });
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    // InputStream의 값을 읽어와서 data에 저장
//                    String data = socketIn.readLine();
                    // Message 객체를 생성, 핸들러에 정보를 보낼 땐 이 메세지 객체를 이용
                    Message msg = myHandler.obtainMessage();
                    msg.obj = "";
                    myHandler.sendMessage(msg);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveFile(java.net.Socket clientSock) throws IOException

    {


        InputStream in = clientSock.getInputStream();

        //바이트 단위로 데이터를 읽는다, 외부로 부터 읽어들이는 역할을 담당

        BufferedInputStream bis = new BufferedInputStream(in);

        //파일을 읽는 경우라면,BufferedReader보다 BufferedInputStream이 더 적절하다.

        FileOutputStream fos = new FileOutputStream("/sdcard/demo.mid");

        //파일을 열어서 어떤식으로 저장할지 알려준다. FileOutputStream을 쓰면 들어오는 파일과 일치하게 파일을 작성해줄 수 있는 장점이 있다.



        int ch;

        while ( (ch = bis.read()) != -1) {

            fos.write(ch);

            //열린 파일시스템에 BufferedInputStream으로 외부로 부터 읽어들여온 파일을 FileOutputStream에 바로 써준다.

        }

        fos.close();

        in.close();

    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            tv.setText(msg.obj.toString());
        }
    }
}



package com.example.helloworld;

import java.io.*;
import java.net.Socket;

public class FileClient {
    private Socket s;



    public FileClient(String host, int port) {

        try {

            s = new Socket(host, port);

            saveFile(s);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }



    private void saveFile(Socket clientSock) throws IOException

    {


        InputStream in = clientSock.getInputStream();

        //바이트 단위로 데이터를 읽는다, 외부로 부터 읽어들이는 역할을 담당

        BufferedInputStream bis = new BufferedInputStream(in);

        //파일을 읽는 경우라면,BufferedReader보다 BufferedInputStream이 더 적절하다.

        FileOutputStream fos = new FileOutputStream("miditest.mid");

        //파일을 열어서 어떤식으로 저장할지 알려준다. FileOutputStream을 쓰면 들어오는 파일과 일치하게 파일을 작성해줄 수 있는 장점이 있다.



        int ch;

        while ( (ch = bis.read()) != -1) {

            fos.write(ch);

            //열린 파일시스템에 BufferedInputStream으로 외부로 부터 읽어들여온 파일을 FileOutputStream에 바로 써준다.

        }

        fos.close();

        in.close();

    }



    public static void main(String[] args) {

        FileClient fc = new FileClient("localhost", 5000);

    }

}



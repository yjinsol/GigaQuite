package com.example.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class Quiet extends Activity {

    ByteBuffer byteBuffer = ByteBuffer.allocate(220500);

    public Thread recordThread = null;  // 녹음 Thread
    public boolean isRecording = false; // 녹음 Thread flag

    public Thread playThread = null; // 재생 Thread
    public boolean isPlaying = false; // 재생 Thread flag

    public Thread invertThread = null; // 반전 Thread
    public boolean isReverse = false; // 반전 Thread flag


    String mfilePath, mfilePath2; // pcm 파일 저장경로
    String wavfilePath, wavfilePath2; // wav 파일 저장경로

    private static final String TAG = "quiet";

    private int mAudioSource = MediaRecorder.AudioSource.MIC; // MIC 사용
    private int mSampleRate = 44100; // 초당 샘플레이트
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO; // 채널
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT; // PCM 16비트 encoding
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat); // ->buffersize = 샘플레이트 *채널 * 16비트/(8비트)

    public AudioRecord mAudioRecord = null; // 녹음
    public AudioTrack mAudioTrack = null; // 트랙 생성
    Button record, start, invert, combi; // pcm button
    Button start2, invert2, convert,combi2; // wav button
    Button array; //wav to array button

    byte[] readData, writeData;  // FileInput/FileOutpt 에 들어갈 바이트 배열

    private byte[] bytes = null;
    private int times = 0;
    private double[] doubleArray = null;

    //  TCP연결 관련
//    private java.net.Socket clientSocket;
    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 5000;
    private final String ip = "192.168.123.105";
    //    private final String ip = "112.186.113.108";
    DataOutputStream os;

    MediaPlayer mediaPlayer = new MediaPlayer(); // wav 재생을 위한 플레이어
    MediaPlayer mediaPlayer2 = new MediaPlayer();
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_quiet);

        record = (Button) findViewById(R.id.recording);
        start = (Button) findViewById(R.id.play);
        invert = (Button) findViewById(R.id.invert);
        combi = (Button) findViewById(R.id.combine);
        start2 = (Button) findViewById(R.id.play2);
        invert2 = (Button) findViewById(R.id.invert2);
        convert = (Button) findViewById(R.id.convert);
        combi2 = (Button) findViewById(R.id.combine2);
        array = (Button) findViewById(R.id.wavtoarray);

        // 버튼-xml 연결

        mfilePath = "/sdcard/원음.pcm";
        mfilePath2 = "/sdcard/반전.pcm";
        wavfilePath = "/sdcard/원음.wav";
        wavfilePath2 = "/sdcard/반전.wav";

        // pcm, wav 파일 저장 경로

        writeData = new byte[mBufferSize];
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecord.startRecording();
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);

        start2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mediaPlayer = new MediaPlayer(); // 초기화
                    mediaPlayer.setDataSource(wavfilePath); // 재생파일 소스 경로
                    mediaPlayer.prepare();// 준비

                } catch (IOException e) {
                    e.printStackTrace();
                }
                start.setText("재생시작");
                mediaPlayer.start();
            }
        });

        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file1 = new File(mfilePath);
                File file2 = new File(wavfilePath);

                File file4 = new File(mfilePath2);
                File file5 = new File(wavfilePath2);

                try {
                    rawToWave(file1, file2);
                    rawToWave(file4, file5);
                    // PCM -> WAV 변환 함수 Return -> file2, file5 WAV헤더를 추가하여 저장
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        invert2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(wavfilePath2);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                start.setText("재생시작");
                mediaPlayer.start();
            }
        });

        array.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    convertToArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

       recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readData = new byte[mBufferSize];

                AudioRecord recorder = null;
                AudioTrack track = null;

                FileOutputStream fos = null;
                FileOutputStream fos2 = null;
                FileOutputStream wavfos =null;

                try {

                    fos = new FileOutputStream(mfilePath);
                    fos2 = new FileOutputStream(mfilePath2);
                    wavfos = new FileOutputStream(wavfilePath);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while (isRecording) {
                    long ret = mAudioRecord.read(readData, 0, mBufferSize);

                    Log.d(TAG, "read byte is" + ret);
                    Log.d("ReadData", Arrays.toString(readData));
                    Log.d("Inverse", Arrays.toString(inverse(readData)));

                    try {
                        fos.write(readData, 0, mBufferSize);
                        fos2.write(inverse(readData), 0, mBufferSize);

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;

                try {
                    fos2.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
                  if (isRecording == true) {
                      isRecording = false;
                      record.setText("녹음시작");
                  } else {
                      isRecording = true;
                      record.setText("녹음중지");

                      if (mAudioRecord == null) {
                          mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                          mAudioRecord.startRecording();
                      }
                      recordThread.start();
                  }
              }
          });

        playThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FileInputStream fis = null;

                try {
                    fis = new FileInputStream(mfilePath);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                DataInputStream dis = new DataInputStream(fis);

                mAudioTrack.play();
                // audiotrak에 저장된 writeData를 재생

                while (isPlaying) {

                    try {

                        int ret = dis.read(writeData
                                , 0, mBufferSize); // writeData를 읽어옴
                        if (ret == -1) {
                            (Quiet.this).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isPlaying = false;
                                    start.setText("재생시작");
                                }
                            });
                            break;
                        }

                        mAudioTrack.write(writeData, 0, ret); // 읽어온 writeData를 audiotrack에 write

                        Log.d("원음 : ", "" + Arrays.toString(writeData));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;


                try {
                    dis.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // PCM 재생

                if (isPlaying == true) {
                    isPlaying = false;
                    start.setText("재생시작");

                } else {
                    isPlaying = true;

                    start.setText("재생중지");

                    if (mAudioTrack == null) {
                        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
                    }
                    playThread.start();
                }
            }
        });

        invertThread = new Thread(new Runnable() {
            @Override
            public void run() {


                FileInputStream fis = null;

                try {
                    fis = new FileInputStream(mfilePath2);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                DataInputStream dis = new DataInputStream(fis);

                mAudioTrack.play();


                while (isReverse) {

                    try {

                        int ret = dis.read(writeData
                                , 0, mBufferSize);
                        if (ret == -1) {
                            (Quiet.this).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isReverse = false;
                                    start.setText("재생시작");
                                }
                            });
                            break;
                        }
                        mAudioTrack.write(writeData, 0, ret);
                        Log.d("반전 : ", "" + Arrays.toString(writeData));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;


                try {
                    dis.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        invert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // PCM 반전재생

                if (isReverse == true) {
                    isReverse = false;
                    start.setText("재생시작");

                } else {
                    isReverse = true;

                    start.setText("재생중지");

                    if (mAudioTrack == null) {
                        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
                    }
                    invertThread.start();
                }
            }
        });

        combi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // PCM 동시재생
                isPlaying = true;
                isReverse = true;

                invertThread.start();
                playThread.start();
            }
        });
    }

    public byte[] invert(byte[] data) {
        for (int i = 0; i < mBufferSize; i += 2) {
            int inverse = -((short) (data[i + 1] << 8 | data[i] & 0xff));
            data[i] = (byte) (inverse & 0xff);
            data[i + 1] = (byte) (inverse >> 8 & 0xff);
        }
        return data;
    }
    //  BIT단위로 SHIFT
    //  아주 작은 오차가 발생하여 사용하지 않음

    public byte[] inverse(byte[] data) {
        for (int i = 0; i < mBufferSize; i++) {
            data[i] = data[i] *= -1;
        }
        return data;
    }
    //  byte마다 -1 곱 연산
    //  정확히 반전된 값이 나옴


    // -- PCM to WAV CONVERT

    private File rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header

            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 2); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, mSampleRate * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }return waveFile;
    }



    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length(); // Size = PCM 파일의 길이
        byte bytes[] = new byte[size]; // bytes 크기 = Size
        byte tmpBuff[] = new byte[size]; // tmpBuffer 크기 = Size
        FileInputStream fis = new FileInputStream(f); // fis에 pcm데이터를 읽어옴
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }
    // Int 변수를 byte 변환

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }
    // Short 변수를 byte 변환

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    private void convertToArray() throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            clientSocket = new Socket(ip, port);
            os = new DataOutputStream(clientSocket.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(os==null) return;   //서버와 연결되어 있지 않다면 전송불가..


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(wavfilePath2));
        int read;
        byte[] buff = new byte[1024];
        while ((read = in.read(buff)) > 0) {
            out.write(buff, 0, read);
        }
        out.flush();
        bytes = out.toByteArray();
        times = Double.SIZE / Byte.SIZE;
        doubleArray = new double[bytes.length / times];
        TextView textView1 = (TextView) findViewById(R.id.text);
        textView1.setMovementMethod(new ScrollingMovementMethod());
        textView1.append("배열 길이: " + doubleArray.length + "\n");
        String msg = "";
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = ByteBuffer.wrap(bytes, i * times, times).getDouble();
            //네트워크 작업이므로 Thread 생성
            msg= Double.toString(doubleArray[i]);
            try {
                os.writeUTF(msg);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print(i + " ");
            System.out.println(doubleArray[i]);
            textView1.append(i + "번 " + doubleArray[i] + "\n");
        }
        System.out.println("finish");
        clientSocket.close();
    }


}
# TCP server example

from socket import *
import socket
import os
import time
import sys

# 이미지 파일 저장경로
src = r"C:\Users\yjs12\Desktop/"

def fileName():
    dte = time.localtime()
    Year = dte.tm_year
    Mon = dte.tm_mon
    Day = dte.tm_mday
    WDay = dte.tm_wday
    Hour = dte.tm_hour
    Min = dte.tm_min
    Sec = dte.tm_sec
    FileName = src + str(Year) + '_' + str(Mon) + '_' + str(Day) + '_' + str(Hour) + '_' + str(Min) + '_' + str(Sec) + '.txt'
    return FileName


# 서버 소켓 오픈
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", 5000))
server_socket.listen(5)

print("TCPServer Waiting for client on port 5000")

cnt = 0
while True:

    # 클라이언트 요청 대기중 .
    client_socket, address = server_socket.accept()
    # 연결 요청 성공
    print("I got a connection from ", address)

    data = None
    cnt = 0
    # Data 수신
    while True:
        data = client_socket.recv(1024)
        if data:
            ret_data = ''
            str_data = str(data)
            str_data = str_data[2:-1]
            print('\nstr_data : ' + str_data)
            replaced_data = str_data
            for i in range(len(str_data)):
                if str_data[i:i + 2] == r'\x':
                    if str_data[i:i + 4] == r'\x00':
                        replaced_data = replaced_data.replace(str_data[i:i + 4], ' ')
                    else:
                        replaced_data = replaced_data.replace(str_data[i:i + 4], '')
            print('replaced_data : ' + replaced_data)
            cnt += 1
            print("cnt: " + str(cnt))
            ret_data += replaced_data + "\n"

            while data:
                print("\nrecving Array...")
                data = client_socket.recv(1024)
                str_data = str(data)
                str_data = str_data[2:-1]
                print('str_data : ' + str_data)
                replaced_data = str_data
                for i in range(len(str_data)):
                    if str_data[i:i+2] == r'\x':
                        if str_data[i:i+4] == r'\x00':
                            replaced_data = replaced_data.replace(str_data[i:i+4], ' ')
                        else:
                            replaced_data = replaced_data.replace(str_data[i:i+4], '')
                print('replaced_data : ' + replaced_data)
                cnt += 1
                print("cnt: " + str(cnt))
                ret_data += replaced_data + "\n"
            else:
                break

    # 받은 데이터 저장
    # 이미지 파일 이름은 현재날짜/시간/분/초.jpg
    img_fileName = fileName()
    img_file = open(img_fileName, "w")
    print("finish Array recv")
    print(sys.getsizeof(ret_data))
    img_file.write(ret_data)
    img_file.close()
    print("Finish ")
    # break // 서버 종료시에 사용
 


client_socket.close()
print("SOCKET closed... END")
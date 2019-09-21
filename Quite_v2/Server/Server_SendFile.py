import socket

import os

import sys


HOST = ''

PORT = 5000

ADDR = (HOST,PORT)

BUFSIZE = 4096

# videofile = r"C:\Users\yjs12\PycharmProjects\인공지능밴드\소켓통신\files/A.JPG"

serv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# filename = r"C:\Users\yjs12\PycharmProjects\인공지능밴드\소켓통신\files/A.JPG"
filename = 'demo.mid'

#Bind Socket

serv.bind(ADDR)

serv.listen(5)

conn, addr = serv.accept()

print('client connected ... ', addr)


#Open the file

#Read and then Send to Client

f=open(filename,'rb')# open file as binary

data=f.read()

print(data,',,,')

exx=conn.sendall(data)

print(exx,'...')

f.flush()

f.close()


#Close the Socket

print('finished writing file')

conn.close()

serv.close()

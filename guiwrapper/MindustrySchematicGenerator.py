from tkinter import *
from tkinter import filedialog
import os

root = Tk()

def generate():
    inputpath=filedialog.askopenfilename(title='이미지 입력', filetypes=[('Image File', '.jpg'), ('Image File', '.png'), ('Image File', '.bmp')])
    outputpath=filedialog.asksaveasfilename(title='설계도 출력', filetypes=[('Mindustry Schematic File', '.msch')], initialfile='result.msch')
    q='java -jar MindustrySchematicGenerator.jar generate \"' + inputpath + '\" ' + str(eval(w.get())) + ' ' + str(eval(h.get())) + ' \"GUI\" \"' + outputpath + '\"'
    print(q)
    os.system(q)


def image():
    inputpath=filedialog.askopenfilename(title='설계도 입력', filetypes=[('Mindustry Schematic File', '.msch')], initialfile='result.bin') 
    outputpath=filedialog.asksaveasfilename(title='이미지 출력', filetypes=[('PNG File', '.png')])
    q='java -jar MindustrySchematicGenerator.jar image \"' + inputpath.name + '\" \"' + outputpath.name + '\"'
    print(q)
    os.system(q)


button1 = Button(root, text="그림으로 설계도 만들기", command=generate)
button1.pack()
button2 = Button(root, text="설계도로 그림 만들기", command=image)
button2.pack();

w = Entry(root)
h = Entry(root)
w.insert(0, '가로')
h.insert(0, '세로')
w.pack()
h.pack()

root.mainloop();
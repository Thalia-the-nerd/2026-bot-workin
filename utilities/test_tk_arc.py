import tkinter as tk
root = tk.Tk()
c = tk.Canvas(root, width=200, height=200)
c.pack()
c.create_oval(50,50,150,150)
c.create_arc(50,50,150,150, start=0, extent=90, fill="red")
c.update()
print("Drawn")

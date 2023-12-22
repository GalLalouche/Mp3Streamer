"""
A very simple busy-wait wrapper on top of langdetect. Basically, a stupid server.
"""
from langdetect import *  # pip install langdetect

def main():
  while True:
    user_string = input()
    print(detect(user_string) + "\n")

if __name__ == '__main__':
  main()

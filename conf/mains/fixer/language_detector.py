"""
A very simple busy-wait wrapper on top of langdetect. Basically, a stupid server.
"""
from langdetect import *  # pip install langdetect
from lingua import Language, LanguageDetectorBuilder

detector = LanguageDetectorBuilder.from_all_spoken_languages().build()


def main():
  while True:
    user_string = input()
    print(str(detector.detect_language_of(user_string)).split('.')[1])


if __name__ == '__main__':
  main()

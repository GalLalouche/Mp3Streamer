from __future__ import annotations

import glob
import os
import shutil
import subprocess
from itertools import chain
from time import sleep
from typing import NamedTuple, Iterable, Optional, Callable, TypeVar, Union

import pyautogui

from genre import Genre

T = TypeVar('T')
_FOOBAR_PATH = r'J:\Program Files (x86)\foobar2000\foobar2000.exe'

def _list_dirs(path: str) -> Iterable[str]:
  return map(lambda x: os.path.join(path, x), next(os.walk(path))[1])

def _flatmap(f: Callable[[T], Iterable[T]], items: Iterable[T]) -> Iterable[T]:
  return chain.from_iterable(map(f, items))

def _find(items: Iterable[T], pred: Callable[[T], bool]) -> Optional[T]:
  for i in items:
    if pred(i):
      return i
  return None

def _check_dir_name(dir_name_to_match: str) -> Callable[[str], bool]:
  normalized = dir_name_to_match.lower()
  return lambda dir_name_to_check: os.path.basename(dir_name_to_check).lower() == normalized

def _hotkey_and_wait(*hotkeys: str, time: float = 0.5) -> None:
  pyautogui.hotkey(*hotkeys)
  sleep(time)

def _escape_and_wait() -> None:
  _hotkey_and_wait('escape')

class Actions(NamedTuple):
  artist: str
  target_genre: str
  src: str
  dst: str

  def validate(self) -> Actions:
    if not self.src:
      raise Exception(f"Could find path for {self.artist}")
    assert (os.path.exists(self.src))
    if not self.dst:
      raise Exception(f"Could find path for {self.target_genre}")
    assert (os.path.exists(self.dst))
    if os.path.dirname(self.src) == self.dst:
      raise Exception(f"{self.artist} is already in {self.target_genre}")
    return self

  # Returns the number of moved files
  def move_files(self) -> int:
    print(f"moving files from {self.src} to {self.dst}")
    count = sum(1 for _ in glob.iglob(self.src + '/**/*', recursive=True))
    print(f"Moving {count} files in total")
    shutil.move(self.src, os.path.join(self.dst, ''))
    return count

  def remove_files_from_foobar(self) -> None:
    print("Removing files from Foobar2000 playlist")
    subprocess.check_call([_FOOBAR_PATH])
    sleep(0.5)
    pyautogui.hotkey('ctrl', 'f')
    pyautogui.write(self.src)
    sleep(0.5)
    _escape_and_wait()
    pyautogui.hotkey('delete')

  def add_moved_files_to_foobar(self, file_count: int) -> None:
    src = os.path.join(self.dst, self.artist)
    print(f"Adding files from {src} to Foobar2000")
    subprocess.Popen(f'"{_FOOBAR_PATH}" /add "{src}"', shell=True)
    sleep(0.05 * file_count)  # (Hopefully) enough time to process the files

  @staticmethod
  def sort_all() -> None:
    print(f"Sorting Foobar2000 playlist")
    subprocess.check_call(_FOOBAR_PATH)
    _escape_and_wait()
    _escape_and_wait()
    _hotkey_and_wait('ctrl', 'a')
    _hotkey_and_wait('alt', 's')
    _hotkey_and_wait('enter')
    _escape_and_wait()

  @staticmethod
  def build(artist: str, target_genre: str) -> Actions:
    sub_genre_dirs = list(_flatmap(_list_dirs, _list_dirs(r'G:\Media\Music')))
    current_artist_path = _find(
      _flatmap(_list_dirs, sub_genre_dirs),
      _check_dir_name(artist))
    target_path = _find(
      sub_genre_dirs,
      _check_dir_name(target_genre))
    return Actions(
      artist=artist,
      target_genre=target_genre,
      src=current_artist_path,
      dst=target_path,
    ).validate()

def main(artist: str, target_genre: Union[str, Genre]):
  genre = target_genre if isinstance(target_genre, str) else target_genre.name()
  actions = Actions.build(artist=artist, target_genre=genre)
  actions.remove_files_from_foobar()
  count = actions.move_files()
  actions.add_moved_files_to_foobar(count)
  actions.sort_all()

if __name__ == '__main__':
  import sys

  main(artist=sys.argv[1], target_genre=sys.argv[2])

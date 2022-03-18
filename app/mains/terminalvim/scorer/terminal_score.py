import os
import sqlite3
import subprocess
import sys
import tempfile


# Using python instead of scala for faster startup times
def main():
  folder = sys.argv[1]

  # TODO show songs, support for album directories
  unwindows = folder.replace("\\", "/")

  artist_name = os.path.basename(unwindows[:-1] if unwindows.endswith('/') else unwindows)
  con = sqlite3.connect("f:/MBRecon.sqlite")
  canonical_name = artist_name.lower()
  artist_scores = con.execute('select score from artist_score where name = ?', (canonical_name,)).fetchall()
  artist_score = artist_scores[0][0] if artist_scores else 'Default'
  album_scores = \
    {e[0]: e[1] for e in
     con.execute('select title, score from album_score where artist = ?', (canonical_name,)).fetchall()}
  album_dirs = sorted(os.listdir(folder))
  scored_albums = [(album, album_scores.get(album[5:], 'Default')) for album in album_dirs if
                   album[0].isdigit()]
  with tempfile.NamedTemporaryFile(mode='w+', dir='/cygdrive/f/') as fp:
    fp.write(f"* {artist_name} ;;; {artist_score}\n")
    for album_name, album_score in scored_albums:
      fp.write(f"** {album_name} ;;; {album_score}\n")
    fp.flush()
    subprocess.check_call(['vim', '-u', '~/.basevimrc', fp.name])
    fp.seek(0)
    with open(fp.name, 'r') as stupid_python_is_stupid:
      edited_lines = [l.strip() for l in stupid_python_is_stupid.readlines()]

  valid_scores = {
    'Default',
    'Crappy',
    'Meh',
    'Okay',
    'Good',
    'Great',
    'Amazing',
    'Classic',
  }
  for line in edited_lines:
    if line.startswith("* "):
      artist, score = line.split(" ;;; ")
      assert (score in valid_scores), f"{score} isn't a valid score enum"
      con.execute('insert or replace into artist_score(name, score) values (?, ?)', (canonical_name, score))
    elif line.startswith("** "):
      year_album, score = line.split(" ;;; ")
      assert (score in valid_scores), f"{score} isn't a valid score enum"
      if score == 'Default':
        continue
      album = year_album[len("** 1234 "):]
      con.execute(
        # TODO execute many?
        'insert or replace into album_score(title, artist, score) values (?, ?, ?)',
        (album, canonical_name, score)
      )
  con.commit()


if __name__ == '__main__':
  main()

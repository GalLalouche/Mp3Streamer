"""
Play creates a RUNNING_PID file but doesn't always delete it. It then refuses to load.
So this script takes that PID and writes its contents to another file.
"""

import os.path
import sys
from datetime import datetime

RUNNING_PID = "RUNNING_PID"
RUNNING_PID_DUMP = "RUNNING_PID_DUMP_GitIgnore"


def main():
  current_path = sys.argv[1]
  print(current_path)
  os.chdir(current_path)
  if os.path.exists(RUNNING_PID):
    print(f"Moving contents of {RUNNING_PID} to {RUNNING_PID_DUMP}")
    with open(RUNNING_PID, 'r') as running_pid:
      lines = running_pid.readlines()
      print(lines)
      if lines:
        with open(RUNNING_PID_DUMP, 'a') as running_pid_dump:
          running_pid_dump.writelines(f"{datetime.now()}\n")
          for line in lines:
            print(line)
            running_pid_dump.write(line)
    os.remove(RUNNING_PID)
  else:
    print("No " + RUNNING_PID)


if __name__ == '__main__':
  main()

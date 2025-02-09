#!/usr/bin/python3
import subprocess


def format(s):
  path = s[s.index("(") + 1:s.index(")")].replace("\\", "/")
  return f'<attribute name="Class-Path" value="/{path}" />'


if __name__ == '__main__':
  output = subprocess.run(["/c/Users/Gal/scoop/apps/openjdk17/current/bin/java.exe",
                           "-Xmx2G",
                           "-jar",
                           "j:/dev/lang/scala/sbt/bin/sbt-launch.jar",
                           "show compile:dependencyClasspath"
                           ],
                          stdout=subprocess.PIPE
                          ).stdout
  if isinstance(output, bytes):
    output = output.decode('utf-8')
  attributes = [format(l.strip()) for l in output.splitlines() if
                l.startswith("[info] * Attributed")]
  print("\n".join(sorted(attributes)))

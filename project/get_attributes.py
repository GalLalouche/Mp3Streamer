#!/usr/bin/python3
import subprocess


def format(s):
  path = s[s.index("(") + 1:s.index(")")].replace("\\", "/")
  return f'<attribute name="Class-Path" value="/{path}" />'


if __name__ == '__main__':
  output = subprocess.run(["/c/Program Files/Java/jdk1.8.0_141/bin/java",
                           "-Xmx2G",
                           "-jar",
                           "c:/dev/lang/scala/sbt/bin/sbt-launch.jar",
                           "show compile:dependencyClasspath"
                           ],
                          stdout=subprocess.PIPE
                          ).stdout
  if isinstance(output, bytes):
    output = output.decode('utf-8')
  attributes = [format(l.strip()) for l in output.splitlines() if l.startswith("[info] * Attributed")]
  print("\n".join(sorted(attributes)))

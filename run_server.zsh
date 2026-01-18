#!/usr/bin/zsh
source ~/Syncthing/aliases
CP=$(sbt --error "export runtime:fullClasspath" | tr -d '\r' | tr '\\' '/')
java=$(normalize_path $(cmd /c where java) | tr -d '\r')
echo $java
$java -cp "$CP" http4s.Main

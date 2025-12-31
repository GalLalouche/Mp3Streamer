#!/usr/bin/zsh
source ~/aliases
CP=$(sbt --error "export runtime:fullClasspath" | tr -d '\r' | tr '\\' '/')
$HOME/Gal/.jdks/corretto-21.0.5/bin/java.exe -cp "$CP" http4s.Main

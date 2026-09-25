import {Song, Volume} from "./types.js";

export namespace SecondaryPlayer {
  const audio = document.createElement('audio');

  export function play(song: Song, fromInSeconds: number): void {
    audio.src = song.offlineUrl!!
    audio.volume = notImplemented() // VolumeSetter.getVolumeBaseline() / 100.0
    audio.play().catch((error) => {
      console.error("Error playing audio:", error);
    });
  }

  export function stop(): void {
    notImplemented()
  }

  export function pause(): void {
    notImplemented()
  }

  export function updateVolume(v: Volume): void {
    notImplemented()
  }
}
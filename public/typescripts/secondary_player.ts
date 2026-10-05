// import {Duration, Song} from "./types.js"
//
// export namespace SecondaryPlayer {
//   const audio: HTMLAudioElement = document.createElement('audio')
//
//   export function play(song: Song, from: Duration): void {
//     audio.src = song.offlineUrl!!
//     audio.volume = notImplemented()
//     audio.currentTime = from.toSeconds()
//     audio.play().catch((error) => {
//       console.error("Error playing audio:", error)
//     })
//   }
//
//   export function reset(): void {
//     pause()
//     audio.removeAttribute('src')
//     audio.load()
//   }
//
//   export function pause(): void {
//     audio.pause()
//   }
//
//   export function updateVolumeRatio(ratio: number): void {
//     notImplemented()
//   }
// }
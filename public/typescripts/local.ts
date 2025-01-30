import {Song, songPath} from "./types.js"
import {isLocalHost} from "./initialization.js"
import {FileDownloader} from "./file_downloader.js"

export namespace Local {
  const fileDownloader = new FileDownloader()

  // Returns the input song. Future proofing, in case we would ever want to make song immutable, or
  // return a different song object.
  export async function maybePreLoad(song: Song): Promise<Song> {
    if (isLocalHost())
      await setOfflineUrl(song)
    return song
  }

  /** Returns the offline blob URL. */
  export async function setOfflineUrl(song: Song): Promise<string> {
    if (song.offlineUrl === undefined) {
      const blob = await fileDownloader.download(songPath(song))
      console.log(`Blob for ${song.file} set`)
      song.offlineUrl = URL.createObjectURL(blob)
    }
    return song.offlineUrl
  }
}
$exposeGlobally!(Local)

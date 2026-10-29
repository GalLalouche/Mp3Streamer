import {LRUCache} from "lru-cache"
import {isLocalHost} from "./initialization.js"
import {Song} from "./media.js"

export async function maybeDownloadLocally(song: Song): Promise<void> {
  if (isLocalHost())
    await setOfflineUrl(song)
}

const requests: Map<string, Promise<Blob>> = new Map()
const cache = new LRUCache<string, Blob>({
  max: 10,
  maxSize: 1_000_000_000,
  sizeCalculation: (value, key) => value.size,
})

async function download(url: string): Promise<Blob> {
  try {
    const existing_blob = cache.get(url)
    if (existing_blob !== undefined)
      return existing_blob
    const existing_request = requests.get(url)
    if (existing_request !== undefined)
      return existing_request
    const blob_promise = fetch(url).then(e => e.blob())
    requests.set(url, blob_promise)
    const blob = await blob_promise
    cache.set(url, blob)
    return blob
  } finally {
    requests.delete(url)
  }
}

/** Returns the offline blob URL. */
async function setOfflineUrl(song: Song): Promise<string> {
  if (song.offlineUrl === undefined)
    song.offlineUrl = URL.createObjectURL(await download(song.path))
  return song.offlineUrl
}

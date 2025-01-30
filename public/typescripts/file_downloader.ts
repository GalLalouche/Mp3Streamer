import {LRUCache} from 'lru-cache'

export class FileDownloader {
  private requests: Map<string, Promise<Blob>> = new Map()
  private cache = new LRUCache<string, Blob>({
    max: 10,
    maxSize: 1_000_000_000,
    sizeCalculation: (value, key) => value.size,
  })

  async download(url: string): Promise<Blob> {
    const existing_blob = this.cache.get(url)
    if (existing_blob !== undefined)
      return existing_blob
    const existing_request = this.requests.get(url)
    if (existing_request !== undefined)
      return existing_request
    console.log(`Downloading ${url}`)
    const blob_promise = fetch(url).then(e => e.blob())
    this.requests.set(url, blob_promise)
    const blob = await blob_promise
    this.cache.set(url, blob)
    this.requests.delete(url)
    return blob
  }
}

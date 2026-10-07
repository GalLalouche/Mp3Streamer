import {RawJSON} from "./api.js"
import {Duration} from "./common_types.js"

type extension = 'mp3' | 'flac'

export class Song {
  readonly title: string
  readonly artistName: string
  readonly albumName: string
  readonly track: number
  readonly year: number
  readonly bitrate: string
  readonly duration: Duration
  readonly size: number
  readonly discNumber?: string
  readonly trackGain: number

  // Classical music fields
  readonly composer?: string
  readonly conductor?: string
  readonly opus?: string
  readonly orchestra?: string
  readonly performanceYear?: number

  // The below represent URLs FIXME then why aren't they URLs?!
  readonly file: string
  readonly poster: string
  // Can be undefined in search results FIXME shouldn't be thought...
  readonly extension?: extension
  offlineUrl?: string

  static fromJSON(json: RawJSON): Song {
    return new Song(
      json.title,
      json.artistName,
      json.albumName,
      json.track,
      json.year,
      json.bitrate,
      Duration.fromJson(json.duration),
      json.size,
      json.discNumber,
      json.trackGain,
      json.composer,
      json.conductor,
      json.opus,
      json.orchestra,
      json.performanceYear,
      json.file,
      json.poster,
      json.mp3,
      json.flac,
    )
  }

  private constructor(
    title: string,
    artistName: string,
    albumName: string,
    track: number,
    year: number,
    bitrate: string,
    duration: Duration,
    size: number,
    discNumber: string | undefined,
    trackGain: number,
    composer: string | undefined,
    conductor: string | undefined,
    opus: string | undefined,
    orchestra: string | undefined,
    performanceYear: number | undefined,
    file: string,
    poster: string,
    mp3: string | undefined,
    flac: string | undefined,
  ) {
    this.title = title
    this.artistName = artistName
    this.albumName = albumName
    this.track = track
    this.year = year
    this.bitrate = bitrate
    this.duration = duration
    this.size = size
    this.discNumber = discNumber
    this.trackGain = trackGain

    this.composer = composer
    this.conductor = conductor
    this.opus = opus
    this.orchestra = orchestra
    this.performanceYear = performanceYear

    this.file = file
    this.poster = poster
    require(mp3 === undefined || flac === undefined, "Song can't have both mp3 and flac")
    if (mp3)
      this.extension = 'mp3'
    if (flac)
      this.extension = 'flac'
  }
  path(): string | undefined {return this.extension}
}

export type AlbumType = 'Album' | 'Live' | 'EP'

export interface Album {
  readonly artistName: string
  readonly title: string
  readonly year: number
  readonly dir: string
  readonly date: Date
  readonly albumType: AlbumType
  readonly discNumbers?: readonly string[]

  // Relevant for new albums
  readonly reconID?: string

  // Classical music fields
  readonly composer?: string
  readonly conductor?: string
  readonly opus?: string
  readonly orchestra?: string
  readonly performanceYear?: number
}

export interface Artist {
  readonly name: string
  readonly albums: readonly Album[]
}

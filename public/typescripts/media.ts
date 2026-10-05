import {RawJSON} from "./api.js"
import {Duration} from "./common_types.js"

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
  // Either mp3 or flac should be available
  // TODO represent this as an ADT since this is a proper parsed class.
  readonly mp3?: string
  readonly flac?: string
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
    this.mp3 = mp3
    this.flac = flac
    assert(
      // It can be neither when it returns as search results, for example.
      this.mp3 === undefined || this.flac === undefined,
      "Song can't have both mp3 and flac",
    )
    this.offlineUrl = undefined
  }
  path(): string {
    return this.mp3 ? this.mp3 : this.flac!
  }
}

export type AlbumType = 'Album' | 'Live' | 'EP'

export interface Album {
  readonly artistName: string
  readonly title: string
  readonly year: number
  readonly dir: string
  readonly date: Date
  readonly albumType: AlbumType

  // Relevant for new albums
  readonly reconID?: string

  // Classical music fields
  readonly composer?: string
  readonly conductor?: string
  readonly opus?: string
  readonly orchestra?: string
  readonly performanceYear?: number
  readonly discNumbers?: string[]
}

export interface Artist {
  readonly name: string
  readonly albums: Album[]
}

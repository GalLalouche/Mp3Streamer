import {PlaylistCustomizations} from "./playlist_customizations.js"
import {Globals} from "./globals.js"
import {RawJSON} from "./api.js"

export class Percentage {
  private readonly _zero_to_one: number

  private constructor(percentage: number) {
    require(
      percentage <= 1 && percentage >= 0,
      `Percentage must be between 0 and 1, got ${percentage}`,
    )
    this._zero_to_one = percentage
  }

  static fromJSON(json: RawJSON) {return new Percentage(json as unknown as number)}
  toJSON(): RawJSON {return this._zero_to_one as unknown as RawJSON}
  static fromMax1(number: number) {return new Percentage(number)}
  static fromMax100(number: number) {return new Percentage(number / 100.0)}

  zeroToOne(): number {return this._zero_to_one}
  zeroToHundred(): number {return this._zero_to_one * 100}
  isZero(): boolean {
    return this._zero_to_one === 0
  }
  times(number: number) {return Percentage.fromMax100(this.zeroToHundred() * number)}
}

export class Duration {
  private readonly millis: number

  private constructor(millis: number) {this.millis = millis}

  static fromSeconds(seconds: number): Duration {return new Duration(seconds * 1000)}
  static fromMillis(number: number) {return new Duration(number)}
  // TODO This is definitely a bit of a hack. The problem is that currently the server send JSON with the
  //  duration as seconds, and when this method is called we don't know if it's called from the
  //  server or from a serialized version of the Duration class, e.g., as used for backups.
  //  A better solution would be to serialize this as a tuple, and deserialize it as either a tuple
  //  or a number.
  static fromJson(json: RawJSON): Duration {return Duration.fromSeconds(json as unknown as number)}
  toJSON(): RawJSON {return this.toSeconds() as unknown as RawJSON}

  timeFormat(): string {return this.toSeconds().timeFormat()}
  toSeconds(): number {return this.millis / 1000}
  toMillis(): number {return this.millis}
  plus(d: Duration): Duration {return new Duration(this.millis + d.millis)}
  isGreaterThanOrEqual(duration: Duration): boolean {return this.millis >= duration.millis}
  minus(fadeoutDuration: Duration): Duration {
    return new Duration(Math.max(this.millis - fadeoutDuration.millis, 0))
  }
  min(duration: Duration): Duration { return this.millis < duration.millis ? this : duration }
}

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

  // The below represent URLs
  readonly file: string
  readonly poster: string
  // Either mp3 or flac should be available
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
}

export function songPath(song: Song): string {
  return song.mp3 ? song.mp3 : song.flac!
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

export class TimeUpdate {
  readonly currentDuration: Duration
  readonly totalDuration: Duration

  constructor(input: { currentDuration: Duration, totalDuration: Duration }) {
    this.currentDuration = input.currentDuration
    this.totalDuration = input.totalDuration
  }
}

export type PlayerEvent = "ENDED" | TimeUpdate | Song

export abstract class Player {
  abstract load(song: Song): void
  abstract playCurrentSong(): void
  abstract stop(): void
  /** Does not unpause. */
  abstract pause(): void
  abstract isPaused(): boolean
  togglePause(): void {
    if (this.isPaused())
      this.playCurrentSong()
    else
      this.pause()
  }
  abstract percentageOfSongPlayed(): Percentage
  abstract duration(): Duration
  abstract currentTime(): Duration
  abstract setVolume(v: Volume): void
  abstract getVolume(): Volume
  abstract skipTo(duration: Duration): void
  abstract clear(): void
  abstract listen(callback: (pe: PlayerEvent) => void): void
  abstract unlisten(callback: (pe: PlayerEvent) => void): void
}

export abstract class Playlist {
  async clear(instant: boolean): Promise<void> {return this.setPlaylist([], instant)}
  async setPlaylist(playlist: Song[], instant: boolean): Promise<void> {
    await this.clear(instant)
    const that = this
    for (const s of playlist) {
      await that.add(s, false)
    }
  }
  abstract add(song: Song | Song[], playNow: boolean): Promise<void>
  protected abstract _next(): void
  next(count?: number): void {
    count = count || 1
    for (let i = 0; i < count; i++)
      this._next()
  }
  abstract play(index: number): Promise<void>
  abstract select(index: number): Promise<void>
  abstract prev(): void
  abstract currentIndex(): number
  currentPlayingSong(): Song {return this.songs()[this.currentIndex()]}
  abstract songs(): Song[]
  last(): Song {return this.songs()[this.length() - 1]}
  length(): number {return this.songs().length}
  toString(song: Song): string {return PlaylistCustomizations.mediaMetadata(song)}
  // The list presentation reversed, so song at index 0 is actually the last song, not the first.
  getDisplayedIndex(index: number): number {return this.length() - 1 - index}
  isLastSongPlaying(): boolean {return this.currentIndex() == this.length() - 1}
}

function makePlaylist(): Playlist {
  function pl(): any {return Globals.playlist}

  const result = new class extends Playlist {
    override currentIndex() {return pl().current}
    override songs() {return pl().playlist}
    override add(song: Song | Song[], playNow: boolean): Promise<void> {return pl().add(song, playNow)}
    override _next(): void {return pl().next()}
    override prev(): void {return pl().previous()}
    override async clear(): Promise<void> {
      const instant = true
      return pl().setPlaylist([], instant)
    }
    override play(index: number): Promise<void> { return pl().play(index)}
    override select(index: number): Promise<void> {return pl().select(index)}
  }
  $(function (): void {pl().getDisplayedIndex = result.getDisplayedIndex})
  return result
}

export const gplaylist: Playlist = makePlaylist()
$exposeGlobally!(gplaylist)

export class Volume {
  private readonly volume: Percentage

  private constructor(volume: Percentage) {
    this.volume = volume
  }

  static fromJSON(json: RawJSON) {return new Volume(Percentage.fromJSON(json))}
  toJSON(): RawJSON {return this.volume.toJSON()}
  static fromPercentage(p: Percentage) {return new Volume(p)}

  times(number: number): Volume {return new Volume(this.volume.times(number))}

  setWidth(volumeBar: JQuery<HTMLElement>) {
    volumeBar.css("width", `${this.volume.zeroToHundred()}%`)
  }
  setVolume(element: HTMLAudioElement): void {
    element.volume = this.volume.zeroToOne()
  }
  isMuted() {return this.volume.isZero()}

  percentage(): Percentage {return this.volume}
}

$exposeGlobally!(Duration)
$exposeGlobally!(Percentage)
$exposeGlobally!(Volume)

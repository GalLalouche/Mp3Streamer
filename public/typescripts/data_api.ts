import {get, RawJSON} from "./api.js";
import {Song} from "./types.js";


export async function getSongRawPath(fullPath: string): Promise<Song> {
  return get(fullPath).then(Song.fromJSON)
}

export async function getSong(path: string): Promise<Song> {
  return getSongRawPath("data/song/" + path)
}

export function nextSong(song: Song): Promise<Song> {
  return getSongRawPath("data/nextSong/" + song.file)
}

export async function getRandomSong(): Promise<Song> {
  return getSongRawPath("data/randomSong")
}

export async function getSongsRawPath(path: string): Promise<Song[]> {
  return get(path).then(d => (d as RawJSON[]).map(Song.fromJSON))
}

export async function getAlbum(path: string): Promise<Song[]> {
  return getSongsRawPath("data/album/" + path)
}

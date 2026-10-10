import {RawJSON} from "./api.js"
import {Percentage} from "./common_types.js"

export class Volume {
  private readonly volume: Percentage

  constructor(volume: Percentage) {
    this.volume = volume
  }

  static fromJSON(json: RawJSON): Volume {return new Volume(Percentage.fromJSON(json))}
  toJSON(): RawJSON {return this.volume.toJSON()}

  times(number: number): Volume {return new Volume(this.volume.times(number))}
  isMuted(): boolean {return this.volume.isZero()}
  percentage(): Percentage {return this.volume}
}

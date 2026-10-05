/** Types unrelated to a specific component or feature, but used across the application. */
import {RawJSON} from "./api"

export class Percentage {
  private readonly _zero_to_one: number
  static readonly MAX: Percentage = Percentage.fromMax1(1)
  static readonly ZERO: Percentage = Percentage.fromMax1(0)

  private constructor(percentage: number) {
    require(
      percentage <= 1 && percentage >= 0,
      `Percentage must be between 0 and 1, got ${percentage}`,
    )
    this._zero_to_one = percentage
  }

  static fromJSON(json: RawJSON): Percentage {return new Percentage(json as unknown as number)}
  toJSON(): RawJSON {return this._zero_to_one as unknown as RawJSON}
  static fromMax1(number: number): Percentage {return new Percentage(number)}
  static fromMax100(number: number): Percentage {return new Percentage(number / 100.0)}

  zeroToOne(): number {return this._zero_to_one}
  zeroToHundred(): number {return this._zero_to_one * 100}
  isZero(): boolean {return this._zero_to_one === 0}
  times(number: number): Percentage {return Percentage.fromMax100(this.zeroToHundred() * number)}
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
  times(number: number): Duration {return Duration.fromMillis(this.millis * number)}
}


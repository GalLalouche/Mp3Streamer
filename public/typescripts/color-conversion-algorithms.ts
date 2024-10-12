type NumbersTriplet = [number, number, number]

class RGB {
  r: number
  g: number
  b: number

  constructor(r: number, g: number, b: number) {
    function requireRange(x: number): void { require(x >= 0 && x <= 255)}

    requireRange(r)
    requireRange(g)
    requireRange(b)
    this.r = r
    this.g = g
    this.b = b
  }

  toArray(): NumbersTriplet {return [this.r, this.g, this.b]}

  static fromArray(array: NumbersTriplet): RGB {return new RGB(array[0], array[1], array[2])}

  /**
   * Return a lighter color by some degree alpha
   * @param  alpha If alpha is zero, the return color will be white; as alpha approaches
   *               infinity, the returned color will be equal to the original color.
   */
  makeLighter(alpha: number): RGB {
    assert(alpha >= 0)
    const hsl = this.toHSL()
    hsl.l += (100 - hsl.l) / (1 + alpha)
    return hsl.toRGB()
  }

  toString(): string {return `rgb(${this.toArray().join(", ")})`}

  // Adapted from https://gmigdos.wordpress.com/2011/01/13/javascript-convert-rgb-values-to-hsl/
  toHSL(): HSL {
    const r = this.r / 255
    const g = this.g / 255
    const b = this.b / 255

    const maxColor = Math.max(r, g, b)
    const minColor = Math.min(r, g, b)
    const diff = maxColor - minColor
    if (diff === 0)
      return new HSL(0, 0, maxColor)

    const sum = maxColor + minColor

    function getH() {
      function aux() {
        if (r === maxColor)
          return (g - b) / diff
        if (g === maxColor)
          return 2.0 + (b - r) / diff
        return 4.0 + (r - g) / diff
      }

      const hPrime = aux()
      return (hPrime < 0 ? hPrime + 6 : hPrime) * 60
    }

    return new HSL(getH(), 100 * diff / Math.min(sum, 2 - sum), 50 * sum)
  }
}

class HSL {
  h: number
  s: number
  l: number

  constructor(h: number, s: number, l: number) {
    require(h >= 0 && h <= 360)
    require(s >= 0 && s <= 100)
    require(l >= 0 && l <= 100)
    this.h = h
    this.s = s
    this.l = l
  }

  // Adapted from https://stackoverflow.com/a/9493060/736508
  toRGB(): RGB {
    const h = this.h / 360
    const s = this.s / 100
    const l = this.l / 100

    function normalize(arr: NumbersTriplet): RGB {
      return RGB.fromArray(arr.map(c => Math.round(c * 255)) as NumbersTriplet)
    }

    if (s === 0)
      return normalize([l, l, l])

    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q

    function hue2rgb(t: number): number {
      if (t < 0) return hue2rgb(t + 1)
      if (t > 1) return hue2rgb(t - 1)
      if (t < 1 / 6) return p + (q - p) * 6 * t
      if (t < 1 / 2) return q
      if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6
      return p
    }

    const r = hue2rgb(h + 1 / 3)
    const g = hue2rgb(h)
    const b = hue2rgb(h - 1 / 3)

    return normalize([r, g, b])
  }
}

function string2rgb(s: string): RGB {
  const result = s.substring(4, s.length - 1)
    .replace(/ /g, '')
    .split(',')
    .map(x => parseInt(x))
  if (result.length != 3)
    throw new IllegalArgumentException(`Invalid RGB string ${s}`)
  return RGB.fromArray(result as NumbersTriplet)
}
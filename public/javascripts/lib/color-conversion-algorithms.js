/**
 * Converts an RGB color to HSL
 * Adapted from https://gmigdos.wordpress.com/2011/01/13/javascript-convert-rgb-values-to-hsl/
 * @param rgbArr {number[]} 3-element RGB array (0-255)
 * @returns {number[]} 3-element array containing the HSL values where
 *          H is between 0 and 360, S and L are between 0 and 100
 */
function rgb2hsl(rgbArr) {
  const normalized = rgbArr.map(x => x / 255)
  const r = normalized[0]
  const g = normalized[1]
  const b = normalized[2]

  const maxColor = Math.max(r, g, b)
  const minColor = Math.min(r, g, b)
  const diff = maxColor - minColor
  if (diff === 0)
    return [0, 0, maxColor]

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
  const S = 100 * diff / Math.min(sum, 2 - sum)
  const L = 50 * sum

  return [getH(), S, L]
}

/**
 * Converts an HSL color to RGB
 * Adapted from https://stackoverflow.com/a/9493060/736508
 * @param hslArr {number[]} 3-element array containing the HSL values where
 *               H is between 0 and 360, S and L are between 0 and 100
 * @returns {number[]} 3-element RGB array (0-255)
 *
 */
function hslToRgb(hslArr) {
  const h = hslArr[0] / 360
  const s = hslArr[1] / 100
  const l = hslArr[2] / 100
  let r, g, b;

  const normalize = arr => arr.map(c => Math.round(c * 255))
  if (s === 0)
    return normalize([l, l, l])

  const q = l < 0.5 ? l * (1 + s) : l + s - l * s
  const p = 2 * l - q

  const hue2rgb = t => {
    if (t < 0) return hue2rgb(p, q, t + 1)
    if (t > 1) return hue2rgb(p, q, t - 1)
    if (t < 1 / 6) return p + (q - p) * 6 * t;
    if (t < 1 / 2) return q;
    if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
    return p;
  }
  r = hue2rgb(h + 1 / 3);
  g = hue2rgb(h);
  b = hue2rgb(h - 1 / 3);

  return normalize([r, g, b])
}

/**
 * Return a lighter color by some degree alpha
 * @param {number[]} rgbArr 3-element RGB array (0-255)
 * @param {number} alpha If alpha is zero, the return color will be white; as alpha approaches infinity, the returned
 *        color will be equal to the original color.
 * @returns {number[]} 3-element RGB array (0-255)
 */
function makeLighter(rgbArr, alpha) {
  assert(alpha >= 0)
  const hslArr = rgb2hsl(rgbArr)
  hslArr[2] += (100 - hslArr[2]) / (1 + alpha)
  return hslToRgb(hslArr)
}

const rgb2String = rgbArr => `rgb(${rgbArr.join(", ")})`
const string2rgb = rgbStr => rgbStr.substring(4, rgbStr.length - 1)
    .replace(/ /g, '')
    .split(',')
    .map(x => parseInt(x))
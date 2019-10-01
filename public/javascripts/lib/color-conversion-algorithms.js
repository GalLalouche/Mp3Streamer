/**
 * Converts an RGB color to HSL
 * Parameters
 *     rgbArr : 3-element array containing the RGB values
 *
 * Result : 3-element array containing the HSL values
 *
 * Adapted from https://gmigdos.wordpress.com/2011/01/13/javascript-convert-rgb-values-to-hsl/
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
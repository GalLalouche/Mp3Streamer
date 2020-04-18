$(function() {
  const cache = {}
  const graynessLevel = rgb => rgb2hsl(rgb)[1]
  getColorAsync = function(src, f) { // Explicitly global.
    if (cache[src])
      return f(cache[src])
    function usingColorThief(image) {
      const palette = new ColorThief().getPalette(image)
      const firstNonGray = palette.find(rgb => graynessLevel(rgb) > 30)
      // Sometimes in life, all you have are shades of gray.
      return firstNonGray || palette[0]
    }
    const usingVibrant = image => new Vibrant(image, 256).LightMutedSwatch.getRgb()

    const image = img(src).css("display", "none")
    $("body").append(image)
    const imageElement = image[0]
    imageElement.addEventListener('load', function() {
      if (cache[src])
        return f(cache[src])
      let result = usingColorThief(imageElement)
      const greyness = graynessLevel(result)
      if (greyness < 30)
        result = [0, 0, 0]
      cache[src] = result
      imageElement.remove()
      f(result);
    })
  }
})

/**
 * @param src {string} a URL source for an image
 * @callback f Will be called when with an RGB int array
 */
getColorAsync = null

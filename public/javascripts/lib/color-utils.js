// f accepts an RGB int array.
function getColorAsync(src, f) {
  function usingColorThief(image) {
    const graynessLevel = rgb => rgb2hsl(rgb)[1]
    const palette = new ColorThief().getPalette(image[0])
    const firstNonGray = palette.find(rgb => graynessLevel(rgb) > 30)
    // Sometimes in life, all you have are shades of gray.
    return firstNonGray || palette[0]
  }
  const usingVibrant = image => new Vibrant(image, 256).VibrantSwatch.getRgb()

  const image = img(src).css("display", "none")
  $("body").append(image)
  const imageElement = image[0]
  imageElement.addEventListener('load', function() {
    const result = usingVibrant(imageElement)
    imageElement.remove()
    f(result);
  })
}

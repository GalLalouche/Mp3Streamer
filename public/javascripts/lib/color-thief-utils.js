// f accepts an RGB int array.
function getColorAsync(src, f) {
  const graynessLevel = rgb => rgb2hsl(rgb)[1]
  const image = $(`<img>`).attr("src", src).css("display", "none")
  $("body").append(image)
  image[0].addEventListener('load', function() {
    const palette = colorThief.getPalette(image[0])
    image.remove()
    const firstNonGray = palette.find(rgb => graynessLevel(rgb) > 30)
    // Sometimes in life, all you have are shades of gray.
    f(firstNonGray || palette[0])
  })
}

// f accepts an RGB int array.
function getColorAsync(imgSelector, f) {
  imgSelector[0].addEventListener('load', () => f(colorThief.getColor(imgSelector[0])))
}

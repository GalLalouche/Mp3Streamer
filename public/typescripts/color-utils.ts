declare class ColorThief {
  getPalette(e: Element): NumbersTriplet[]
}

class ColorGetter {
  private readonly cache: Record<string, RGB> = {}
  private static graynessLevel(rgb: RGB): number {return rgb.toHSL().s}
  async getColorAsync(src: string): Promise<RGB> {
    if (this.cache[src])
      return this.cache[src]

    function usingColorThief(image: Element): RGB {
      const palette = new ColorThief().getPalette(image).map(e => RGB.fromArray(e))
      const firstNonGray = palette.find(rgb => ColorGetter.graynessLevel(rgb) > 30)
      // Sometimes in life, all you have are shades of gray.
      return firstNonGray || palette[0]
    }

    const image = img(src).css("display", "none")
    $("body").append(image)
    const imageElement = image[0]
    const cache = this.cache
    await eventListenerToPromise(imageElement, 'load')
    if (cache[src])
      return cache[src]
    let result = usingColorThief(imageElement)
    const greyness = ColorGetter.graynessLevel(result)
    if (greyness < 30)
      result = new RGB(0, 0, 0)
    cache[src] = result
    imageElement.remove()
    return result
  }

  // TODO remove this, replace using code with async
  getColorCallback(src: string, callback: (nt: RGB) => void): void {
    this.getColorAsync(src).then(callback)
  }
}

const colorGetter: ColorGetter = new ColorGetter;

(window as any).getColorAsync = colorGetter.getColorCallback.bind(colorGetter)
export namespace ColorGetter {
  export async function getColor(src: string): Promise<RGB> {
    if (cache[src])
      return cache[src]

    function usingColorThief(image: Element): RGB {
      const palette = new ColorThief().getPalette(image).map(e => RGB.fromArray(e))
      const firstNonGray = palette.find(rgb => graynessLevel(rgb) > 30)
      // Sometimes in life, all you have are shades of gray.
      return firstNonGray || palette[0]
    }

    const image = img(src).css("display", "none")
    $("body").append(image)
    const imageElement = image[0]
    await eventListenerToPromise(imageElement, 'load')
    if (cache[src])
      return cache[src]
    let result = usingColorThief(imageElement)
    const greyness = graynessLevel(result)
    if (greyness < 30)
      result = new RGB(0, 0, 0)
    cache[src] = result
    imageElement.remove()
    return result
  }
}

declare class ColorThief {
  getPalette(e: Element): NumbersTriplet[]
}

const cache: Record<string, RGB> = {}

function graynessLevel(rgb: RGB): number {return rgb.toHSL().s}


$exposeGlobally!(ColorGetter)
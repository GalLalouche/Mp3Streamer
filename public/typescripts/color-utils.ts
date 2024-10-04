declare class ColorThief {
    getPalette(e: Element): NumbersTriplet[]
}

class ColorGetter {
    private readonly cache: Record<string, NumbersTriplet> = {}
    private static graynessLevel(rgb: NumbersTriplet): number {return rgb2hsl(rgb)[1]}
    async getColorAsync(src: string): Promise<NumbersTriplet> {
        if (this.cache[src])
            return this.cache[src]

        function usingColorThief(image: Element): NumbersTriplet {
            const palette = new ColorThief().getPalette(image)
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
            result = [0, 0, 0]
        cache[src] = result
        imageElement.remove()
        return result
    }

    // TODO remove this, replace using code with async
    getColorCallback(src: string, callback: (nt: NumbersTriplet) => void): void {
        this.getColorAsync(src).then(callback)
    }
}

const colorGetter: ColorGetter = new ColorGetter;

(window as any).getColorAsync = colorGetter.getColorCallback.bind(colorGetter)